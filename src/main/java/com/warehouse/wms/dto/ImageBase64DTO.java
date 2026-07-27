package com.warehouse.wms.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class ImageBase64DTO {
    private Long id;
    private String fileName;
    private String base64Data;
    private Long fileSize;
    private String fileType;
    private LocalDateTime uploadedAt;
}