package com.warehouse.wms.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DepartmentRequest {

    @NotBlank(message = "Department code is required")
    @Size(max = 50)
    private String code;

    @NotBlank(message = "Department name is required")
    @Size(max = 200)
    private String name;

    private String description;

    private String headOfDepartment;

    private String location;

    private Boolean isActive;
}