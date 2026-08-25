package com.minhhao.novelscout.auth;

import com.minhhao.novelscout.auth.dto.AuthUserResponse;
import com.minhhao.novelscout.auth.dto.RegisterRequest;
import com.minhhao.novelscout.auth.dto.RegisterWithOtpRequest;
import com.minhhao.novelscout.auth.dto.SendOtpRequest;
import com.minhhao.novelscout.common.api.ApiException;
import com.minhhao.novelscout.common.email.EmailSenderService;
import com.minhhao.novelscout.user.Role;
import com.minhhao.novelscout.user.RoleName;
import com.minhhao.novelscout.user.RoleRepository;
import com.minhhao.novelscout.user.User;
import com.minhhao.novelscout.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Locale;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailVerificationRepository emailVerificationRepository;
    private final EmailSenderService emailSenderService;

    public AuthService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            EmailVerificationRepository emailVerificationRepository,
            EmailSenderService emailSenderService
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailVerificationRepository = emailVerificationRepository;
        this.emailSenderService = emailSenderService;
    }

    @Transactional
    public void sendOtp(SendOtpRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ApiException(HttpStatus.CONFLICT, "EMAIL_EXISTS", "Email này đã được sử dụng cho tài khoản khác");
        }

        // Sinh mã OTP 6 chữ số ngẫu nhiên
        SecureRandom random = new SecureRandom();
        String otpCode = String.format("%06d", random.nextInt(1000000));
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(5);

        EmailVerification verification = new EmailVerification(email, otpCode, expiresAt);
        emailVerificationRepository.save(verification);

        // Gửi email thực sự
        emailSenderService.sendOtpEmail(email, otpCode);
    }

    @Transactional
    public AuthUserResponse registerWithOtp(RegisterWithOtpRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        String username = request.username().trim().toLowerCase(Locale.ROOT);
        String otpCode = request.otpCode().trim();

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ApiException(HttpStatus.CONFLICT, "EMAIL_EXISTS", "Email đã được sử dụng");
        }
        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new ApiException(HttpStatus.CONFLICT, "USERNAME_EXISTS", "Tên đăng nhập đã được sử dụng");
        }

        // Kiểm tra OTP
        EmailVerification verification = emailVerificationRepository.findTopByEmailAndOtpCodeOrderByCreatedAtDesc(email, otpCode)
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "INVALID_OTP", "Mã xác thực OTP không đúng"));

        if (verification.isExpired()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "EXPIRED_OTP", "Mã xác thực OTP đã hết hạn (chỉ có hiệu lực trong 5 phút)");
        }

        verification.setVerified(true);
        emailVerificationRepository.save(verification);

        Role readerRole = roleRepository.findByName(RoleName.READER)
                .orElseThrow(() -> new IllegalStateException("Thiếu vai trò READER trong hệ thống"));
        
        String displayName = (request.displayName() != null && !request.displayName().isBlank()) 
                ? request.displayName().trim() : username;

        User user = User.createReader(email, username, passwordEncoder.encode(request.password()), displayName, readerRole);
        return AuthUserResponse.from(userRepository.saveAndFlush(user));
    }

    @Transactional
    public AuthUserResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        String username = request.username().trim().toLowerCase(Locale.ROOT);
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ApiException(HttpStatus.CONFLICT, "EMAIL_EXISTS", "Email đã được sử dụng");
        }
        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new ApiException(HttpStatus.CONFLICT, "USERNAME_EXISTS", "Tên đăng nhập đã được sử dụng");
        }
        Role readerRole = roleRepository.findByName(RoleName.READER)
                .orElseThrow(() -> new IllegalStateException("Thiếu vai trò READER trong hệ thống"));
        User user = User.createReader(email, username, passwordEncoder.encode(request.password()),
                request.displayName().trim(), readerRole);
        return AuthUserResponse.from(userRepository.saveAndFlush(user));
    }

    @Transactional(readOnly = true)
    public User getRequiredUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHENTICATED", "Phiên đăng nhập không hợp lệ"));
    }

    @Transactional(readOnly = true)
    public AuthUserResponse getCurrentUser(Long id) {
        return AuthUserResponse.from(getRequiredUser(id));
    }
}
