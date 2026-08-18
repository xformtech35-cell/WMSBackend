// ====== FILE: src/main/java/com/warehouse/wms/dto/response/PutawayTaskResponse.java ======
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
public class PutawayTaskResponse {

    private Long id;
    private String taskNumber;
    private String grnNumber;
    private Long inboundId;
    private String assignedTo;
    private String assignedBy;
    private LocalDateTime assignedAt;
    private String status;
    private String stage;
    private Integer totalQuantity;
    private Integer putawayQuantity;
    private Integer pendingQuantity;
    private String warehouseId;
    private String receivingArea;
    private LocalDateTime startedAt;
    private LocalDateTime pickedAt;
    private LocalDateTime transportedAt;
    private LocalDateTime scannedAt;
    private LocalDateTime placedAt;
    private LocalDateTime confirmedAt;
    private LocalDateTime completedAt;
    private String confirmationNumber;
    private String confirmedBy;
    private String remarks;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<PutawayLineResponse> lines;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PutawayLineResponse {
        private Long id;
        private Integer lineNumber;
        private String itemCode;
        private String itemName;
        private String uom;
        private Integer quantity;
        private Integer putawayQuantity;
        private Integer remainingQuantity;
        private String suggestedWarehouse;
        private String suggestedZone;
        private String suggestedAisle;
        private String suggestedRack;
        private String suggestedShelf;
        private String suggestedLevel;

        private String suggestedBin;
        private String fullpath;
        private String actualWarehouse;
        private String actualZone;
        private String actualAisle;
        private String actualRack;
        private String actualShelf;
        private String actualLevel;

        private String actualBin;
        private String binBarcode;
        private String batchNumber;
        private String serialNumber;
        private String qrCodeValue;
        private String status;
        private String remarks;
    }
}