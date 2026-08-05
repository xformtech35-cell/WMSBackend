// ====== FILE: src/main/java/com/warehouse/wms/service/impl/RackCompartmentServiceImpl.java ======
package com.warehouse.wms.service.impl;

import com.warehouse.wms.dto.request.RackCompartmentRequest;
import com.warehouse.wms.dto.response.RackCompartmentResponse;
import com.warehouse.wms.entity.Rack;
import com.warehouse.wms.entity.RackCompartment;
import com.warehouse.wms.entity.SalesOrder;
import com.warehouse.wms.entity.Trolley;
import com.warehouse.wms.exception.InvalidOperationException;
import com.warehouse.wms.exception.ResourceNotFoundException;
import com.warehouse.wms.mapper.RackCompartmentMapper;
import com.warehouse.wms.repository.RackCompartmentRepository;
import com.warehouse.wms.repository.RackRepository;
import com.warehouse.wms.repository.SalesOrderRepository;
import com.warehouse.wms.repository.TrolleyRepository;
import com.warehouse.wms.service.RackCompartmentService;
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
public class RackCompartmentServiceImpl implements RackCompartmentService {

    private final RackCompartmentRepository compartmentRepository;
    private final RackRepository rackRepository;
    private final TrolleyRepository trolleyRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final RackCompartmentMapper compartmentMapper;

    // ====== Create ======

    @Override
    public RackCompartmentResponse createCompartment(RackCompartmentRequest request) {
        log.info("📦 Creating rack compartment: {}", request.getCompartmentId());

        // Validate compartment ID uniqueness
        if (compartmentRepository.existsByCompartmentId(request.getCompartmentId())) {
            throw new InvalidOperationException("Compartment ID already exists: " + request.getCompartmentId());
        }

        // Validate rack exists
        Rack rack = rackRepository.findById(request.getRackId())
                .orElseThrow(() -> new ResourceNotFoundException("Rack not found with ID: " + request.getRackId()));

        // Create compartment
        RackCompartment compartment = compartmentMapper.toEntity(request);
        compartment.setRack(rack);
        compartment.setAvailableCapacity(request.getCapacity());
        compartment.setUsedCapacity(0);

        // Set optional relationships
        if (request.getTrolleyId() != null) {
            Trolley trolley = trolleyRepository.findById(request.getTrolleyId())
                    .orElseThrow(() -> new ResourceNotFoundException("Trolley not found with ID: " + request.getTrolleyId()));
            compartment.setTrolley(trolley);
        }

        if (request.getSalesOrderId() != null) {
            SalesOrder salesOrder = salesOrderRepository.findById(request.getSalesOrderId())
                    .orElseThrow(() -> new ResourceNotFoundException("Sales order not found with ID: " + request.getSalesOrderId()));
            compartment.setSalesOrder(salesOrder);
        }

        RackCompartment savedCompartment = compartmentRepository.save(compartment);

        // Update rack total shelves
        long totalShelves = compartmentRepository.countByRackId(rack.getId());
        rack.setTotalShelves((int) totalShelves);
        rackRepository.save(rack);

        log.info("✅ Rack compartment created: {}", savedCompartment.getCompartmentId());
        return compartmentMapper.toResponse(savedCompartment);
    }

    // ====== Read ======

    @Override
    public RackCompartmentResponse getCompartmentById(Long id) {
        RackCompartment compartment = compartmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Compartment not found with ID: " + id));
        return compartmentMapper.toResponse(compartment);
    }

    @Override
    public RackCompartmentResponse getCompartmentByCompartmentId(String compartmentId) {
        RackCompartment compartment = compartmentRepository.findByCompartmentId(compartmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Compartment not found: " + compartmentId));
        return compartmentMapper.toResponse(compartment);
    }

    @Override
    public Page<RackCompartmentResponse> getAllCompartments(Pageable pageable, String search, Long rackId) {
        if (search != null && !search.isEmpty() && rackId != null) {
            return compartmentRepository.searchCompartmentsByRack(rackId, search, pageable)
                    .map(compartmentMapper::toResponse);
        } else if (search != null && !search.isEmpty()) {
            return compartmentRepository.searchCompartments(search, pageable)
                    .map(compartmentMapper::toResponse);
        } else if (rackId != null) {
            return compartmentRepository.findByRackId(rackId, pageable)
                    .map(compartmentMapper::toResponse);
        }
        return compartmentRepository.findAll(pageable)
                .map(compartmentMapper::toResponse);
    }

    @Override
    public List<RackCompartmentResponse> getCompartmentsByRack(Long rackId) {
        List<RackCompartment> compartments = compartmentRepository.findByRackId(rackId);
        return compartments.stream()
                .map(compartmentMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<RackCompartmentResponse> getActiveCompartmentsByRack(Long rackId) {
        List<RackCompartment> compartments = compartmentRepository.findByRackIdAndIsActiveTrue(rackId);
        return compartments.stream()
                .map(compartmentMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<RackCompartmentResponse> getCompartmentsByTrolley(Long trolleyId) {
        List<RackCompartment> compartments = compartmentRepository.findByTrolleyId(trolleyId);
        return compartments.stream()
                .map(compartmentMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<RackCompartmentResponse> getCompartmentsBySalesOrder(Long salesOrderId) {
        List<RackCompartment> compartments = compartmentRepository.findBySalesOrderId(salesOrderId);
        return compartments.stream()
                .map(compartmentMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<RackCompartmentResponse> getAvailableCompartments(Long rackId, Integer requiredCapacity) {
        List<RackCompartment> compartments = compartmentRepository.findAvailableCompartments(rackId, requiredCapacity);
        return compartments.stream()
                .map(compartmentMapper::toResponse)
                .collect(Collectors.toList());
    }

    // ====== Update ======

    @Override
    public RackCompartmentResponse updateCompartment(Long id, RackCompartmentRequest request) {
        log.info("📦 Updating rack compartment: {}", id);

        RackCompartment compartment = compartmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Compartment not found with ID: " + id));

        // Check uniqueness if compartmentId is changed
        if (!request.getCompartmentId().equals(compartment.getCompartmentId()) &&
            compartmentRepository.existsByCompartmentId(request.getCompartmentId())) {
            throw new InvalidOperationException("Compartment ID already exists: " + request.getCompartmentId());
        }

        // Update rack if changed
        if (!request.getRackId().equals(compartment.getRack().getId())) {
            Rack newRack = rackRepository.findById(request.getRackId())
                    .orElseThrow(() -> new ResourceNotFoundException("Rack not found with ID: " + request.getRackId()));
            
            // Update old rack total shelves
            Rack oldRack = compartment.getRack();
            long oldCount = compartmentRepository.countByRackId(oldRack.getId());
            oldRack.setTotalShelves((int) oldCount);
            rackRepository.save(oldRack);

            // Update new rack total shelves
            compartment.setRack(newRack);
            long newCount = compartmentRepository.countByRackId(newRack.getId());
            newRack.setTotalShelves((int) newCount);
            rackRepository.save(newRack);
        }

        // Update optional relationships
        if (request.getTrolleyId() != null) {
            Trolley trolley = trolleyRepository.findById(request.getTrolleyId())
                    .orElseThrow(() -> new ResourceNotFoundException("Trolley not found with ID: " + request.getTrolleyId()));
            compartment.setTrolley(trolley);
        } else {
            compartment.setTrolley(null);
        }

        if (request.getSalesOrderId() != null) {
            SalesOrder salesOrder = salesOrderRepository.findById(request.getSalesOrderId())
                    .orElseThrow(() -> new ResourceNotFoundException("Sales order not found with ID: " + request.getSalesOrderId()));
            compartment.setSalesOrder(salesOrder);
        } else {
            compartment.setSalesOrder(null);
        }

        // Update compartment fields
        compartmentMapper.updateEntity(compartment, request);

        // Update capacity if changed
        if (request.getCapacity() != null && request.getCapacity() > compartment.getAvailableCapacity()) {
            int additionalCapacity = request.getCapacity() - compartment.getCapacity();
            compartment.setAvailableCapacity(compartment.getAvailableCapacity() + additionalCapacity);
            compartment.setCapacity(request.getCapacity());
        }

        RackCompartment updatedCompartment = compartmentRepository.save(compartment);
        log.info("✅ Rack compartment updated: {}", updatedCompartment.getCompartmentId());

        return compartmentMapper.toResponse(updatedCompartment);
    }

    @Override
    public RackCompartmentResponse toggleCompartmentStatus(Long id, Boolean isActive) {
        log.info("📦 Toggling compartment status: {} to {}", id, isActive);

        RackCompartment compartment = compartmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Compartment not found with ID: " + id));

        compartment.setIsActive(isActive);
        RackCompartment updatedCompartment = compartmentRepository.save(compartment);

        log.info("✅ Compartment status updated: {} -> {}", compartment.getCompartmentId(), isActive);
        return compartmentMapper.toResponse(updatedCompartment);
    }

    @Override
    public RackCompartmentResponse allocateCapacity(Long id, Integer quantity) {
        log.info("📦 Allocating {} capacity to compartment: {}", quantity, id);

        RackCompartment compartment = compartmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Compartment not found with ID: " + id));

        if (!compartment.hasCapacity(quantity)) {
            throw new InvalidOperationException("Insufficient capacity in compartment: " + compartment.getCompartmentId() +
                    ". Available: " + compartment.getAvailableCapacity() + ", Required: " + quantity);
        }

        int updated = compartmentRepository.allocateCapacity(id, quantity);
        if (updated == 0) {
            throw new InvalidOperationException("Failed to allocate capacity. Compartment may not have enough space.");
        }

        RackCompartment updatedCompartment = compartmentRepository.findById(id).get();
        log.info("✅ Allocated {} capacity to compartment: {}", quantity, updatedCompartment.getCompartmentId());

        return compartmentMapper.toResponse(updatedCompartment);
    }

    @Override
    public RackCompartmentResponse releaseCapacity(Long id, Integer quantity) {
        log.info("📦 Releasing {} capacity from compartment: {}", quantity, id);

        RackCompartment compartment = compartmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Compartment not found with ID: " + id));

        if (compartment.getUsedCapacity() < quantity) {
            throw new InvalidOperationException("Cannot release more than used capacity. Used: " +
                    compartment.getUsedCapacity() + ", Requested: " + quantity);
        }

        int updated = compartmentRepository.releaseCapacity(id, quantity);
        if (updated == 0) {
            throw new InvalidOperationException("Failed to release capacity.");
        }

        RackCompartment updatedCompartment = compartmentRepository.findById(id).get();
        log.info("✅ Released {} capacity from compartment: {}", quantity, updatedCompartment.getCompartmentId());

        return compartmentMapper.toResponse(updatedCompartment);
    }

    // ====== Delete ======

    @Override
    public void deleteCompartment(Long id) {
        log.info("📦 Deleting compartment: {}", id);

        RackCompartment compartment = compartmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Compartment not found with ID: " + id));

        // Soft delete
        compartment.setIsActive(false);
        compartmentRepository.save(compartment);

        // Update rack total shelves
        Rack rack = compartment.getRack();
        long totalShelves = compartmentRepository.countByRackId(rack.getId());
        rack.setTotalShelves((int) totalShelves);
        rackRepository.save(rack);

        log.info("✅ Compartment deactivated: {}", id);
    }

    @Override
    public void deleteCompartmentByCompartmentId(String compartmentId) {
        log.info("📦 Deleting compartment by compartmentId: {}", compartmentId);

        RackCompartment compartment = compartmentRepository.findByCompartmentId(compartmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Compartment not found: " + compartmentId));

        compartment.setIsActive(false);
        compartmentRepository.save(compartment);

        // Update rack total shelves
        Rack rack = compartment.getRack();
        long totalShelves = compartmentRepository.countByRackId(rack.getId());
        rack.setTotalShelves((int) totalShelves);
        rackRepository.save(rack);

        log.info("✅ Compartment deactivated: {}", compartmentId);
    }
}