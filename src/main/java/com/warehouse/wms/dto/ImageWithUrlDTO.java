package com.warehouse.wms.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class ImageWithUrlDTO {
    private Long id;
    private String fileName;
    private String filePath;
    private String fullUrl;
    private String thumbnailUrl;
    private String downloadUrl;
    private Long fileSize;
    private String fileType;
    private LocalDateTime uploadedAt;
}