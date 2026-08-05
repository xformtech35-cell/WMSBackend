// ====== FILE: src/main/java/com/warehouse/wms/mapper/RackMapper.java ======
package com.warehouse.wms.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import com.warehouse.wms.dto.request.RackRequest;
import com.warehouse.wms.dto.response.RackResponse;
import com.warehouse.wms.entity.Rack;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {BinMapper.class})  // ✅ Remove RackCompartmentMapper
public interface RackMapper {

    @Mapping(target = "aisle", ignore = true)
    @Mapping(target = "bins", source = "bins")
    @Mapping(target = "compartments", ignore = true)  // ✅ Ignore compartments
    RackResponse toResponse(Rack rack);

    @Mapping(target = "aisle", ignore = true)
    @Mapping(target = "bins", ignore = true)
    @Mapping(target = "compartments", ignore = true)
    @Mapping(target = "totalShelves", ignore = true)
    Rack toEntity(RackRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "aisle", ignore = true)
    @Mapping(target = "bins", ignore = true)
    @Mapping(target = "compartments", ignore = true)
    @Mapping(target = "totalShelves", ignore = true)
    void updateEntity(@MappingTarget Rack rack, RackRequest request);
}