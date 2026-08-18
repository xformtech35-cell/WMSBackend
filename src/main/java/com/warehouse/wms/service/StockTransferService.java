// ====== FILE: src/main/java/com/warehouse/wms/service/StockTransferService.java ======
package com.warehouse.wms.service;

import com.warehouse.wms.dto.request.StockTransferRequest;
import com.warehouse.wms.dto.response.StockTransferResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface StockTransferService {
    
    StockTransferResponse transferStock(StockTransferRequest request);
    
    StockTransferResponse getTransferByNumber(String transferNumber);
    
    Page<StockTransferResponse> getAllTransfers(Pageable pageable);
    
    Page<StockTransferResponse> getTransfersWithFilter(
            String itemCode,
            String sourceLocation,
            String targetLocation,
            String batchNumber,
            String grnNumber,
            String inventoryNumber,
            String search,
            LocalDateTime startDate,
            LocalDateTime endDate,
            String transferStatus,
            Pageable pageable
        );
    
    List<StockTransferResponse> getTransfersByItemCode(String itemCode);
    
    List<StockTransferResponse> getTransfersBySourceLocation(String locationPath);
    
    List<StockTransferResponse> getTransfersByTargetLocation(String locationPath);
    
    StockTransferResponse cancelTransfer(String transferNumber);
    
    void validateTransfer(StockTransferRequest request);
}