// ====== FILE: src/main/java/com/warehouse/wms/mapper/LevelMapper.java ======
package com.warehouse.wms.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import com.warehouse.wms.dto.request.LevelRequest;
import com.warehouse.wms.dto.response.LevelResponse;
import com.warehouse.wms.entity.Level;

// ✅ Remove BinMapper from uses - only keep RackMapper
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {RackMapper.class})  // ✅ Remove BinMapper
public interface LevelMapper {

    @Mapping(target = "rack", source = "rack")
    @Mapping(target = "bins", ignore = true)  // ✅ Ignore bins to avoid circular reference
    LevelResponse toResponse(Level level);

    @Mapping(target = "rack", ignore = true)
    @Mapping(target = "bins", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Level toEntity(LevelRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "rack", ignore = true)
    @Mapping(target = "bins", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(@MappingTarget Level level, LevelRequest request);

    // Custom mapping for list
    default List<LevelResponse> toResponseList(List<Level> levels) {
        if (levels == null) {
            return null;
        }
        return levels.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}