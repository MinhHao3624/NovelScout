package com.minhhao.novelscout.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minhhao.novelscout.common.api.ApiException;
import com.minhhao.novelscout.user.Role;
import com.minhhao.novelscout.user.RoleName;
import com.minhhao.novelscout.user.RoleRepository;
import com.minhhao.novelscout.user.User;
import com.minhhao.novelscout.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Locale;
import java.util.UUID;

@Service
public class GoogleAuthService {

    private static final Logger log = LoggerFactory.getLogger(GoogleAuthService.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public GoogleAuthService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User authenticateGoogleUser(String credential) {
        if (credential == null || credential.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_TOKEN", "Google credential token không được để trống");
        }

        try {
            // Token dạng Header.Payload.Signature
            String[] parts = credential.split("\\.");
            if (parts.length < 2) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "MALFORMED_TOKEN", "Định dạng Google Token không hợp lệ");
            }

            // Decode Base64Url Payload
            byte[] payloadBytes = Base64.getUrlDecoder().decode(parts[1]);
            String payloadJson = new String(payloadBytes, StandardCharsets.UTF_8);

            JsonNode payload = objectMapper.readTree(payloadJson);
            String email = payload.has("email") ? payload.get("email").asText().toLowerCase(Locale.ROOT) : null;
            String name = payload.has("name") ? payload.get("name").asText() : "Độc giả Google";
            String picture = payload.has("picture") ? payload.get("picture").asText() : null;

            if (email == null || email.isBlank()) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "NO_EMAIL_IN_TOKEN", "Không thể lấy địa chỉ email từ tài khoản Google");
            }

            log.info("🔑 Đang xử lý đăng nhập Google cho Email: {} (Họ tên: {})", email, name);

            // Kiểm tra xem độc giả đã có trong CSDL chưa
            return userRepository.findByEmailIgnoreCase(email)
                    .map(existingUser -> {
                        // Nếu chưa có avatar thì cập nhật avatar từ Google
                        if ((existingUser.getAvatarUrl() == null || existingUser.getAvatarUrl().isBlank()) && picture != null) {
                            existingUser.updateProfile(existingUser.getDisplayName(), picture);
                            userRepository.save(existingUser);
                        }
                        return existingUser;
                    })
                    .orElseGet(() -> {
                        // Tạo mới tài khoản độc giả từ Google Profile
                        String baseUsername = email.contains("@") ? email.substring(0, email.indexOf("@")) : "google_user";
                        String uniqueUsername = generateUniqueUsername(baseUsername);

                        Role readerRole = roleRepository.findByName(RoleName.READER)
                                .orElseThrow(() -> new IllegalStateException("Thiếu vai trò READER trong hệ thống"));

                        String randomPasswordHash = passwordEncoder.encode(UUID.randomUUID().toString());
                        User newUser = User.createGoogleUser(email, uniqueUsername, randomPasswordHash, name, picture, readerRole);

                        User savedUser = userRepository.saveAndFlush(newUser);
                        log.info("🎉 Đã tạo thành công độc giả mới từ Google: @{} ({})", savedUser.getUsername(), savedUser.getEmail());
                        return savedUser;
                    });

        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("Lỗi xác thực Google ID Token JWT: {}", e.getMessage(), e);
            throw new ApiException(HttpStatus.UNAUTHORIZED, "GOOGLE_AUTH_FAILED", "Xác thực tài khoản Google thất bại: " + e.getMessage());
        }
    }

    private String generateUniqueUsername(String base) {
        String cleaned = base.replaceAll("[^a-zA-Z0-9_]", "").toLowerCase(Locale.ROOT);
        if (cleaned.length() < 3) cleaned = "user_" + cleaned;
        if (cleaned.length() > 25) cleaned = cleaned.substring(0, 25);

        String candidate = cleaned;
        int count = 1;
        while (userRepository.existsByUsernameIgnoreCase(candidate)) {
            candidate = cleaned + count;
            count++;
        }
        return candidate;
    }
}
