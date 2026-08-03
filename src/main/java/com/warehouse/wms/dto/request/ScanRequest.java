// ====== FILE: src/main/java/com/warehouse/wms/dto/request/ScanRequest.java ======
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
public class ScanRequest {

    @NotBlank(message = "Scanned value is required")
    private String scannedValue;

    @NotBlank(message = "Scan type is required")
    private String scanType; // QR, BARCODE, BIN

    private String taskNumber;

    private String operatorName;

    private String deviceId;

    private String location;

    private String remarks;
}