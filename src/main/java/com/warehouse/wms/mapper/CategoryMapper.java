package com.warehouse.wms.mapper;

import com.warehouse.wms.dto.request.CategoryRequest;
import com.warehouse.wms.dto.response.CategoryResponse;
import com.warehouse.wms.entity.Category;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public Category toEntity(CategoryRequest request, Category parent) {
        return Category.builder()
                .code(request.getCode())
                .name(request.getName())
                .description(request.getDescription())
                .parent(parent)
                .active(request.getActive() != null ? request.getActive() : true)
                .build();
    }

    public CategoryResponse toResponse(Category entity) {
        return CategoryResponse.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .name(entity.getName())
                .description(entity.getDescription())
                .parentId(entity.getParent() != null ? entity.getParent().getId() : null)
                .parentName(entity.getParent() != null ? entity.getParent().getName() : null)
                .active(entity.getActive())
                .build();
    }

    public void updateEntity(Category entity, CategoryRequest request, Category parent) {
        entity.setCode(request.getCode());
        entity.setName(request.getName());
        entity.setDescription(request.getDescription());
        entity.setParent(parent);
        if (request.getActive() != null) {
            entity.setActive(request.getActive());
        }
    }
}