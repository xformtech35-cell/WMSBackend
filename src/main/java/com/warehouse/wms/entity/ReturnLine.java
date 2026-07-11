package com.warehouse.wms.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

@Data
@Entity
@Table(name = "wms_return_line")
public class ReturnLine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ToString.Exclude
    @ManyToOne(optional = false)
    @JoinColumn(name = "return_order_id", nullable = false)
    private ReturnOrder returnOrder;

    @ManyToOne(optional = false)
    @JoinColumn(name = "sku_id", nullable = false)
    private Sku sku;

    @Column(name = "ordered_qty", nullable = false)
    private Integer orderedQty;

    @Column(name = "returned_qty", nullable = false)
    private Integer returnedQty;

    @Enumerated(EnumType.STRING)
    @Column(name = "condition_grade")
    private ConditionGrade conditionGrade; // RESELLABLE, DAMAGED, SCRAP

    @Column(name = "inspection_notes", length = 1000)
    private String inspectionNotes;

    @ManyToOne
    @JoinColumn(name = "restocked_bin_id")
    private Bin restockedBin;

    @Column(name = "batch_number")
    private String batchNumber;

    public enum ConditionGrade {
        RESELLABLE, DAMAGED, SCRAP
    }
}
