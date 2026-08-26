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
public class BarcodeScanResponse {
    private String barcode;
    private String barcodeType;
    private String labelNumber;
    private String packageNumber;
    private String soNumber;
    private String customerName;
    private String itemName;
    private String status;
    private String scannedBy;
    private LocalDateTime scannedAt;
    private Boolean isValid;
    private String message;
}