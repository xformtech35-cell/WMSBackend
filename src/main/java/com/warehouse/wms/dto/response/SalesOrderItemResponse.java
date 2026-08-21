package com.warehouse.wms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesOrderItemResponse {

    private Long id;
    private String itemCode;
    private String itemName;
    private String uom;
    private Integer orderedQuantity;
    private Integer reservedQuantity;
    private Integer pickedQuantity;
    private Integer shippedQuantity;
    private String batchNumber;
    private String sourceLocation;
}