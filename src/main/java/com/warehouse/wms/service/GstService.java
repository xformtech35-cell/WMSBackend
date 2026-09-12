package com.warehouse.wms.service;

import com.warehouse.wms.dto.request.GstRequest;
import com.warehouse.wms.dto.response.GstResponse;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.util.List;

public interface GstService {
    GstResponse create(GstRequest request);
    GstResponse update(Long id, GstRequest request);
    GstResponse getById(Long id);
    List<GstResponse> getAll();
    Page<GstResponse> searchAndFilter(String keyword, BigDecimal rate, Boolean active, int page, int size, String sortBy);
    void delete(Long id);
}