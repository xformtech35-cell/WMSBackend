package com.warehouse.wms.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class InboundImageDTO {
    private Long inboundId;
    private String inboundNumber;
    private String supplierName;
    private String qualityStatus;
    private List<ItemImageGroupDTO> items;
}