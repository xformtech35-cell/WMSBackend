// ====== FILE: src/main/java/com/warehouse/wms/dto/response/AisleResponse.java ======
package com.warehouse.wms.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
public class AisleResponse {
    private Long id;
    private String aisleId;
    private String name;
    private String description;
    private Boolean isActive;
    private Double width;
    private Double length;
    private Integer totalRacks;
    private String unit;
    private String remarks;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    
    private Integer maxCapacity;
    private Integer minCapacity;

    private String capacityUnit;
    
    
    private StockAvailabilitySummary stockSummary;

    
    private String barcodeData; // Store the actual barcode data (warehouseId-zoneId)

    private String barcodeImage; // Base64 encoded barcode image

    private String barcodeFormat; // CODE128, CODE39, etc.


    @JsonIgnoreProperties({"aisles"})
    private ZoneResponse zone;  // ✅ This will have warehouse
    
    @JsonIgnoreProperties({"aisle"})
    private List<RackResponse> racks;
}