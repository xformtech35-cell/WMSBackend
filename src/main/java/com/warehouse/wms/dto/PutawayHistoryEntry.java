package com.warehouse.wms.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PutawayHistoryEntry {
    private Long id;
    private String serialNo;
    private String fromState;
    private String toState;
    private String binBarcode;
    private String suggestedBinBarcode;
    private String action;
    private Long userId;
    private String userName;
    private String userRole;
    private LocalDateTime createdAt;
}
