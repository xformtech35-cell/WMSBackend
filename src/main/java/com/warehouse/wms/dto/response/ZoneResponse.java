// ====== FILE: src/main/java/com/warehouse/wms/dto/response/ZoneResponse.java ======
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
public class ZoneResponse {
    private Long id;
    private String zoneId;
    private String name;
    private String description;
    private String zoneType;
    private Boolean isActive;
    private Integer priority;
    private Integer totalAisles;
    private String remarks;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private WarehouseResponse warehouse;
    private List<AisleResponse> aisles;
}