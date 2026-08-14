// ====== FILE: src/main/java/com/warehouse/wms/dto/request/RackRequest.java ======
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
public class RackRequest {

    @NotBlank(message = "Rack ID is required")
    private String rackId;

    @NotBlank(message = "Rack name is required")
    private String name;

    private String description;
    private Boolean isActive = true;
    private Double height;
    private Double width;
    private String unit;

    private Double depth;
    private String createdBy;
    private String remarks;

    private Integer maxCapacity;
    private Integer minCapacity;
    private String capacityUnit;
    
    
    @NotNull(message = "Aisle ID is required")
    private Long aisleId;
}