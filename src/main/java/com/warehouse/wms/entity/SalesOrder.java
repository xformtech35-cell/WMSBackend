package com.warehouse.wms.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "wms_sales_order", indexes = {
    @Index(name = "idx_so_number", columnList = "so_number"),
    @Index(name = "idx_so_customer", columnList = "customer_code"),
    @Index(name = "idx_so_warehouse", columnList = "warehouse_id"),
    @Index(name = "idx_so_status", columnList = "status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "so_number", unique = true, nullable = false, length = 50)
    private String soNumber;

//    @Column(name = "so_date", nullable = false)
//    private LocalDateTime soDate;
    
    @Column(name = "order_date")
    private LocalDateTime orderDate;

    @Column(name = "customer_code", nullable = false, length = 50)
    private String customerCode;

    @Column(name = "customer_name", nullable = false, length = 200)
    private String customerName;

    @Column(name = "warehouse_id", nullable = false, length = 20)
    private String warehouseId;

    @Column(name = "delivery_date")
    private LocalDateTime deliveryDate;

    @Column(name = "priority", length = 20)
    private String priority; // HIGH, MEDIUM, LOW

    @Column(name = "delivery_address", columnDefinition = "TEXT")
    private String deliveryAddress;

    @Column(name = "total_quantity")
    private Integer totalQuantity = 0;

    @Column(name = "total_weight")
    private Double totalWeight = 0.0;

    @Column(name = "shipping_method", length = 50)
    private String shippingMethod;

    @Column(name = "status", nullable = false, length = 30)
    private String status = "CONFIRMED"; // CONFIRMED, PROCESSING, PICKING, PACKING, DISPATCHED, DELIVERED, CANCELLED

    @Column(columnDefinition = "TEXT")
    private String remarks;

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

    @OneToMany(mappedBy = "salesOrder", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<SalesOrderItem> items = new ArrayList<>();
}