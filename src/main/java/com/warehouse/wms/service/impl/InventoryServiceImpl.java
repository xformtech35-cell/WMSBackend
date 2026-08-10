package com.warehouse.wms.service.impl;



import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.warehouse.wms.dto.InventoryResponse;
import com.warehouse.wms.dto.request.InventoryRequest;
import com.warehouse.wms.dto.request.InventorySearchRequest;
import com.warehouse.wms.entity.Inventory;
import com.warehouse.wms.exception.ResourceNotFoundException;
import com.warehouse.wms.mapper.InventoryMapper;
import com.warehouse.wms.repository.InventoryRepository;
import com.warehouse.wms.service.InventoryService;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final InventoryMapper inventoryMapper;

    @Override
    public InventoryResponse createInventory(InventoryRequest request) {
        log.info("Creating inventory for item: {}", request.getItemCode());
        
        // Check if serial number already exists
        if (request.getSerialNo() != null && !request.getSerialNo().isEmpty()) {
            inventoryRepository.findBySerialNo(request.getSerialNo())
                .ifPresent(existing -> {
                    throw new RuntimeException("Inventory with serial number " + 
                        request.getSerialNo() + " already exists");
                });
        }
        
        Inventory inventory = inventoryMapper.toEntity(request);
        inventory.setState(Inventory.InventoryState.RECEIVED);
        inventory.setCreatedAt(LocalDateTime.now());
        inventory.setUpdatedAt(LocalDateTime.now());
        
        Inventory savedInventory = inventoryRepository.save(inventory);
        log.info("Inventory created with ID: {}", savedInventory.getId());
        
        return inventoryMapper.toResponse(savedInventory);
    }

    @Override
    public InventoryResponse updateInventory(Long id, InventoryRequest request) {
        log.info("Updating inventory with ID: {}", id);
        
        Inventory inventory = inventoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Inventory not found with ID: " + id));
        
        // Check if serial number is being changed and if it already exists
        if (request.getSerialNo() != null && !request.getSerialNo().isEmpty() 
            && !request.getSerialNo().equals(inventory.getSerialNo())) {
            inventoryRepository.findBySerialNo(request.getSerialNo())
                .ifPresent(existing -> {
                    throw new RuntimeException("Inventory with serial number " + 
                        request.getSerialNo() + " already exists");
                });
        }
        
        inventoryMapper.updateEntity(inventory, request);
        inventory.setUpdatedAt(LocalDateTime.now());
        
        Inventory updatedInventory = inventoryRepository.save(inventory);
        log.info("Inventory updated with ID: {}", updatedInventory.getId());
        
        return inventoryMapper.toResponse(updatedInventory);
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryResponse getInventoryById(Long id) {
        log.info("Fetching inventory with ID: {}", id);
        
        Inventory inventory = inventoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Inventory not found with ID: " + id));
        
        return inventoryMapper.toResponse(inventory);
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryResponse getInventoryBySerialNo(String serialNo) {
        log.info("Fetching inventory with serial number: {}", serialNo);
        
        Inventory inventory = inventoryRepository.findBySerialNo(serialNo)
            .orElseThrow(() -> new ResourceNotFoundException("Inventory not found with serial number: " + serialNo));
        
        return inventoryMapper.toResponse(inventory);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InventoryResponse> getAllInventories(Pageable pageable) {
        log.info("Fetching all inventories with pagination: page {}, size {}", 
            pageable.getPageNumber(), pageable.getPageSize());
        
        Page<Inventory> inventories = inventoryRepository.findAll(pageable);
        return inventories.map(inventoryMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryResponse> getInventoriesByItemCode(String itemCode) {
        log.info("Fetching inventories by item code: {}", itemCode);
        
        List<Inventory> inventories = inventoryRepository.findByItemCode(itemCode);
        return inventoryMapper.toResponseList(inventories);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryResponse> getInventoriesBySkuId(Long skuId) {
        log.info("Fetching inventories by SKU ID: {}", skuId);
        
        List<Inventory> inventories = inventoryRepository.findBySkuId(skuId);
        return inventoryMapper.toResponseList(inventories);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryResponse> getInventoriesByBinId(Long binId) {
        log.info("Fetching inventories by bin ID: {}", binId);
        
        List<Inventory> inventories = inventoryRepository.findByBinId(binId);
        return inventoryMapper.toResponseList(inventories);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InventoryResponse> searchInventories(InventorySearchRequest searchRequest, Pageable pageable) {
        log.info("Searching inventories with filters: {}", searchRequest);
        
        Specification<Inventory> specification = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (searchRequest.getItemCode() != null && !searchRequest.getItemCode().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("itemCode"), searchRequest.getItemCode()));
            }
            
            if (searchRequest.getItemName() != null && !searchRequest.getItemName().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("itemName")), 
                    "%" + searchRequest.getItemName().toLowerCase() + "%"
                ));
            }
            
            if (searchRequest.getSerialNo() != null && !searchRequest.getSerialNo().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("serialNo"), searchRequest.getSerialNo()));
            }
            
            if (searchRequest.getBatchNo() != null && !searchRequest.getBatchNo().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("batchNo"), searchRequest.getBatchNo()));
            }
            
            if (searchRequest.getState() != null && !searchRequest.getState().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("state"), 
                    Inventory.InventoryState.valueOf(searchRequest.getState())));
            }
            
            if (searchRequest.getMinQuantity() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                    root.get("quantity"), searchRequest.getMinQuantity()));
            }
            
            if (searchRequest.getMaxQuantity() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                    root.get("quantity"), searchRequest.getMaxQuantity()));
            }
            
            if (searchRequest.getFromDate() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                    root.get("createdAt"), searchRequest.getFromDate()));
            }
            
            if (searchRequest.getToDate() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                    root.get("createdAt"), searchRequest.getToDate()));
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
        
        Page<Inventory> inventories = inventoryRepository.findAll(specification, pageable);
        return inventories.map(inventoryMapper::toResponse);
    }

    @Override
    public void deleteInventory(Long id) {
        log.info("Deleting inventory with ID: {}", id);
        
        Inventory inventory = inventoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Inventory not found with ID: " + id));
        
        inventoryRepository.delete(inventory);
        log.info("Inventory deleted with ID: {}", id);
    }

    @Override
    public void updateInventoryState(Long id, String state) {
        log.info("Updating inventory state for ID: {} to: {}", id, state);
        
        Inventory inventory = inventoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Inventory not found with ID: " + id));
        
        inventory.setState(Inventory.InventoryState.valueOf(state));
        inventory.setUpdatedAt(LocalDateTime.now());
        inventoryRepository.save(inventory);
        
        log.info("Inventory state updated for ID: {}", id);
    }

    @Override
    public void reserveInventory(Long id, Integer quantity) {
        log.info("Reserving inventory ID: {}, quantity: {}", id, quantity);
        
        Inventory inventory = inventoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Inventory not found with ID: " + id));
        
        if (inventory.getQuantity() < quantity) {
            throw new RuntimeException("Insufficient quantity to reserve");
        }
        
        inventory.setQuantity(inventory.getQuantity() - quantity);
        inventory.setState(Inventory.InventoryState.RESERVED);
        inventory.setUpdatedAt(LocalDateTime.now());
        inventoryRepository.save(inventory);
        
        log.info("Inventory reserved for ID: {}, remaining quantity: {}", id, inventory.getQuantity());
    }

    @Override
    public void unreserveInventory(Long id, Integer quantity) {
        log.info("Unreserving inventory ID: {}, quantity: {}", id, quantity);
        
        Inventory inventory = inventoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Inventory not found with ID: " + id));
        
        inventory.setQuantity(inventory.getQuantity() + quantity);
        inventory.setState(Inventory.InventoryState.AVAILABLE);
        inventory.setUpdatedAt(LocalDateTime.now());
        inventoryRepository.save(inventory);
        
        log.info("Inventory unreserved for ID: {}, new quantity: {}", id, inventory.getQuantity());
    }
}