// ====== FILE: src/main/java/com/warehouse/wms/service/impl/BinLocationServiceImpl.java ======
package com.warehouse.wms.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.warehouse.wms.dto.request.BinLocationRequest;
import com.warehouse.wms.dto.response.BinLocationResponse;
import com.warehouse.wms.dto.response.BinLocationStatistics;
import com.warehouse.wms.entity.BinLocation;
import com.warehouse.wms.exception.InvalidOperationException;
import com.warehouse.wms.exception.ResourceNotFoundException;
import com.warehouse.wms.mapper.BinLocationMapper;
import com.warehouse.wms.repository.BinLocationRepository;
import com.warehouse.wms.service.BinLocationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BinLocationServiceImpl implements BinLocationService {

    private final BinLocationRepository binLocationRepository;
    private final BinLocationMapper binLocationMapper;

    // ====== Create Methods ======

    @Override
    public BinLocationResponse createBinLocation(BinLocationRequest request) {
        log.info("📦 Creating bin location: {}", request.getBinId());

        // Validate uniqueness
        if (binLocationRepository.existsByBinId(request.getBinId())) {
            throw new InvalidOperationException("Bin location already exists: " + request.getBinId());
        }

        if (binLocationRepository.existsByBinBarcode(request.getBinBarcode())) {
            throw new InvalidOperationException("Bin barcode already exists: " + request.getBinBarcode());
        }

        BinLocation binLocation = BinLocation.builder()
                .binId(request.getBinId())
                .binBarcode(request.getBinBarcode())
                .warehouseId(request.getWarehouseId())
                .zone(request.getZone())
                .aisle(request.getAisle())
                .rack(request.getRack())
                .shelf(request.getShelf())
                .level(request.getLevel())
                .position(request.getPosition())
                .capacity(request.getCapacity())
                .availableCapacity(request.getCapacity())
                .usedCapacity(0)
                .minThreshold(request.getMinThreshold() != null ? request.getMinThreshold() : 0)
                .maxThreshold(request.getMaxThreshold() != null ? request.getMaxThreshold() : request.getCapacity())
                .isOccupied(false)
                .isActive(true)
                .isReserved(false)
                .locationType(request.getLocationType())
                .zoneType(request.getZoneType())
                .movementType(request.getMovementType())
                .priority(request.getPriority() != null ? request.getPriority() : 0)
                .distanceFromDispatch(request.getDistanceFromDispatch())
                .createdBy(request.getCreatedBy())
                .remarks(request.getRemarks())
                .build();

        BinLocation savedBin = binLocationRepository.save(binLocation);
        log.info("✅ Bin location created: {}", savedBin.getBinId());

        return binLocationMapper.toResponse(savedBin);
    }

    @Override
    public List<BinLocationResponse> createBatchBinLocations(List<BinLocationRequest> requests) {
        log.info("📦 Creating {} bin locations", requests.size());
        List<BinLocationResponse> responses = new ArrayList<>();
        for (BinLocationRequest request : requests) {
            responses.add(createBinLocation(request));
        }
        return responses;
    }

    // ====== Read Methods ======

    @Override
    public BinLocationResponse getBinLocationById(Long id) {
        BinLocation binLocation = binLocationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bin location not found with ID: " + id));
        return binLocationMapper.toResponse(binLocation);
    }

    @Override
    public BinLocationResponse getBinLocationByBinId(String binId) {
        BinLocation binLocation = binLocationRepository.findByBinId(binId)
                .orElseThrow(() -> new ResourceNotFoundException("Bin location not found: " + binId));
        return binLocationMapper.toResponse(binLocation);
    }

    @Override
    public BinLocationResponse getBinLocationByBarcode(String binBarcode) {
        BinLocation binLocation = binLocationRepository.findByBinBarcode(binBarcode)
                .orElseThrow(() -> new ResourceNotFoundException("Bin location not found for barcode: " + binBarcode));
        return binLocationMapper.toResponse(binLocation);
    }

    @Override
    public Page<BinLocationResponse> getAllBinLocations(Pageable pageable, String warehouseId) {
        if (warehouseId != null && !warehouseId.isEmpty()) {
            return binLocationRepository.findByWarehouseId(warehouseId, pageable)
                    .map(binLocationMapper::toResponse);
        }
        return binLocationRepository.findAll(pageable)
                .map(binLocationMapper::toResponse);
    }

    @Override
    public List<BinLocationResponse> getBinLocationsByWarehouse(String warehouseId) {
        List<BinLocation> binLocations = binLocationRepository.findByWarehouseId(warehouseId);
        return binLocations.stream()
                .map(binLocationMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<BinLocationResponse> getBinLocationsByWarehouseAndZone(String warehouseId, String zone) {
        List<BinLocation> binLocations = binLocationRepository.findByWarehouseIdAndZone(warehouseId, zone);
        return binLocations.stream()
                .map(binLocationMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<BinLocationResponse> getAvailableBinLocations(String warehouseId, String zone) {
        List<BinLocation> binLocations;
        if (zone != null && !zone.isEmpty()) {
            binLocations = binLocationRepository.findByWarehouseIdAndZoneAndIsOccupiedFalse(warehouseId, zone);
        } else {
            binLocations = binLocationRepository.findByWarehouseIdAndIsOccupiedFalse(warehouseId);
        }
        return binLocations.stream()
                .map(binLocationMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<BinLocationResponse> getOccupiedBinLocations(String warehouseId) {
        List<BinLocation> binLocations = binLocationRepository.findByIsOccupiedTrue();
        if (warehouseId != null && !warehouseId.isEmpty()) {
            binLocations = binLocations.stream()
                    .filter(b -> b.getWarehouseId().equals(warehouseId))
                    .collect(Collectors.toList());
        }
        return binLocations.stream()
                .map(binLocationMapper::toResponse)
                .collect(Collectors.toList());
    }

    // ====== Update Methods ======

    @Override
    public BinLocationResponse updateBinLocation(Long id, BinLocationRequest request) {
        log.info("📦 Updating bin location: {}", id);

        BinLocation binLocation = binLocationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bin location not found with ID: " + id));

        // Check uniqueness if binId is being changed
        if (request.getBinId() != null && !request.getBinId().equals(binLocation.getBinId())) {
            if (binLocationRepository.existsByBinId(request.getBinId())) {
                throw new InvalidOperationException("Bin ID already exists: " + request.getBinId());
            }
        }

        // Check uniqueness if binBarcode is being changed
        if (request.getBinBarcode() != null && !request.getBinBarcode().equals(binLocation.getBinBarcode())) {
            if (binLocationRepository.existsByBinBarcode(request.getBinBarcode())) {
                throw new InvalidOperationException("Bin barcode already exists: " + request.getBinBarcode());
            }
        }

        // Update fields
        binLocationMapper.updateEntity(binLocation, request);

        // Recalculate available capacity if capacity changed
        if (request.getCapacity() != null) {
            binLocation.setCapacity(request.getCapacity());
            binLocation.setAvailableCapacity(request.getCapacity() - binLocation.getUsedCapacity());
        }

        BinLocation updatedBin = binLocationRepository.save(binLocation);
        log.info("✅ Bin location updated: {}", updatedBin.getBinId());

        return binLocationMapper.toResponse(updatedBin);
    }

    @Override
    public BinLocationResponse allocateBinCapacity(Long id, Integer quantity, String itemCode, String itemName, String uom) {
        log.info("📦 Allocating {} capacity to bin: {}", quantity, id);

        BinLocation binLocation = binLocationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bin location not found with ID: " + id));

        if (!binLocation.hasCapacity(quantity)) {
            throw new InvalidOperationException("Insufficient capacity in bin: " + binLocation.getBinId() + 
                                               ". Available: " + binLocation.getAvailableCapacity() + 
                                               ", Required: " + quantity);
        }

        binLocation.addUsedCapacity(quantity);
        binLocation.setItemCode(itemCode);
        binLocation.setItemName(itemName);
        binLocation.setUom(uom);
        binLocation.setLastPutawayAt(LocalDateTime.now());
        binLocation.setIsOccupied(binLocation.getAvailableCapacity() == 0);

        BinLocation updatedBin = binLocationRepository.save(binLocation);
        log.info("✅ Allocated {} to bin: {}", quantity, updatedBin.getBinId());

        return binLocationMapper.toResponse(updatedBin);
    }

    @Override
    public BinLocationResponse releaseBinCapacity(Long id, Integer quantity) {
        log.info("📦 Releasing {} capacity from bin: {}", quantity, id);

        BinLocation binLocation = binLocationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bin location not found with ID: " + id));

        if (binLocation.getUsedCapacity() < quantity) {
            throw new InvalidOperationException("Cannot release more than used capacity. Used: " + 
                                               binLocation.getUsedCapacity() + ", Requested: " + quantity);
        }

        binLocation.removeUsedCapacity(quantity);
        binLocation.setLastAccessedAt(LocalDateTime.now());
        binLocation.setIsOccupied(binLocation.getUsedCapacity() > 0);
        
        // Clear item info if no items left
        if (binLocation.getUsedCapacity() == 0) {
            binLocation.setItemCode(null);
            binLocation.setItemName(null);
            binLocation.setUom(null);
        }

        BinLocation updatedBin = binLocationRepository.save(binLocation);
        log.info("✅ Released {} from bin: {}", quantity, updatedBin.getBinId());

        return binLocationMapper.toResponse(updatedBin);
    }

    @Override
    public BinLocationResponse toggleActiveStatus(Long id, Boolean isActive) {
        log.info("📦 Toggling active status for bin: {} to {}", id, isActive);

        BinLocation binLocation = binLocationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bin location not found with ID: " + id));

        binLocation.setIsActive(isActive);
        BinLocation updatedBin = binLocationRepository.save(binLocation);
        log.info("✅ Bin active status updated: {} -> {}", updatedBin.getBinId(), isActive);

        return binLocationMapper.toResponse(updatedBin);
    }

    // ====== Delete Methods ======

    @Override
    public void deleteBinLocation(Long id) {
        log.info("📦 Deleting bin location: {}", id);
        
        BinLocation binLocation = binLocationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bin location not found with ID: " + id));
        
        // Soft delete - just deactivate
        binLocation.setIsActive(false);
        binLocationRepository.save(binLocation);
        log.info("✅ Bin location deactivated: {}", id);
    }

    @Override
    public void deleteBinLocationByBinId(String binId) {
        log.info("📦 Deleting bin location by binId: {}", binId);
        
        BinLocation binLocation = binLocationRepository.findByBinId(binId)
                .orElseThrow(() -> new ResourceNotFoundException("Bin location not found: " + binId));
        
        binLocation.setIsActive(false);
        binLocationRepository.save(binLocation);
        log.info("✅ Bin location deactivated: {}", binId);
    }

    // ====== Statistics Methods ======

    @Override
    public BinLocationStatistics getBinLocationStatistics(String warehouseId) {
        log.info("📊 Getting bin location statistics for warehouse: {}", warehouseId);
        
        Long totalBins = binLocationRepository.countActiveByWarehouse(warehouseId);
        Long occupiedBins = binLocationRepository.countOccupiedByWarehouse(warehouseId);
        Long availableBins = totalBins - occupiedBins;
        Long totalCapacity = binLocationRepository.sumAvailableCapacityByWarehouse(warehouseId);
        
        return BinLocationStatistics.builder()
                .warehouseId(warehouseId)
                .totalBins(totalBins != null ? totalBins : 0)
                .occupiedBins(occupiedBins != null ? occupiedBins : 0)
                .availableBins(availableBins)
                .totalAvailableCapacity(totalCapacity != null ? totalCapacity : 0)
                .build();
    }
}