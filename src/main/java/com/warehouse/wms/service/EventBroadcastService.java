package com.warehouse.wms.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventBroadcastService {

    private final SimpMessagingTemplate messagingTemplate;

    public void broadcastDashboardUpdate(Object data) {
        log.info("[EventBroadcastService] Broadcasting dashboard update: {}", data);
        messagingTemplate.convertAndSend("/topic/dashboard", data);
    }

    public void broadcastInventoryChange(Long skuId, Object data) {
        log.info("[EventBroadcastService] Broadcasting inventory change for SKU {}: {}", skuId, data);
        messagingTemplate.convertAndSend("/topic/inventory/" + skuId, data);
    }

    public void broadcastCycleCountUpdate(Long taskId, Object data) {
        log.info("[EventBroadcastService] Broadcasting cycle count update for task {}: {}", taskId, data);
        messagingTemplate.convertAndSend("/topic/cycle-count/" + taskId, data);
    }

    public void broadcastReturnsUpdate(Long returnId, Object data) {
        log.info("[EventBroadcastService] Broadcasting returns update for return {}: {}", returnId, data);
        messagingTemplate.convertAndSend("/topic/returns/" + returnId, data);
    }

    public void broadcastAlert(String alertType, String message) {
        log.info("[EventBroadcastService] Broadcasting alert [{}]: {}", alertType, message);
        messagingTemplate.convertAndSend("/topic/alerts", Map.of("type", alertType, "message", message));
    }
}
