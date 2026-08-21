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
@Table(name = "wms_pick_list_item", indexes = {
    @Index(name = "idx_pli_pl_number", columnList = "pick_list_number"),
    @Index(name = "idx_pli_item_code", columnList = "item_code")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PickListItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pick_list_number", nullable = false, length = 50)
    private String pickListNumber;

    @Column(name = "so_number", length = 50)
    private String soNumber;

    @Column(name = "item_code", nullable = false, length = 50)
    private String itemCode;

    @Column(name = "item_name", length = 200)
    private String itemName;

    @Column(name = "uom", length = 10)
    private String uom;

    @Column(name = "required_quantity", nullable = false)
    private Integer requiredQuantity = 0;

    @Column(name = "picked_quantity")
    private Integer pickedQuantity = 0;

    @Column(name = "short_quantity")
    private Integer shortQuantity = 0;

    @Column(name = "source_location", length = 200)
    private String sourceLocation;

    @Column(name = "batch_number", length = 50)
    private String batchNumber;

    @Column(name = "status", length = 30)
    private String status = "PENDING"; // PENDING, PICKING, COMPLETED, SHORT

    @Column(name = "priority", length = 20)
    private String priority;

    @Column(columnDefinition = "TEXT")
    private String remarks;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pick_list_id")
    private PickList pickList;
}