package com.warehouse.wms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BinStatusResponse {

    private String status;
    private Long count;

    // Convenience constructor used by JPQL: matches (BinStatus, Long)
    public BinStatusResponse(Object status, Long count) {
        this.status = status != null ? status.toString() : null;
        this.count = count;
    }
}