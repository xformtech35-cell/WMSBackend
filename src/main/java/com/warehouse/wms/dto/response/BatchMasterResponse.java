package com.warehouse.wms.dto.response;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchMasterResponse {
    private Long id;
    private String batchCode;
    private String batchName;
    private String description;
    private LocalDate manufacturingDate;
    private LocalDate expiryDate;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}