package com.warehouse.wms.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.warehouse.wms.dto.ApiResponse;
import com.warehouse.wms.dto.CreateInboundDTO;
import com.warehouse.wms.dto.GateEntryDTO;
import com.warehouse.wms.dto.GoodsReceivingDTO;
import com.warehouse.wms.dto.InboundDTO;
import com.warehouse.wms.dto.InboundFilterDTO;
import com.warehouse.wms.dto.InboundFilterRequestDTO;
import com.warehouse.wms.dto.QualityInspectionDTO;
import com.warehouse.wms.dto.UnloadingDTO;
import com.warehouse.wms.entity.InboundStatus;
import com.warehouse.wms.service.InboundService;
import com.warehouse.wms.util.SecurityUtils;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/inbound")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class InboundController {

    private final InboundService inboundService;

    // ============ 1. CREATE INBOUND FROM PO ============
    @PostMapping
    public ResponseEntity<ApiResponse<InboundDTO>> createInbound(
            @Valid @RequestBody CreateInboundDTO requestDTO) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            InboundDTO created = inboundService.createInbound(requestDTO, userId);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Inbound created successfully", created));
        } catch (Exception e) {
            log.error("Error creating inbound", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Error creating inbound: " + e.getMessage()));
        }
    }

    // ============ 2. GATE ENTRY ============
    @PostMapping("/{inboundId}/gate-entry")
    public ResponseEntity<ApiResponse<InboundDTO>> gateEntry(
            @PathVariable Long inboundId,
            @Valid @RequestBody GateEntryDTO gateEntryDTO) {
        try {
            InboundDTO updated = inboundService.gateEntry(inboundId, gateEntryDTO);
            return ResponseEntity.ok(ApiResponse.success("Gate entry completed successfully", updated));
        } catch (Exception e) {
            log.error("Error in gate entry", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Error in gate entry: " + e.getMessage()));
        }
    }

    // ============ 3. TRUCK UNLOADING ============
    @PostMapping("/{inboundId}/unloading")
    public ResponseEntity<ApiResponse<InboundDTO>> unloading(
            @PathVariable Long inboundId,
            @Valid @RequestBody UnloadingDTO unloadingDTO) {
        try {
            InboundDTO updated = inboundService.unloading(inboundId, unloadingDTO);
            return ResponseEntity.ok(ApiResponse.success("Truck unloading completed successfully", updated));
        } catch (Exception e) {
            log.error("Error in unloading", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Error in unloading: " + e.getMessage()));
        }
    }

    // ============ 4. GOODS RECEIVING ============
    @PostMapping("/{inboundId}/receive")
    public ResponseEntity<ApiResponse<InboundDTO>> goodsReceiving(
            @PathVariable Long inboundId,
            @Valid @RequestBody GoodsReceivingDTO receivingDTO) {
        try {
            InboundDTO updated = inboundService.goodsReceiving(inboundId, receivingDTO);
            return ResponseEntity.ok(ApiResponse.success("Goods receiving completed successfully", updated));
        } catch (Exception e) {
            log.error("Error in goods receiving", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Error in goods receiving: " + e.getMessage()));
        }
    }

    // ============ 5. QUALITY INSPECTION ============
    @PostMapping("/{inboundId}/quality-inspection")
    public ResponseEntity<ApiResponse<InboundDTO>> qualityInspection(
            @PathVariable Long inboundId,
            @Valid @RequestBody QualityInspectionDTO inspectionDTO) {
        try {
            InboundDTO updated = inboundService.qualityInspection(inboundId, inspectionDTO);
            return ResponseEntity.ok(ApiResponse.success("Quality inspection completed successfully", updated));
        } catch (Exception e) {
            log.error("Error in quality inspection", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Error in quality inspection: " + e.getMessage()));
        }
    }

    // ============ 6. GENERATE GRN ============
    @PostMapping("/{inboundId}/generate-grn")
    public ResponseEntity<ApiResponse<InboundDTO>> generateGRN(@PathVariable Long inboundId) {
        try {
            InboundDTO updated = inboundService.generateGRN(inboundId);
            return ResponseEntity.ok(ApiResponse.success("GRN generated successfully", updated));
        } catch (Exception e) {
            log.error("Error generating GRN", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Error generating GRN: " + e.getMessage()));
        }
    }

    // ============ GET INBOUND BY ID ============
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InboundDTO>> getInboundById(@PathVariable Long id) {
        try {
            InboundDTO inbound = inboundService.getInboundById(id);
            return ResponseEntity.ok(ApiResponse.success(inbound));
        } catch (Exception e) {
            log.error("Error getting inbound", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("Inbound not found: " + e.getMessage()));
        }
    }

    // ============ GET INBOUND BY NUMBER ============
    @GetMapping("/number/{inboundNumber}")
    public ResponseEntity<ApiResponse<InboundDTO>> getInboundByNumber(@PathVariable String inboundNumber) {
        try {
            InboundDTO inbound = inboundService.getInboundByNumber(inboundNumber);
            return ResponseEntity.ok(ApiResponse.success(inbound));
        } catch (Exception e) {
            log.error("Error getting inbound by number", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("Inbound not found: " + e.getMessage()));
        }
    }

    // ============ GET ALL INBOUNDS ============
    @GetMapping
    public ResponseEntity<ApiResponse<Page<InboundDTO>>> getAllInbounds(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        try {
            Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
            Page<InboundDTO> inbounds = inboundService.getAllInbounds(pageable);
            return ResponseEntity.ok(ApiResponse.success(inbounds));
        } catch (Exception e) {
            log.error("Error getting all inbounds", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Error getting inbounds: " + e.getMessage()));
        }
    }

    // ============ FILTER INBOUNDS ============
    @PostMapping("/filter")
    public ResponseEntity<ApiResponse<Page<InboundDTO>>> filterInbounds(
            @RequestBody(required = false) InboundFilterRequestDTO filterRequest) {
        try {
            if (filterRequest == null) {
                filterRequest = InboundFilterRequestDTO.builder()
                    .filters(InboundFilterDTO.builder().build())
                    .build();
            }
            
            if (filterRequest.getFilters() == null) {
                filterRequest.setFilters(InboundFilterDTO.builder().build());
            }
            
            Sort.Direction direction = filterRequest.getSortDir().equalsIgnoreCase("desc") ? 
                Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(
                filterRequest.getPage(), 
                filterRequest.getSize(), 
                Sort.by(direction, filterRequest.getSortBy())
            );
            
            Page<InboundDTO> result = inboundService.filterInbounds(filterRequest.getFilters(), pageable);
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            log.error("Error filtering inbounds", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Error filtering inbounds: " + e.getMessage()));
        }
    }

    // ============ GET INBOUNDS BY STATUS ============
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<InboundDTO>>> getInboundsByStatus(@PathVariable InboundStatus status) {
        try {
            List<InboundDTO> inbounds = inboundService.getInboundsByStatus(status);
            return ResponseEntity.ok(ApiResponse.success(inbounds));
        } catch (Exception e) {
            log.error("Error getting inbounds by status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Error getting inbounds: " + e.getMessage()));
        }
    }
}