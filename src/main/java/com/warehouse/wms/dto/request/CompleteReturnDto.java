package com.warehouse.wms.dto.request;

import lombok.Data;

@Data
public class CompleteReturnDto {
    private String remarks;
    private Double refundAmount;
}