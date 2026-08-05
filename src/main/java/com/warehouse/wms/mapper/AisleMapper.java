// ====== FILE: src/main/java/com/warehouse/wms/mapper/AisleMapper.java ======
package com.warehouse.wms.mapper;

import com.warehouse.wms.dto.request.AisleRequest;
import com.warehouse.wms.dto.response.AisleResponse;
import com.warehouse.wms.entity.Aisle;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {ZoneMapper.class})  // ✅ Remove RackMapper
public interface AisleMapper {

    @Mapping(target = "zone", source = "zone")
    @Mapping(target = "racks", ignore = true)  // ✅ Ignore racks to avoid cycle
    AisleResponse toResponse(Aisle aisle);

    @Mapping(target = "zone", ignore = true)
    @Mapping(target = "racks", ignore = true)
    @Mapping(target = "totalRacks", ignore = true)
    Aisle toEntity(AisleRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "zone", ignore = true)
    @Mapping(target = "racks", ignore = true)
    @Mapping(target = "totalRacks", ignore = true)
    void updateEntity(@MappingTarget Aisle aisle, AisleRequest request);
}