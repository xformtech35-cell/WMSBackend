package com.warehouse.wms.service;

import com.warehouse.wms.dto.request.BatchMasterRequest;
import com.warehouse.wms.dto.response.BatchMasterResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BatchMasterService {
    
    BatchMasterResponse create(BatchMasterRequest request);
    
    BatchMasterResponse update(Long id, BatchMasterRequest request);
    
    BatchMasterResponse getById(Long id);
    
    void delete(Long id);
    
    // Get All with Filter and Search (Pagination included)
    Page<BatchMasterResponse> getAll(String search, Boolean isActive, Pageable pageable);
}