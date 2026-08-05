// ====== FILE: src/main/java/com/warehouse/wms/mapper/TrolleyMapper.java ======
package com.warehouse.wms.mapper;

import com.warehouse.wms.dto.request.TrolleyRequest;
import com.warehouse.wms.dto.response.TrolleyResponse;
import com.warehouse.wms.entity.Trolley;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TrolleyMapper {

    @Mapping(target = "compartments", ignore = true)  // ✅ Ignore compartments
    @Mapping(target = "utilizationPercentage", expression = "java(trolley.getCapacity() != null && trolley.getCapacity() > 0 ? (trolley.getCurrentLoad() != null ? trolley.getCurrentLoad().doubleValue() : 0) / trolley.getCapacity().doubleValue() * 100 : 0)")
    TrolleyResponse toResponse(Trolley trolley);

    @Mapping(target = "compartments", ignore = true)
    @Mapping(target = "currentLoad", ignore = true)
    @Mapping(target = "lastUsedAt", ignore = true)
    Trolley toEntity(TrolleyRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "compartments", ignore = true)
    @Mapping(target = "currentLoad", ignore = true)
    @Mapping(target = "lastUsedAt", ignore = true)
    void updateEntity(@MappingTarget Trolley trolley, TrolleyRequest request);
}