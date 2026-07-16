package com.warehouse.wms.entity;

public enum QuotationStatus {
    PENDING,        // Waiting for review
    APPROVED,       // Approved
    REJECTED,       // Rejected
    COMPARED,       // Compared with others
    CONVERTED,      // Converted to PO
    CANCELLED,       // Cancelled
    COMPLETED
}