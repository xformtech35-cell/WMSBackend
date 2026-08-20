package com.warehouse.wms.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Value("${app.name:Warehouse WMS}")
    private String appName;

    public void sendOtpEmail(String toEmail, String otp, String username) {
        try {
            Context context = new Context();
            context.setVariable("username", username != null ? username : "User");
            context.setVariable("otp", otp);
            context.setVariable("appName", appName);
            context.setVariable("baseUrl", baseUrl);
            context.setVariable("otpExpiryMinutes", 5);
            context.setVariable("currentYear", java.time.Year.now().getValue());

            String htmlContent = templateEngine.process("email/otp-email", context);
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Password Reset OTP - " + appName);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            log.info("OTP email sent successfully to: {}", toEmail);
        } catch (MessagingException e) {
            log.error("Failed to send OTP email to {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Failed to send OTP email. Please try again.");
        }
    }

    public void sendPasswordResetConfirmation(String toEmail, String username) {
        try {
            Context context = new Context();
            context.setVariable("username", username != null ? username : "User");
            context.setVariable("appName", appName);
            context.setVariable("baseUrl", baseUrl);
            context.setVariable("currentYear", java.time.Year.now().getValue());

            String htmlContent = templateEngine.process("email/password-reset-confirmation", context);
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Password Reset Successful - " + appName);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            log.info("Password reset confirmation email sent to: {}", toEmail);
        } catch (MessagingException e) {
            log.error("Failed to send password reset confirmation to {}: {}", toEmail, e.getMessage());
        }
    }
}