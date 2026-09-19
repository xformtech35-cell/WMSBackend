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
@Table(name = "wms_pick_confirmation", indexes = {
    @Index(name = "idx_pc_number",  columnList = "confirmation_number"),
    @Index(name = "idx_pc_so",      columnList = "so_number"),
    @Index(name = "idx_pc_pt",      columnList = "pick_task_number"),
    @Index(name = "idx_pc_status",  columnList = "status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PickConfirmation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Unique per confirmation (one header = one confirmation). */
    @Column(name = "confirmation_number", unique = true, nullable = false, length = 50)
    private String confirmationNumber;

    @Column(name = "pick_task_number", nullable = false, length = 50)
    private String pickTaskNumber;

    @Column(name = "pick_list_number", length = 50)
    private String pickListNumber;

    @Column(name = "so_number", length = 50)
    private String soNumber;

    @Column(name = "warehouse_id", length = 50)
    private String warehouseId;

    @Column(name = "confirmed_by", length = 100)
    private String confirmedBy;

    @Column(name = "confirmed_date")
    private LocalDateTime confirmedDate;

    /** CONFIRMED, PARTIAL, REJECTED, CONFIRMED_TO_PACK */
    @Column(name = "status", nullable = false, length = 30)
    private String status = "CONFIRMED";

    @Column(name = "total_items")
    private Integer totalItems = 0;

    @Column(name = "total_picked_quantity")
    private Integer totalPickedQuantity = 0;

    @Column(name = "total_short_quantity")
    private Integer totalShortQuantity = 0;

    @Column(columnDefinition = "TEXT")
    private String remarks;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ---------------- One-to-Many with items ----------------
    @OneToMany(mappedBy = "pickConfirmation",
               cascade = CascadeType.ALL,
               orphanRemoval = true,
               fetch = FetchType.LAZY)
    @Builder.Default
    private List<PickConfirmationItem> items = new ArrayList<>();

    public void addItem(PickConfirmationItem item) {
        items.add(item);
        item.setPickConfirmation(this);
    }

    public void removeItem(PickConfirmationItem item) {
        items.remove(item);
        item.setPickConfirmation(null);
    }
}