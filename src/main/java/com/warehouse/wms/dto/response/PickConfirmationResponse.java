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
public class PickConfirmationResponse {

    private String confirmationNumber;
    private String pickTaskNumber;
    private String pickListNumber;
    private String soNumber;
    private String itemCode;
    private String itemName;
    private Integer requiredQuantity;
    private Integer pickedQuantity;
    private Integer shortQuantity;
    private String barcode;
    private String confirmedBy;
    private LocalDateTime confirmedDate;
    private String status;
    private String remarks;
}