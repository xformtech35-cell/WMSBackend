package com.warehouse.wms.entity;

public enum InboundStatus {
    PENDING,
    GATE_ENTRY,
    UNLOADING,
    RECEIVING,
    QUALITY_INSPECTION,
    COMPLETED,
    REJECTED,
    CANCELLED
}