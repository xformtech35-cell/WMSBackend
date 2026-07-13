package com.warehouse.wms.dto;

import jakarta.validation.constraints.Min;
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
public class PurchaseRequestItemDTO {
    
    private Long id;
    
    @NotBlank(message = "Item Code is required")
    private String itemCode;
    
    @NotBlank(message = "Item Name is required")
    private String itemName;
    
    private String description;
    
    @NotBlank(message = "UOM is required")
    private String uom;
    
    @NotNull(message = "Requested Quantity is required")
    @Min(value = 1, message = "Requested Quantity must be at least 1")
    private Integer requestedQty;
    
    private Integer currentStock;
    
    private String reason;
    
    // Receiving related fields
    private Integer receivedQuantity;
    private Integer pendingQuantity;
    private String itemStatus;
    
    // Add receipts field
    private List<ItemReceiptDTO> receipts;
}