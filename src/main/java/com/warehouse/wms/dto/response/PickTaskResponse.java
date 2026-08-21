package com.warehouse.wms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PickTaskResponse {

    private String pickTaskNumber;
    private String pickListNumber;
    private String soNumber;
    private String itemCode;
    private String itemName;
    private String uom;
    private Integer requiredQuantity;
    private Integer pickedQuantity;
    private String locationBarcode;
    private String itemBarcode;
    private String binId;
    private String batchNumber;
    private String pickerId;
    private String pickerName;
    private LocalDateTime scanTime;
    private String status;
    private Boolean isScanned;
    private String remarks;
    private LocalDateTime createdAt;
    
    private Long salesOrderLineId;  // ADD THIS FIELD

    private Long inventoryId;  // ADD THIS

    private Integer quantityToPick;  // ADD THIS

}