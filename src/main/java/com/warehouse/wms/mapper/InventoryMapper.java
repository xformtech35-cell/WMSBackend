package com.warehouse.wms.mapper;


import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.warehouse.wms.dto.InventoryResponse;
import com.warehouse.wms.dto.request.InventoryRequest;
import com.warehouse.wms.entity.Bin;
import com.warehouse.wms.entity.Inventory;
import com.warehouse.wms.entity.Sku;

@Component
public class InventoryMapper {

    public Inventory toEntity(InventoryRequest request) {
        if (request == null) return null;
        
        Inventory inventory = new Inventory();
        inventory.setId(request.getId());
        inventory.setBatchNo(request.getBatchNo());
        inventory.setSerialNo(request.getSerialNo());
        inventory.setItemCode(request.getItemCode());
        inventory.setItemName(request.getItemName());
        inventory.setQuantity(request.getQuantity());
        inventory.setManufactureDate(request.getManufactureDate());
        inventory.setExpiryDate(request.getExpiryDate());
        
        if (request.getState() != null) {
            inventory.setState(Inventory.InventoryState.valueOf(request.getState()));
        }
        
        // Set relationships
        if (request.getSkuId() != null) {
            Sku sku = new Sku();
            sku.setId(request.getSkuId());
            inventory.setSku(sku);
        }
        
        if (request.getBinId() != null) {
            Bin bin = new Bin();
            bin.setId(request.getBinId());
            inventory.setBin(bin);
        }
        
        return inventory;
    }

    public InventoryResponse toResponse(Inventory inventory) {
        if (inventory == null) return null;
        
        return InventoryResponse.builder()
            .id(inventory.getId())
            .purchaseRequestItemId(inventory.getPurchaseRequestItem() != null ? 
                inventory.getPurchaseRequestItem().getId() : null)
            .skuId(inventory.getSku() != null ? inventory.getSku().getId() : null)
            .skuCode(inventory.getSku() != null ? inventory.getSku().getSkuCode() : null)
            .skuName(inventory.getSku() != null ? inventory.getSku().getName() : null)
            .binId(inventory.getBin() != null ? inventory.getBin().getId() : null)
            .binCode(inventory.getBin() != null ? inventory.getBin().getBarcode() : null)
            .goodsReceiptLineId(inventory.getGoodsReceiptLine() != null ? 
                inventory.getGoodsReceiptLine().getId() : null)
            .batchNo(inventory.getBatchNo())
            .serialNo(inventory.getSerialNo())
            .itemCode(inventory.getItemCode())
            .itemName(inventory.getItemName())
            .quantity(inventory.getQuantity())
            .state(inventory.getState() != null ? inventory.getState().name() : null)
            .manufactureDate(inventory.getManufactureDate())
            .expiryDate(inventory.getExpiryDate())
            .createdAt(inventory.getCreatedAt())
            .updatedAt(inventory.getUpdatedAt())
            .build();
    }

    public List<InventoryResponse> toResponseList(List<Inventory> inventories) {
        if (inventories == null) return null;
        return inventories.stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    public void updateEntity(Inventory inventory, InventoryRequest request) {
        if (request == null) return;
        
        if (request.getBatchNo() != null) {
            inventory.setBatchNo(request.getBatchNo());
        }
        if (request.getSerialNo() != null) {
            inventory.setSerialNo(request.getSerialNo());
        }
        if (request.getItemCode() != null) {
            inventory.setItemCode(request.getItemCode());
        }
        if (request.getItemName() != null) {
            inventory.setItemName(request.getItemName());
        }
        if (request.getQuantity() != null) {
            inventory.setQuantity(request.getQuantity());
        }
        if (request.getState() != null) {
            inventory.setState(Inventory.InventoryState.valueOf(request.getState()));
        }
        if (request.getManufactureDate() != null) {
            inventory.setManufactureDate(request.getManufactureDate());
        }
        if (request.getExpiryDate() != null) {
            inventory.setExpiryDate(request.getExpiryDate());
        }
    }
}