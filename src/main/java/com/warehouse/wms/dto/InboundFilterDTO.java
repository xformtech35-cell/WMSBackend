package com.warehouse.wms.dto;

import com.warehouse.wms.entity.InboundStage;
import com.warehouse.wms.entity.InboundStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InboundFilterDTO {
    private InboundStatus status;
    private InboundStage stage;
    private String poNumber;
    private String supplierName;
    private String searchTerm;
}