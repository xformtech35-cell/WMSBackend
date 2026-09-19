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
@Table(name = "wms_pick_task", indexes = {
    @Index(name = "idx_pt_number", columnList = "pick_task_number"),
    @Index(name = "idx_pt_pl_number", columnList = "pick_list_number"),
    @Index(name = "idx_pt_status", columnList = "status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PickTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pick_task_number", unique = true, nullable = false, length = 50)
    private String pickTaskNumber;

    @Column(name = "pick_list_number", nullable = false, length = 50)
    private String pickListNumber;

    @Column(name = "so_number", length = 50)
    private String soNumber;

    @Column(name = "warehouse_id", length = 50)
    private String warehouseId;

    @Column(name = "assigned_to", length = 100)
    private String assignedTo;

    @Column(name = "priority", length = 20)
    private String priority;

    @Column(name = "total_items")
    private Integer totalItems = 0;

    @Column(name = "total_quantity")
    private Integer totalQuantity = 0;

    @Column(name = "status", nullable = false, length = 30)
    private String status = "PENDING"; // PENDING, PICKING, PARTIAL, COMPLETED, CANCELLED

    @Column(columnDefinition = "TEXT")
    private String remarks;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @Column(name = "completed_date")
    private LocalDateTime completedDate;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // One-to-Many relationship with items
    @OneToMany(mappedBy = "pickTask", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<PickTaskItem> items = new ArrayList<>();

    // Helper methods
    public void addItem(PickTaskItem item) {
        items.add(item);
        item.setPickTask(this);
    }

    public void removeItem(PickTaskItem item) {
        items.remove(item);
        item.setPickTask(null);
    }
}