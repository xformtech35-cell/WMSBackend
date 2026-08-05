// ====== FILE: src/main/java/com/warehouse/wms/dto/request/TrolleyRequest.java ======
package com.warehouse.wms.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrolleyRequest {

    @NotBlank(message = "Trolley identifier is required")
    private String trolleyIdentifier;

    private String name;

    private String description;

    private String trolleyType; // MANUAL, ELECTRIC, HAND_PALLET, FORKLIFT

    @NotNull(message = "Capacity is required")
    private Integer capacity;

    private String status = "AVAILABLE";

    private Boolean isActive = true;

    private LocalDateTime maintenanceDueDate;

    private String createdBy;

    private String remarks;
}