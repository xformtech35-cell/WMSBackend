package com.warehouse.wms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PackageResponse {

    private String packageNumber;
    private String packageBarcode;
    private String soNumber;
    private String pickListNumber;
    private String itemCode;
    private String itemName;
    private Integer packedQuantity;
    private String packageType;
    private Double weight;
    private Double length;
    private Double width;
    private Double height;
    private Double volume;
    private String packedBy;
    private LocalDateTime packedDate;
    private String status;
    private String remarks;
    private LocalDateTime createdAt;
}