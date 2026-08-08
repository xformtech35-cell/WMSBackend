// ====== FILE: src/main/java/com/warehouse/wms/dto/request/PutawayInitiateRequest.java ======
package com.warehouse.wms.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
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
public class PutawayInitiateRequest {

    @NotBlank(message = "GRN number is required")
    private String grnNumber;

    @NotBlank(message = "Warehouse ID is required")
    private String warehouseId;

    private String assignedTo;

    private String receivingArea;

    private String createdBy;

    @Valid
    @NotEmpty(message = "At least one line item is required")
    private List<PutawayLineRequest> lines;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PutawayLineRequest {
        @NotBlank(message = "Item code is required")
        private String itemCode;

        private String itemName;

        private String uom;
        
        

        @NotNull(message = "Quantity is required")
        private Integer quantity;

        private Long inboundLineId;

        private String batchNumber;

        private String serialNumber;

        private String suggestedBin;

        private String remarks;
    }
}