// ====== FILE: src/main/java/com/warehouse/wms/dto/response/ZoneResponse.java ======
package com.warehouse.wms.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ZoneResponse {
    private Long id;
    private String zoneId;
    private String name;
    private String description;
    private String zoneType;
    private Boolean isActive;
    private Integer priority;
    private Integer totalAisles;
    private String remarks;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Stock availability - make sure this field exists
    private StockAvailabilitySummary stockSummary;
    
    private String barcodeData; // Store the actual barcode data (warehouseId-zoneId)

    private String barcodeImage; // Base64 encoded barcode image

    private String barcodeFormat; // CODE128, CODE39, etc.

    @JsonIgnoreProperties({"zones"})
    private WarehouseResponse warehouse;  // ✅ This will show warehouse details
    
    @JsonIgnoreProperties({"zone"})
    private List<AisleResponse> aisles;
}