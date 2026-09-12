package com.warehouse.wms.service;

import com.warehouse.wms.dto.request.DepartmentRequest;
import com.warehouse.wms.dto.response.DepartmentResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface DepartmentService {
    DepartmentResponse create(DepartmentRequest request);
    DepartmentResponse update(Long id, DepartmentRequest request);
    DepartmentResponse getById(Long id);
    DepartmentResponse getByCode(String code);
    List<DepartmentResponse> getAll();
    Page<DepartmentResponse> searchAndFilter(String keyword, String location, Boolean isActive,
                                             int page, int size, String sortBy);
    void delete(Long id);
    DepartmentResponse toggleActive(Long id);
}