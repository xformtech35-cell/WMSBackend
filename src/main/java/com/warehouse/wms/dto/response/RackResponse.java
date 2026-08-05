// ====== FILE: src/main/java/com/warehouse/wms/dto/response/RackResponse.java ======
package com.warehouse.wms.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
public class RackResponse {
    private Long id;
    private String rackId;
    private String name;
    private String description;
    private Boolean isActive;
    private Double height;
    private Double width;
    private String unit;
    private Double depth;
    private Integer totalShelves;
    private String remarks;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @JsonIgnoreProperties({"racks"})  // ✅ Break circular reference
    private AisleResponse aisle;  // ✅ This will have zone → warehouse
    
    private List<BinResponse> bins;
    
    @JsonIgnoreProperties({"rack"})
    private List<RackCompartmentResponse> compartments;
}