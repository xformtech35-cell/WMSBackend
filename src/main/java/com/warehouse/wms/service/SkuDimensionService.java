// ====== FILE: src/main/java/com/warehouse/wms/service/SkuDimensionService.java ======
package com.warehouse.wms.service;

import com.warehouse.wms.dto.request.SkuDimensionRequest;
import com.warehouse.wms.dto.response.SkuDimensionResponse;

public interface SkuDimensionService {

    SkuDimensionResponse createDimension(Long skuId, SkuDimensionRequest request);

    SkuDimensionResponse getDimensionBySkuId(Long skuId);

    SkuDimensionResponse updateDimension(Long skuId, SkuDimensionRequest request);

    void deleteDimensionBySkuId(Long skuId);
}