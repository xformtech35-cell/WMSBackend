package com.warehouse.wms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "wms_customer", indexes = {
    @Index(name = "idx_customer_code", columnList = "customer_code"),
    @Index(name = "idx_customer_email", columnList = "email"),
    @Index(name = "idx_customer_phone", columnList = "phone"),
    @Index(name = "idx_customer_status", columnList = "status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_code", unique = true, nullable = false, length = 50)
    private String customerCode;

    @Column(name = "customer_name", nullable = false, length = 200)
    private String customerName;

    @Column(name = "company_name", length = 200)
    private String companyName;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "mobile", length = 20)
    private String mobile;

    @Column(name = "address_line1", length = 255)
    private String addressLine1;

    @Column(name = "address_line2", length = 255)
    private String addressLine2;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "state", length = 100)
    private String state;

    @Column(name = "pincode", length = 10)
    private String pincode;

    @Column(name = "country", length = 100)
    private String country;

    @Column(name = "gst_number", length = 50)
    private String gstNumber;

    @Column(name = "pan_number", length = 20)
    private String panNumber;

    @Column(name = "tax_id", length = 50)
    private String taxId;

    @Column(name = "contact_person", length = 100)
    private String contactPerson;

    @Column(name = "contact_designation", length = 100)
    private String contactDesignation;

    @Column(name = "contact_phone", length = 20)
    private String contactPhone;

    @Column(name = "contact_email", length = 100)
    private String contactEmail;

    @Column(name = "payment_terms", length = 50)
    private String paymentTerms;

    @Column(name = "credit_limit")
    private Double creditLimit = 0.0;

    @Column(name = "credit_days")
    private Integer creditDays = 0;

    @Column(name = "discount_percentage")
    private Double discountPercentage = 0.0;

    @Column(name = "status", nullable = false, length = 30)
    private String status = "ACTIVE"; // ACTIVE, INACTIVE, BLOCKED, SUSPENDED

    @Column(name = "customer_type", length = 30)
    private String customerType; // RETAIL, WHOLESALE, DISTRIBUTOR, CORPORATE

    @Column(name = "industry_type", length = 50)
    private String industryType;

    @Column(name = "website", length = 200)
    private String website;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "preferred_warehouse", length = 20)
    private String preferredWarehouse;

    @Column(name = "default_shipping_method", length = 50)
    private String defaultShippingMethod;

    @Column(name = "last_order_date")
    private LocalDateTime lastOrderDate;

    @Column(name = "total_orders")
    private Integer totalOrders = 0;

    @Column(name = "total_spent")
    private Double totalSpent = 0.0;

    @Column(name = "average_order_value")
    private Double averageOrderValue = 0.0;

    @Column(name = "loyalty_points")
    private Integer loyaltyPoints = 0;

    @Column(name = "loyalty_tier", length = 30)
    private String loyaltyTier; // BRONZE, SILVER, GOLD, PLATINUM

    @Column(name = "is_verified")
    private Boolean isVerified = false;

    @Column(name = "is_blacklisted")
    private Boolean isBlacklisted = false;

    @Column(name = "blacklist_reason")
    private String blacklistReason;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}