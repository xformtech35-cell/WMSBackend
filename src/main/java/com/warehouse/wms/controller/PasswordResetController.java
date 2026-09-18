package com.warehouse.wms.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.warehouse.wms.dto.ChangePasswordRequest;
import com.warehouse.wms.dto.ForgotPasswordRequest;
import com.warehouse.wms.dto.PasswordResetResponse;
import com.warehouse.wms.dto.ResetPasswordRequest;
import com.warehouse.wms.dto.VerifyOtpRequest;
import com.warehouse.wms.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
    
    @PostMapping("/change")
    public ResponseEntity<PasswordResetResponse> changePassword(
            @Valid @RequestBody ChangePasswordRequest request) {
        PasswordResetResponse response = userService.changePassword(request);
        return ResponseEntity.ok(response);
    }
}