// ====== FILE: src/main/java/com/warehouse/wms/dto/request/RackCompartmentRequest.java ======
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
public class RackCompartmentRequest {

    @NotBlank(message = "Compartment ID is required")
    private String compartmentId;

    private String level;

    private String position;

    private Boolean isActive = true;

    @NotNull(message = "Capacity is required")
    private Integer capacity;

    private Double width; // in cm

    private Double height; // in cm

    private Double depth; // in cm

    private String createdBy;

    private String remarks;

    @NotNull(message = "Rack ID is required")
    private Long rackId;

    private Long trolleyId; // Optional

    private Long salesOrderId; // Optional
}