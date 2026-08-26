package com.warehouse.wms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "wms_delivery_challan", indexes = {
    @Index(name = "idx_dc_challan_number", columnList = "challan_number"),
    @Index(name = "idx_dc_so_number", columnList = "so_number"),
    @Index(name = "idx_dc_status", columnList = "status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryChallan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "challan_number", unique = true, nullable = false, length = 50)
    private String challanNumber;

    private String  soNumber;


    @Column(name = "shipment_number", length = 50)
    private String shipmentNumber;

    @Column(name = "transporter", length = 100)
    private String transporter;

    @Column(name = "vehicle_number", length = 20)
    private String vehicleNumber;

    @Column(name = "driver_name", length = 100)
    private String driverName;

    @Column(name = "driver_phone", length = 20)
    private String driverPhone;

    @Column(name = "total_packages")
    private Integer totalPackages = 0;

    @Column(name = "total_quantity")
    private Integer totalQuantity = 0;

    @Column(name = "total_weight")
    private Double totalWeight = 0.0;

    @Column(name = "total_volume")
    private Double totalVolume = 0.0;

    @Column(name = "status", nullable = false, length = 30)
    private String status = "CREATED";

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

    @OneToMany(mappedBy = "deliveryChallan", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DeliveryChallanPackage> packages = new ArrayList<>();
}