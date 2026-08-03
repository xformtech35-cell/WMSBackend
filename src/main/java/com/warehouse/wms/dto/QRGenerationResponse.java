package com.warehouse.wms.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QRGenerationResponse {
    private Boolean success;
    private String batchId;
    private Integer totalQRCodes;
    private List<QRCodeDTO> qrCodes;
    private List<PrintJobDTO> printQueue;
    private String message;
    private LocalDateTime generatedAt;
}

// ====== FILE: src/main/java/com/warehouse/wms/dto/PrintJobDTO.java ======

