package com.warehouse.wms.event;

import org.springframework.context.ApplicationEvent;

public class RefundEvent extends ApplicationEvent {
    private final Long returnOrderId;
    private final Long originalOrderId;
    private final String refundStatus;

    public RefundEvent(Object source, Long returnOrderId, Long originalOrderId, String refundStatus) {
        super(source);
        this.returnOrderId = returnOrderId;
        this.originalOrderId = originalOrderId;
        this.refundStatus = refundStatus;
    }

    public Long getReturnOrderId() {
        return returnOrderId;
    }

    public Long getOriginalOrderId() {
        return originalOrderId;
    }

    public String getRefundStatus() {
        return refundStatus;
    }
}
