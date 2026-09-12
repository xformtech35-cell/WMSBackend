package com.warehouse.wms.mapper;

import com.warehouse.wms.dto.request.UomRequest;
import com.warehouse.wms.dto.response.UomResponse;
import com.warehouse.wms.entity.Uom;
import org.springframework.stereotype.Component;

@Component
public class UomMapper {

    public Uom toEntity(UomRequest request) {
        return Uom.builder()
                .code(request.getCode())
                .name(request.getName())
                .description(request.getDescription())
                .active(request.getActive() != null ? request.getActive() : true)
                .build();
    }

    public UomResponse toResponse(Uom entity) {
        return UomResponse.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .name(entity.getName())
                .description(entity.getDescription())
                .active(entity.getActive())
                .build();
    }

    public void updateEntity(Uom entity, UomRequest request) {
        entity.setCode(request.getCode());
        entity.setName(request.getName());
        entity.setDescription(request.getDescription());
        if (request.getActive() != null) {
            entity.setActive(request.getActive());
        }
    }
}