package com.warehouse.wms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PickConfirmationItemResponse {

    // ---- Identifiers ----
    private Long id;
    private Long pickConfirmationId;   // FK back to parent

    // ---- Item ----
    private String itemCode;
    private String itemName;
    private String uom;

    // ---- Quantities ----
    private Integer requiredQuantity;
    private Integer pickedQuantity;
    private Integer shortQuantity;

    // ---- Barcode / Location ----
    private String barcode;
    private String binId;
    private String batchNumber;

    // ---- Status ----
    private String status;    // CONFIRMED, PARTIAL, REJECTED
    private String remarks;
}