package com.warehouse.wms.util;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class SoNumberGenerator {

    private static final String PREFIX = "SO";
    private static final String DATE_FORMAT = "yyyyMMdd";
    private static final int SEQUENCE_LENGTH = 4;
    
    private final AtomicLong sequence = new AtomicLong(1);

    /**
     * Generate SO Number with format: SO-YYYYMMDD-XXXX
     * Example: SO-20260821-0001
     */
    public String generateSoNumber() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_FORMAT));
        String seq = String.format("%0" + SEQUENCE_LENGTH + "d", sequence.getAndIncrement());
        return PREFIX + "-" + date + "-" + seq;
    }

    /**
     * Generate SO Number with custom date
     */
    public String generateSoNumber(LocalDateTime date) {
        String dateStr = date.format(DateTimeFormatter.ofPattern(DATE_FORMAT));
        String seq = String.format("%0" + SEQUENCE_LENGTH + "d", sequence.getAndIncrement());
        return PREFIX + "-" + dateStr + "-" + seq;
    }

    /**
     * Generate SO Number with custom prefix
     */
    public String generateSoNumber(String customPrefix) {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_FORMAT));
        String seq = String.format("%0" + SEQUENCE_LENGTH + "d", sequence.getAndIncrement());
        return customPrefix + "-" + date + "-" + seq;
    }

    /**
     * Reset sequence (for testing or new day)
     */
    public void resetSequence() {
        sequence.set(1);
    }

    /**
     * Get next sequence number without generating full number
     */
    public long getNextSequence() {
        return sequence.get();
    }
}