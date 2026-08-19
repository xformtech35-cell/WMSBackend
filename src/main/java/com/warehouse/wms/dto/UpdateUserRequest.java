package com.warehouse.wms.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserRequest {
    @Size(min = 3, max = 64)
    private String username;

    @Size(min = 8, max = 128)
    private String password;

    private String role;

    // Profile fields
    @Size(max = 100)
    private String fullName;

    @Size(max = 20)
    private String mobileNumber;

    @Size(max = 100)
    private String designation;

    @Size(max = 50)
    private String employeeId;

    @Email
    @Size(max = 100)
    private String email;

    @Size(max = 100)
    private String department;

    @Size(max = 100)
    private String location;

    @Size(max = 500)
    private String bio;

    private Boolean isActive;
}