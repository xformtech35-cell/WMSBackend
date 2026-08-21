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
@Table(name = "wms_package_info", indexes = {
    @Index(name = "idx_pkg_number", columnList = "package_number"),
    @Index(name = "idx_pkg_so_number", columnList = "so_number"),
    @Index(name = "idx_pkg_barcode", columnList = "package_barcode")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PackageInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "package_number", unique = true, nullable = false, length = 50)
    private String packageNumber;

    @Column(name = "package_barcode", unique = true, nullable = false, length = 100)
    private String packageBarcode;

    @Column(name = "so_number", nullable = false, length = 50)
    private String soNumber;

    @Column(name = "pick_list_number", length = 50)
    private String pickListNumber;

    @Column(name = "item_code", nullable = false, length = 50)
    private String itemCode;

    @Column(name = "item_name", length = 200)
    private String itemName;

    @Column(name = "packed_quantity", nullable = false)
    private Integer packedQuantity = 0;

    @Column(name = "package_type", length = 50)
    private String packageType; // CARTON, BOX, PALLET, BAG

    @Column(name = "weight")
    private Double weight = 0.0;

    @Column(name = "length")
    private Double length = 0.0;

    @Column(name = "width")
    private Double width = 0.0;

    @Column(name = "height")
    private Double height = 0.0;

    @Column(name = "volume")
    private Double volume = 0.0;

    @Column(name = "packed_by", length = 100)
    private String packedBy;

    @Column(name = "packed_date")
    private LocalDateTime packedDate;

    @Column(name = "status", nullable = false, length = 30)
    private String status = "PACKED"; // PACKED, LABELED, DISPATCHED, DELIVERED

    @Column(columnDefinition = "TEXT")
    private String remarks;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}