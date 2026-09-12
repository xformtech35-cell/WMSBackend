package com.warehouse.wms.service.impl;

import com.warehouse.wms.dto.request.DepartmentRequest;
import com.warehouse.wms.dto.response.DepartmentResponse;
import com.warehouse.wms.entity.Department;
import com.warehouse.wms.exception.DuplicateResourceException;
import com.warehouse.wms.exception.ResourceNotFoundException;
import com.warehouse.wms.mapper.DepartmentMapper;
import com.warehouse.wms.repository.DepartmentRepository;
import com.warehouse.wms.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
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
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository repository;
    private final DepartmentMapper mapper;

    // ================= CREATE =================
    @Override
    @Transactional
    public DepartmentResponse create(DepartmentRequest request) {
        if (request.getCode() != null && repository.existsByCode(request.getCode())) {
            throw new DuplicateResourceException("Department Code already exists: " + request.getCode());
        }
        try {
            Department entity = mapper.toEntity(request);
            return mapper.toResponse(repository.save(entity));
        } catch (DataIntegrityViolationException ex) {
            throw new DuplicateResourceException("Department already exists with given code");
        }
    }

    // ================= UPDATE =================
    @Override
    @Transactional
    public DepartmentResponse update(Long id, DepartmentRequest request) {
        Department entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));

        // Check duplicate code only if it's being changed
        if (request.getCode() != null
                && !request.getCode().equals(entity.getCode())
                && repository.existsByCode(request.getCode())) {
            throw new DuplicateResourceException("Department Code already exists: " + request.getCode());
        }

        mapper.updateEntity(entity, request);
        return mapper.toResponse(repository.save(entity));
    }

    // ================= GET BY ID =================
    @Override
    public DepartmentResponse getById(Long id) {
        Department entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));
        return mapper.toResponse(entity);
    }

    // ================= GET BY CODE =================
    @Override
    public DepartmentResponse getByCode(String code) {
        Department entity = repository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with code: " + code));
        return mapper.toResponse(entity);
    }

    // ================= GET ALL =================
    @Override
    public List<DepartmentResponse> getAll() {
        return repository.findAll().stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    // ================= SEARCH / FILTER / PAGINATION =================
    @Override
    public Page<DepartmentResponse> searchAndFilter(String keyword, String location, Boolean isActive,
                                                    int page, int size, String sortBy) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).ascending());
        return repository.searchDepartments(keyword, location, isActive, pageable)
                .map(mapper::toResponse);
    }

    // ================= DELETE =================
    @Override
    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Department not found with id: " + id);
        }
        repository.deleteById(id);
    }

    // ================= TOGGLE ACTIVE =================
    @Override
    @Transactional
    public DepartmentResponse toggleActive(Long id) {
        Department entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));
        entity.setIsActive(!Boolean.TRUE.equals(entity.getIsActive()));
        return mapper.toResponse(repository.save(entity));
    }
}