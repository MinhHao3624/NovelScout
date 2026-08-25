package com.minhhao.novelscout.auth;

import com.minhhao.novelscout.auth.dto.*;
import com.minhhao.novelscout.common.api.ApiException;
import com.minhhao.novelscout.user.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final GoogleAuthService googleAuthService;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;
    private final SessionAuthenticationStrategy sessionAuthenticationStrategy;
    private final CsrfTokenRepository csrfTokenRepository;

    public AuthController(
            AuthService authService,
            GoogleAuthService googleAuthService,
            AuthenticationManager authenticationManager,
            SecurityContextRepository securityContextRepository,
            SessionAuthenticationStrategy sessionAuthenticationStrategy,
            CsrfTokenRepository csrfTokenRepository
    ) {
        this.authService = authService;
        this.googleAuthService = googleAuthService;
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
        this.sessionAuthenticationStrategy = sessionAuthenticationStrategy;
        this.csrfTokenRepository = csrfTokenRepository;
    }

    @GetMapping("/csrf")
    CsrfResponse csrf(CsrfToken csrfToken) {
        return new CsrfResponse(csrfToken.getHeaderName(), csrfToken.getToken());
    }

    @PostMapping("/send-otp")
    ResponseEntity<Map<String, String>> sendOtp(@Valid @RequestBody SendOtpRequest request) {
        authService.sendOtp(request);
        return ResponseEntity.ok(Map.of("message", "Mã xác thực OTP đã được gửi đến hòm thư " + request.email()));
    }

    @PostMapping("/register-with-otp")
    ResponseEntity<AuthUserResponse> registerWithOtp(
            @Valid @RequestBody RegisterWithOtpRequest request,
            HttpServletRequest httpRequest, HttpServletResponse httpResponse
    ) {
        AuthUserResponse userResponse = authService.registerWithOtp(request);
        try {
            Authentication authentication = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(request.username(), request.password()));
            sessionAuthenticationStrategy.onAuthentication(authentication, httpRequest, httpResponse);
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
            securityContextRepository.saveContext(context, httpRequest, httpResponse);
            csrfTokenRepository.saveToken(null, httpRequest, httpResponse);
        } catch (Exception e) {
            // Do not break flow if auto-login fails
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(userResponse);
    }

    @PostMapping("/google")
    ResponseEntity<AuthUserResponse> googleLogin(
            @Valid @RequestBody GoogleAuthRequest request,
            HttpServletRequest httpRequest, HttpServletResponse httpResponse
    ) {
        User user = googleAuthService.authenticateGoogleUser(request.credential());
        CustomUserPrincipal principal = CustomUserPrincipal.from(user);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                principal.getAuthorities()
        );
        sessionAuthenticationStrategy.onAuthentication(authentication, httpRequest, httpResponse);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, httpRequest, httpResponse);
        csrfTokenRepository.saveToken(null, httpRequest, httpResponse);
        return ResponseEntity.ok(AuthUserResponse.from(user));
    }

    @PostMapping("/register")
    ResponseEntity<AuthUserResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    AuthUserResponse login(@Valid @RequestBody LoginRequest request,
                           HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(request.login(), request.password()));
            sessionAuthenticationStrategy.onAuthentication(authentication, httpRequest, httpResponse);
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
            securityContextRepository.saveContext(context, httpRequest, httpResponse);
            csrfTokenRepository.saveToken(null, httpRequest, httpResponse);
            return authService.getCurrentUser(((CustomUserPrincipal) authentication.getPrincipal()).id());
        } catch (AuthenticationException exception) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "BAD_CREDENTIALS", "Email, tên đăng nhập hoặc mật khẩu không đúng");
        }
    }

    @PostMapping("/logout")
    ResponseEntity<Void> logout(Authentication authentication, HttpServletRequest request, HttpServletResponse response) {
        new SecurityContextLogoutHandler().logout(request, response, authentication);
        csrfTokenRepository.saveToken(null, request, response);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    AuthUserResponse me(Authentication authentication) {
        return authService.getCurrentUser(((CustomUserPrincipal) authentication.getPrincipal()).id());
    }
}
