// ====== FILE: src/main/java/com/warehouse/wms/dto/response/ScanResponse.java ======
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
public class ScanResponse {

    private String scannedValue;
    private String scanType;
    private Boolean isValid;
    private String message;
    private String taskNumber;
    private String itemCode;
    private String itemName;
    private Integer quantity;
    private String binId;
    private String binBarcode;
    private String fullLocation;
    private String status;
    private String stage;
    private String operatorName;
    private LocalDateTime scannedAt;
    private Boolean isVerified;
    private String verifiedBy;
    private String remarks;
}