// ====== FILE: src/main/java/com/warehouse/wms/mapper/WarehouseMapper.java ======
package com.warehouse.wms.mapper;

import com.warehouse.wms.dto.request.WarehouseRequest;
import com.warehouse.wms.dto.response.WarehouseResponse;
import com.warehouse.wms.entity.Warehouse;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {ZoneMapper.class})
public interface WarehouseMapper {

    @Mapping(target = "zones", source = "zones", ignore = true)
    WarehouseResponse toResponse(Warehouse warehouse);

    Warehouse toEntity(WarehouseRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(@MappingTarget Warehouse warehouse, WarehouseRequest request);
}