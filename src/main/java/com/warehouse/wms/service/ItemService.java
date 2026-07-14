package com.warehouse.wms.service;

import com.warehouse.wms.dto.CreateItemDTO;
import com.warehouse.wms.dto.ItemDTO;
import com.warehouse.wms.dto.ItemFilterDTO;
import com.warehouse.wms.dto.UpdateItemDTO;
import com.warehouse.wms.entity.Item;
import com.warehouse.wms.exception.ResourceNotFoundException;
import com.warehouse.wms.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemService {
    
    private final ItemRepository itemRepository;
    
    // CREATE
    @Transactional
    public ItemDTO createItem(CreateItemDTO createItemDTO) {
        log.info("Creating new item: {}", createItemDTO.getItemCode());
        
        // Check if item code already exists
        if (itemRepository.existsByItemCode(createItemDTO.getItemCode())) {
            throw new IllegalStateException("Item code already exists: " + createItemDTO.getItemCode());
        }
        
        Item item = mapToEntity(createItemDTO);
        item = itemRepository.save(item);
        
        log.info("Item created with ID: {}", item.getId());
        return mapToDTO(item);
    }
    
    // READ - Get by ID
    public ItemDTO getItemById(Long id) {
        Item item = itemRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Item not found with id: " + id));
        return mapToDTO(item);
    }
    
    // READ - Get by Code
    public ItemDTO getItemByCode(String itemCode) {
        Item item = itemRepository.findByItemCode(itemCode)
            .orElseThrow(() -> new ResourceNotFoundException("Item not found with code: " + itemCode));
        return mapToDTO(item);
    }
    
    // READ - Get all with pagination
    public Page<ItemDTO> getAllItems(Pageable pageable) {
        return itemRepository.findAll(pageable).map(this::mapToDTO);
    }
    
    // READ - Search with filters
    public Page<ItemDTO> searchItems(ItemFilterDTO filter, Pageable pageable) {
        Page<Item> items = itemRepository.searchItems(
            filter.getItemCode(),
            filter.getItemName(),
            filter.getCategory(),
            filter.getBrand(),
            filter.getIsActive(),
            filter.getMinPrice(),
            filter.getMaxPrice(),
            filter.getMinStock(),
            filter.getMaxStock(),
            filter.getIsGstApplicable(),
            pageable
        );
        return items.map(this::mapToDTO);
    }
    
    // READ - Get by category
    public List<ItemDTO> getItemsByCategory(String category) {
        return itemRepository.findByCategory(category).stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }
    
    // READ - Get by brand
    public List<ItemDTO> getItemsByBrand(String brand) {
        return itemRepository.findByBrand(brand).stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }
    
    // READ - Get active items
    public List<ItemDTO> getActiveItems() {
        return itemRepository.findByIsActiveTrue().stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }
    
    // READ - Get low stock items
    public List<ItemDTO> getLowStockItems() {
        return itemRepository.findLowStockItems().stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }
    
    // READ - Get items needing reorder
    public List<ItemDTO> getItemsNeedingReorder() {
        return itemRepository.findByCurrentStockLessThanEqual(0).stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }
    
    // READ - Get GST applicable items
    public List<ItemDTO> getGstApplicableItems() {
        return itemRepository.findGstApplicableItems().stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }
    
    // READ - Get by supplier
    public List<ItemDTO> getItemsBySupplier(Long supplierId) {
        return itemRepository.findBySupplierId(supplierId).stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }
    
    // UPDATE
    @Transactional
    public ItemDTO updateItem(Long id, UpdateItemDTO updateItemDTO) {
        log.info("Updating item: {}", id);
        
        Item item = itemRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Item not found with id: " + id));
        
        // Check if item code is being changed and if it already exists
        if (!item.getItemCode().equals(updateItemDTO.getItemCode()) && 
            itemRepository.existsByItemCode(updateItemDTO.getItemCode())) {
            throw new IllegalStateException("Item code already exists: " + updateItemDTO.getItemCode());
        }
        
        // Update fields
        updateEntity(item, updateItemDTO);
        item = itemRepository.save(item);
        
        log.info("Item updated: {}", id);
        return mapToDTO(item);
    }
    
    // UPDATE - Partial update (PATCH)
    @Transactional
    public ItemDTO patchItem(Long id, CreateItemDTO patchDTO) {
        log.info("Patching item: {}", id);
        
        Item item = itemRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Item not found with id: " + id));
        
        // Only update fields that are not null
        if (patchDTO.getItemCode() != null) {
            if (!item.getItemCode().equals(patchDTO.getItemCode()) && 
                itemRepository.existsByItemCode(patchDTO.getItemCode())) {
                throw new IllegalStateException("Item code already exists: " + patchDTO.getItemCode());
            }
            item.setItemCode(patchDTO.getItemCode());
        }
        if (patchDTO.getItemName() != null) item.setItemName(patchDTO.getItemName());
        if (patchDTO.getDescription() != null) item.setDescription(patchDTO.getDescription());
        if (patchDTO.getUom() != null) item.setUom(patchDTO.getUom());
        if (patchDTO.getGstRate() != null) item.setGstRate(patchDTO.getGstRate());
        if (patchDTO.getGstHsnCode() != null) item.setGstHsnCode(patchDTO.getGstHsnCode());
        if (patchDTO.getGstSacCode() != null) item.setGstSacCode(patchDTO.getGstSacCode());
        if (patchDTO.getIsGstApplicable() != null) item.setIsGstApplicable(patchDTO.getIsGstApplicable());
        if (patchDTO.getCgstRate() != null) item.setCgstRate(patchDTO.getCgstRate());
        if (patchDTO.getSgstRate() != null) item.setSgstRate(patchDTO.getSgstRate());
        if (patchDTO.getIgstRate() != null) item.setIgstRate(patchDTO.getIgstRate());
        if (patchDTO.getUnitPrice() != null) item.setUnitPrice(patchDTO.getUnitPrice());
        if (patchDTO.getCurrentStock() != null) item.setCurrentStock(patchDTO.getCurrentStock());
        if (patchDTO.getMinStockLevel() != null) item.setMinStockLevel(patchDTO.getMinStockLevel());
        if (patchDTO.getReorderLevel() != null) item.setReorderLevel(patchDTO.getReorderLevel());
        if (patchDTO.getIsActive() != null) item.setIsActive(patchDTO.getIsActive());
        if (patchDTO.getCategory() != null) item.setCategory(patchDTO.getCategory());
        if (patchDTO.getBrand() != null) item.setBrand(patchDTO.getBrand());
        if (patchDTO.getSupplierId() != null) item.setSupplierId(patchDTO.getSupplierId());
        if (patchDTO.getNotes() != null) item.setNotes(patchDTO.getNotes());
        
        item = itemRepository.save(item);
        log.info("Item patched: {}", id);
        return mapToDTO(item);
    }
    
    // UPDATE - Update stock
    @Transactional
    public ItemDTO updateStock(Long id, Integer quantity) {
        Item item = itemRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Item not found with id: " + id));
        
        int newStock = item.getCurrentStock() + quantity;
        if (newStock < 0) {
            throw new IllegalStateException("Insufficient stock. Current stock: " + item.getCurrentStock());
        }
        
        item.setCurrentStock(newStock);
        item = itemRepository.save(item);
        log.info("Stock updated for item {}: {}", item.getItemCode(), newStock);
        return mapToDTO(item);
    }
    
    // UPDATE - Toggle active status
    @Transactional
    public ItemDTO toggleItemStatus(Long id) {
        Item item = itemRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Item not found with id: " + id));
        item.setIsActive(!item.getIsActive());
        item = itemRepository.save(item);
        log.info("Item status toggled for {}: {}", id, item.getIsActive());
        return mapToDTO(item);
    }
    
    // DELETE
    @Transactional
    public void deleteItem(Long id) {
        Item item = itemRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Item not found with id: " + id));
        itemRepository.delete(item);
        log.info("Item deleted: {}", id);
    }
    
    // DELETE - Soft delete (just deactivate)
    @Transactional
    public ItemDTO softDeleteItem(Long id) {
        Item item = itemRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Item not found with id: " + id));
        item.setIsActive(false);
        item = itemRepository.save(item);
        log.info("Item soft deleted: {}", id);
        return mapToDTO(item);
    }
    
    // GET - Statistics
    public Object getStatistics() {
        long totalItems = itemRepository.count();
        long activeItems = itemRepository.findByIsActiveTrue().size();
        long lowStockItems = itemRepository.findLowStockItems().size();
        long gstItems = itemRepository.findGstApplicableItems().size();
        
        return java.util.Map.of(
            "totalItems", totalItems,
            "activeItems", activeItems,
            "lowStockItems", lowStockItems,
            "gstApplicableItems", gstItems
        );
    }
    
    // ============ MAPPING METHODS ============
    
    private Item mapToEntity(CreateItemDTO dto) {
        Item item = new Item();
        item.setItemCode(dto.getItemCode());
        item.setItemName(dto.getItemName());
        item.setDescription(dto.getDescription());
        item.setUom(dto.getUom());
        item.setGstRate(dto.getGstRate());
        item.setGstHsnCode(dto.getGstHsnCode());
        item.setGstSacCode(dto.getGstSacCode());
        item.setIsGstApplicable(dto.getIsGstApplicable());
        item.setCgstRate(dto.getCgstRate());
        item.setSgstRate(dto.getSgstRate());
        item.setIgstRate(dto.getIgstRate());
        item.setUnitPrice(dto.getUnitPrice());
        item.setCurrentStock(dto.getCurrentStock());
        item.setMinStockLevel(dto.getMinStockLevel());
        item.setReorderLevel(dto.getReorderLevel());
        item.setIsActive(dto.getIsActive());
        item.setCategory(dto.getCategory());
        item.setBrand(dto.getBrand());
        item.setSupplierId(dto.getSupplierId());
        item.setNotes(dto.getNotes());
        return item;
    }
    
    private void updateEntity(Item item, UpdateItemDTO dto) {
        item.setItemCode(dto.getItemCode());
        item.setItemName(dto.getItemName());
        item.setDescription(dto.getDescription());
        item.setUom(dto.getUom());
        item.setGstRate(dto.getGstRate());
        item.setGstHsnCode(dto.getGstHsnCode());
        item.setGstSacCode(dto.getGstSacCode());
        item.setIsGstApplicable(dto.getIsGstApplicable());
        item.setCgstRate(dto.getCgstRate());
        item.setSgstRate(dto.getSgstRate());
        item.setIgstRate(dto.getIgstRate());
        item.setUnitPrice(dto.getUnitPrice());
        item.setCurrentStock(dto.getCurrentStock());
        item.setMinStockLevel(dto.getMinStockLevel());
        item.setReorderLevel(dto.getReorderLevel());
        item.setIsActive(dto.getIsActive());
        item.setCategory(dto.getCategory());
        item.setBrand(dto.getBrand());
        item.setSupplierId(dto.getSupplierId());
        item.setNotes(dto.getNotes());
    }
    
    private ItemDTO mapToDTO(Item item) {
        return ItemDTO.builder()
            .id(item.getId())
            .itemCode(item.getItemCode())
            .itemName(item.getItemName())
            .description(item.getDescription())
            .uom(item.getUom())
            .gstRate(item.getGstRate())
            .gstHsnCode(item.getGstHsnCode())
            .gstSacCode(item.getGstSacCode())
            .isGstApplicable(item.getIsGstApplicable())
            .cgstRate(item.getCgstRate())
            .sgstRate(item.getSgstRate())
            .igstRate(item.getIgstRate())
            .unitPrice(item.getUnitPrice())
            .currentStock(item.getCurrentStock())
            .minStockLevel(item.getMinStockLevel())
            .reorderLevel(item.getReorderLevel())
            .isActive(item.getIsActive())
            .category(item.getCategory())
            .brand(item.getBrand())
            .supplierId(item.getSupplierId())
            .notes(item.getNotes())
            .createdAt(item.getCreatedAt())
            .updatedAt(item.getUpdatedAt())
            .build();
    }
}