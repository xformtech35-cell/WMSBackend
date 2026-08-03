package com.warehouse.wms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QRCodeItemDTO {
    private String itemCode;
    private String itemName;
    private Integer quantity;
    private String batchNumber;
    private String expiryDate;
    private String uom;
}
