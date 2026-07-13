package com.warehouse.wms.entity;

public enum RequestStatus {
    DRAFT,
    SUBMITTED,
    APPROVED,
    REJECTED,
    PARTIAL,      // Add this
    COMPLETED,    // Add this
    IN_PROGRESS ,  // Add this (optional)
    PENDING
}