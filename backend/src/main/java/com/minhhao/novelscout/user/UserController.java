package com.minhhao.novelscout.user;

import com.minhhao.novelscout.auth.CustomUserPrincipal;
import com.minhhao.novelscout.auth.dto.AuthUserResponse;
import com.minhhao.novelscout.user.dto.ChangePasswordRequest;
import com.minhhao.novelscout.user.dto.UpdateProfileRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/me")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) { this.userService = userService; }

    @PutMapping("/profile")
    AuthUserResponse updateProfile(Authentication authentication, @Valid @RequestBody UpdateProfileRequest request) {
        return userService.updateProfile(principal(authentication).id(), request);
    }

    @PutMapping("/password")
    ResponseEntity<Void> changePassword(Authentication authentication,
                                        @Valid @RequestBody ChangePasswordRequest request,
                                        HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        userService.changePassword(principal(authentication).id(), request);
        new SecurityContextLogoutHandler().logout(httpRequest, httpResponse, authentication);
        return ResponseEntity.noContent().build();
    }

    private CustomUserPrincipal principal(Authentication authentication) {
        return (CustomUserPrincipal) authentication.getPrincipal();
    }
}
