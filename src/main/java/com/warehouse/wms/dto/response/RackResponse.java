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
    private Double depth;
    private Integer totalShelves;
    private String remarks;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @JsonIgnoreProperties({"racks", "zone"})  // ✅ Ignore backward references
    private AisleResponse aisle;
    
    private List<BinResponse> bins;
    
    @JsonIgnoreProperties({"rack"})  // ✅ Ignore backward references
    private List<RackCompartmentResponse> compartments;
}