package com.warehouse.wms.controller;

import com.warehouse.wms.dto.request.UomRequest;
import com.warehouse.wms.dto.response.UomResponse;
import com.warehouse.wms.service.UomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/uoms")
@RequiredArgsConstructor
public class UomController {

    private final UomService service;

    // POST - Create
    @PostMapping
    public ResponseEntity<UomResponse> create(@Valid @RequestBody UomRequest request) {
        return new ResponseEntity<>(service.create(request), HttpStatus.CREATED);
    }

    // PUT - Update
    @PutMapping("/{id}")
    public ResponseEntity<UomResponse> update(
            @PathVariable Long id, 
            @Valid @RequestBody UomRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    // GET - Get By ID
    @GetMapping("/{id}")
    public ResponseEntity<UomResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    // GET - Get All (No Pagination)
    @GetMapping("/all")
    public ResponseEntity<List<UomResponse>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    // GET - Search, Filter, Pagination
    // Example: /api/v1/uoms?keyword=kg&active=true&page=0&size=10&sortBy=name
    @GetMapping
    public ResponseEntity<Page<UomResponse>> searchAndFilter(
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