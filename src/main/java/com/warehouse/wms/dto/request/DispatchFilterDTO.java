package com.warehouse.wms.dto.request;

import java.time.LocalDate;

import com.warehouse.wms.entity.ReturnDispatch;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DispatchFilterDTO {

    // Free-text search (dispatchNumber, vroNumber, supplierName, lrNumber, awbNumber, vehicleNumber, driverName)
    private String searchTerm;

    // Dispatch identity
    private String dispatchNumber;

    // Order linkage
    private Long returnOrderId;
    private String vroNumber;
    private String supplierName;
    private String supplierCode;

    // Transport
    private ReturnDispatch.TransportMode transportMode;
    private String transporterName;
    private String transportCompany;
    private String vehicleNumber;
    private String driverName;
    private String driverPhone;

    // Documents
    private String lrNumber;
    private String awbNumber;
    private String returnChallanNumber;

    // Status / POD
    private ReturnDispatch.DispatchStatus status;
    private Boolean podReceived;

    // Item-level
    private String itemCode;
    private String itemName;

    // Date filters (dispatchDate)
    private LocalDate dispatchFromDate;
    private LocalDate dispatchToDate;

    // Date filters (podDate)
    private LocalDate podFromDate;
    private LocalDate podToDate;

    // Weight / volume
    private Double minWeight;
    private Double maxWeight;
    private Double minVolume;
    private Double maxVolume;
}