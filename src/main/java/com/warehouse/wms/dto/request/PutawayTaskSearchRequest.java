// ====== FILE: src/main/java/com/warehouse/wms/dto/PutawayTaskSearchRequest.java ======
package com.warehouse.wms.dto.request;

import com.warehouse.wms.constant.PutawayStage;
import com.warehouse.wms.constant.PutawayStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PutawayTaskSearchRequest {
    private String searchTerm;          // Search across taskNumber, grnNumber, assignedTo
    private String taskNumber;
    private String grnNumber;
    private String assignedTo;
    private PutawayStatus status;
    private PutawayStage stage;
    private String warehouseId;
    private String receivingArea;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Boolean isCompleted;
}