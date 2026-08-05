// ====== FILE: src/main/java/com/warehouse/wms/dto/response/RackCompartmentResponse.java ======
package com.warehouse.wms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RackCompartmentResponse {
    private Long id;
    private String compartmentId;
    private String level;
    private String position;
    private Boolean isActive;
    private Integer capacity;
    private Integer usedCapacity;
    private Integer availableCapacity;
    private Double width;
    private Double height;
    private Double depth;
    private String remarks;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private RackResponse rack;
    private TrolleyResponse trolley;
    private SalesOrderResponse salesOrder;
}

// ====== FILE: src/main/java/com/warehouse/wms/dto/response/RackResponse.java ======
