package com.warehouse.wms.dto.response;

import com.warehouse.wms.entity.ReturnDispatch;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DispatchResponseDTO {
    private Long id;
    private String dispatchNumber;
    private LocalDate dispatchDate;
    private LocalTime dispatchTime;
    
    // Order reference
    private Long returnOrderId;
    private String returnOrderNumber;
    private String supplierName;
    
    // Transport details
    private ReturnDispatch.TransportMode transportMode;
    private String transportModeDisplayName;
    private String transporterName;
    private String transportCompany;
    private String vehicleNumber;
    private String driverName;
    private String driverPhone;
    
    // Shipment tracking
    private String lrNumber;
    private String awbNumber;
    private String trackingUrl;
    
    // Return Challan
    private String returnChallanNumber;
    private LocalDate returnChallanDate;
    
    // POD
    private String podNumber;
    private LocalDate podDate;
    private Boolean podReceived;
    private String podDocumentPath;
    
    // Status
    private ReturnDispatch.DispatchStatus status;
    private String statusDisplayName;
    
    // Quantities
    private Integer totalItems;
    private BigDecimal totalWeight;
    private BigDecimal totalVolume;
    private String weightUnit;
    private String volumeUnit;
    
    // Audit
    private Long createdBy;
    private String createdByName;
    private LocalDateTime createdAt;
    private Long updatedBy;
    private String updatedByName;
    private LocalDateTime updatedAt;
    
    // Items
    private List<DispatchItemResponseDTO> items;
}