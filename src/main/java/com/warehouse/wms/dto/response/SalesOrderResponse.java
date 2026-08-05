// ====== FILE: src/main/java/com/warehouse/wms/dto/response/SalesOrderResponse.java ======
package com.warehouse.wms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesOrderResponse {
    private Long id;
    private String soNumber;
    private String customerName;
    private LocalDate orderDate;
    private String status;
    private BigDecimal totalAmount;  // ✅ Changed to BigDecimal
    private String createdBy;
    private String remarks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<SalesOrderLineResponse> lines;
}