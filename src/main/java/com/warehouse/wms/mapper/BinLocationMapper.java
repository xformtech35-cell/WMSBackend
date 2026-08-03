// ====== FILE: src/main/java/com/warehouse/wms/mapper/BinLocationMapper.java ======
package com.warehouse.wms.mapper;

import com.warehouse.wms.dto.response.LocationSuggestionResponse;
import com.warehouse.wms.entity.BinLocation;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BinLocationMapper {

    @Mapping(target = "fullLocation", expression = "java(binLocation.getFullLocation())")
    @Mapping(target = "suggestedQuantity", expression = "java(calculateSuggestedQuantity(binLocation, quantity))")
    LocationSuggestionResponse.SuggestedLocation toSuggestedLocation(BinLocation binLocation, @Context Integer quantity);

    default Integer calculateSuggestedQuantity(BinLocation binLocation, Integer quantity) {
        if (binLocation.getAvailableCapacity() == null || quantity == null) {
            return 0;
        }
        return Math.min(binLocation.getAvailableCapacity(), quantity);
    }
}