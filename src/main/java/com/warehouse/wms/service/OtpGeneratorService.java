package com.warehouse.wms.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Slf4j
@Service
public class OtpGeneratorService {

    private static final SecureRandom secureRandom = new SecureRandom();
    private static final int OTP_LENGTH = 6;
    private static final int OTP_EXPIRY_MINUTES = 5;

    /**
     * Generate a random 6-digit OTP
     */
    public String generateOtp() {
        int otp = 100000 + secureRandom.nextInt(900000);
        return String.valueOf(otp);
    }

    /**
     * Get OTP expiry time (5 minutes from now)
     */
    public LocalDateTime getOtpExpiry() {
        return LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES);
    }

    /**
     * Validate if OTP is expired
     */
    public boolean isOtpExpired(LocalDateTime otpExpiry) {
        if (otpExpiry == null) {
            return true;
        }
        return LocalDateTime.now().isAfter(otpExpiry);
    }

    /**
     * Validate OTP format
     */
    public boolean isValidOtpFormat(String otp) {
        if (otp == null || otp.length() != OTP_LENGTH) {
            return false;
        }
        return otp.matches("\\d+");
    }

    /**
     * Validate OTP (format + expiry)
     */
    public boolean validateOtp(String inputOtp, String storedOtp, LocalDateTime otpExpiry) {
        if (!isValidOtpFormat(inputOtp)) {
            return false;
        }
        if (storedOtp == null || !storedOtp.equals(inputOtp)) {
            return false;
        }
        if (isOtpExpired(otpExpiry)) {
            return false;
        }
        return true;
    }
}