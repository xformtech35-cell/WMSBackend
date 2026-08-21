package com.warehouse.wms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationSummaryResponse {
	  private String itemCode;
	    private String itemName;
	    private Integer requested;
	    private Integer reserved;
	    private Integer shortQuantity;
	    private String status; // FULLY_RESERVED, PARTIALLY_RESERVED, NOT_RESERVED
}

