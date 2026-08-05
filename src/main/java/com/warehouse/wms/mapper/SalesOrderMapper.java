// ====== FILE: src/main/java/com/warehouse/wms/mapper/SalesOrderMapper.java ======
package com.warehouse.wms.mapper;

import java.math.BigDecimal;
import java.util.List;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;

import com.warehouse.wms.dto.SalesOrderRequest;
import com.warehouse.wms.dto.response.SalesOrderLineResponse;
import com.warehouse.wms.dto.response.SalesOrderResponse;
import com.warehouse.wms.entity.SalesOrder;
import com.warehouse.wms.entity.SalesOrderLine;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)  // ✅ Remove all uses
public abstract class SalesOrderMapper {

    @Autowired
    private SkuMapper skuMapper;

    @Mapping(target = "lines", source = "lines")
    @Mapping(target = "totalAmount", expression = "java(calculateTotalAmount(salesOrder))")
    public abstract SalesOrderResponse toResponse(SalesOrder salesOrder);

    @Mapping(target = "lines", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    public abstract SalesOrder toEntity(SalesOrderRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "lines", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    public abstract void updateEntity(@MappingTarget SalesOrder salesOrder, SalesOrderRequest request);

    @Mapping(target = "skuId", source = "sku.id")
    @Mapping(target = "skuCode", source = "sku.skuCode")
    @Mapping(target = "skuName", source = "sku.name")
    @Mapping(target = "unitPrice", source = "sku.price")
    @Mapping(target = "totalPrice", expression = "java(calculateLineTotalPrice(line))")
    public abstract SalesOrderLineResponse toLineResponse(SalesOrderLine line);

    @Mapping(target = "salesOrder", ignore = true)
    @Mapping(target = "sku", ignore = true)
    @Mapping(target = "totalPrice", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    public abstract SalesOrderLine toLineEntity(SalesOrderRequest.SalesOrderLineRequest request);

    public abstract List<SalesOrderLineResponse> toLineResponses(List<SalesOrderLine> lines);
    
    public abstract List<SalesOrderLine> toLineEntities(List<SalesOrderRequest.SalesOrderLineRequest> requests);

    protected BigDecimal calculateTotalAmount(SalesOrder salesOrder) {
        if (salesOrder == null || salesOrder.getLines() == null || salesOrder.getLines().isEmpty()) {
            return BigDecimal.ZERO;
        }
        return salesOrder.getLines().stream()
                .map(line -> {
                    BigDecimal quantity = BigDecimal.valueOf(line.getQuantity() != null ? line.getQuantity() : 0);
                    BigDecimal price = line.getSku() != null && line.getSku().getPrice() != null ? 
                                       line.getSku().getPrice() : BigDecimal.ZERO;
                    return quantity.multiply(price);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    protected BigDecimal calculateLineTotalPrice(SalesOrderLine line) {
        if (line == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal quantity = BigDecimal.valueOf(line.getQuantity() != null ? line.getQuantity() : 0);
        BigDecimal price = line.getSku() != null && line.getSku().getPrice() != null ? 
                           line.getSku().getPrice() : BigDecimal.ZERO;
        return quantity.multiply(price);
    }
}