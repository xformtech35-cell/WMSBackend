package com.warehouse.wms.dto.reports;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertResponse {
    private String type;
    private String severity;
    private String message;
    private String action;
    private LocalDateTime timestamp;
    private Boolean isRead;
}