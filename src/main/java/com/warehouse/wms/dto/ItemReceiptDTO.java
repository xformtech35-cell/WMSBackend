package com.warehouse.wms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemReceiptDTO {
    private Long id;
    private LocalDateTime receivedDate;
    private Integer receivedQuantity;
    private Integer defectiveQuantity;
    private String qualityStatus;
    private String remarks;
    private List<String> images;
}