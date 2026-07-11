package com.warehouse.wms.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "wms_shipment_record")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ShipmentRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "sales_order_id", nullable = false)
    private SalesOrder salesOrder;

    // Expose orderId without loading the full SalesOrder graph
    @Column(name = "sales_order_id", insertable = false, updatable = false)
    private Long orderId;

    @Column(name = "awb_number", nullable = false)
    private String awbNumber;

    @Column(name = "courier_name", nullable = false)
    private String courierName;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
