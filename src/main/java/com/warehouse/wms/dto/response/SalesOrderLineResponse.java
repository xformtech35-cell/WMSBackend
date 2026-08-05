// ====== FILE: src/main/java/com/warehouse/wms/dto/response/SalesOrderLineResponse.java ======
package com.warehouse.wms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesOrderLineResponse {
    private Long id;
    private Long skuId;
    private String skuCode;
    private String skuName;
    private Integer quantity;
    private BigDecimal unitPrice;   // ✅ Changed to BigDecimal
    private BigDecimal totalPrice;  // ✅ Changed to BigDecimal
    private LocalDateTime createdAt;
}