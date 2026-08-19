package com.warehouse.wms.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    public void sendOtpEmail(String toEmail, String otp, String username) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Password Reset OTP - Warehouse WMS");
            message.setText(getOtpEmailBody(username, otp));
            
            mailSender.send(message);
            log.info("OTP email sent successfully to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send OTP email to {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Failed to send OTP email. Please try again.");
        }
    }

    public void sendPasswordResetConfirmation(String toEmail, String username) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Password Reset Successful - Warehouse WMS");
            message.setText(getPasswordResetConfirmationBody(username));
            
            mailSender.send(message);
            log.info("Password reset confirmation email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send password reset confirmation to {}: {}", toEmail, e.getMessage());
            // Don't throw exception for confirmation email failure
        }
    }

    private String getOtpEmailBody(String username, String otp) {
        return String.format("""
                Dear %s,
                
                We received a request to reset your password for the Warehouse WMS system.
                
                Your OTP for password reset is: %s
                
                This OTP is valid for 5 minutes. Please do not share this OTP with anyone.
                
                If you did not request a password reset, please ignore this email or contact your system administrator.
                
                Best regards,
                Warehouse WMS Team
                
                This is an automated message. Please do not reply to this email.
                """, 
                username != null ? username : "User",
                otp);
    }

    private String getPasswordResetConfirmationBody(String username) {
        return String.format("""
                Dear %s,
                
                Your password has been successfully reset for the Warehouse WMS system.
                
                If you did not perform this action, please contact your system administrator immediately.
                
                Best regards,
                Warehouse WMS Team
                
                This is an automated message. Please do not reply to this email.
                """, 
                username != null ? username : "User");
    }
}