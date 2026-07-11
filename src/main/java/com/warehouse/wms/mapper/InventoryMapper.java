package com.warehouse.wms.mapper;

import com.warehouse.wms.dto.InventoryResponse;
import com.warehouse.wms.entity.Inventory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface InventoryMapper {

    @Mapping(target = "skuCode", source = "sku.skuCode")
    @Mapping(target = "itemBarcode", source = "serialNo")
    @Mapping(target = "state", expression = "java(inventory.getState().name())")
    @Mapping(target = "binBarcode", expression = "java(inventory.getBin() != null ? inventory.getBin().getBarcode() : null)")
    @Mapping(target = "itemCode", source = "itemCode")  // ADD THIS
    @Mapping(target = "itemName", source = "itemName")  // ADD THIS
    InventoryResponse toResponse(Inventory inventory);
}