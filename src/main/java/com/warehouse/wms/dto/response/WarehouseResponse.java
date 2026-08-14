// ====== FILE: src/main/java/com/warehouse/wms/dto/response/WarehouseResponse.java ======
package com.warehouse.wms.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseResponse {
    private Long id;
    private String warehouseId;
    private String name;
    private String location;
    private String address;
    private String contactPerson;
    private String contactPhone;
    private String contactEmail;
    private Boolean isActive;
    private Integer capacity;
    private Integer totalZones;
    private String remarks;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    private StockAvailabilitySummary stockSummary;


    @JsonIgnoreProperties({"warehouse"})  // ✅ Break circular reference
    private List<ZoneResponse> zones;
}