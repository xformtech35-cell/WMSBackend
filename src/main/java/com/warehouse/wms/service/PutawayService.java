// ====== FILE: src/main/java/com/warehouse/wms/service/PutawayService.java ======
package com.warehouse.wms.service;

import com.warehouse.wms.dto.request.PutawayInitiateRequest;
import com.warehouse.wms.dto.request.PutawayExecuteRequest;
import com.warehouse.wms.dto.request.PutawayConfirmRequest;
import com.warehouse.wms.dto.response.PutawayTaskResponse;
import com.warehouse.wms.dto.response.LocationSuggestionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PutawayService {

    PutawayTaskResponse initiatePutaway(PutawayInitiateRequest request);

    PutawayTaskResponse executePutawayStage(PutawayExecuteRequest request);

    PutawayTaskResponse confirmPutaway(PutawayConfirmRequest request);

    LocationSuggestionResponse suggestLocation(String itemCode, Integer quantity, String warehouseId);

    PutawayTaskResponse getPutawayTaskByNumber(String taskNumber);

    PutawayTaskResponse getPutawayTaskByGrnNumber(String grnNumber);

    List<PutawayTaskResponse> getPutawayTasksByStatus(String status);

    List<PutawayTaskResponse> getPutawayTasksByAssignedTo(String assignedTo);

    Page<PutawayTaskResponse> getAllPutawayTasks(Pageable pageable);

    void cancelPutawayTask(String taskNumber, String reason);

    void updateInventoryAfterPutaway(String confirmationNumber);
}