package com.warehouse.wms.dto;

import lombok.Data;

@Data
public class InventoryResponse {
    private Long id;
    private String skuCode;
    private String itemBarcode;
    private String state;
    private String binBarcode;
    private String batchNo;
    private Integer quantity;
    // ADD THESE NEW FIELDS
    private String itemCode;
    private String itemName;
}