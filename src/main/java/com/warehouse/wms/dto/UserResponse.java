package com.warehouse.wms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String username;
    private String role;
    private List<String> permissions;
    
    // Profile fields
    private String fullName;
    private String mobileNumber;
    private String designation;
    private String employeeId;
    private String email;
    private String department;
    private String location;
    private String bio;
    private String profilePhotoUrl;
    private Boolean isActive;
    private LocalDateTime joiningDate;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}