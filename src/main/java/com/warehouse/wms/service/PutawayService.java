// ====== FILE: src/main/java/com/warehouse/wms/service/PutawayService.java ======
package com.warehouse.wms.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.warehouse.wms.constant.PutawayStage;
import com.warehouse.wms.constant.PutawayStatus;
import com.warehouse.wms.dto.request.PutawayConfirmRequest;
import com.warehouse.wms.dto.request.PutawayExecuteRequest;
import com.warehouse.wms.dto.request.PutawayInitiateRequest;
import com.warehouse.wms.dto.response.LocationSuggestionResponse;
import com.warehouse.wms.dto.response.PutawayTaskResponse;

public interface PutawayService {

    PutawayTaskResponse initiatePutaway(PutawayInitiateRequest request);

    PutawayTaskResponse executePutawayStage(PutawayExecuteRequest request);

    PutawayTaskResponse confirmPutaway(PutawayConfirmRequest request);

    LocationSuggestionResponse suggestLocation(String itemCode, Integer quantity, String warehouseId);

    PutawayTaskResponse getPutawayTaskByNumber(String taskNumber);

    PutawayTaskResponse getPutawayTaskByGrnNumber(String grnNumber);

    List<PutawayTaskResponse> getPutawayTasksByStatus(String status);

    List<PutawayTaskResponse> getPutawayTasksByAssignedTo(String assignedTo);

//    Page<PutawayTaskResponse> getAllPutawayTasks(Pageable pageable);
    
 // ====== FILE: src/main/java/com/warehouse/wms/service/PutawayService.java ======
    Page<PutawayTaskResponse> getAllPutawayTasks(
            String search,
            PutawayStatus status,
            PutawayStage stage,
            String grnNumber,
            String assignedTo,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable);

    void cancelPutawayTask(String taskNumber, String reason);

    void updateInventoryAfterPutaway(String confirmationNumber);
}