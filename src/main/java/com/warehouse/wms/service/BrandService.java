package com.warehouse.wms.service;

import com.warehouse.wms.dto.request.BrandRequest;
import com.warehouse.wms.dto.response.BrandResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface BrandService {
    BrandResponse create(BrandRequest request);
    BrandResponse update(Long id, BrandRequest request);
    BrandResponse getById(Long id);
    List<BrandResponse> getAll();
    Page<BrandResponse> searchAndFilter(String keyword, Boolean active, int page, int size, String sortBy);
    void delete(Long id);
}