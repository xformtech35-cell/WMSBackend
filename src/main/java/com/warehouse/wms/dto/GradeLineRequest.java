package com.warehouse.wms.dto;

import com.warehouse.wms.entity.ReturnLine.ConditionGrade;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GradeLineRequest {
    @NotNull(message = "conditionGrade is required")
    private ConditionGrade conditionGrade;

    private String inspectionNotes;

    private String batchNumber;
}
