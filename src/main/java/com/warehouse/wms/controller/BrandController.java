package com.warehouse.wms.controller;

import com.warehouse.wms.dto.request.BrandRequest;
import com.warehouse.wms.dto.response.BrandResponse;
import com.warehouse.wms.service.BrandService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/brands")
@RequiredArgsConstructor
public class BrandController {

    private final BrandService service;

    // POST - Create
    @PostMapping
    public ResponseEntity<BrandResponse> create(@Valid @RequestBody BrandRequest request) {
        return new ResponseEntity<>(service.create(request), HttpStatus.CREATED);
    }

    // PUT - Update
    @PutMapping("/{id}")
    public ResponseEntity<BrandResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody BrandRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    // GET - Get By ID
    @GetMapping("/{id}")
    public ResponseEntity<BrandResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    // GET - Get All (No Pagination)
    @GetMapping("/all")
    public ResponseEntity<List<BrandResponse>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    // GET - Search, Filter, Pagination
    // Example: /api/v1/brands?keyword=sam&active=true&page=0&size=10&sortBy=name
    @GetMapping
    public ResponseEntity<Page<BrandResponse>> searchAndFilter(
            @RequestParam(required = false) String Search,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy) {

        return ResponseEntity.ok(service.searchAndFilter(Search, active, page, size, sortBy));
    }

    // DELETE - Delete
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}