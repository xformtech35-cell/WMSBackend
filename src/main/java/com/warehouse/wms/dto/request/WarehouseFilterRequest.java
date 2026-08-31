// ====== FILE: src/main/java/com/warehouse/wms/dto/request/WarehouseFilterRequest.java ======
package com.warehouse.wms.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseFilterRequest {
    // Warehouse filters
    private String warehouseId;
    private String name;
    private String location;
    private Boolean isActive;
    private String contactPerson;
    private String contactPhone;
    private String contactEmail;
    
    // Zone filters
    private String zoneName;
    private String zoneType;
    private Boolean zoneActive;
    
    // Aisle filters
    private String aisleName;
    private Boolean aisleActive;
    
    // Rack filters
    private String rackName;
    private Boolean rackActive;
    
    // Level filters
    private Integer levelNumber;
    private Boolean levelActive;
    
    // Bin filters
    private String binBarcode;
    private String binStatus;
    private Boolean binActive;
    
    // Date filters
    private String createdFrom;
    private String createdTo;
    
    // Pagination
    private Integer page;
    private Integer size;
    private String sortBy;
    private String sortDirection;
}