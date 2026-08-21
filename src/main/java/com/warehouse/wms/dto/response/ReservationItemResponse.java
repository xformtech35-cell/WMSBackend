package com.warehouse.wms.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationItemResponse {
	   private String itemCode;
	    private String itemName;
	    private String uom;
	    private Integer requiredQuantity;
	    private Integer reservedQuantity;
	    private Integer availableQuantity;
	    private String batchNumber;
	    private String status;
	    private List<ReservationLocationResponse> locations;
}
