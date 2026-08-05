// ====== FILE: src/main/java/com/warehouse/wms/dto/SalesOrderRequest.java ======
package com.warehouse.wms.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesOrderRequest {

    @NotBlank(message = "SO Number is required")
    private String soNumber;

    @NotBlank(message = "Customer name is required")
    private String customerName;

    private LocalDate orderDate;

    private String status; // PENDING, PROCESSING, COMPLETED, CANCELLED

    private String createdBy;

    private String remarks;

    @Valid
    @NotEmpty(message = "At least one line item is required")
    private List<SalesOrderLineRequest> lines;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SalesOrderLineRequest {

        @NotNull(message = "SKU ID is required")
        private Long skuId;

        private String skuCode;

        @NotNull(message = "Quantity is required")
        private Integer quantity;
    }
}