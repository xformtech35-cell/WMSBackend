package com.warehouse.wms.service;

import com.warehouse.wms.dto.CarrierRateDto;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Service
public class ShiprocketCarrierService implements CarrierService {

    @Override
    public List<CarrierRateDto> getRates(Long orderId, String destinationPincode) {
        double rateValue = 90.0 + (orderId % 7) * 12.0;
        return Arrays.asList(
            CarrierRateDto.builder()
                .carrierName(getCarrierName())
                .serviceType("Shiprocket Air")
                .rate(BigDecimal.valueOf(rateValue))
                .estimatedDays(1)
                .build(),
            CarrierRateDto.builder()
                .carrierName(getCarrierName())
                .serviceType("Shiprocket Surface")
                .rate(BigDecimal.valueOf(rateValue * 0.65))
                .estimatedDays(6)
                .build()
        );
    }

    @Override
    public String generateAWB(Long orderId) {
        long seq = 2000000000L + (orderId * 23) + new Random().nextInt(100000);
        return "SR" + seq;
    }

    @Override
    public String getCarrierName() {
        return "Shiprocket";
    }
}
