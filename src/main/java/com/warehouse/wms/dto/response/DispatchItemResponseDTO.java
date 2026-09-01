package com.warehouse.wms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DispatchItemResponseDTO {
    private Long id;
    private Long vroLineId;
    private String itemCode;
    private String itemName;
    private Integer dispatchedQuantity;
    private Integer packedQuantity;
    private String packagingType;
    private Integer packageCount;
    private BigDecimal packageWeight;
    private String packageWeightUnit;
    private LocalDateTime createdAt;
}