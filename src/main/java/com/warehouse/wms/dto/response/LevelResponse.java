// ====== FILE: src/main/java/com/warehouse/wms/dto/response/LevelResponse.java ======
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
public class LevelResponse {
    private Long id;
    private String levelId;
    private String name;
    private String description;
    private String unit;

    private Integer levelNumber;
    private Double heightCm;
    private Double maxWeightKg;
    private Integer maxItems;
    private Boolean isActive;
    private String remarks;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    private String barcodeData; // Store the actual barcode data (warehouseId-zoneId)
	
    private String barcodeImage; // Base64 encoded barcode image

    private String barcodeFormat; // CODE128, CODE39, etc.

    private StockAvailabilitySummary stockSummary;

    
    
    private Integer maxCapacity;
    private Integer minCapacity;

    private String capacityUnit;

    // ✅ Allow rack to show its full hierarchy
    @JsonIgnoreProperties({"levels", "bins", "compartments"})
    private RackResponse rack;
    
    @JsonIgnoreProperties({"level"})
    private List<BinResponse> bins;
}