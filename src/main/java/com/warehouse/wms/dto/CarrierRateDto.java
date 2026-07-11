package com.warehouse.wms.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class CarrierRateDto {
    private String carrierName;
    private String serviceType;
    private BigDecimal rate;
    private Integer estimatedDays;
}
