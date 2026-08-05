// ====== FILE: src/main/java/com/warehouse/wms/service/impl/TrolleyServiceImpl.java ======
package com.warehouse.wms.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.warehouse.wms.dto.request.TrolleyRequest;
import com.warehouse.wms.dto.response.TrolleyResponse;
import com.warehouse.wms.entity.Trolley;
import com.warehouse.wms.exception.InvalidOperationException;
import com.warehouse.wms.exception.ResourceNotFoundException;
import com.warehouse.wms.mapper.TrolleyMapper;
import com.warehouse.wms.repository.TrolleyRepository;
import com.warehouse.wms.service.TrolleyService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TrolleyServiceImpl implements TrolleyService {

    private final TrolleyRepository trolleyRepository;
    private final TrolleyMapper trolleyMapper;

    // ====== Create ======

    @Override
    public TrolleyResponse createTrolley(TrolleyRequest request) {
        log.info("📦 Creating trolley: {}", request.getTrolleyIdentifier());

        // Validate trolley identifier uniqueness
        if (trolleyRepository.existsByTrolleyIdentifier(request.getTrolleyIdentifier())) {
            throw new InvalidOperationException("Trolley identifier already exists: " + request.getTrolleyIdentifier());
        }

        // Create trolley
        Trolley trolley = trolleyMapper.toEntity(request);
        trolley.setCurrentLoad(0);
        trolley.setStatus(request.getStatus() != null ? request.getStatus() : "AVAILABLE");

        Trolley savedTrolley = trolleyRepository.save(trolley);
        log.info("✅ Trolley created: {}", savedTrolley.getTrolleyIdentifier());

        return trolleyMapper.toResponse(savedTrolley);
    }

    // ====== Read ======

    @Override
    public TrolleyResponse getTrolleyById(Long id) {
        Trolley trolley = trolleyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trolley not found with ID: " + id));
        return trolleyMapper.toResponse(trolley);
    }

    @Override
    public TrolleyResponse getTrolleyByIdentifier(String trolleyIdentifier) {
        Trolley trolley = trolleyRepository.findByTrolleyIdentifier(trolleyIdentifier)
                .orElseThrow(() -> new ResourceNotFoundException("Trolley not found: " + trolleyIdentifier));
        return trolleyMapper.toResponse(trolley);
    }

    @Override
    public Page<TrolleyResponse> getAllTrolleys(Pageable pageable, String search, String status) {
        if (search != null && !search.isEmpty() && status != null && !status.isEmpty()) {
            return trolleyRepository.searchTrolleysByStatus(search, status, pageable)
                    .map(trolleyMapper::toResponse);
        } else if (search != null && !search.isEmpty()) {
            return trolleyRepository.searchTrolleys(search, pageable)
                    .map(trolleyMapper::toResponse);
        } else if (status != null && !status.isEmpty()) {
            return trolleyRepository.findByStatus(status, pageable)
                    .map(trolleyMapper::toResponse);
        }
        return trolleyRepository.findAll(pageable)
                .map(trolleyMapper::toResponse);
    }

    @Override
    public List<TrolleyResponse> getTrolleysByStatus(String status) {
        List<Trolley> trolleys = trolleyRepository.findByStatus(status);
        return trolleys.stream()
                .map(trolleyMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<TrolleyResponse> getTrolleysByType(String trolleyType) {
        List<Trolley> trolleys = trolleyRepository.findByTrolleyType(trolleyType);
        return trolleys.stream()
                .map(trolleyMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<TrolleyResponse> getAvailableTrolleys(Integer requiredWeight) {
        List<Trolley> trolleys;
        if (requiredWeight != null && requiredWeight > 0) {
            trolleys = trolleyRepository.findAvailableTrolleys(requiredWeight);
        } else {
            trolleys = trolleyRepository.findTrolleysWithAvailableCapacity();
        }
        return trolleys.stream()
                .map(trolleyMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<TrolleyResponse> getTrolleysDueForMaintenance() {
        List<Trolley> trolleys = trolleyRepository.findTrolleysDueForMaintenance(LocalDateTime.now());
        return trolleys.stream()
                .map(trolleyMapper::toResponse)
                .collect(Collectors.toList());
    }

    // ====== Update ======

    @Override
    public TrolleyResponse updateTrolley(Long id, TrolleyRequest request) {
        log.info("📦 Updating trolley: {}", id);

        Trolley trolley = trolleyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trolley not found with ID: " + id));

        // Check uniqueness if trolley identifier is changed
        if (!request.getTrolleyIdentifier().equals(trolley.getTrolleyIdentifier()) &&
            trolleyRepository.existsByTrolleyIdentifier(request.getTrolleyIdentifier())) {
            throw new InvalidOperationException("Trolley identifier already exists: " + request.getTrolleyIdentifier());
        }

        // Update trolley fields
        trolleyMapper.updateEntity(trolley, request);

        Trolley updatedTrolley = trolleyRepository.save(trolley);
        log.info("✅ Trolley updated: {}", updatedTrolley.getTrolleyIdentifier());

        return trolleyMapper.toResponse(updatedTrolley);
    }

    @Override
    public TrolleyResponse updateTrolleyStatus(Long id, String status) {
        log.info("📦 Updating trolley status: {} to {}", id, status);

        Trolley trolley = trolleyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trolley not found with ID: " + id));

        trolley.setStatus(status);
        Trolley updatedTrolley = trolleyRepository.save(trolley);

        log.info("✅ Trolley status updated: {} -> {}", trolley.getTrolleyIdentifier(), status);
        return trolleyMapper.toResponse(updatedTrolley);
    }

    @Override
    public TrolleyResponse addTrolleyLoad(Long id, Integer weight) {
        log.info("📦 Adding load to trolley: {} - {}", id, weight);

        Trolley trolley = trolleyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trolley not found with ID: " + id));

        if (!trolley.hasCapacity(weight)) {
            throw new InvalidOperationException("Insufficient capacity on trolley: " + trolley.getTrolleyIdentifier() +
                    ". Available: " + (trolley.getCapacity() - trolley.getCurrentLoad()) + 
                    ", Required: " + weight);
        }

        int updated = trolleyRepository.addLoad(id, weight);
        if (updated == 0) {
            throw new InvalidOperationException("Failed to add load to trolley.");
        }

        Trolley updatedTrolley = trolleyRepository.findById(id).get();
        log.info("✅ Load added to trolley: {} - Current load: {}", 
                 updatedTrolley.getTrolleyIdentifier(), updatedTrolley.getCurrentLoad());

        return trolleyMapper.toResponse(updatedTrolley);
    }

    @Override
    public TrolleyResponse removeTrolleyLoad(Long id, Integer weight) {
        log.info("📦 Removing load from trolley: {} - {}", id, weight);

        Trolley trolley = trolleyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trolley not found with ID: " + id));

        if (trolley.getCurrentLoad() < weight) {
            throw new InvalidOperationException("Cannot remove more than current load. Current load: " +
                    trolley.getCurrentLoad() + ", Requested: " + weight);
        }

        int updated = trolleyRepository.removeLoad(id, weight);
        if (updated == 0) {
            throw new InvalidOperationException("Failed to remove load from trolley.");
        }

        Trolley updatedTrolley = trolleyRepository.findById(id).get();
        
        // Update status if load becomes 0
        if (updatedTrolley.getCurrentLoad() == 0) {
            updatedTrolley.setStatus("AVAILABLE");
            trolleyRepository.save(updatedTrolley);
        }

        log.info("✅ Load removed from trolley: {} - Current load: {}", 
                 updatedTrolley.getTrolleyIdentifier(), updatedTrolley.getCurrentLoad());

        return trolleyMapper.toResponse(updatedTrolley);
    }

    @Override
    public TrolleyResponse toggleTrolleyStatus(Long id, Boolean isActive) {
        log.info("📦 Toggling trolley status: {} to {}", id, isActive);

        Trolley trolley = trolleyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trolley not found with ID: " + id));

        trolley.setIsActive(isActive);
        Trolley updatedTrolley = trolleyRepository.save(trolley);

        log.info("✅ Trolley status updated: {} -> {}", trolley.getTrolleyIdentifier(), isActive);
        return trolleyMapper.toResponse(updatedTrolley);
    }

    // ====== Delete ======

    @Override
    public void deleteTrolley(Long id) {
        log.info("📦 Deleting trolley: {}", id);

        Trolley trolley = trolleyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trolley not found with ID: " + id));

        // Soft delete
        trolley.setIsActive(false);
        trolleyRepository.save(trolley);

        log.info("✅ Trolley deactivated: {}", id);
    }

    @Override
    public void deleteTrolleyByIdentifier(String trolleyIdentifier) {
        log.info("📦 Deleting trolley by identifier: {}", trolleyIdentifier);

        Trolley trolley = trolleyRepository.findByTrolleyIdentifier(trolleyIdentifier)
                .orElseThrow(() -> new ResourceNotFoundException("Trolley not found: " + trolleyIdentifier));

        trolley.setIsActive(false);
        trolleyRepository.save(trolley);

        log.info("✅ Trolley deactivated: {}", trolleyIdentifier);
    }
}