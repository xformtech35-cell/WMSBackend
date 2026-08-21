package com.warehouse.wms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerResponse {

    private Long id;
    private String customerCode;
    private String customerName;
    private String companyName;
    private String email;
    private String phone;
    private String mobile;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String pincode;
    private String country;
    private String gstNumber;
    private String panNumber;
    private String taxId;
    private String contactPerson;
    private String contactDesignation;
    private String contactPhone;
    private String contactEmail;
    private String paymentTerms;
    private Double creditLimit;
    private Integer creditDays;
    private Double discountPercentage;
    private String status;
    private String customerType;
    private String industryType;
    private String website;
    private String notes;
    private String preferredWarehouse;
    private String defaultShippingMethod;
    private LocalDateTime lastOrderDate;
    private Integer totalOrders;
    private Double totalSpent;
    private Double averageOrderValue;
    private Integer loyaltyPoints;
    private String loyaltyTier;
    private Boolean isVerified;
    private Boolean isBlacklisted;
    private String blacklistReason;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}