package com.warehouse.wms.service;

import com.warehouse.wms.dto.CarrierRateDto;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Service
public class DelhiveryCarrierService implements CarrierService {

    @Override
    public List<CarrierRateDto> getRates(Long orderId, String destinationPincode) {
        double rateValue = 80.0 + (orderId % 5) * 15.0;
        return Arrays.asList(
            CarrierRateDto.builder()
                .carrierName(getCarrierName())
                .serviceType("Delhivery Express")
                .rate(BigDecimal.valueOf(rateValue))
                .estimatedDays(2)
                .build(),
            CarrierRateDto.builder()
                .carrierName(getCarrierName())
                .serviceType("Delhivery Ground")
                .rate(BigDecimal.valueOf(rateValue * 0.75))
                .estimatedDays(5)
                .build()
        );
    }

    @Override
    public String generateAWB(Long orderId) {
        long seq = 1000000000L + (orderId * 17) + new Random().nextInt(100000);
        return "DEL" + seq;
    }

    @Override
    public String getCarrierName() {
        return "Delhivery";
    }
}
