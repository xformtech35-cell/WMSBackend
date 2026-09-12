package com.warehouse.wms.service;

import com.warehouse.wms.dto.request.UomRequest;
import com.warehouse.wms.dto.response.UomResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface UomService {
    UomResponse create(UomRequest request);
    UomResponse update(Long id, UomRequest request);
    UomResponse getById(Long id);
    List<UomResponse> getAll();
    Page<UomResponse> searchAndFilter(String keyword, Boolean active, int page, int size, String sortBy);
    void delete(Long id);
}