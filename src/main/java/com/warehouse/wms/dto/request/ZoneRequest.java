// ====== FILE: src/main/java/com/warehouse/wms/dto/request/ZoneRequest.java ======
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
public class ZoneRequest {

    @NotBlank(message = "Zone ID is required")
    private String zoneId;

    @NotBlank(message = "Zone name is required")
    private String name;

    private String description;
    private String zoneType; // PICKING, BULK, OVERFLOW, DANGEROUS
    private Boolean isActive = true;
    private Integer priority = 0;
    private String createdBy;
    private String remarks;

    @NotNull(message = "Warehouse ID is required")
    private Long warehouseId;
}