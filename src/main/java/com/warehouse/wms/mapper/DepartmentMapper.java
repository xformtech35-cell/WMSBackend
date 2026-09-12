package com.warehouse.wms.mapper;

import com.warehouse.wms.dto.request.DepartmentRequest;
import com.warehouse.wms.dto.response.DepartmentResponse;
import com.warehouse.wms.entity.Department;
import org.springframework.stereotype.Component;

@Component
public class DepartmentMapper {

    public Department toEntity(DepartmentRequest request) {
        return Department.builder()
                .code(request.getCode())
                .name(request.getName())
                .description(request.getDescription())
                .headOfDepartment(request.getHeadOfDepartment())
                .location(request.getLocation())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();
    }

    public DepartmentResponse toResponse(Department entity) {
        return DepartmentResponse.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .name(entity.getName())
                .description(entity.getDescription())
                .headOfDepartment(entity.getHeadOfDepartment())
                .location(entity.getLocation())
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy())
                .build();
    }

    // Null-safe update — only overwrites non-null fields
    public void updateEntity(Department entity, DepartmentRequest request) {
        if (request.getCode() != null)             entity.setCode(request.getCode());
        if (request.getName() != null)             entity.setName(request.getName());
        if (request.getDescription() != null)      entity.setDescription(request.getDescription());
        if (request.getHeadOfDepartment() != null) entity.setHeadOfDepartment(request.getHeadOfDepartment());
        if (request.getLocation() != null)         entity.setLocation(request.getLocation());
        if (request.getIsActive() != null)         entity.setIsActive(request.getIsActive());
    }
}