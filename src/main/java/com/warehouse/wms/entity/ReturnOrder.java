package com.warehouse.wms.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "wms_return_order")
public class ReturnOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "original_order_id", nullable = false)
    private SalesOrder originalOrder;

    @Column(name = "customer_ref")
    private String customerRef;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReturnStatus status;

    @OneToMany(mappedBy = "returnOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReturnLine> lines = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public enum ReturnStatus {
        RETURN_REQUESTED,
        AWAITING_PICKUP,
        IN_TRANSIT,
        RECEIVED,
        INSPECTING,
        RESTOCKED,
        SCRAPPED,
        REJECTED,
        REFUND_TRIGGERED,
        CLOSED
    }
}
