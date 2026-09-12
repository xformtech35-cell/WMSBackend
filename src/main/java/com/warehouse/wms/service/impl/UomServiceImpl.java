package com.warehouse.wms.service.impl;

import com.warehouse.wms.dto.request.UomRequest;
import com.warehouse.wms.dto.response.UomResponse;
import com.warehouse.wms.entity.Uom;
import com.warehouse.wms.exception.ResourceNotFoundException; // Assume this exists
import com.warehouse.wms.mapper.UomMapper;
import com.warehouse.wms.repository.UomRepository;
import com.warehouse.wms.service.UomService;
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
public class UomServiceImpl implements UomService {

    private final UomRepository repository;
    private final UomMapper mapper;

    @Override
    @Transactional
    public UomResponse create(UomRequest request) {
        if (repository.existsByCode(request.getCode())) {
            throw new RuntimeException("UOM Code already exists"); // Use custom exception
        }
        Uom entity = mapper.toEntity(request);
        return mapper.toResponse(repository.save(entity));
    }

    @Override
    @Transactional
    public UomResponse update(Long id, UomRequest request) {
        Uom entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("UOM not found with id: " + id));
        
        mapper.updateEntity(entity, request);
        return mapper.toResponse(repository.save(entity));
    }

    @Override
    public UomResponse getById(Long id) {
        Uom entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("UOM not found with id: " + id));
        return mapper.toResponse(entity);
    }

    @Override
    public List<UomResponse> getAll() {
        return repository.findAll().stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Page<UomResponse> searchAndFilter(String keyword, Boolean active, int page, int size, String sortBy) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).ascending());
        return repository.searchUoms(keyword, active, pageable)
                .map(mapper::toResponse);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("UOM not found with id: " + id);
        }
        repository.deleteById(id);
    }
}