// ====== FILE: src/main/java/com/warehouse/wms/dto/response/SkuResponse.java ======
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
public class SkuResponse {
    private Long id;
    private String skuCode;
    private String name;
    private String description;
    private BigDecimal price;  // ✅ Changed to BigDecimal
    private String uom;
    private Boolean isPerishable;
    private Boolean isActive;
    private String remarks;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}