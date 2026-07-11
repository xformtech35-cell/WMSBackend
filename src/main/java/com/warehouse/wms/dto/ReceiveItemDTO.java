package com.warehouse.wms.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReceiveItemDTO {

    @NotNull(message = "Received quantity is required")
    @Min(value = 0, message = "Received quantity must be greater than or equal to 0")
    private Integer receivedQuantity;

    @Min(value = 0, message = "Defective quantity must be greater than or equal to 0")
    private Integer defectiveQuantity = 0;

    @NotBlank(message = "Quality status is required")
    private String qualityStatus; // GOOD, PARTIAL, REJECTED

    private String remarks;

    private List<String> images;
}
