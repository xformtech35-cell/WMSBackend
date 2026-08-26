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
public class ShippingLabelBarcodeResponse {
    private String labelNumber;
    private String packageNumber;
    private String packageBarcode;
    private String soNumber;
    private String customerName;
    private String itemName;
    private Integer quantity;
    private String trackingNumber;
    private String labelStatus;
    private String barcodeBase64;
    private String barcodeType;
    private String barcodeData;
    private LocalDateTime generatedAt;
}