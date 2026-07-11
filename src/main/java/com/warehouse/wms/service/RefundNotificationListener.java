package com.warehouse.wms.service;

import com.warehouse.wms.event.RefundEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class RefundNotificationListener {

    @EventListener
    public void handleRefundEvent(RefundEvent event) {
        log.info("[RefundNotificationListener] Received RefundEvent for returnOrderId={}, originalOrderId={}, status={}. Processing billing credit note...",
                event.getReturnOrderId(), event.getOriginalOrderId(), event.getRefundStatus());
    }
}
