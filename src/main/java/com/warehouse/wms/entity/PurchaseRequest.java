package com.warehouse.wms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "purchase_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseRequest {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String prNumber;
    
    @Column(name = "pr_date", nullable = false)
    private LocalDate prDate;
    
    @Column(name = "requested_by", nullable = false)
    private String requestedBy;
    
    @Column(nullable = false)
    private String department;
    
    @Column(nullable = false)
    private String warehouse;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Priority priority;
    
    @Column(name = "required_date", nullable = false)
    private LocalDate requiredDate;
    
    @Column(columnDefinition = "TEXT")
    private String remarks;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestStatus status = RequestStatus.DRAFT;
    
    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;
    
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;
    
    // Make sure this is the only field mapped to 'rejection_reason'
    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;
    
    @OneToMany(mappedBy = "purchaseRequest", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<PurchaseRequestItem> items = new ArrayList<>();
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Helper methods
    public void addItem(PurchaseRequestItem item) {
        items.add(item);
        item.setPurchaseRequest(this);
    }
    
    public void removeItem(PurchaseRequestItem item) {
        items.remove(item);
        item.setPurchaseRequest(null);
    }
}