// ====== FILE: src/main/java/com/warehouse/wms/dto/request/LevelRequest.java ======
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
public class LevelRequest {

    @NotBlank(message = "Level ID is required")
    private String levelId;

    @NotBlank(message = "Level name is required")
    private String name;

    private String description;
    
    private String unit;
    
    private Integer maxCapacity;
    private Integer minCapacity;

    private String capacityUnit;

    @NotNull(message = "Level number is required")
    private Integer levelNumber;

    private Double heightCm;

    private Double maxWeightKg;

    private Integer maxItems;

    private Boolean isActive = true;

    private String createdBy;

    private String remarks;

    @NotNull(message = "Rack ID is required")
    private Long rackId;
}