// ====== FILE: src/main/java/com/warehouse/wms/mapper/RackCompartmentMapper.java ======
package com.warehouse.wms.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import com.warehouse.wms.dto.request.RackCompartmentRequest;
import com.warehouse.wms.dto.response.RackCompartmentResponse;
import com.warehouse.wms.entity.RackCompartment;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)  // ✅ Remove all uses
public interface RackCompartmentMapper {

    @Mapping(target = "rack", ignore = true)  // ✅ Ignore rack
    @Mapping(target = "trolley", ignore = true)  // ✅ Ignore trolley
    @Mapping(target = "salesOrder", ignore = true)  // ✅ Ignore salesOrder
    @Mapping(target = "availableCapacity", expression = "java(rackCompartment.getAvailableCapacity())")
    RackCompartmentResponse toResponse(RackCompartment rackCompartment);

    @Mapping(target = "rack", ignore = true)
    @Mapping(target = "trolley", ignore = true)
    @Mapping(target = "salesOrder", ignore = true)
    @Mapping(target = "availableCapacity", ignore = true)
    @Mapping(target = "usedCapacity", ignore = true)
    RackCompartment toEntity(RackCompartmentRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "rack", ignore = true)
    @Mapping(target = "trolley", ignore = true)
    @Mapping(target = "salesOrder", ignore = true)
    @Mapping(target = "availableCapacity", ignore = true)
    @Mapping(target = "usedCapacity", ignore = true)
    void updateEntity(@MappingTarget RackCompartment rackCompartment, RackCompartmentRequest request);
}