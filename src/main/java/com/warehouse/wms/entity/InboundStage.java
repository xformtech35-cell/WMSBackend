package com.warehouse.wms.entity;

public enum InboundStage {
    PENDING_INBOUND,
    GATE_ENTRY,
    UNLOADING,
    GOODS_RECEIVING,
    QUALITY_INSPECTION,
    GRN_GENERATED,
    COMPLETED
}