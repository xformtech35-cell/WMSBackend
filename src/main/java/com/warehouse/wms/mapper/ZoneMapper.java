package com.warehouse.wms.mapper;

import com.warehouse.wms.dto.request.ZoneRequest;
import com.warehouse.wms.dto.response.ZoneResponse;
import com.warehouse.wms.entity.Zone;
import org.mapstruct.*;
import org.springframework.context.annotation.Lazy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {WarehouseMapper.class})
public interface ZoneMapper {

    @Mapping(target = "aisles", ignore = true)  // Ignore the collection to avoid recursion
    ZoneResponse toResponse(Zone zone);

    @Mapping(target = "warehouse", ignore = true)
    Zone toEntity(ZoneRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(@MappingTarget Zone zone, ZoneRequest request);
}