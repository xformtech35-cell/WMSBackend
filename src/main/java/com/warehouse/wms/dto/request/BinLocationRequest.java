// ====== FILE: src/main/java/com/warehouse/wms/dto/request/BinLocationRequest.java ======
package com.warehouse.wms.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BinLocationRequest {

    @NotBlank(message = "Bin ID is required")
    private String binId;

    @NotBlank(message = "Bin barcode is required")
    private String binBarcode;

    @NotBlank(message = "Warehouse ID is required")
    private String warehouseId;

    private String zone;
    private String aisle;
    private String rack;
    private String shelf;
    private String level;
    private String position;

    @NotNull(message = "Capacity is required")
    private Integer capacity;

    private Integer minThreshold;
    private Integer maxThreshold;
    private String locationType;
    private String zoneType;
    private String movementType;
    private Integer priority;
    private Integer distanceFromDispatch;
    private String createdBy;
    private String remarks;
}