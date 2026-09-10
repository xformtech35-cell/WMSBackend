package com.warehouse.wms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PackedOrderItemDTO {

    private Long lineId;
    private String itemCode;
    private String itemName;
    private String uom;

    private Integer orderQuantity;
    private Integer pickedQuantity;
    private Integer qcQuantity;
    private Integer packedQuantity;

    private String packBarcode;
    private String packBarcodeImageType;
    private String packBarcodeImageBase64;   // ⬅️ include only if client needs inline image

    private String status;                   // LineStatus
}