// ====== FILE: src/main/java/com/warehouse/wms/service/impl/RackServiceImpl.java ======
package com.warehouse.wms.service.impl;

import com.warehouse.wms.dto.request.RackRequest;
import com.warehouse.wms.dto.response.RackResponse;
import com.warehouse.wms.entity.Aisle;
import com.warehouse.wms.entity.Rack;
import com.warehouse.wms.exception.InvalidOperationException;
import com.warehouse.wms.exception.ResourceNotFoundException;
import com.warehouse.wms.mapper.RackMapper;
import com.warehouse.wms.repository.AisleRepository;
import com.warehouse.wms.repository.RackRepository;
import com.warehouse.wms.service.RackService;
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
public class RackServiceImpl implements RackService {

    private final RackRepository rackRepository;
    private final AisleRepository aisleRepository;
    private final RackMapper rackMapper;
    private final RackBarcodeServiceImpl rackBarcodeServiceImpl;

    // ====== Create ======

    @Override
    public RackResponse createRack(RackRequest request) {
        log.info("📦 Creating rack: {}", request.getRackId());

        // Validate rack ID uniqueness
        if (rackRepository.existsByRackId(request.getRackId())) {
            throw new InvalidOperationException("Rack ID already exists: " + request.getRackId());
        }

        // Validate aisle exists
        Aisle aisle = aisleRepository.findById(request.getAisleId())
                .orElseThrow(() -> new ResourceNotFoundException("Aisle not found with ID: " + request.getAisleId()));

        // Create rack
        Rack rack = rackMapper.toEntity(request);
        rack.setAisle(aisle);
        rack.setTotalShelves(0);

        Rack savedRack = rackRepository.save(rack);

        // Update aisle total racks count
        long totalRacks = rackRepository.countByAisleId(aisle.getId());
        aisle.setTotalRacks((int) totalRacks);
        aisleRepository.save(aisle);
        
        
        rackBarcodeServiceImpl.generateRackBarcode(savedRack.getAisle().getZone().getWarehouse().getWarehouseId(),savedRack.getAisle().getZone().getZoneId(),savedRack.getAisle().getAisleId(),savedRack.getRackId());
        

        log.info("✅ Rack created: {} in aisle: {}", savedRack.getRackId(), aisle.getAisleId());
        return rackMapper.toResponse(savedRack);
    }

    // ====== Read ======

    @Override
    public RackResponse getRackById(Long id) {
        Rack rack = rackRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rack not found with ID: " + id));
        return rackMapper.toResponse(rack);
    }

    @Override
    public RackResponse getRackByRackId(String rackId) {
        Rack rack = rackRepository.findByRackId(rackId)
                .orElseThrow(() -> new ResourceNotFoundException("Rack not found: " + rackId));
        return rackMapper.toResponse(rack);
    }

    @Override
    public Page<RackResponse> getAllRacks(Pageable pageable, String search, Long aisleId) {
        if (search != null && !search.isEmpty() && aisleId != null) {
            return rackRepository.searchRacksByAisle(aisleId, search, pageable)
                    .map(rackMapper::toResponse);
        } else if (search != null && !search.isEmpty()) {
            return rackRepository.searchRacks(search, pageable)
                    .map(rackMapper::toResponse);
        } else if (aisleId != null) {
            return rackRepository.findByAisleId(aisleId, pageable)
                    .map(rackMapper::toResponse);
        }
        return rackRepository.findAll(pageable)
                .map(rackMapper::toResponse);
    }

    @Override
    public List<RackResponse> getRacksByAisle(Long aisleId) {
        List<Rack> racks = rackRepository.findByAisleId(aisleId);
        return racks.stream()
                .map(rackMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<RackResponse> getActiveRacksByAisle(Long aisleId) {
        List<Rack> racks = rackRepository.findByAisleIdAndIsActiveTrue(aisleId);
        return racks.stream()
                .map(rackMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<RackResponse> getRacksByAisleId(String aisleId) {
        List<Rack> racks = rackRepository.findByAisleAisleId(aisleId);
        return racks.stream()
                .map(rackMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<RackResponse> getRacksByZone(Long zoneId) {
        List<Rack> racks = rackRepository.findByZoneId(zoneId);
        return racks.stream()
                .map(rackMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<RackResponse> getRacksByWarehouse(Long warehouseId) {
        List<Rack> racks = rackRepository.findByWarehouseId(warehouseId);
        return racks.stream()
                .map(rackMapper::toResponse)
                .collect(Collectors.toList());
    }

    // ====== Update ======

    @Override
    public RackResponse updateRack(Long id, RackRequest request) {
        log.info("📦 Updating rack: {}", id);

        Rack rack = rackRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rack not found with ID: " + id));

        // Check uniqueness if rackId is changed
        if (!request.getRackId().equals(rack.getRackId()) &&
            rackRepository.existsByRackId(request.getRackId())) {
            throw new InvalidOperationException("Rack ID already exists: " + request.getRackId());
        }

        // Update aisle if changed
        if (!request.getAisleId().equals(rack.getAisle().getId())) {
            Aisle newAisle = aisleRepository.findById(request.getAisleId())
                    .orElseThrow(() -> new ResourceNotFoundException("Aisle not found with ID: " + request.getAisleId()));
            
            // Update old aisle total racks
            Aisle oldAisle = rack.getAisle();
            long oldCount = rackRepository.countByAisleId(oldAisle.getId());
            oldAisle.setTotalRacks((int) oldCount);
            aisleRepository.save(oldAisle);

            // Update new aisle total racks
            rack.setAisle(newAisle);
            long newCount = rackRepository.countByAisleId(newAisle.getId());
            newAisle.setTotalRacks((int) newCount);
            aisleRepository.save(newAisle);
        }

        // Update rack fields
        rackMapper.updateEntity(rack, request);

        Rack updatedRack = rackRepository.save(rack);
        log.info("✅ Rack updated: {}", updatedRack.getRackId());

        return rackMapper.toResponse(updatedRack);
    }

    @Override
    public RackResponse toggleRackStatus(Long id, Boolean isActive) {
        log.info("📦 Toggling rack status: {} to {}", id, isActive);

        Rack rack = rackRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rack not found with ID: " + id));

        rack.setIsActive(isActive);
        Rack updatedRack = rackRepository.save(rack);

        log.info("✅ Rack status updated: {} -> {}", rack.getRackId(), isActive);
        return rackMapper.toResponse(updatedRack);
    }

    // ====== Delete ======

    @Override
    public void deleteRack(Long id) {
        log.info("📦 Deleting rack: {}", id);

        Rack rack = rackRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rack not found with ID: " + id));

        // Soft delete
        rack.setIsActive(false);
        rackRepository.save(rack);

        // Update aisle total racks
        Aisle aisle = rack.getAisle();
        long totalRacks = rackRepository.countByAisleId(aisle.getId());
        aisle.setTotalRacks((int) totalRacks);
        aisleRepository.save(aisle);

        log.info("✅ Rack deactivated: {}", id);
    }

    @Override
    public void deleteRackByRackId(String rackId) {
        log.info("📦 Deleting rack by rackId: {}", rackId);

        Rack rack = rackRepository.findByRackId(rackId)
                .orElseThrow(() -> new ResourceNotFoundException("Rack not found: " + rackId));

        rack.setIsActive(false);
        rackRepository.save(rack);

        // Update aisle total racks
        Aisle aisle = rack.getAisle();
        long totalRacks = rackRepository.countByAisleId(aisle.getId());
        aisle.setTotalRacks((int) totalRacks);
        aisleRepository.save(aisle);

        log.info("✅ Rack deactivated: {}", rackId);
    }
}