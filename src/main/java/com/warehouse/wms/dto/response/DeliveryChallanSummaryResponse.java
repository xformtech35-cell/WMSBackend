package com.warehouse.wms.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryChallanSummaryResponse {
    private Long totalChallans;
    private Long created;
    private Long printed;
    private Long dispatched;
    private Long delivered;
    private Long cancelled;
    private Integer totalQuantity;
    private Double totalWeight;
    private List<RecentDeliveryChallanResponse> recentChallans;
    private List<DeliveryChallanStatusCountResponse> statusCounts;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class RecentDeliveryChallanResponse {
    private String challanNumber;
    private String soNumber;
    private String customerName;
    private String status;
    private Integer totalQuantity;
    private LocalDateTime dispatchDate;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class DeliveryChallanStatusCountResponse {
    private String status;
    private Long count;
}