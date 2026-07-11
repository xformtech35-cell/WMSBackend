package com.warehouse.wms.controller;

import com.warehouse.wms.dto.CarrierRateDto;
import com.warehouse.wms.entity.ShipmentRecord;
import com.warehouse.wms.service.DelhiveryCarrierService;
import com.warehouse.wms.service.ShiprocketCarrierService;
import com.warehouse.wms.service.ShippingService;
import com.warehouse.wms.dto.ShipmentRequest;
import com.warehouse.wms.service.EventBroadcastService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/carrier")
@RequiredArgsConstructor
public class CarrierController {

    private final DelhiveryCarrierService delhiveryCarrierService;
    private final ShiprocketCarrierService shiprocketCarrierService;
    private final ShippingService shippingService;
    private final EventBroadcastService eventBroadcastService;

    @GetMapping("/rates")
    @PreAuthorize("hasAuthority('SHIPPING_VIEW')")
    public ResponseEntity<List<CarrierRateDto>> getRates(
            @RequestParam Long orderId,
            @RequestParam(defaultValue = "110001") String pincode) {
        
        List<CarrierRateDto> rates = new ArrayList<>();
        rates.addAll(delhiveryCarrierService.getRates(orderId, pincode));
        rates.addAll(shiprocketCarrierService.getRates(orderId, pincode));
        return ResponseEntity.ok(rates);
    }

    @PostMapping("/generate-awb")
    @PreAuthorize("hasAuthority('SHIPPING_CONFIRM')")
    public ResponseEntity<ShipmentRecord> generateAwb(
            @RequestParam Long orderId,
            @RequestParam String carrierName) {

        String awbNumber;
        if ("Delhivery".equalsIgnoreCase(carrierName)) {
            awbNumber = delhiveryCarrierService.generateAWB(orderId);
        } else {
            awbNumber = shiprocketCarrierService.generateAWB(orderId);
        }

        ShipmentRequest req = new ShipmentRequest();
        req.setOrderId(orderId);
        req.setAwbNumber(awbNumber);
        req.setCourierName(carrierName);

        ShipmentRecord record = shippingService.confirmShipment(req);
        return ResponseEntity.ok(record);
    }

    @PostMapping("/webhook/update-status")
    public ResponseEntity<String> carrierWebhook(@RequestBody Map<String, Object> payload) {
        String awbNumber = (String) payload.get("awbNumber");
        String status = (String) payload.get("status");
        
        eventBroadcastService.broadcastDashboardUpdate("CARRIER_WEBHOOK_UPDATE");
        return ResponseEntity.ok("Webhook processed. Status updated: " + status + " for AWB: " + awbNumber);
    }
}
