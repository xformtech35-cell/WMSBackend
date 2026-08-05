// ====== FILE: src/main/java/com/warehouse/wms/mapper/SkuMapper.java ======
package com.warehouse.wms.mapper;

import com.warehouse.wms.dto.response.SkuResponse;
import com.warehouse.wms.entity.Sku;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SkuMapper {

    SkuResponse toResponse(Sku sku);
}