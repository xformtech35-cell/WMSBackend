// ====== FILE: src/main/java/com/warehouse/wms/dto/request/AisleRequest.java ======
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
public class AisleRequest {

    @NotBlank(message = "Aisle ID is required")
    private String aisleId;

    @NotBlank(message = "Aisle name is required")
    private String name;

    private String description;
    private Boolean isActive = true;
    private Double width;
    private Double length;
    private String unit;

    private String createdBy;
    private String remarks;

    @NotNull(message = "Zone ID is required")
    private Long zoneId;
}