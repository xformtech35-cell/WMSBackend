package com.warehouse.wms.dto;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QualityInspectionItemDTO {
    private Long lineId;
    private String itemCode;
    private String itemName;
    private Integer receivedQuantity;
    private Integer acceptedQuantity;
    private Integer rejectedQuantity;
    private String qualityStatus; // GOOD, PARTIAL, REJECTED
    private String reason;
    private String remarks;
    
    private List<MultipartFile> imageFiles;

}