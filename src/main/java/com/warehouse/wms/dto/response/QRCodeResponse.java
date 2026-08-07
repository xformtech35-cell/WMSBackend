// ====== FILE: src/main/java/com/warehouse/wms/dto/response/QRCodeResponse.java ======
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
public class QRCodeResponse {

    private Long id;
    private String qrId;
    private String qrCode;
    private String qrImage;
    private String qrData;
    private String barcode;
    private String barcodeImage;
    private String qrType;
    private String labelLevel;
    private String labelType;
    private String grnNumber;
    private Long putawayTaskId;
    private Long putawayLineId;
    private String itemCode;
    private String itemName;
    private String batchNumber;
    private String serialNumbers;
    private Integer quantity;
    private String uom;
    private LocalDateTime mfgDate;
    private LocalDateTime expiryDate;
    private String warehouseId;
    private String zone;
    private String aisle;
    private String rack;
    private String shelf;
    private String level;

    private String binId;
    private String palletNumber;
    private String status;
    private String printedBy;
    private LocalDateTime printedAt;
    private Integer printCopies;
    private String scannedBy;
    private LocalDateTime scannedAt;
    private Integer scanCount;
    private String generatedBy;
    private String templateName;
    private String labelFormat;
    private String remarks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}