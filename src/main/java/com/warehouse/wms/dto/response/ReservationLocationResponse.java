package com.warehouse.wms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationLocationResponse {
	  private String warehouseId;
	    private String zoneId;
	    private String aisleId;
	    private String rackId;
	    private String levelId;
	    private String binId;
	    private Integer reservedQuantity;
	    private Integer availableQuantity;
	    private Integer pysicalQuantity;

	    private String batchNumber;
}
