package com.warehouse.wms.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class ItemImageGroupDTO {
    private Long lineId;
    private String itemCode;
    private String itemName;
    private String qualityStatus;
    private Integer acceptedQuantity;
    private Integer rejectedQuantity;
    private Integer defectiveQuantity;
    private List<ImageWithUrlDTO> images;
}