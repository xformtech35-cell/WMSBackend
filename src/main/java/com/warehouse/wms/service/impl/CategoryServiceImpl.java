package com.warehouse.wms.service.impl;

import com.warehouse.wms.dto.request.CategoryRequest;
import com.warehouse.wms.dto.response.CategoryResponse;
import com.warehouse.wms.entity.Category;
import com.warehouse.wms.exception.ResourceNotFoundException;
import com.warehouse.wms.mapper.CategoryMapper;
import com.warehouse.wms.repository.CategoryRepository;
import com.warehouse.wms.service.CategoryService;
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
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository repository;
    private final CategoryMapper mapper;

    @Override
    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        if (repository.existsByCode(request.getCode())) {
            throw new RuntimeException("Category Code already exists");
        }

        Category parent = null;
        if (request.getParentId() != null) {
            parent = repository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent Category not found"));
        }

        Category entity = mapper.toEntity(request, parent);
        return mapper.toResponse(repository.save(entity));
    }

    @Override
    @Transactional
    public CategoryResponse update(Long id, CategoryRequest request) {
        Category entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        Category parent = null;
        if (request.getParentId() != null) {
            // Prevent setting itself as parent
            if (request.getParentId().equals(id)) {
                throw new RuntimeException("Category cannot be its own parent");
            }
            parent = repository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent Category not found"));
        }

        mapper.updateEntity(entity, request, parent);
        return mapper.toResponse(repository.save(entity));
    }

    @Override
    public CategoryResponse getById(Long id) {
        Category entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        return mapper.toResponse(entity);
    }

    @Override
    public List<CategoryResponse> getAll() {
        return repository.findAll().stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Page<CategoryResponse> searchAndFilter(String keyword, Boolean active, Long parentId, int page, int size, String sortBy) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).ascending());
        return repository.searchCategories(keyword, active, parentId, pageable)
                .map(mapper::toResponse);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Category entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        // Optional: Check if it has child categories before deleting
        repository.delete(entity);
    }
}