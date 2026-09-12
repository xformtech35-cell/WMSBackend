package com.warehouse.wms.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CategoryResponse {
    private Long id;
    private String code;
    private String name;
    private String description;
    private Long parentId;       // To avoid infinite recursion in JSON
    private String parentName;   // Useful for UI display
    private Boolean active;
}