package com.warehouse.wms.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerRequest {

    @NotBlank(message = "Customer code is required")
    @Size(max = 50, message = "Customer code must be less than 50 characters")
    private String customerCode;

    @NotBlank(message = "Customer name is required")
    @Size(max = 200, message = "Customer name must be less than 200 characters")
    private String customerName;

    @Size(max = 200, message = "Company name must be less than 200 characters")
    private String companyName;

    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email must be less than 100 characters")
    private String email;

    @Pattern(regexp = "^[0-9\\-\\+\\s]*$", message = "Invalid phone number format")
    @Size(max = 20, message = "Phone must be less than 20 characters")
    private String phone;

    @Pattern(regexp = "^[0-9\\-\\+\\s]*$", message = "Invalid mobile number format")
    @Size(max = 20, message = "Mobile must be less than 20 characters")
    private String mobile;

    @Size(max = 255, message = "Address line 1 must be less than 255 characters")
    private String addressLine1;

    @Size(max = 255, message = "Address line 2 must be less than 255 characters")
    private String addressLine2;

    @Size(max = 100, message = "City must be less than 100 characters")
    private String city;

    @Size(max = 100, message = "State must be less than 100 characters")
    private String state;

    @Size(max = 10, message = "Pincode must be less than 10 characters")
    private String pincode;

    @Size(max = 100, message = "Country must be less than 100 characters")
    private String country;

    @Size(max = 50, message = "GST number must be less than 50 characters")
    private String gstNumber;

    @Size(max = 20, message = "PAN number must be less than 20 characters")
    private String panNumber;

    @Size(max = 50, message = "Tax ID must be less than 50 characters")
    private String taxId;

    @Size(max = 100, message = "Contact person must be less than 100 characters")
    private String contactPerson;

    @Size(max = 100, message = "Contact designation must be less than 100 characters")
    private String contactDesignation;

    @Pattern(regexp = "^[0-9\\-\\+\\s]*$", message = "Invalid contact phone format")
    @Size(max = 20, message = "Contact phone must be less than 20 characters")
    private String contactPhone;

    @Email(message = "Invalid contact email format")
    @Size(max = 100, message = "Contact email must be less than 100 characters")
    private String contactEmail;

    @Size(max = 50, message = "Payment terms must be less than 50 characters")
    private String paymentTerms;

    private Double creditLimit;
    private Integer creditDays;
    private Double discountPercentage;

    @Size(max = 30, message = "Customer type must be less than 30 characters")
    private String customerType; // RETAIL, WHOLESALE, DISTRIBUTOR, CORPORATE

    @Size(max = 50, message = "Industry type must be less than 50 characters")
    private String industryType;

    @Size(max = 200, message = "Website must be less than 200 characters")
    private String website;

    private String notes;

    @Size(max = 20, message = "Preferred warehouse must be less than 20 characters")
    private String preferredWarehouse;

    @Size(max = 50, message = "Default shipping method must be less than 50 characters")
    private String defaultShippingMethod;

    private String createdBy;
}