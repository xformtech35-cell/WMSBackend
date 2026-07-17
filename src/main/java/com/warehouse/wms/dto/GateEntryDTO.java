package com.warehouse.wms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GateEntryDTO {
    private String driverName;
    private String driverContact;
    private String driverId;
    private String trackNumber;
    private String gateNumber;
    private Long approvedBy;
    private LocalDateTime gateEntryDateTime;
    private String remarks;
}