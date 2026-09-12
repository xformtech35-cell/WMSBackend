package com.warehouse.wms.service.impl;

import com.warehouse.wms.dto.request.BrandRequest;
import com.warehouse.wms.dto.response.BrandResponse;
import com.warehouse.wms.entity.Brand;
import com.warehouse.wms.exception.ResourceNotFoundException;
import com.warehouse.wms.mapper.BrandMapper;
import com.warehouse.wms.repository.BrandRepository;
import com.warehouse.wms.service.BrandService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BrandServiceImpl implements BrandService {

    private final BrandRepository repository;
    private final BrandMapper mapper;

    @Override
    @Transactional
    public BrandResponse create(BrandRequest request) {
        if (repository.existsByCode(request.getCode())) {
            throw new RuntimeException("Brand Code already exists: " + request.getCode());
        }
        Brand entity = mapper.toEntity(request);
        return mapper.toResponse(repository.save(entity));
    }

    @Override
    @Transactional
    public BrandResponse update(Long id, BrandRequest request) {
        Brand entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found with id: " + id));

        // Check if code changed and new code already exists
        if (!entity.getCode().equals(request.getCode()) && repository.existsByCode(request.getCode())) {
            throw new RuntimeException("Brand Code already exists: " + request.getCode());
        }

        mapper.updateEntity(entity, request);
        return mapper.toResponse(repository.save(entity));
    }

    @Override
    public BrandResponse getById(Long id) {
        Brand entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found with id: " + id));
        return mapper.toResponse(entity);
    }

    @Override
    public List<BrandResponse> getAll() {
        return repository.findAll().stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Page<BrandResponse> searchAndFilter(String keyword, Boolean active, int page, int size, String sortBy) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).ascending());
        return repository.searchBrands(keyword, active, pageable)
                .map(mapper::toResponse);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Brand not found with id: " + id);
        }
        repository.deleteById(id);
    }
}