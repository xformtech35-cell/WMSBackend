package com.warehouse.wms.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class PickTask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "sales_order_line_id", nullable = false)
    private SalesOrderLine salesOrderLine;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "inventory_id", nullable = false)
    private Inventory inventory;

    @Column(name = "bin_barcode")
    private String binBarcode;

    @Column(name = "sku_code")
    private String skuCode;

    @Column(nullable = false)
    private Integer quantityToPick;

    private String status;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "trolley_id")
    private Trolley trolley;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "rack_compartment_id")
    private RackCompartment rackCompartment;
}
