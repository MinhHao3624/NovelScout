package com.minhhao.novelscout.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {
    Optional<EmailVerification> findTopByEmailAndOtpCodeOrderByCreatedAtDesc(String email, String otpCode);
    Optional<EmailVerification> findTopByEmailOrderByCreatedAtDesc(String email);
    void deleteByEmail(String email);
}
