// ====== FILE: src/main/java/com/warehouse/wms/service/impl/WarehouseServiceImpl.java ======
package com.warehouse.wms.service.impl;

import com.warehouse.wms.dto.request.WarehouseRequest;
import com.warehouse.wms.dto.response.WarehouseResponse;
import com.warehouse.wms.entity.Warehouse;
import com.warehouse.wms.exception.ResourceNotFoundException;
import com.warehouse.wms.exception.InvalidOperationException;
import com.warehouse.wms.mapper.WarehouseMapper;
import com.warehouse.wms.repository.WarehouseRepository;
import com.warehouse.wms.service.WarehouseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class WarehouseServiceImpl implements WarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final WarehouseMapper warehouseMapper;

    @Override
    public WarehouseResponse createWarehouse(WarehouseRequest request) {
        log.info("Creating warehouse: {}", request.getWarehouseId());

        if (warehouseRepository.existsByWarehouseId(request.getWarehouseId())) {
            throw new InvalidOperationException("Warehouse ID already exists: " + request.getWarehouseId());
        }

        Warehouse warehouse = warehouseMapper.toEntity(request);
        warehouse.setTotalZones(0);

        Warehouse savedWarehouse = warehouseRepository.save(warehouse);
        log.info("✅ Warehouse created: {}", savedWarehouse.getWarehouseId());

        return warehouseMapper.toResponse(savedWarehouse);
    }

    @Override
    public WarehouseResponse getWarehouseById(Long id) {
        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found with ID: " + id));
        return warehouseMapper.toResponse(warehouse);
    }

    @Override
    public WarehouseResponse getWarehouseByWarehouseId(String warehouseId) {
        Warehouse warehouse = warehouseRepository.findByWarehouseId(warehouseId)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found: " + warehouseId));
        return warehouseMapper.toResponse(warehouse);
    }

    @Override
    public Page<WarehouseResponse> getAllWarehouses(Pageable pageable, String search) {
        if (search != null && !search.isEmpty()) {
            return warehouseRepository.searchWarehouses(search, pageable)
                    .map(warehouseMapper::toResponse);
        }
        return warehouseRepository.findAll(pageable)
                .map(warehouseMapper::toResponse);
    }

    @Override
    public List<WarehouseResponse> getActiveWarehouses() {
        return warehouseRepository.findByIsActiveTrue()
                .stream()
                .map(warehouseMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public WarehouseResponse updateWarehouse(Long id, WarehouseRequest request) {
        log.info("Updating warehouse: {}", id);

        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found with ID: " + id));

        // Check uniqueness if warehouseId is changed
        if (!request.getWarehouseId().equals(warehouse.getWarehouseId()) &&
            warehouseRepository.existsByWarehouseId(request.getWarehouseId())) {
            throw new InvalidOperationException("Warehouse ID already exists: " + request.getWarehouseId());
        }

        warehouseMapper.updateEntity(warehouse, request);
        Warehouse updatedWarehouse = warehouseRepository.save(warehouse);
        log.info("✅ Warehouse updated: {}", updatedWarehouse.getWarehouseId());

        return warehouseMapper.toResponse(updatedWarehouse);
    }

    @Override
    public void deleteWarehouse(Long id) {
        log.info("Deleting warehouse: {}", id);
        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found with ID: " + id));

        // Soft delete
        warehouse.setIsActive(false);
        warehouseRepository.save(warehouse);
        log.info("✅ Warehouse deactivated: {}", id);
    }

    @Override
    public void toggleWarehouseStatus(Long id, Boolean isActive) {
        log.info("Toggling warehouse status: {} to {}", id, isActive);
        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found with ID: " + id));

        warehouse.setIsActive(isActive);
        warehouseRepository.save(warehouse);
        log.info("✅ Warehouse status updated: {} -> {}", id, isActive);
    }
}