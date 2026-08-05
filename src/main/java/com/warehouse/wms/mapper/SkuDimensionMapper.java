// ====== FILE: src/main/java/com/warehouse/wms/mapper/SkuDimensionMapper.java ======
package com.warehouse.wms.mapper;

import com.warehouse.wms.dto.request.SkuDimensionRequest;
import com.warehouse.wms.dto.response.SkuDimensionResponse;
import com.warehouse.wms.entity.SkuDimension;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SkuDimensionMapper {

    SkuDimensionResponse toResponse(SkuDimension dimension);

    @Mapping(target = "sku", ignore = true)
    SkuDimension toEntity(SkuDimensionRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "sku", ignore = true)
    void updateEntity(@MappingTarget SkuDimension dimension, SkuDimensionRequest request);
}