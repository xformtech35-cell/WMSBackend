package com.warehouse.wms.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchMasterRequest {

    @NotBlank(message = "Batch Code is required")
    private String batchCode;

    @NotBlank(message = "Batch Name is required")
    private String batchName;

    private String description;

    @NotNull(message = "Manufacturing Date is required")
    private LocalDate manufacturingDate;

    @NotNull(message = "Expiry Date is required")
    private LocalDate expiryDate;

    private Boolean isActive;
}