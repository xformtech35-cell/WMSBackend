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
import com.warehouse.wms.dto.BinResponse;
import com.warehouse.wms.entity.Bin;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {RackCompartmentMapper.class, BinMapper.class})  // ✅ Add BinMapper
public interface RackMapper {

    @Mapping(target = "aisle", ignore = true)  // Ignore to avoid circular dependency
    @Mapping(target = "bins", source = "bins")  // ✅ Let MapStruct use BinMapper
    @Mapping(target = "compartments", ignore = true)  // Ignore to avoid circular dependency
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