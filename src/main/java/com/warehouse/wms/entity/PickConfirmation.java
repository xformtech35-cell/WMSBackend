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
@Table(name = "wms_pick_confirmation", indexes = {
    @Index(name = "idx_pc_number", columnList = "confirmation_number"),
    @Index(name = "idx_pc_so_number", columnList = "so_number"),
    @Index(name = "idx_pc_pt_number", columnList = "pick_task_number")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PickConfirmation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "confirmation_number", unique = true, nullable = false, length = 50)
    private String confirmationNumber;

    @Column(name = "pick_task_number", nullable = false, length = 50)
    private String pickTaskNumber;

    @Column(name = "pick_list_number", length = 50)
    private String pickListNumber;

    @Column(name = "so_number", length = 50)
    private String soNumber;

    @Column(name = "item_code", nullable = false, length = 50)
    private String itemCode;

    @Column(name = "item_name", length = 200)
    private String itemName;

    @Column(name = "required_quantity")
    private Integer requiredQuantity = 0;

    @Column(name = "picked_quantity", nullable = false)
    private Integer pickedQuantity = 0;

    @Column(name = "short_quantity")
    private Integer shortQuantity = 0;

    @Column(name = "barcode", length = 100)
    private String barcode;

    @Column(name = "confirmed_by", length = 100)
    private String confirmedBy;

    @Column(name = "confirmed_date")
    private LocalDateTime confirmedDate;

    @Column(name = "status", nullable = false, length = 30)
    private String status = "CONFIRMED"; // CONFIRMED, PARTIAL, REJECTED

    @Column(columnDefinition = "TEXT")
    private String remarks;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}