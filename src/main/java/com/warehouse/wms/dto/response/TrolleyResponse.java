// ====== FILE: src/main/java/com/warehouse/wms/dto/response/TrolleyResponse.java ======
package com.warehouse.wms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrolleyResponse {
    private Long id;
    private String trolleyIdentifier;
    private String name;
    private String description;
    private String trolleyType;
    private Integer capacity;
    private Integer currentLoad;
    private Double utilizationPercentage;
    private String status;
    private Boolean isActive;
    private LocalDateTime lastUsedAt;
    private LocalDateTime maintenanceDueDate;
    private String remarks;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<RackCompartmentResponse> compartments;

    public Double getUtilizationPercentage() {
        if (capacity == null || capacity == 0 || currentLoad == null) {
            return 0.0;
        }
        return (currentLoad.doubleValue() / capacity.doubleValue()) * 100;
    }
}