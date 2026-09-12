package com.warehouse.wms.controller;

import com.warehouse.wms.dto.request.GstRequest;
import com.warehouse.wms.dto.response.GstResponse;
import com.warehouse.wms.service.GstService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/gst")
@RequiredArgsConstructor
public class GstController {

    private final GstService service;

    // POST - Create
    @PostMapping
    public ResponseEntity<GstResponse> create(@Valid @RequestBody GstRequest request) {
        return new ResponseEntity<>(service.create(request), HttpStatus.CREATED);
    }

    // PUT - Update
    @PutMapping("/{id}")
    public ResponseEntity<GstResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody GstRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    // GET - Get By ID
    @GetMapping("/{id}")
    public ResponseEntity<GstResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    // GET - Get All (No Pagination)
    @GetMapping("/all")
    public ResponseEntity<List<GstResponse>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    // GET - Search, Filter, Pagination
    // Example: /api/v1/gst?keyword=gst&rate=18&active=true&page=0&size=10&sortBy=rate
    @GetMapping
    public ResponseEntity<Page<GstResponse>> searchAndFilter(
            @RequestParam(required = false) String Search,
            @RequestParam(required = false) BigDecimal rate,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy) {

        return ResponseEntity.ok(service.searchAndFilter(Search, rate, active, page, size, sortBy));
    }

    // DELETE - Delete
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}