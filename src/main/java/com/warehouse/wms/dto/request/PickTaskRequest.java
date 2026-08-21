package com.warehouse.wms.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PickTaskRequest {

    @NotBlank(message = "Pick List Number is required")
    private String pickListNumber;

    @NotBlank(message = "Item code is required")
    private String itemCode;

    @NotNull(message = "Required quantity is required")
    @Positive(message = "Required quantity must be greater than 0")
    private Integer requiredQuantity;

    @NotBlank(message = "Location barcode is required")
    private String locationBarcode;

    @NotBlank(message = "Item barcode is required")
    private String itemBarcode;

    private String binId;

    private String batchNumber;

    private String pickerId;
    
    private Long inventoryId;  // ADD THIS FIELD
    
    private Long salesOrderLineId;  // ADD THIS FIELD

    

    private String pickerName;

    private String createdBy;
}