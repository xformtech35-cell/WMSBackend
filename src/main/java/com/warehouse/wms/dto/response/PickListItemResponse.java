package com.warehouse.wms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PickListItemResponse {

    private Long id;
    private String itemCode;
    private String itemName;
    private String uom;
    private Integer requiredQuantity;
    private Integer pickedQuantity;
    private Integer shortQuantity;
    private String sourceLocation;
    private String batchNumber;
    private String status;
    private String priority;
}