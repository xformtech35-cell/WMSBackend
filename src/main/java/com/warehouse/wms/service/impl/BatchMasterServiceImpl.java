package com.warehouse.wms.service.impl;

import com.warehouse.wms.dto.request.BatchMasterRequest;
import com.warehouse.wms.dto.response.BatchMasterResponse;
import com.warehouse.wms.entity.BatchMaster;
import com.warehouse.wms.exception.ResourceNotFoundException; // Assuming you have this
import com.warehouse.wms.repository.BatchMasterRepository;
import com.warehouse.wms.service.BatchMasterService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BatchMasterServiceImpl implements BatchMasterService {

    private final BatchMasterRepository repository;

    @Override
    @Transactional
    public BatchMasterResponse create(BatchMasterRequest request) {
        if (repository.existsByBatchCode(request.getBatchCode())) {
            throw new RuntimeException("Batch Code already exists: " + request.getBatchCode());
        }

        BatchMaster entity = BatchMaster.builder()
                .batchCode(request.getBatchCode())
                .batchName(request.getBatchName())
                .description(request.getDescription())
                .manufacturingDate(request.getManufacturingDate())
                .expiryDate(request.getExpiryDate())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        BatchMaster saved = repository.save(entity);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public BatchMasterResponse update(Long id, BatchMasterRequest request) {
        BatchMaster entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Batch not found with id: " + id));

        // Check if batch code is being changed and if it already exists
        if (!entity.getBatchCode().equals(request.getBatchCode()) && 
            repository.existsByBatchCode(request.getBatchCode())) {
            throw new RuntimeException("Batch Code already exists: " + request.getBatchCode());
        }

        entity.setBatchCode(request.getBatchCode());
        entity.setBatchName(request.getBatchName());
        entity.setDescription(request.getDescription());
        entity.setManufacturingDate(request.getManufacturingDate());
        entity.setExpiryDate(request.getExpiryDate());
        if (request.getIsActive() != null) {
            entity.setIsActive(request.getIsActive());
        }

        BatchMaster updated = repository.save(entity);
        return mapToResponse(updated);
    }

    @Override
    public BatchMasterResponse getById(Long id) {
        BatchMaster entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Batch not found with id: " + id));
        return mapToResponse(entity);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Batch not found with id: " + id);
        }
        repository.deleteById(id);
    }

    @Override
    public Page<BatchMasterResponse> getAll(String search, Boolean isActive, Pageable pageable) {
        Specification<BatchMaster> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filter by Active Status
            if (isActive != null) {
                predicates.add(cb.equal(root.get("isActive"), isActive));
            }

            // Search across Batch Code, Batch Name, or Description
            if (search != null && !search.trim().isEmpty()) {
                String searchPattern = "%" + search.toLowerCase() + "%";
                Predicate codeLike = cb.like(cb.lower(root.get("batchCode")), searchPattern);
                Predicate nameLike = cb.like(cb.lower(root.get("batchName")), searchPattern);
                Predicate descLike = cb.like(cb.lower(root.get("description")), searchPattern);
                
                predicates.add(cb.or(codeLike, nameLike, descLike));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return repository.findAll(spec, pageable).map(this::mapToResponse);
    }

    // Manual Mapper Method (Alternatively, use MapStruct)
    private BatchMasterResponse mapToResponse(BatchMaster entity) {
        return BatchMasterResponse.builder()
                .id(entity.getId())
                .batchCode(entity.getBatchCode())
                .batchName(entity.getBatchName())
                .description(entity.getDescription())
                .manufacturingDate(entity.getManufacturingDate())
                .expiryDate(entity.getExpiryDate())
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}