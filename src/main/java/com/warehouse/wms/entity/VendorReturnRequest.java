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
@Table(name = "wms_vendor_return_request")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VendorReturnRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "return_request_number", nullable = false, unique = true)
    private String returnRequestNumber;

    @Column(name = "request_date", nullable = false)
    private LocalDate requestDate;

    @Column(name = "po_number")
    private String poNumber;

    @Column(name = "grn_number")
    private String grnNumber;
    
    private String remarks;


    @Column(name = "invoice_number")
    private String invoiceNumber;

    @Column(name = "supplier_name")
    private String supplierName;

    @Column(name = "supplier_code")
    private String supplierCode;

    @Column(name = "return_type")
    @Enumerated(EnumType.STRING)
    private ReturnType returnType;

    @Column(name = "return_reason", columnDefinition = "TEXT")
    private String returnReason;

    @Column(name = "priority")
    @Enumerated(EnumType.STRING)
    private Priority priority = Priority.MEDIUM;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private RequestStatus status = RequestStatus.DRAFT;

    @Column(name = "approved_by")
    private Long approvedBy;

    @Column(name = "approved_date")
    private LocalDateTime approvedDate;

    @Column(name = "rejected_by")
    private Long rejectedBy;

    @Column(name = "rejected_date")
    private LocalDateTime rejectedDate;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    @OneToMany(mappedBy = "returnRequest", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<VendorReturnRequestLine> lines = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    public enum ReturnType {
        DAMAGED("Damaged"),
        DEFECTIVE("Defective"),
        EXCESS("Excess Quantity"),
        WRONG_ITEM("Wrong Item"),
        QUALITY_ISSUE("Quality Issue"),
        EXPIRED("Expired"),
        OTHER("Other");

        private final String displayName;

        ReturnType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum Priority {
        LOW("Low"),
        MEDIUM("Medium"),
        HIGH("High"),
        URGENT("Urgent");

        private final String displayName;

        Priority(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum RequestStatus {
        DRAFT("Draft"),
        SUBMITTED("Submitted"),
        PENDING_APPROVAL("Pending Approval"),
        APPROVED("Approved"),
        REJECTED("Rejected"),
        CANCELLED("Cancelled");

        private final String displayName;

        RequestStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public void addLine(VendorReturnRequestLine line) {
        lines.add(line);
        line.setReturnRequest(this);
    }

    public void removeLine(VendorReturnRequestLine line) {
        lines.remove(line);
        line.setReturnRequest(null);
    }

    public boolean isEditable() {
        return status == RequestStatus.DRAFT || status == RequestStatus.SUBMITTED;
    }
}