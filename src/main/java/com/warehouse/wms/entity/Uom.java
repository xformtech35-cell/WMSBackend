package com.warehouse.wms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "uom")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Uom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code; // e.g., "KG", "PCS"

    @Column(nullable = false)
    private String name; // e.g., "Kilogram", "Pieces"

    private String description;

    @Column(nullable = false)
    private Boolean active = true;
}