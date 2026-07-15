package com.warehouse.wms.entity;

public enum RfqStatus {
    DRAFT,          // Initial state
    SUBMITTED,      // Sent to vendors
    IN_PROGRESS,    // Receiving quotations
    PARTIAL,        // Some vendors responded
    COMPLETED,      // All vendors responded
    CLOSED,         // RFQ closed
    CANCELLED       // Cancelled
}