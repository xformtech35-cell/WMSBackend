package com.warehouse.wms.mapper;

import com.warehouse.wms.dto.request.GstRequest;
import com.warehouse.wms.dto.response.GstResponse;
import com.warehouse.wms.entity.Gst;
import org.springframework.stereotype.Component;

@Component
public class GstMapper {

    public Gst toEntity(GstRequest request) {
        return Gst.builder()
                .code(request.getCode())
                .name(request.getName())
                .rate(request.getRate())
                .description(request.getDescription())
                .active(request.getActive() != null ? request.getActive() : true)
                .build();
    }

    public GstResponse toResponse(Gst entity) {
        return GstResponse.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .name(entity.getName())
                .rate(entity.getRate())
                .description(entity.getDescription())
                .active(entity.getActive())
                .build();
    }

    public void updateEntity(Gst entity, GstRequest request) {
        entity.setCode(request.getCode());
        entity.setName(request.getName());
        entity.setRate(request.getRate());
        entity.setDescription(request.getDescription());
        if (request.getActive() != null) {
            entity.setActive(request.getActive());
        }
    }
}