package com.minhhao.novelscout.auth;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "email_verifications")
public class EmailVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(name = "otp_code", nullable = false, length = 10)
    private String otpCode;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private boolean verified = false;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    protected EmailVerification() {}

    public EmailVerification(String email, String otpCode, LocalDateTime expiresAt) {
        this.email = email;
        this.otpCode = otpCode;
        this.expiresAt = expiresAt;
        this.verified = false;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getOtpCode() { return otpCode; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public boolean isVerified() { return verified; }
    public void setVerified(boolean verified) { this.verified = verified; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
}
