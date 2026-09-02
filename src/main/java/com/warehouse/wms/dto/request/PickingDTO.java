package com.warehouse.wms.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PickingDTO {
	
   

    private Long lineId;
    private Integer pickedQuantity;
    private Long pickedBy;
    private String pickLocation;
    private String remarks;
}

// QCDTO.java
