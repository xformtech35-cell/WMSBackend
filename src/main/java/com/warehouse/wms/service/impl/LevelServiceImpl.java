// ====== FILE: src/main/java/com/warehouse/wms/service/impl/LevelServiceImpl.java ======
package com.warehouse.wms.service.impl;

import com.warehouse.wms.dto.request.LevelRequest;
import com.warehouse.wms.dto.response.LevelResponse;
import com.warehouse.wms.entity.Level;
import com.warehouse.wms.entity.Rack;
import com.warehouse.wms.exception.InvalidOperationException;
import com.warehouse.wms.exception.ResourceNotFoundException;
import com.warehouse.wms.mapper.LevelMapper;
import com.warehouse.wms.repository.LevelRepository;
import com.warehouse.wms.repository.RackRepository;
import com.warehouse.wms.service.LevelService;
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
public class LevelServiceImpl implements LevelService {

    private final LevelRepository levelRepository;
    private final RackRepository rackRepository;
    private final LevelMapper levelMapper;
    private final LevelBarcodeServiceImpl levelBarcodeServiceImpl;

    // ====== Create ======

    @Override
    public LevelResponse createLevel(LevelRequest request) {
        log.info("📦 Creating level: {}", request.getLevelId());

        // Validate level ID uniqueness
        if (levelRepository.existsByLevelId(request.getLevelId())) {
            throw new InvalidOperationException("Level ID already exists: " + request.getLevelId());
        }

        // Validate rack exists
        Rack rack = rackRepository.findById(request.getRackId())
                .orElseThrow(() -> new ResourceNotFoundException("Rack not found with ID: " + request.getRackId()));

        // Create level
        Level level = levelMapper.toEntity(request);
        level.setRack(rack);

        Level savedLevel = levelRepository.save(level);

        // Update rack total shelves
        long totalShelves = levelRepository.countByRackId(rack.getId());
        rack.setTotalShelves((int) totalShelves);
        rackRepository.save(rack);
        
        
        levelBarcodeServiceImpl.generateLevelBarcode(savedLevel.getRack().getAisle().getZone().getWarehouse().getWarehouseId(),savedLevel.getRack().getAisle().getZone().getZoneId(),savedLevel.getRack().getAisle().getAisleId(),savedLevel.getRack().getRackId(),savedLevel.getLevelId());

        log.info("✅ Level created: {} in rack: {}", savedLevel.getLevelId(), rack.getRackId());
        return levelMapper.toResponse(savedLevel);
    }

    // ====== Read ======

    @Override
    public LevelResponse getLevelById(Long id) {
        Level level = levelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Level not found with ID: " + id));
        return levelMapper.toResponse(level);
    }

    @Override
    public LevelResponse getLevelByLevelId(String levelId) {
        Level level = levelRepository.findByLevelId(levelId)
                .orElseThrow(() -> new ResourceNotFoundException("Level not found: " + levelId));
        return levelMapper.toResponse(level);
    }

    @Override
    public Page<LevelResponse> getAllLevels(Pageable pageable, String search, Long rackId) {
        if (search != null && !search.isEmpty()) {
            // Search by level ID or name
            return levelRepository.findAll(pageable)
                    .map(levelMapper::toResponse);
        } else if (rackId != null) {
            return levelRepository.findByRackId(rackId, pageable)
                    .map(levelMapper::toResponse);
        }
        return levelRepository.findAll(pageable)
                .map(levelMapper::toResponse);
    }

    @Override
    public List<LevelResponse> getLevelsByRack(Long rackId) {
        List<Level> levels = levelRepository.findByRackId(rackId);
        return levels.stream()
                .map(levelMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<LevelResponse> getActiveLevelsByRack(Long rackId) {
        List<Level> levels = levelRepository.findByRackIdAndIsActiveTrue(rackId);
        return levels.stream()
                .map(levelMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<LevelResponse> getLevelsByRackOrdered(Long rackId) {
        List<Level> levels = levelRepository.findByRackIdOrderByLevelNumberAsc(rackId);
        return levels.stream()
                .map(levelMapper::toResponse)
                .collect(Collectors.toList());
    }

    // ====== Update ======

    @Override
    public LevelResponse updateLevel(Long id, LevelRequest request) {
        log.info("📦 Updating level: {}", id);

        Level level = levelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Level not found with ID: " + id));

        // Check uniqueness if levelId is changed
        if (!request.getLevelId().equals(level.getLevelId()) &&
            levelRepository.existsByLevelId(request.getLevelId())) {
            throw new InvalidOperationException("Level ID already exists: " + request.getLevelId());
        }

        // Update rack if changed
        if (!request.getRackId().equals(level.getRack().getId())) {
            Rack newRack = rackRepository.findById(request.getRackId())
                    .orElseThrow(() -> new ResourceNotFoundException("Rack not found with ID: " + request.getRackId()));
            
            // Update old rack total shelves
            Rack oldRack = level.getRack();
            long oldCount = levelRepository.countByRackId(oldRack.getId());
            oldRack.setTotalShelves((int) oldCount);
            rackRepository.save(oldRack);

            // Update new rack total shelves
            level.setRack(newRack);
            long newCount = levelRepository.countByRackId(newRack.getId());
            newRack.setTotalShelves((int) newCount);
            rackRepository.save(newRack);
        }

        // Update level fields
        levelMapper.updateEntity(level, request);

        Level updatedLevel = levelRepository.save(level);
        log.info("✅ Level updated: {}", updatedLevel.getLevelId());

        return levelMapper.toResponse(updatedLevel);
    }

    @Override
    public LevelResponse toggleLevelStatus(Long id, Boolean isActive) {
        log.info("📦 Toggling level status: {} to {}", id, isActive);

        Level level = levelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Level not found with ID: " + id));

        level.setIsActive(isActive);
        Level updatedLevel = levelRepository.save(level);

        log.info("✅ Level status updated: {} -> {}", level.getLevelId(), isActive);
        return levelMapper.toResponse(updatedLevel);
    }

    // ====== Delete ======

    @Override
    public void deleteLevel(Long id) {
        log.info("📦 Deleting level: {}", id);

        Level level = levelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Level not found with ID: " + id));

        // Soft delete
        level.setIsActive(false);
        levelRepository.save(level);

        // Update rack total shelves
        Rack rack = level.getRack();
        long totalShelves = levelRepository.countByRackId(rack.getId());
        rack.setTotalShelves((int) totalShelves);
        rackRepository.save(rack);

        log.info("✅ Level deactivated: {}", id);
    }

    @Override
    public void deleteLevelByLevelId(String levelId) {
        log.info("📦 Deleting level by levelId: {}", levelId);

        Level level = levelRepository.findByLevelId(levelId)
                .orElseThrow(() -> new ResourceNotFoundException("Level not found: " + levelId));

        level.setIsActive(false);
        levelRepository.save(level);

        // Update rack total shelves
        Rack rack = level.getRack();
        long totalShelves = levelRepository.countByRackId(rack.getId());
        rack.setTotalShelves((int) totalShelves);
        rackRepository.save(rack);

        log.info("✅ Level deactivated: {}", levelId);
    }
}