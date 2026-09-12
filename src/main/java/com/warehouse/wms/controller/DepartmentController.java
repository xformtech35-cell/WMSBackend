package com.warehouse.wms.controller;

import com.warehouse.wms.dto.request.DepartmentRequest;
import com.warehouse.wms.dto.response.DepartmentResponse;
import com.warehouse.wms.service.DepartmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService service;

    // POST - Create
    @PostMapping
    public ResponseEntity<DepartmentResponse> create(@Valid @RequestBody DepartmentRequest request) {
        return new ResponseEntity<>(service.create(request), HttpStatus.CREATED);
    }

    // PUT - Update (partial update supported)
    @PutMapping("/{id}")
    public ResponseEntity<DepartmentResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody DepartmentRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    // GET - By ID
    @GetMapping("/{id}")
    public ResponseEntity<DepartmentResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    // GET - By Code
    @GetMapping("/code/{code}")
    public ResponseEntity<DepartmentResponse> getByCode(@PathVariable String code) {
        return ResponseEntity.ok(service.getByCode(code));
    }

    // GET - All
    @GetMapping("/all")
    public ResponseEntity<List<DepartmentResponse>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    // GET - Search + Filter + Pagination
    // Example: /api/v1/departments?keyword=hr&location=building&isActive=true&page=0&size=10&sortBy=name
    @GetMapping
    public ResponseEntity<Page<DepartmentResponse>> searchAndFilter(
            @RequestParam(required = false) String Search,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy) {

        return ResponseEntity.ok(service.searchAndFilter(Search, location, isActive, page, size, sortBy));
    }

    // PATCH - Toggle Active
    @PatchMapping("/{id}/toggle-active")
    public ResponseEntity<DepartmentResponse> toggleActive(@PathVariable Long id) {
        return ResponseEntity.ok(service.toggleActive(id));
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}