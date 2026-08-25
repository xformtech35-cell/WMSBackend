// PickConfirmationSummaryResponse.java
package com.warehouse.wms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PickConfirmationSummaryResponse {
    private Long totalConfirmations;
    private Long confirmed;
    private Long partial;
    private Long rejected;
    private Integer totalPickedQuantity;
    private Integer totalShortQuantity;
    private List<RecentPickConfirmationResponse> recentConfirmations;
    private List<PickConfirmationStatusCountResponse> statusCounts;
}

// RecentPickConfirmationResponse.java
