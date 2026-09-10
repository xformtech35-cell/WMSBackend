package com.warehouse.wms.dto.request;

import java.time.LocalDate;

import com.warehouse.wms.entity.VendorReturnOrder;
import com.warehouse.wms.entity.VendorReturnRequest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PackedOrderFilterDTO {

    // Free-text search (vroNumber, supplierName, packBarcode, itemCode, itemName)
    private String searchTerm;

    // Order filters
    private String vroNumber;
    private String supplierName;
    private String supplierCode;

    // Status / Priority / Type
    private VendorReturnOrder.OrderStatus status;       // e.g. PACKED, DISPATCHED
    private VendorReturnRequest.Priority priority;
    private VendorReturnRequest.ReturnType returnType;

    // Pack-specific filters
    private String packedBy;                            // Long as string (packed_by)
    private String packBarcode;                         // line-level pack barcode
    private Boolean hasPackBarcodeImage;                // true → only lines with generated barcode
    private String itemCode;
    private String itemName;

    // Date filters (on order.packedAt)
    private LocalDate packedFromDate;
    private LocalDate packedToDate;

    // Quantity filters (packed qty per order)
    private Integer minPackedQuantity;
    private Integer maxPackedQuantity;

    // Amount filters
    private Double minAmount;
    private Double maxAmount;
}