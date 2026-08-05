// ====== FILE: src/main/java/com/warehouse/wms/dto/response/AisleResponse.java ======
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
public class AisleResponse {
    private Long id;
    private String aisleId;
    private String name;
    private String description;
    private Boolean isActive;
    private Double width;
    private Double length;
    private Integer totalRacks;
    private String remarks;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private ZoneResponse zone;
    private List<RackResponse> racks;
}