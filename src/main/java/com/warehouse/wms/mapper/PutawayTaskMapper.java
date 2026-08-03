// ====== FILE: src/main/java/com/warehouse/wms/mapper/PutawayTaskMapper.java ======
package com.warehouse.wms.mapper;

import com.warehouse.wms.dto.request.PutawayInitiateRequest;
import com.warehouse.wms.dto.response.PutawayTaskResponse;
import com.warehouse.wms.entity.PutawayTask;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PutawayTaskMapper {

    PutawayTask toEntity(PutawayInitiateRequest request);

    PutawayTaskResponse toResponse(PutawayTask putawayTask);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(@MappingTarget PutawayTask putawayTask, PutawayInitiateRequest request);
}