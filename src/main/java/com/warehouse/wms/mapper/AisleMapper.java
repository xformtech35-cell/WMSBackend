// ====== FILE: src/main/java/com/warehouse/wms/mapper/AisleMapper.java ======
package com.warehouse.wms.mapper;

import com.warehouse.wms.dto.request.AisleRequest;
import com.warehouse.wms.dto.response.AisleResponse;
import com.warehouse.wms.dto.response.RackResponse;
import com.warehouse.wms.entity.Aisle;
import com.warehouse.wms.entity.Rack;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class AisleMapper {

    @Autowired
    @Lazy
    protected ZoneMapper zoneMapper;

    @Autowired
    @Lazy
    protected RackMapper rackMapper;

    @Mapping(target = "zone", expression = "java(zoneMapper.toResponse(aisle.getZone()))")
    @Mapping(target = "racks", expression = "java(mapRacks(aisle.getRacks()))")
    public abstract AisleResponse toResponse(Aisle aisle);

    @Mapping(target = "zone", ignore = true)
    @Mapping(target = "racks", ignore = true)
    @Mapping(target = "totalRacks", ignore = true)
    public abstract Aisle toEntity(AisleRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "zone", ignore = true)
    @Mapping(target = "racks", ignore = true)
    @Mapping(target = "totalRacks", ignore = true)
    public abstract void updateEntity(@MappingTarget Aisle aisle, AisleRequest request);

    // ✅ Helper method to map racks
    protected List<RackResponse> mapRacks(List<Rack> racks) {
        if (racks == null || racks.isEmpty()) {
            return null;
        }
        return racks.stream()
                .map(rack -> rackMapper.toResponse(rack))
                .collect(Collectors.toList());
    }

    // ✅ Method for list mapping
    public List<AisleResponse> toResponseList(List<Aisle> aisles) {
        if (aisles == null) {
            return null;
        }
        return aisles.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}