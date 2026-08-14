// ====== FILE: src/main/java/com/warehouse/wms/dto/request/WarehouseRequest.java ======
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
public class WarehouseRequest {

    @NotBlank(message = "Warehouse ID is required")
    private String warehouseId;

    @NotBlank(message = "Warehouse name is required")
    private String name;

    private String location;
    private String address;
    private String contactPerson;
    private String contactPhone;
    private String contactEmail;

    @NotNull(message = "Capacity is required")
    private Integer capacity;

    private Boolean isActive = true;
    private String createdBy;
    private String remarks;
    
    
    private Integer maxCapacity;
    private Integer minCapacity;

    private String capacityUnit;
}