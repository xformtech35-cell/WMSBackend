package com.warehouse.wms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "wms_return_dispatch")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReturnDispatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "dispatch_number", nullable = false, unique = true)
    private String dispatchNumber;

    @Column(name = "dispatch_date", nullable = false)
    private LocalDate dispatchDate;

    @Column(name = "dispatch_time")
    private LocalTime dispatchTime;

    @Column(name = "transport_mode")
    @Enumerated(EnumType.STRING)
    private TransportMode transportMode;

    @Column(name = "transporter_name")
    private String transporterName;

    @Column(name = "transport_company")
    private String transportCompany;

    @Column(name = "vehicle_number")
    private String vehicleNumber;

    @Column(name = "driver_name")
    private String driverName;

    @Column(name = "driver_phone")
    private String driverPhone;

    @Column(name = "lr_number")
    private String lrNumber;

    @Column(name = "awb_number")
    private String awbNumber;

    @Column(name = "tracking_url")
    private String trackingUrl;

    @Column(name = "return_challan_number")
    private String returnChallanNumber;

    @Column(name = "return_challan_date")
    private LocalDate returnChallanDate;

    @Column(name = "pod_number")
    private String podNumber;

    @Column(name = "pod_date")
    private LocalDate podDate;

    @Column(name = "pod_received")
    private Boolean podReceived = false;

    @Column(name = "pod_document_path")
    private String podDocumentPath;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private DispatchStatus status = DispatchStatus.CREATED;

    @Column(name = "total_items")
    private Integer totalItems;

    @Column(name = "total_weight")
    private BigDecimal totalWeight;

    @Column(name = "total_volume")
    private BigDecimal totalVolume;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vro_id")
    private VendorReturnOrder returnOrder;

    @OneToMany(mappedBy = "dispatch", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<ReturnDispatchItem> items = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private Long createdBy;

    public enum TransportMode {
        ROAD("Road"),
        AIR("Air"),
        SEA("Sea"),
        RAIL("Rail");

        private final String displayName;

        TransportMode(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum DispatchStatus {
        CREATED("Created"),
        IN_TRANSIT("In Transit"),
        DELIVERED("Delivered"),
        RECEIVED("Received");

        private final String displayName;

        DispatchStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public void addItem(ReturnDispatchItem item) {
        items.add(item);
        item.setDispatch(this);
    }
}