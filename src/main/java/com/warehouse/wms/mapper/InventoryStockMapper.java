package com.warehouse.wms.mapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.warehouse.wms.constant.InventoryStatus;
import com.warehouse.wms.dto.request.InventoryStockRequest;
import com.warehouse.wms.dto.response.InventoryStockResponse;
import com.warehouse.wms.entity.InventoryStock;

@Component
public class InventoryStockMapper {

    public InventoryStock toEntity(InventoryStockRequest request) {
        if (request == null) return null;
        
        return InventoryStock.builder()
            .id(request.getId())
            .inventoryNumber(request.getInventoryNumber())
            .itemCode(request.getItemCode())
            .itemName(request.getItemName())
            .uom(request.getUom())
            .quantity(request.getQuantity() != null ? request.getQuantity() : 0)
            .availableQuantity(request.getAvailableQuantity() != null ? 
                request.getAvailableQuantity() : request.getQuantity())
            .reservedQuantity(request.getReservedQuantity() != null ? 
                request.getReservedQuantity() : 0)
            .inTransitQuantity(request.getInTransitQuantity() != null ? 
                request.getInTransitQuantity() : 0)
            .warehouseId(request.getWarehouseId())
            .zone(request.getZone())
            .aisle(request.getAisle())
            .rack(request.getRack())
            .shelf(request.getShelf())
            .binId(request.getBinId())
            .binBarcode(request.getBinBarcode())
            .batchNumber(request.getBatchNumber())
            .serialNumbers(request.getSerialNumbers())
            .mfgDate(request.getMfgDate())
            .expiryDate(request.getExpiryDate())
            .receivedDate(LocalDateTime.now())
            .lastUpdatedDate(LocalDateTime.now())
            .status(request.getStatus() != null ? 
                InventoryStatus.valueOf(request.getStatus()) : InventoryStatus.ACTIVE)
            .isAvailable(request.getIsAvailable() != null ? 
                request.getIsAvailable() : true)
            .isAllocated(request.getIsAllocated() != null ? 
                request.getIsAllocated() : false)
            .isFrozen(request.getIsFrozen() != null ? 
                request.getIsFrozen() : false)
            .remarks(request.getRemarks())
            .build();
    }

    public InventoryStockResponse toResponse(InventoryStock stock) {
        if (stock == null) return null;
        
        return InventoryStockResponse.builder()
            .id(stock.getId())
            .inventoryNumber(stock.getInventoryNumber())
            .itemCode(stock.getItemCode())
            .itemName(stock.getItemName())
            .uom(stock.getUom())
            .quantity(stock.getQuantity())
            .availableQuantity(stock.getAvailableQuantity())
            .reservedQuantity(stock.getReservedQuantity())
            .inTransitQuantity(stock.getInTransitQuantity())
            .warehouseId(stock.getWarehouseId())
            .zone(stock.getZone())
            .aisle(stock.getAisle())
            .rack(stock.getRack())
            .shelf(stock.getShelf())
            .level(stock.getLevel())
            .binId(stock.getBinId())
            .fullLocation(stock.getFullLocation())
            .binBarcode(stock.getBinBarcode())
            .batchNumber(stock.getBatchNumber())
            .serialNumbers(stock.getSerialNumbers())
            .mfgDate(stock.getMfgDate())
            .expiryDate(stock.getExpiryDate())
            .receivedDate(stock.getReceivedDate())
            .lastUpdatedDate(stock.getLastUpdatedDate())
            .grnNumber(stock.getGrnNumber())
            .putawayTaskNumber(stock.getPutawayTaskNumber())
            .confirmationNumber(stock.getConfirmationNumber())
            .qrCodeValue(stock.getQrCodeValue())
            .status(stock.getStatus() != null ? stock.getStatus().name() : null)
            .isAvailable(stock.getIsAvailable())
            .isAllocated(stock.getIsAllocated())
            .isFrozen(stock.getIsFrozen())
            .remarks(stock.getRemarks())
            .createdBy(stock.getCreatedBy())
            .createdAt(stock.getCreatedAt())
            .updatedAt(stock.getUpdatedAt())
            .build();
    }

    public List<InventoryStockResponse> toResponseList(List<InventoryStock> stocks) {
        if (stocks == null) return null;
        return stocks.stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    public void updateEntity(InventoryStock stock, InventoryStockRequest request) {
        if (request == null) return;
        
        if (request.getItemCode() != null) {
            stock.setItemCode(request.getItemCode());
        }
        if (request.getItemName() != null) {
            stock.setItemName(request.getItemName());
        }
        if (request.getUom() != null) {
            stock.setUom(request.getUom());
        }
        if (request.getQuantity() != null) {
            stock.setQuantity(request.getQuantity());
        }
        if (request.getAvailableQuantity() != null) {
            stock.setAvailableQuantity(request.getAvailableQuantity());
        }
        if (request.getReservedQuantity() != null) {
            stock.setReservedQuantity(request.getReservedQuantity());
        }
        if (request.getInTransitQuantity() != null) {
            stock.setInTransitQuantity(request.getInTransitQuantity());
        }
        if (request.getWarehouseId() != null) {
            stock.setWarehouseId(request.getWarehouseId());
        }
        if (request.getZone() != null) {
            stock.setZone(request.getZone());
        }
        if (request.getAisle() != null) {
            stock.setAisle(request.getAisle());
        }
        if (request.getRack() != null) {
            stock.setRack(request.getRack());
        }
        if (request.getShelf() != null) {
            stock.setShelf(request.getShelf());
        }
        if (request.getBinId() != null) {
            stock.setBinId(request.getBinId());
        }
        if (request.getBinBarcode() != null) {
            stock.setBinBarcode(request.getBinBarcode());
        }
        if (request.getBatchNumber() != null) {
            stock.setBatchNumber(request.getBatchNumber());
        }
        if (request.getSerialNumbers() != null) {
            stock.setSerialNumbers(request.getSerialNumbers());
        }
        if (request.getMfgDate() != null) {
            stock.setMfgDate(request.getMfgDate());
        }
        if (request.getExpiryDate() != null) {
            stock.setExpiryDate(request.getExpiryDate());
        }
        if (request.getStatus() != null) {
            stock.setStatus(InventoryStatus.valueOf(request.getStatus()));
        }
        if (request.getIsAvailable() != null) {
            stock.setIsAvailable(request.getIsAvailable());
        }
        if (request.getIsAllocated() != null) {
            stock.setIsAllocated(request.getIsAllocated());
        }
        if (request.getIsFrozen() != null) {
            stock.setIsFrozen(request.getIsFrozen());
        }
        if (request.getRemarks() != null) {
            stock.setRemarks(request.getRemarks());
        }
        stock.setLastUpdatedDate(LocalDateTime.now());
    }
}