// ====== FILE: src/main/java/com/warehouse/wms/dto/response/BinResponse.java ======
package com.warehouse.wms.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BinResponse {
    private Long id;
    private String binId;
    private String binBarcode;
    private String warehouseId;
    private String zone;
    private String aisle;
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
    private String status;
    private BigDecimal utilizationPercentage;
    private LocalDateTime lastAccessedAt;
    private LocalDateTime lastPutawayAt;
    private LocalDateTime lastPickAt;
    private String remarks;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ✅ Full Hierarchy: Rack → Aisle → Zone → Warehouse
    @JsonIgnoreProperties({"bins", "compartments"})
    private RackResponse rack;
}