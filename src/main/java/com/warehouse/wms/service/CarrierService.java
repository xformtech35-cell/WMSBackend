package com.warehouse.wms.service;

import com.warehouse.wms.dto.CarrierRateDto;
import java.util.List;

public interface CarrierService {
    List<CarrierRateDto> getRates(Long orderId, String destinationPincode);
    String generateAWB(Long orderId);
    String getCarrierName();
}
