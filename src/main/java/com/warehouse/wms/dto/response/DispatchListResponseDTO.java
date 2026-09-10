package com.warehouse.wms.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import com.warehouse.wms.entity.ReturnDispatch;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DispatchListResponseDTO {

    private Long id;
    private String dispatchNumber;
    private LocalDate dispatchDate;
    private LocalTime dispatchTime;

    // Order linkage
    private Long returnOrderId;
    private String returnOrderNumber;   // vroNumber
    private String supplierName;
    private String supplierCode;

    // Transport
    private ReturnDispatch.TransportMode transportMode;
    private String transportModeDisplayName;
    private String transporterName;
    private String transportCompany;
    private String vehicleNumber;
    private String driverName;
    private String driverPhone;

    // Docs
    private String lrNumber;
    private String awbNumber;
    private String trackingUrl;
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

    // Totals
    private Integer totalItems;
    private Integer totalQuantity;
    private BigDecimal totalWeight;
    private BigDecimal totalVolume;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Optional — only populated on detail endpoint, not list
    private List<DispatchItemResponseDTO> items;
}