package com.warehouse.wms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "gst_master")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Gst {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String code; // e.g., "GST5", "GST12"

    private String name; // e.g., "GST 5%", "GST 12%"

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal rate; // e.g., 5.00, 12.00, 18.00

    private String description; // e.g., "Applicable for packaged food items"

    @Column(nullable = false)
    private Boolean active = true;
}