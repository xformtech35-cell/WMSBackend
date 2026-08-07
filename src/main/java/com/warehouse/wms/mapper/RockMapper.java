// ====== FILE: src/main/java/com/warehouse/wms/mapper/RockMapper.java ======
package com.warehouse.wms.mapper;

import com.warehouse.wms.dto.request.RockRequest;
import com.warehouse.wms.dto.response.RockResponse;
import com.warehouse.wms.entity.Rock;
import com.warehouse.wms.entity.Warehouse;
import org.mapstruct.*;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {WarehouseMapper.class})
public interface RockMapper {

    @Mapping(target = "warehouse", source = "warehouse")
    @Mapping(target = "volumeCm3", expression = "java(rock.getVolumeCm3())")
    @Mapping(target = "totalWeight", expression = "java(rock.getTotalWeight())")
    RockResponse toResponse(Rock rock);

    @Mapping(target = "warehouse", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Rock toEntity(RockRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "warehouse", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(@MappingTarget Rock rock, RockRequest request);

    default List<RockResponse> toResponseList(List<Rock> rocks) {
        if (rocks == null) {
            return null;
        }
        return rocks.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}