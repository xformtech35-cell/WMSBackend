package com.warehouse.wms.controller;

import com.warehouse.wms.dto.*;
import com.warehouse.wms.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth/password")
@RequiredArgsConstructor
public class PasswordResetController {

    private final UserService userService;

    /**
     * Request OTP for password reset
     * POST /api/auth/password/forgot
     */
    @PostMapping("/forgot")
    public ResponseEntity<PasswordResetResponse> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {
        PasswordResetResponse response = userService.initiatePasswordReset(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Verify OTP
     * POST /api/auth/password/verify-otp
     */
    @PostMapping("/verify-otp")
    public ResponseEntity<PasswordResetResponse> verifyOtp(
            @Valid @RequestBody VerifyOtpRequest request) {
        PasswordResetResponse response = userService.verifyOtp(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Reset password using OTP
     * POST /api/auth/password/reset
     */
    @PostMapping("/reset")
    public ResponseEntity<PasswordResetResponse> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        PasswordResetResponse response = userService.resetPassword(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Resend OTP
     * POST /api/auth/password/resend-otp
     */
    @PostMapping("/resend-otp")
    public ResponseEntity<PasswordResetResponse> resendOtp(
            @Valid @RequestBody ForgotPasswordRequest request) {
        PasswordResetResponse response = userService.resendOtp(request.getEmail());
        return ResponseEntity.ok(response);
    }
}