package com.minhhao.novelscout.user;

import com.minhhao.novelscout.auth.AuthService;
import com.minhhao.novelscout.auth.dto.AuthUserResponse;
import com.minhhao.novelscout.common.api.ApiException;
import com.minhhao.novelscout.user.dto.ChangePasswordRequest;
import com.minhhao.novelscout.user.dto.UpdateProfileRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    private final AuthService authService;
    private final PasswordEncoder passwordEncoder;

    public UserService(AuthService authService, PasswordEncoder passwordEncoder) {
        this.authService = authService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public AuthUserResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = authService.getRequiredUser(userId);
        String avatarUrl = request.avatarUrl() == null || request.avatarUrl().isBlank() ? null : request.avatarUrl().trim();
        user.updateProfile(request.displayName().trim(), avatarUrl);
        return AuthUserResponse.from(user);
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = authService.getRequiredUser(userId);
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "WRONG_PASSWORD", "Mật khẩu hiện tại không đúng");
        }
        if (passwordEncoder.matches(request.newPassword(), user.getPasswordHash())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "PASSWORD_UNCHANGED", "Mật khẩu mới phải khác mật khẩu hiện tại");
        }
        user.changePassword(passwordEncoder.encode(request.newPassword()));
    }
}
