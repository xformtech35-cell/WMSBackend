// ====== FILE: src/main/java/com/warehouse/wms/entity/PutawayTask.java ======
package com.warehouse.wms.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.warehouse.wms.constant.PutawayStage;
import com.warehouse.wms.constant.PutawayStatus;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "wms_putaway_tasks", indexes = {
    @Index(name = "idx_task_number", columnList = "task_number"),
    @Index(name = "idx_grn_number", columnList = "grn_number"),
    @Index(name = "idx_assigned_to", columnList = "assigned_to"),
    @Index(name = "idx_status", columnList = "status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PutawayTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "task_number", nullable = false, unique = true, length = 50)
    private String taskNumber;

    @Column(name = "grn_number", nullable = false, length = 50)
    private String grnNumber;

    @Column(name = "inbound_id")
    private Long inboundId;

    @Column(name = "assigned_to", length = 100)
    private String assignedTo;

    @Column(name = "assigned_by", length = 100)
    private String assignedBy;

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PutawayStatus status = PutawayStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "stage", nullable = false)
    private PutawayStage stage = PutawayStage.INITIATED;

    @Column(name = "total_quantity")
    private Integer totalQuantity = 0;

    @Column(name = "putaway_quantity")
    private Integer putawayQuantity = 0;

    @Column(name = "pending_quantity")
    private Integer pendingQuantity = 0;

    @Column(name = "warehouse_id", length = 20)
    private String warehouseId;

    @Column(name = "receiving_area", length = 50)
    private String receivingArea;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "picked_at")
    private LocalDateTime pickedAt;

    @Column(name = "transported_at")
    private LocalDateTime transportedAt;

    @Column(name = "scanned_at")
    private LocalDateTime scannedAt;

    @Column(name = "placed_at")
    private LocalDateTime placedAt;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "confirmation_number", length = 50)
    private String confirmationNumber;

    @Column(name = "confirmed_by", length = 100)
    private String confirmedBy;

    @Column(columnDefinition = "TEXT")
    private String remarks;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    // ✅ FIX: Initialize with @Builder.Default to work with Lombok builder
    @Builder.Default
    @OneToMany(mappedBy = "putawayTask", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<PutawayLine> lines = new ArrayList<>();

    @OneToOne(mappedBy = "putawayTask", cascade = CascadeType.ALL)
    private PutawayConfirmation confirmation;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    // ✅ Helper methods with null safety
    public void addLine(PutawayLine line) {
        if (this.lines == null) {
            this.lines = new ArrayList<>();
        }
        this.lines.add(line);
        line.setPutawayTask(this);
    }

    public void removeLine(PutawayLine line) {
        if (this.lines != null) {
            this.lines.remove(line);
            line.setPutawayTask(null);
        }
    }
}