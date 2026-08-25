package com.warehouse.wms.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecentPickConfirmationResponse {
    private String confirmationNumber;
    private String pickTaskNumber;
    private String itemCode;
    private String itemName;
    private Integer pickedQuantity;
    private String confirmedBy;
    private String status;
    private LocalDateTime confirmedDate;
}

// PickConfirmationStatusCountResponse.java
