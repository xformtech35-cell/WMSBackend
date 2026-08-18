// ====== FILE: src/main/java/com/warehouse/wms/dto/response/StockTransferResponse.java ======
package com.warehouse.wms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockTransferResponse {
    private String transferNumber;
    private String sourceLocation;
    private String targetLocation;
    private String itemCode;
    private String itemName;
    private Integer quantityTransferred;
    private String batchNumber;
    private String inventoryNumber;
    private String status;
    private String transferReason;
    private LocalDateTime transferDate;
    private String createdBy;
    private String grnNumber;
    private List<TransferDetail> details;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransferDetail {
        private String sourceBinId;
        private String sourceLocationPath;
        private Integer sourceOldQuantity;
        private Integer sourceNewQuantity;
        private String targetBinId;
        private String targetLocationPath;
        private Integer targetOldQuantity;
        private Integer targetNewQuantity;
        private Integer transferredQuantity;
        private String status;
        private String grnNumber;
        private String batchNumber;
        private String inventoryNumber;
    }
}