package com.warehouse.wms.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CategoryRequest {
    @NotBlank(message = "Code is required")
    private String code;

    @NotBlank(message = "Name is required")
    private String name;

    private String description;

    // Optional: To set a parent category
    private Long parentId;

    private Boolean active;
}