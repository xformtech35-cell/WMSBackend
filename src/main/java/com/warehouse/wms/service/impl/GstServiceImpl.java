package com.warehouse.wms.service.impl;

import com.warehouse.wms.dto.request.GstRequest;
import com.warehouse.wms.dto.response.GstResponse;
import com.warehouse.wms.entity.Gst;
import com.warehouse.wms.exception.ResourceNotFoundException;
import com.warehouse.wms.mapper.GstMapper;
import com.warehouse.wms.repository.GstRepository;
import com.warehouse.wms.service.GstService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GstServiceImpl implements GstService {

    private final GstRepository repository;
    private final GstMapper mapper;

    @Override
    @Transactional
    public GstResponse create(GstRequest request) {
        if (repository.existsByCode(request.getCode())) {
            throw new RuntimeException("GST Code already exists: " + request.getCode());
        }
        Gst entity = mapper.toEntity(request);
        return mapper.toResponse(repository.save(entity));
    }

    @Override
    @Transactional
    public GstResponse update(Long id, GstRequest request) {
        Gst entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("GST not found with id: " + id));

//        // Check if code changed and new code already exists
//        if (!entity.getCode().equals(request.getCode()) && repository.existsByCode(request.getCode())) {
//            throw new RuntimeException("GST Code already exists: " + request.getCode());
//        }

        mapper.updateEntity(entity, request);
        return mapper.toResponse(repository.save(entity));
    }

    @Override
    public GstResponse getById(Long id) {
        Gst entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("GST not found with id: " + id));
        return mapper.toResponse(entity);
    }

    @Override
    public List<GstResponse> getAll() {
        return repository.findAll().stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Page<GstResponse> searchAndFilter(String keyword, BigDecimal rate, Boolean active, int page, int size, String sortBy) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).ascending());
        return repository.searchGst(keyword, rate, active, pageable)
                .map(mapper::toResponse);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("GST not found with id: " + id);
        }
        repository.deleteById(id);
    }
}