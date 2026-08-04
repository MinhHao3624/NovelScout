package com.minhhao.novelscout.auth;

import com.minhhao.novelscout.auth.dto.AuthUserResponse;
import com.minhhao.novelscout.auth.dto.RegisterRequest;
import com.minhhao.novelscout.common.api.ApiException;
import com.minhhao.novelscout.user.Role;
import com.minhhao.novelscout.user.RoleName;
import com.minhhao.novelscout.user.RoleRepository;
import com.minhhao.novelscout.user.User;
import com.minhhao.novelscout.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
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
