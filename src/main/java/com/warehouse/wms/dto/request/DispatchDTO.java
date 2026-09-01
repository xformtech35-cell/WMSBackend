package com.warehouse.wms.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import com.warehouse.wms.entity.ReturnDispatch;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DispatchDTO {
    
    private Long id;
    private String dispatchNumber;
    
    @NotNull(message = "Dispatch date is required")
    private LocalDate dispatchDate;
    
    private LocalTime dispatchTime;
    private Long returnOrderId;
    
    private ReturnDispatch.TransportMode transportMode;
    private String transporterName;
    private String transportCompany;
    private String vehicleNumber;
    private String driverName;
    private String driverPhone;
    
    private String lrNumber;
    private String awbNumber;
    private String trackingUrl;
    
    private String returnChallanNumber;
    private LocalDate returnChallanDate;
    
    private String podNumber;
    private LocalDate podDate;
    private Boolean podReceived;
    
    private BigDecimal totalWeight;
    private BigDecimal totalVolume;
    
    private List<DispatchItemDTO> items;
}

// DispatchItemDTO.java
