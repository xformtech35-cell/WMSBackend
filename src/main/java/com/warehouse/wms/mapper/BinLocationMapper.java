// ====== FILE: src/main/java/com/warehouse/wms/mapper/BinLocationMapper.java ======
package com.warehouse.wms.mapper;

import com.warehouse.wms.dto.request.BinLocationRequest;
import com.warehouse.wms.dto.response.BinLocationResponse;
import com.warehouse.wms.entity.BinLocation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BinLocationMapper {

    @Mapping(target = "fullLocation", expression = "java(binLocation.getFullLocation())")
    BinLocationResponse toResponse(BinLocation binLocation);

    BinLocation toEntity(BinLocationRequest request);

    void updateEntity(@MappingTarget BinLocation binLocation, BinLocationRequest request);
}