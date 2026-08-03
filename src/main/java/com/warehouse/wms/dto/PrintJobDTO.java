package com.warehouse.wms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrintJobDTO {
    private String printerName;
    private List<String> qrIds;
    private Integer copies;
    private String labelFormat;
    private String paperSize;
}