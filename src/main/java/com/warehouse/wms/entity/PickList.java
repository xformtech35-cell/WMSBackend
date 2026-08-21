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
@Table(name = "wms_pick_list", indexes = {
    @Index(name = "idx_pl_number", columnList = "pick_list_number"),
    @Index(name = "idx_pl_so_number", columnList = "so_number"),
    @Index(name = "idx_pl_status", columnList = "status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PickList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pick_list_number", unique = true, nullable = false, length = 50)
    private String pickListNumber;

    @Column(name = "so_number", nullable = false, length = 50)
    private String soNumber;

    @Column(name = "warehouse_id", nullable = false, length = 20)
    private String warehouseId;

    @Column(name = "priority", length = 20)
    private String priority;

    @Column(name = "total_items")
    private Integer totalItems = 0;

    @Column(name = "total_quantity")
    private Integer totalQuantity = 0;

    @Column(name = "status", nullable = false, length = 30)
    private String status = "RELEASED"; // RELEASED, PICKING, COMPLETED, CANCELLED

    @Column(name = "created_by", length = 100)
    private String createdBy;
    
    private String updatedBy;


    @Column(name = "assigned_to", length = 100)
    private String assignedTo;

    @Column(name = "completed_date")
    private LocalDateTime completedDate;

    @Column(columnDefinition = "TEXT")
    private String remarks;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "pickList", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PickListItem> items = new ArrayList<>();
}