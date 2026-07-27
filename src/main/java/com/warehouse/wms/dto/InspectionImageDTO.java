package com.warehouse.wms.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class InspectionImageDTO {
    private Long id;
    private Long inboundLineId;
    private Long inboundId;
    private String fileName;
    private String filePath;
    private Long fileSize;
    private String fileType;
    private String fileExtension;
    private Long uploadedBy;
    private String remarks;
    private LocalDateTime uploadedAt;
}