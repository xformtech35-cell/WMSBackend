// ====== FILE: src/main/java/com/warehouse/wms/dto/request/QRCodeGenerateRequest.java ======
package com.warehouse.wms.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QRCodeGenerateRequest {

    @NotBlank(message = "QR type is required")
    private String qrType; // QR_CODE, CODE128, DATAMATRIX, GS1_128

    @NotBlank(message = "Label level is required")
    private String labelLevel; // UNIT, BOX, PALLET, BATCH, LOCATION

    @NotBlank(message = "Label type is required")
    private String labelType; // PUTAWAY, BIN, PRODUCT, SHIPMENT

    private String grnNumber;

    private Long putawayTaskId;

    private Long putawayLineId;
    
    private Long inboundLineId;

    

    private String binId;

    private String itemCode;

    private String itemName;

    private String batchNumber;

    @NotNull(message = "Quantity is required")
    private Integer quantity;

    private String uom;

    private String warehouseId;

    private String zone;

    private String aisle;

    private String rack;

    private String shelf;

    private String generatedBy;

    private String templateName;

    private String labelFormat; // PNG, PDF, ZPL, SVG

    private String remarks;
}