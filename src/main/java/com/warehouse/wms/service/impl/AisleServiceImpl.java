// ====== FILE: src/main/java/com/warehouse/wms/service/impl/AisleServiceImpl.java ======
package com.warehouse.wms.service.impl;

import com.warehouse.wms.dto.request.AisleRequest;
import com.warehouse.wms.dto.response.AisleResponse;
import com.warehouse.wms.entity.Aisle;
import com.warehouse.wms.entity.Zone;
import com.warehouse.wms.exception.InvalidOperationException;
import com.warehouse.wms.exception.ResourceNotFoundException;
import com.warehouse.wms.mapper.AisleMapper;
import com.warehouse.wms.repository.AisleRepository;
import com.warehouse.wms.repository.ZoneRepository;
import com.warehouse.wms.service.AisleService;
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
public class AisleServiceImpl implements AisleService {

    private final AisleRepository aisleRepository;
    private final ZoneRepository zoneRepository;
    private final AisleMapper aisleMapper;

    // ====== Create ======

    @Override
    public AisleResponse createAisle(AisleRequest request) {
        log.info("📦 Creating aisle: {}", request.getAisleId());

        // Validate aisle ID uniqueness
        if (aisleRepository.existsByAisleId(request.getAisleId())) {
            throw new InvalidOperationException("Aisle ID already exists: " + request.getAisleId());
        }

        // Validate zone exists
        Zone zone = zoneRepository.findById(request.getZoneId())
                .orElseThrow(() -> new ResourceNotFoundException("Zone not found with ID: " + request.getZoneId()));

        // Create aisle
        Aisle aisle = aisleMapper.toEntity(request);
        aisle.setZone(zone);
        aisle.setTotalRacks(0);

        Aisle savedAisle = aisleRepository.save(aisle);

        // Update zone total aisles count
        long totalAisles = aisleRepository.countByZoneId(zone.getId());
        zone.setTotalAisles((int) totalAisles);
        zoneRepository.save(zone);

        log.info("✅ Aisle created: {} in zone: {}", savedAisle.getAisleId(), zone.getZoneId());
        return aisleMapper.toResponse(savedAisle);
    }

    // ====== Read ======

    @Override
    public AisleResponse getAisleById(Long id) {
        Aisle aisle = aisleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Aisle not found with ID: " + id));
        return aisleMapper.toResponse(aisle);
    }

    @Override
    public AisleResponse getAisleByAisleId(String aisleId) {
        Aisle aisle = aisleRepository.findByAisleId(aisleId)
                .orElseThrow(() -> new ResourceNotFoundException("Aisle not found: " + aisleId));
        return aisleMapper.toResponse(aisle);
    }

    @Override
    public Page<AisleResponse> getAllAisles(Pageable pageable, String search, Long zoneId) {
        if (search != null && !search.isEmpty() && zoneId != null) {
            return aisleRepository.searchAislesByZone(zoneId, search, pageable)
                    .map(aisleMapper::toResponse);
        } else if (search != null && !search.isEmpty()) {
            return aisleRepository.searchAisles(search, pageable)
                    .map(aisleMapper::toResponse);
        } else if (zoneId != null) {
            return aisleRepository.findByZoneId(zoneId, pageable)
                    .map(aisleMapper::toResponse);
        }
        return aisleRepository.findAll(pageable)
                .map(aisleMapper::toResponse);
    }

    @Override
    public List<AisleResponse> getAislesByZone(Long zoneId) {
        List<Aisle> aisles = aisleRepository.findByZoneId(zoneId);
        return aisles.stream()
                .map(aisleMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<AisleResponse> getActiveAislesByZone(Long zoneId) {
        List<Aisle> aisles = aisleRepository.findByZoneIdAndIsActiveTrue(zoneId);
        return aisles.stream()
                .map(aisleMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<AisleResponse> getAislesByZoneId(String zoneId) {
        List<Aisle> aisles = aisleRepository.findByZoneZoneId(zoneId);
        return aisles.stream()
                .map(aisleMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<AisleResponse> getAislesByWarehouse(Long warehouseId) {
        List<Aisle> aisles = aisleRepository.findByWarehouseId(warehouseId);
        return aisles.stream()
                .map(aisleMapper::toResponse)
                .collect(Collectors.toList());
    }

    // ====== Update ======

    @Override
    public AisleResponse updateAisle(Long id, AisleRequest request) {
        log.info("📦 Updating aisle: {}", id);

        Aisle aisle = aisleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Aisle not found with ID: " + id));

        // Check uniqueness if aisleId is changed
        if (!request.getAisleId().equals(aisle.getAisleId()) &&
            aisleRepository.existsByAisleId(request.getAisleId())) {
            throw new InvalidOperationException("Aisle ID already exists: " + request.getAisleId());
        }

        // Update zone if changed
        if (!request.getZoneId().equals(aisle.getZone().getId())) {
            Zone newZone = zoneRepository.findById(request.getZoneId())
                    .orElseThrow(() -> new ResourceNotFoundException("Zone not found with ID: " + request.getZoneId()));
            
            // Update old zone total aisles
            Zone oldZone = aisle.getZone();
            long oldCount = aisleRepository.countByZoneId(oldZone.getId());
            oldZone.setTotalAisles((int) oldCount);
            zoneRepository.save(oldZone);

            // Update new zone total aisles
            aisle.setZone(newZone);
            long newCount = aisleRepository.countByZoneId(newZone.getId());
            newZone.setTotalAisles((int) newCount);
            zoneRepository.save(newZone);
        }

        // Update aisle fields
        aisleMapper.updateEntity(aisle, request);

        Aisle updatedAisle = aisleRepository.save(aisle);
        log.info("✅ Aisle updated: {}", updatedAisle.getAisleId());

        return aisleMapper.toResponse(updatedAisle);
    }

    @Override
    public AisleResponse toggleAisleStatus(Long id, Boolean isActive) {
        log.info("📦 Toggling aisle status: {} to {}", id, isActive);

        Aisle aisle = aisleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Aisle not found with ID: " + id));

        aisle.setIsActive(isActive);
        Aisle updatedAisle = aisleRepository.save(aisle);

        log.info("✅ Aisle status updated: {} -> {}", aisle.getAisleId(), isActive);
        return aisleMapper.toResponse(updatedAisle);
    }

    // ====== Delete ======

    @Override
    public void deleteAisle(Long id) {
        log.info("📦 Deleting aisle: {}", id);

        Aisle aisle = aisleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Aisle not found with ID: " + id));

        // Soft delete
        aisle.setIsActive(false);
        aisleRepository.save(aisle);

        // Update zone total aisles
        Zone zone = aisle.getZone();
        long totalAisles = aisleRepository.countByZoneId(zone.getId());
        zone.setTotalAisles((int) totalAisles);
        zoneRepository.save(zone);

        log.info("✅ Aisle deactivated: {}", id);
    }

    @Override
    public void deleteAisleByAisleId(String aisleId) {
        log.info("📦 Deleting aisle by aisleId: {}", aisleId);

        Aisle aisle = aisleRepository.findByAisleId(aisleId)
                .orElseThrow(() -> new ResourceNotFoundException("Aisle not found: " + aisleId));

        aisle.setIsActive(false);
        aisleRepository.save(aisle);

        // Update zone total aisles
        Zone zone = aisle.getZone();
        long totalAisles = aisleRepository.countByZoneId(zone.getId());
        zone.setTotalAisles((int) totalAisles);
        zoneRepository.save(zone);

        log.info("✅ Aisle deactivated: {}", aisleId);
    }
}