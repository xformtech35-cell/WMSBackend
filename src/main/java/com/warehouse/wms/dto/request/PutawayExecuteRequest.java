// ====== FILE: src/main/java/com/warehouse/wms/dto/request/PutawayExecuteRequest.java ======
package com.warehouse.wms.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PutawayExecuteRequest {

    @NotBlank(message = "Task number is required")
    private String taskNumber;

    private String stage; // PICKED, TRANSPORTED, SCANNED, PLACED

    private String binId;
    
    private long putawayLineId;


    private String binBarcode;

    private String scannedBy;

    private String operatorName;

    private String remarks;
}