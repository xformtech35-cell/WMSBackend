// ====== FILE: src/main/java/com/warehouse/wms/service/impl/RockServiceImpl.java ======
package com.warehouse.wms.service.impl;

import com.warehouse.wms.dto.request.RockRequest;
import com.warehouse.wms.dto.response.RockResponse;
import com.warehouse.wms.entity.Rock;
import com.warehouse.wms.entity.Warehouse;
import com.warehouse.wms.exception.InvalidOperationException;
import com.warehouse.wms.exception.ResourceNotFoundException;
import com.warehouse.wms.mapper.RockMapper;
import com.warehouse.wms.repository.RockRepository;
import com.warehouse.wms.repository.WarehouseRepository;
import com.warehouse.wms.service.RockService;
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
public class RockServiceImpl implements RockService {

    private final RockRepository rockRepository;
    private final WarehouseRepository warehouseRepository;
    private final RockMapper rockMapper;

    // ====== Create ======

    @Override
    public RockResponse createRock(RockRequest request) {
        log.info("📦 Creating rock: {}", request.getRockId());

        // Validate rock ID uniqueness
        if (rockRepository.existsByRockId(request.getRockId())) {
            throw new InvalidOperationException("Rock ID already exists: " + request.getRockId());
        }

        // Validate warehouse exists
        Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId())
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found with ID: " + request.getWarehouseId()));

        // Create rock
        Rock rock = rockMapper.toEntity(request);
        rock.setWarehouse(warehouse);

        Rock savedRock = rockRepository.save(rock);
        log.info("✅ Rock created: {} in warehouse: {}", savedRock.getRockId(), warehouse.getWarehouseId());

        return rockMapper.toResponse(savedRock);
    }

    // ====== Read ======

    @Override
    public RockResponse getRockById(Long id) {
        Rock rock = rockRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rock not found with ID: " + id));
        return rockMapper.toResponse(rock);
    }

    @Override
    public RockResponse getRockByRockId(String rockId) {
        Rock rock = rockRepository.findByRockId(rockId)
                .orElseThrow(() -> new ResourceNotFoundException("Rock not found: " + rockId));
        return rockMapper.toResponse(rock);
    }

    @Override
    public Page<RockResponse> getAllRocks(Pageable pageable, String search, Long warehouseId) {
        if (search != null && !search.isEmpty() && warehouseId != null) {
            return rockRepository.searchRocksByWarehouse(warehouseId, search, pageable)
                    .map(rockMapper::toResponse);
        } else if (search != null && !search.isEmpty()) {
            return rockRepository.searchRocks(search, pageable)
                    .map(rockMapper::toResponse);
        } else if (warehouseId != null) {
            return rockRepository.findByWarehouseId(warehouseId, pageable)
                    .map(rockMapper::toResponse);
        }
        return rockRepository.findAll(pageable)
                .map(rockMapper::toResponse);
    }

    @Override
    public List<RockResponse> getRocksByWarehouse(Long warehouseId) {
        List<Rock> rocks = rockRepository.findByWarehouseId(warehouseId);
        return rocks.stream()
                .map(rockMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<RockResponse> getActiveRocksByWarehouse(Long warehouseId) {
        List<Rock> rocks = rockRepository.findByWarehouseIdAndIsActiveTrue(warehouseId);
        return rocks.stream()
                .map(rockMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<RockResponse> getRocksByType(String rockType) {
        List<Rock> rocks = rockRepository.findByRockType(rockType);
        return rocks.stream()
                .map(rockMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<RockResponse> getLowStockRocks() {
        List<Rock> rocks = rockRepository.findLowStockRocks();
        return rocks.stream()
                .map(rockMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<RockResponse> getOverStockRocks() {
        List<Rock> rocks = rockRepository.findOverStockRocks();
        return rocks.stream()
                .map(rockMapper::toResponse)
                .collect(Collectors.toList());
    }

    // ====== Update ======

    @Override
    public RockResponse updateRock(Long id, RockRequest request) {
        log.info("📦 Updating rock: {}", id);

        Rock rock = rockRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rock not found with ID: " + id));

        // Check uniqueness if rockId is changed
        if (!request.getRockId().equals(rock.getRockId()) &&
            rockRepository.existsByRockId(request.getRockId())) {
            throw new InvalidOperationException("Rock ID already exists: " + request.getRockId());
        }

        // Update warehouse if changed
        if (!request.getWarehouseId().equals(rock.getWarehouse().getId())) {
            Warehouse newWarehouse = warehouseRepository.findById(request.getWarehouseId())
                    .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found with ID: " + request.getWarehouseId()));
            rock.setWarehouse(newWarehouse);
        }

        // Update rock fields
        rockMapper.updateEntity(rock, request);

        Rock updatedRock = rockRepository.save(rock);
        log.info("✅ Rock updated: {}", updatedRock.getRockId());

        return rockMapper.toResponse(updatedRock);
    }

    @Override
    public RockResponse toggleRockStatus(Long id, Boolean isActive) {
        log.info("📦 Toggling rock status: {} to {}", id, isActive);

        Rock rock = rockRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rock not found with ID: " + id));

        rock.setIsActive(isActive);
        Rock updatedRock = rockRepository.save(rock);

        log.info("✅ Rock status updated: {} -> {}", rock.getRockId(), isActive);
        return rockMapper.toResponse(updatedRock);
    }

    @Override
    public RockResponse addRockQuantity(Long id, Integer quantity) {
        log.info("📦 Adding quantity to rock: {} - {}", id, quantity);

        Rock rock = rockRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rock not found with ID: " + id));

        if (quantity <= 0) {
            throw new InvalidOperationException("Quantity must be greater than 0");
        }

        int updated = rockRepository.addQuantity(id, quantity);
        if (updated == 0) {
            throw new InvalidOperationException("Failed to add quantity.");
        }

        Rock updatedRock = rockRepository.findById(id).get();
        log.info("✅ Quantity added to rock: {} - New quantity: {}", updatedRock.getRockId(), updatedRock.getQuantity());

        return rockMapper.toResponse(updatedRock);
    }

    @Override
    public RockResponse deductRockQuantity(Long id, Integer quantity) {
        log.info("📦 Deducting quantity from rock: {} - {}", id, quantity);

        Rock rock = rockRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rock not found with ID: " + id));

        if (quantity <= 0) {
            throw new InvalidOperationException("Quantity must be greater than 0");
        }

        if (rock.getQuantity() < quantity) {
            throw new InvalidOperationException("Insufficient quantity. Available: " + rock.getQuantity() + 
                                                ", Requested: " + quantity);
        }

        int updated = rockRepository.deductQuantity(id, quantity);
        if (updated == 0) {
            throw new InvalidOperationException("Failed to deduct quantity.");
        }

        Rock updatedRock = rockRepository.findById(id).get();
        log.info("✅ Quantity deducted from rock: {} - New quantity: {}", updatedRock.getRockId(), updatedRock.getQuantity());

        return rockMapper.toResponse(updatedRock);
    }

    // ====== Delete ======

    @Override
    public void deleteRock(Long id) {
        log.info("📦 Deleting rock: {}", id);

        Rock rock = rockRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rock not found with ID: " + id));

        rock.setIsActive(false);
        rockRepository.save(rock);

        log.info("✅ Rock deactivated: {}", id);
    }

    @Override
    public void deleteRockByRockId(String rockId) {
        log.info("📦 Deleting rock by rockId: {}", rockId);

        Rock rock = rockRepository.findByRockId(rockId)
                .orElseThrow(() -> new ResourceNotFoundException("Rock not found: " + rockId));

        rock.setIsActive(false);
        rockRepository.save(rock);

        log.info("✅ Rock deactivated: {}", rockId);
    }
}