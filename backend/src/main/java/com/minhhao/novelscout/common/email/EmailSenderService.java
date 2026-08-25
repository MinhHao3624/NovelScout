package com.minhhao.novelscout.common.email;

import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailSenderService {

    private static final Logger log = LoggerFactory.getLogger(EmailSenderService.class);

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@novelscout.com}")
    private String fromEmail;

    public void sendOtpEmail(String toEmail, String otpCode) {
        String subject = "[NovelScout] Mã xác thực đăng ký tài khoản: " + otpCode;
        String htmlContent = buildOtpHtmlContent(otpCode);

        boolean emailSentSuccess = false;

        if (mailSender != null && fromEmail != null && !fromEmail.isBlank()) {
            try {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
                helper.setFrom(fromEmail, "NovelScout System");
                helper.setTo(toEmail);
                helper.setSubject(subject);
                helper.setText(htmlContent, true);

                mailSender.send(message);
                emailSentSuccess = true;
                log.info("✅ Đã gửi email chứa mã OTP {} thành công đến hòm thư: {}", otpCode, toEmail);
            } catch (Exception e) {
                log.warn("⚠️ Không thể gửi mail qua SMTP Server ({}), fallback ghi log console. Lỗi: {}", toEmail, e.getMessage());
            }
        }

        // Luôn ghi log console rõ ràng
        log.info("==================================================================");
        log.info("[EMAIL OTP VERIFICATION] Target Email: {}", toEmail);
        log.info("[MÃ XÁC NHẬN CỦA BẠN LÀ]: >>> {} <<< (Hiệu lực: 5 phút)", otpCode);
        log.info("==================================================================");
    }

    private String buildOtpHtmlContent(String otpCode) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f8fafc; padding: 20px; color: #1e293b; }
                    .card { max-width: 520px; margin: 0 auto; background: #ffffff; border-radius: 12px; padding: 32px; border: 1px solid #e2e8f0; box-shadow: 0 4px 12px rgba(0,0,0,0.05); }
                    .brand { font-size: 20px; font-weight: 700; color: #059669; margin-bottom: 20px; display: flex; align-items: center; gap: 8px; }
                    .otp-box { background: #ecfdf5; border: 2px dashed #059669; padding: 18px; border-radius: 10px; text-align: center; margin: 24px 0; }
                    .otp-code { font-size: 32px; font-weight: 800; letter-spacing: 6px; color: #047857; }
                    .footer { font-size: 12px; color: #94a3b8; margin-top: 24px; text-align: center; border-top: 1px solid #f1f5f9; padding-top: 16px; }
                </style>
            </head>
            <body>
                <div class="card">
                    <div class="brand">NovelScout - Chào mừng bạn đọc mới</div>
                    <h2>Mã Xác Thực Đăng Ký Tài Khoản</h2>
                    <p>Chào bạn,</p>
                    <p>Bạn vừa yêu cầu đăng ký tài khoản mới trên hệ thống NovelScout. Dưới đây là mã xác thực OTP của bạn:</p>
                    <div class="otp-box">
                        <div class="otp-code">%s</div>
                    </div>
                    <p>Mã xác thực này có hiệu lực trong vòng <strong>5 phút</strong>. Vui lòng không chia sẻ mã này cho bất kỳ ai khác.</p>
                    <p>Nếu bạn không thực hiện yêu cầu này, vui lòng bỏ qua email.</p>
                    <div class="footer">
                        &copy; 2026 NovelScout System. All rights reserved.
                    </div>
                </div>
            </body>
            </html>
            """.formatted(otpCode);
    }
}
