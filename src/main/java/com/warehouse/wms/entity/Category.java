package com.warehouse.wms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "category_master")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code; // e.g., "ELEC", "FMCG"

    @Column(nullable = false)
    private String name; // e.g., "Electronics", "Fast Moving Consumer Goods"

    private String description;

    @Column(nullable = false)
    private Boolean active = true;
}