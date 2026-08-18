// ====== FILE: src/main/java/com/warehouse/wms/dto/request/StockTransferRequest.java ======
package com.warehouse.wms.dto.request;

import jakarta.validation.constraints.Min;
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
public class StockTransferRequest {
    
    @NotBlank(message = "Source location is required")
    private String sourceLocation; // Format: WH-ZN-AL-RK-LV-BN
    
    @NotBlank(message = "Target location is required")
    private String targetLocation; // Format: WH-ZN-AL-RK-LV-BN
    
    @NotBlank(message = "Item code is required")
    private String itemCode;
    
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;
    
    private String inventoryNumber;
    private String transferReason;
    private String remarks;
    private String createdBy;
    private String putawayId;
}