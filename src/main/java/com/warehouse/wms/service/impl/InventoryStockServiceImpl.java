package com.warehouse.wms.service.impl;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.warehouse.wms.constant.InventoryStatus;
import com.warehouse.wms.dto.request.InventorySearchRequest;
import com.warehouse.wms.dto.request.InventoryStockRequest;
import com.warehouse.wms.dto.response.InventoryStockResponse;
import com.warehouse.wms.entity.InventoryStock;
import com.warehouse.wms.exception.ResourceNotFoundException;
import com.warehouse.wms.mapper.InventoryStockMapper;
import com.warehouse.wms.repository.InventoryStockRepository;
import com.warehouse.wms.service.InventoryStockService;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class InventoryStockServiceImpl implements InventoryStockService {

    private final InventoryStockRepository inventoryStockRepository;
    private final InventoryStockMapper inventoryStockMapper;

    @Override
    public InventoryStockResponse createStock(InventoryStockRequest request) {
        log.info("Creating inventory stock for item: {}", request.getItemCode());
        
        // Generate inventory number if not provided
        if (request.getInventoryNumber() == null || request.getInventoryNumber().isEmpty()) {
            request.setInventoryNumber(generateInventoryNumber());
        }
        
        // Check if stock already exists in this bin
        if (request.getBinId() != null && request.getItemCode() != null) {
            inventoryStockRepository.findByBinIdAndItemCode(request.getBinId(), request.getItemCode())
                .ifPresent(existing -> {
                    throw new RuntimeException("Stock already exists for item " + 
                        request.getItemCode() + " in bin " + request.getBinId());
                });
        }
        
        InventoryStock stock = inventoryStockMapper.toEntity(request);
        stock.setReceivedDate(LocalDateTime.now());
        stock.setLastUpdatedDate(LocalDateTime.now());
        
        InventoryStock savedStock = inventoryStockRepository.save(stock);
        log.info("Inventory stock created with ID: {}", savedStock.getId());
        
        return inventoryStockMapper.toResponse(savedStock);
    }

    @Override
    public InventoryStockResponse updateStock(Long id, InventoryStockRequest request) {
        log.info("Updating inventory stock with ID: {}", id);
        
        InventoryStock stock = inventoryStockRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Inventory stock not found with ID: " + id));
        
        inventoryStockMapper.updateEntity(stock, request);
        stock.setUpdatedAt(LocalDateTime.now());
        
        InventoryStock updatedStock = inventoryStockRepository.save(stock);
        log.info("Inventory stock updated with ID: {}", updatedStock.getId());
        
        return inventoryStockMapper.toResponse(updatedStock);
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryStockResponse getStockById(Long id) {
        log.info("Fetching inventory stock with ID: {}", id);
        
        InventoryStock stock = inventoryStockRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Inventory stock not found with ID: " + id));
        
        return inventoryStockMapper.toResponse(stock);
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryStockResponse getStockByInventoryNumber(String inventoryNumber) {
        log.info("Fetching inventory stock with inventory number: {}", inventoryNumber);
        
        InventoryStock stock = inventoryStockRepository.findByInventoryNumber(inventoryNumber)
            .orElseThrow(() -> new ResourceNotFoundException("Inventory stock not found with number: " + inventoryNumber));
        
        return inventoryStockMapper.toResponse(stock);
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryStockResponse getStockByItemAndBin(String itemCode, String binId) {
        log.info("Fetching inventory stock for item: {} in bin: {}", itemCode, binId);
        
        InventoryStock stock = inventoryStockRepository.findByBinIdAndItemCode(binId, itemCode)
            .orElseThrow(() -> new ResourceNotFoundException("Stock not found for item " + 
                itemCode + " in bin " + binId));
        
        return inventoryStockMapper.toResponse(stock);
    }

//    @Override
//    @Transactional(readOnly = true)
//    public Page<InventoryStockResponse> getAllStocks(Pageable pageable) {
//        log.info("Fetching all inventory stocks with pagination: page {}, size {}", 
//            pageable.getPageNumber(), pageable.getPageSize());
//        
//        Page<InventoryStock> stocks = inventoryStockRepository.findAll(pageable);
//        return stocks.map(inventoryStockMapper::toResponse);
//    }
    
    
    @Override
    @Transactional(readOnly = true)
    public Page<InventoryStockResponse> getAllStocks(
            String search,
            String itemCode,
            String itemName,
            InventoryStatus status,
            String warehouseId,
            String zone,
            String aisle,
            String rack,
            String level,
            String binId,
            String batchNumber,
            String grnNumber,
            Boolean isAvailable,
            Boolean isAllocated,
            Boolean isFrozen,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Integer minQuantity,
            Integer maxQuantity,
            Pageable pageable) {
        
        log.info("Fetching inventory stocks with pagination and filters: page {}, size {}, search: {}, status: {}, warehouse: {}, zone: {}, aisle: {}, rack: {}, level: {}, bin: {}",
                pageable.getPageNumber(), pageable.getPageSize(), search, status, warehouseId, zone, aisle, rack, level, binId);
        
        Page<InventoryStock> stocks = inventoryStockRepository.findAll(
                InventoryStockSpecification.filterBy(
                        search, itemCode, itemName, status, warehouseId, zone, aisle, rack, level, binId,
                        batchNumber, grnNumber, isAvailable, isAllocated, isFrozen,
                        startDate, endDate, minQuantity, maxQuantity),
                pageable
        );
        
        return stocks.map(inventoryStockMapper::toResponse);
    }
    
    
    

    @Override
    @Transactional(readOnly = true)
    public List<InventoryStockResponse> getStocksByItemCode(String itemCode) {
        log.info("Fetching inventory stocks by item code: {}", itemCode);
        
        List<InventoryStock> stocks = inventoryStockRepository.findByItemCode(itemCode);
        return inventoryStockMapper.toResponseList(stocks);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryStockResponse> getStocksByBinId(String binId) {
        log.info("Fetching inventory stocks by bin ID: {}", binId);
        
        List<InventoryStock> stocks = inventoryStockRepository.findByBinId(binId);
        return inventoryStockMapper.toResponseList(stocks);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryStockResponse> getStocksByWarehouseId(String warehouseId) {
        log.info("Fetching inventory stocks by warehouse ID: {}", warehouseId);
        
        List<InventoryStock> stocks = inventoryStockRepository.findByWarehouseId(warehouseId);
        return inventoryStockMapper.toResponseList(stocks);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InventoryStockResponse> searchStocks(InventorySearchRequest searchRequest, Pageable pageable) {
        log.info("Searching inventory stocks with filters: {}", searchRequest);
        
        Specification<InventoryStock> specification = (root, query, criteriaBuilder) -> {
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
            
            if (searchRequest.getBinId() != null && !searchRequest.getBinId().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("binId"), searchRequest.getBinId()));
            }
            
            if (searchRequest.getWarehouseId() != null && !searchRequest.getWarehouseId().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("warehouseId"), searchRequest.getWarehouseId()));
            }
            
            if (searchRequest.getStatus() != null && !searchRequest.getStatus().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("status"), 
                    InventoryStatus.valueOf(searchRequest.getStatus())));
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
        
        Page<InventoryStock> stocks = inventoryStockRepository.findAll(specification, pageable);
        return stocks.map(inventoryStockMapper::toResponse);
    }

    @Override
    public void deleteStock(Long id) {
        log.info("Deleting inventory stock with ID: {}", id);
        
        InventoryStock stock = inventoryStockRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Inventory stock not found with ID: " + id));
        
        inventoryStockRepository.delete(stock);
        log.info("Inventory stock deleted with ID: {}", id);
    }

    @Override
    public void addQuantity(Long id, Integer quantity) {
        log.info("Adding quantity to stock ID: {}, quantity: {}", id, quantity);
        
        InventoryStock stock = inventoryStockRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Inventory stock not found with ID: " + id));
        
        stock.addQuantity(quantity);
        stock.setUpdatedAt(LocalDateTime.now());
        inventoryStockRepository.save(stock);
        
        log.info("Quantity added to stock ID: {}, new quantity: {}", id, stock.getQuantity());
    }

    @Override
    public void removeQuantity(Long id, Integer quantity) {
        log.info("Removing quantity from stock ID: {}, quantity: {}", id, quantity);
        
        InventoryStock stock = inventoryStockRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Inventory stock not found with ID: " + id));
        
        if (stock.getQuantity() < quantity) {
            throw new RuntimeException("Insufficient quantity to remove");
        }
        
        stock.removeQuantity(quantity);
        stock.setUpdatedAt(LocalDateTime.now());
        inventoryStockRepository.save(stock);
        
        log.info("Quantity removed from stock ID: {}, new quantity: {}", id, stock.getQuantity());
    }

    @Override
    public void reserveStock(Long id, Integer quantity) {
        log.info("Reserving stock ID: {}, quantity: {}", id, quantity);
        
        InventoryStock stock = inventoryStockRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Inventory stock not found with ID: " + id));
        
        stock.reserveQuantity(quantity);
        stock.setUpdatedAt(LocalDateTime.now());
        inventoryStockRepository.save(stock);
        
        log.info("Stock reserved for ID: {}, available quantity: {}", id, stock.getAvailableQuantity());
    }

    @Override
    public void unreserveStock(Long id, Integer quantity) {
        log.info("Unreserving stock ID: {}, quantity: {}", id, quantity);
        
        InventoryStock stock = inventoryStockRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Inventory stock not found with ID: " + id));
        
        stock.unreserveQuantity(quantity);
        stock.setUpdatedAt(LocalDateTime.now());
        inventoryStockRepository.save(stock);
        
        log.info("Stock unreserved for ID: {}, available quantity: {}", id, stock.getAvailableQuantity());
    }

    @Override
    public void updateStockStatus(Long id, String status) {
        log.info("Updating stock status for ID: {} to: {}", id, status);
        
        InventoryStock stock = inventoryStockRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Inventory stock not found with ID: " + id));
        
        stock.setStatus(InventoryStatus.valueOf(status));
        stock.setUpdatedAt(LocalDateTime.now());
        inventoryStockRepository.save(stock);
        
        log.info("Stock status updated for ID: {}", id);
    }

    @Override
    public void freezeStock(Long id) {
        log.info("Freezing stock with ID: {}", id);
        
        InventoryStock stock = inventoryStockRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Inventory stock not found with ID: " + id));
        
        stock.setIsFrozen(true);
        stock.setUpdatedAt(LocalDateTime.now());
        inventoryStockRepository.save(stock);
        
        log.info("Stock frozen with ID: {}", id);
    }

    @Override
    public void unfreezeStock(Long id) {
        log.info("Unfreezing stock with ID: {}", id);
        
        InventoryStock stock = inventoryStockRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Inventory stock not found with ID: " + id));
        
        stock.setIsFrozen(false);
        stock.setUpdatedAt(LocalDateTime.now());
        inventoryStockRepository.save(stock);
        
        log.info("Stock unfrozen with ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryStockResponse> getLowStockItems() {
        log.info("Fetching low stock items");
        
        List<InventoryStock> lowStockItems = inventoryStockRepository.findLowStockItems();
        return inventoryStockMapper.toResponseList(lowStockItems);
    }

    private String generateInventoryNumber() {
        return "INV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase() 
               + "-" + System.currentTimeMillis() % 10000;
    }
}