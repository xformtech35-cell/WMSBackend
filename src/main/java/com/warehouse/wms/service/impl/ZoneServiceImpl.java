// ====== FILE: src/main/java/com/warehouse/wms/service/impl/ZoneServiceImpl.java ======
package com.warehouse.wms.service.impl;

import com.warehouse.wms.dto.request.ZoneRequest;
import com.warehouse.wms.dto.response.ZoneResponse;
import com.warehouse.wms.entity.Warehouse;
import com.warehouse.wms.entity.Zone;
import com.warehouse.wms.exception.InvalidOperationException;
import com.warehouse.wms.exception.ResourceNotFoundException;
import com.warehouse.wms.mapper.ZoneMapper;
import com.warehouse.wms.repository.WarehouseRepository;
import com.warehouse.wms.repository.ZoneRepository;
import com.warehouse.wms.service.ZoneService;
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
public class ZoneServiceImpl implements ZoneService {

    private final ZoneRepository zoneRepository;
    private final WarehouseRepository warehouseRepository;
    private final ZoneMapper zoneMapper;

    // ====== Create ======

    @Override
    public ZoneResponse createZone(ZoneRequest request) {
        log.info("📦 Creating zone: {}", request.getZoneId());

        // Validate zone ID uniqueness
        if (zoneRepository.existsByZoneId(request.getZoneId())) {
            throw new InvalidOperationException("Zone ID already exists: " + request.getZoneId());
        }

        // Validate warehouse exists
        Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId())
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found with ID: " + request.getWarehouseId()));

        // Create zone
        Zone zone = zoneMapper.toEntity(request);
        zone.setWarehouse(warehouse);
        zone.setTotalAisles(0);

        Zone savedZone = zoneRepository.save(zone);

        // Update warehouse total zones count
        long totalZones = zoneRepository.countByWarehouseId(warehouse.getId());
        warehouse.setTotalZones((int) totalZones);
        warehouseRepository.save(warehouse);

        log.info("✅ Zone created: {} in warehouse: {}", savedZone.getZoneId(), warehouse.getWarehouseId());
        return zoneMapper.toResponse(savedZone);
    }

    // ====== Read ======

    @Override
    public ZoneResponse getZoneById(Long id) {
        Zone zone = zoneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Zone not found with ID: " + id));
        return zoneMapper.toResponse(zone);
    }

    @Override
    public ZoneResponse getZoneByZoneId(String zoneId) {
        Zone zone = zoneRepository.findByZoneId(zoneId)
                .orElseThrow(() -> new ResourceNotFoundException("Zone not found: " + zoneId));
        return zoneMapper.toResponse(zone);
    }

    @Override
    public Page<ZoneResponse> getAllZones(Pageable pageable, String search, Long warehouseId) {
        if (search != null && !search.isEmpty() && warehouseId != null) {
            return zoneRepository.searchZonesByWarehouse(warehouseId, search, pageable)
                    .map(zoneMapper::toResponse);
        } else if (search != null && !search.isEmpty()) {
            return zoneRepository.searchZones(search, pageable)
                    .map(zoneMapper::toResponse);
        } else if (warehouseId != null) {
            return zoneRepository.findByWarehouseId(warehouseId, pageable)
                    .map(zoneMapper::toResponse);
        }
        return zoneRepository.findAll(pageable)
                .map(zoneMapper::toResponse);
    }

    @Override
    public List<ZoneResponse> getZonesByWarehouse(Long warehouseId) {
        List<Zone> zones = zoneRepository.findByWarehouseId(warehouseId);
        return zones.stream()
                .map(zoneMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ZoneResponse> getActiveZonesByWarehouse(Long warehouseId) {
        List<Zone> zones = zoneRepository.findByWarehouseIdAndIsActiveTrue(warehouseId);
        return zones.stream()
                .map(zoneMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ZoneResponse> getZonesByType(String zoneType) {
        List<Zone> zones = zoneRepository.findByZoneType(zoneType);
        return zones.stream()
                .map(zoneMapper::toResponse)
                .collect(Collectors.toList());
    }

    // ====== Update ======

    @Override
    public ZoneResponse updateZone(Long id, ZoneRequest request) {
        log.info("📦 Updating zone: {}", id);

        Zone zone = zoneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Zone not found with ID: " + id));

        // Check uniqueness if zoneId is changed
        if (!request.getZoneId().equals(zone.getZoneId()) &&
            zoneRepository.existsByZoneId(request.getZoneId())) {
            throw new InvalidOperationException("Zone ID already exists: " + request.getZoneId());
        }

        // Update warehouse if changed
        if (!request.getWarehouseId().equals(zone.getWarehouse().getId())) {
            Warehouse newWarehouse = warehouseRepository.findById(request.getWarehouseId())
                    .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found with ID: " + request.getWarehouseId()));
            
            // Update old warehouse total zones
            Warehouse oldWarehouse = zone.getWarehouse();
            long oldCount = zoneRepository.countByWarehouseId(oldWarehouse.getId());
            oldWarehouse.setTotalZones((int) oldCount);
            warehouseRepository.save(oldWarehouse);

            // Update new warehouse total zones
            zone.setWarehouse(newWarehouse);
            long newCount = zoneRepository.countByWarehouseId(newWarehouse.getId());
            newWarehouse.setTotalZones((int) newCount);
            warehouseRepository.save(newWarehouse);
        }

        // Update zone fields
        zoneMapper.updateEntity(zone, request);

        Zone updatedZone = zoneRepository.save(zone);
        log.info("✅ Zone updated: {}", updatedZone.getZoneId());

        return zoneMapper.toResponse(updatedZone);
    }

    @Override
    public ZoneResponse toggleZoneStatus(Long id, Boolean isActive) {
        log.info("📦 Toggling zone status: {} to {}", id, isActive);

        Zone zone = zoneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Zone not found with ID: " + id));

        zone.setIsActive(isActive);
        Zone updatedZone = zoneRepository.save(zone);

        log.info("✅ Zone status updated: {} -> {}", zone.getZoneId(), isActive);
        return zoneMapper.toResponse(updatedZone);
    }

    // ====== Delete ======

    @Override
    public void deleteZone(Long id) {
        log.info("📦 Deleting zone: {}", id);

        Zone zone = zoneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Zone not found with ID: " + id));

        // Soft delete
        zone.setIsActive(false);
        zoneRepository.save(zone);

        // Update warehouse total zones
        Warehouse warehouse = zone.getWarehouse();
        long totalZones = zoneRepository.countByWarehouseId(warehouse.getId());
        warehouse.setTotalZones((int) totalZones);
        warehouseRepository.save(warehouse);

        log.info("✅ Zone deactivated: {}", id);
    }

    @Override
    public void deleteZoneByZoneId(String zoneId) {
        log.info("📦 Deleting zone by zoneId: {}", zoneId);

        Zone zone = zoneRepository.findByZoneId(zoneId)
                .orElseThrow(() -> new ResourceNotFoundException("Zone not found: " + zoneId));

        zone.setIsActive(false);
        zoneRepository.save(zone);

        // Update warehouse total zones
        Warehouse warehouse = zone.getWarehouse();
        long totalZones = zoneRepository.countByWarehouseId(warehouse.getId());
        warehouse.setTotalZones((int) totalZones);
        warehouseRepository.save(warehouse);

        log.info("✅ Zone deactivated: {}", zoneId);
    }
}