package com.warehouse.wms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PickListItemDTO {
    private Long id;
    private Long lineId;
    private String itemCode;
    private String itemName;
    private String uom;
    private Integer orderQuantity;
    private Integer pickedQuantity;
    private Integer remainingQuantity;
    private String pickLocation;
    private Integer pickSequence;
    private String status;
    private String statusDisplayName;
    private String batchNumber;
    private String serialNumbers;
    private String remarks;
}