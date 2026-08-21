package com.warehouse.wms.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationAvailability {
	private String warehouseId;
    private String warehouseName;
    private String zone;
    private String aisle;
    private String rack;
    private String level;
    private String binId;
    private String binBarcode;
    private Integer availableQuantity;
    private Integer reservedQuantity;
    private Integer totalQuantity;
    private String status; // AVAILABLE, RESERVED, FULL, EMPTY
    private Double utilizationPercentage;
    private LocalDateTime lastUpdatedDate;
    private String batchNumber;
    private String inventoryNumber;
}


