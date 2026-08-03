// ====== FILE: src/main/java/com/warehouse/wms/dto/request/QRCodePrintRequest.java ======
package com.warehouse.wms.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QRCodePrintRequest {

    @NotNull(message = "QR Code IDs are required")
    private List<Long> qrCodeIds;

    @NotBlank(message = "Printed by is required")
    private String printedBy;

    @NotNull(message = "Print copies is required")
    private Integer printCopies = 1;

    private String printerName;

    private String remarks;
}