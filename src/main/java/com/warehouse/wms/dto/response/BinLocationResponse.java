// ====== FILE: src/main/java/com/warehouse/wms/dto/response/BinLocationResponse.java ======
package com.warehouse.wms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BinLocationResponse {
    private Long id;
    private String binId;
    private String binBarcode;
    private String warehouseId;
    private String zone;
    private String aisle;
    private String rack;
    private String shelf;
    private String level;
    private String position;
    private Integer capacity;
    private Integer availableCapacity;
    private Integer usedCapacity;
    private Integer minThreshold;
    private Integer maxThreshold;
    private String itemCode;
    private String itemName;
    private String uom;
    private Boolean isOccupied;
    private Boolean isActive;
    private Boolean isReserved;
    private String reservedFor;
    private String locationType;
    private String zoneType;
    private String movementType;
    private Integer priority;
    private Integer distanceFromDispatch;
    private String fullLocation;
    private LocalDateTime lastAccessedAt;
    private LocalDateTime lastPutawayAt;
    private LocalDateTime lastPickAt;
    private String remarks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
}