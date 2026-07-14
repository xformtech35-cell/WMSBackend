package com.warehouse.wms.entity;

public enum PurchaseOrderStatus {
    DRAFT,          // Initial state
    SUBMITTED,      // Sent to supplier
    APPROVED,       // Approved
    REJECTED,       // Rejected
    IN_PROGRESS,    // Being processed
    PARTIAL,        // Partially received
    SHIPPED,        // Shipped by supplier
    DELIVERED,      // Delivered
    COMPLETED,      // Fully received
    CANCELLED,      // Cancelled
    CLOSED          // Closed
}