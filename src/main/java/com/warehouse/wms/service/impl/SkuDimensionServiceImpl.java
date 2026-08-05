// ====== FILE: src/main/java/com/warehouse/wms/service/impl/SkuDimensionServiceImpl.java ======
package com.warehouse.wms.service.impl;

import com.warehouse.wms.dto.request.SkuDimensionRequest;
import com.warehouse.wms.dto.response.SkuDimensionResponse;
import com.warehouse.wms.entity.Sku;
import com.warehouse.wms.entity.SkuDimension;
import com.warehouse.wms.exception.ResourceNotFoundException;
import com.warehouse.wms.mapper.SkuDimensionMapper;
import com.warehouse.wms.repository.SkuDimensionRepository;
import com.warehouse.wms.repository.SkuRepository;
import com.warehouse.wms.service.SkuDimensionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SkuDimensionServiceImpl implements SkuDimensionService {

    private final SkuDimensionRepository skuDimensionRepository;
    private final SkuRepository skuRepository;
    private final SkuDimensionMapper skuDimensionMapper;

    @Override
    public SkuDimensionResponse createDimension(Long skuId, SkuDimensionRequest request) {
        log.info("📦 Creating dimension for SKU: {}", skuId);

        Sku sku = skuRepository.findById(skuId)
                .orElseThrow(() -> new ResourceNotFoundException("SKU not found with ID: " + skuId));

        // Check if dimension already exists
        if (skuDimensionRepository.existsBySkuId(skuId)) {
            throw new RuntimeException("Dimension already exists for SKU: " + skuId);
        }

        SkuDimension dimension = skuDimensionMapper.toEntity(request);
        dimension.setSku(sku);

        SkuDimension savedDimension = skuDimensionRepository.save(dimension);
        log.info("✅ Dimension created for SKU: {}", sku.getSkuCode());

        return skuDimensionMapper.toResponse(savedDimension);
    }

    @Override
    public SkuDimensionResponse getDimensionBySkuId(Long skuId) {
        log.info("📦 Getting dimension for SKU: {}", skuId);

        SkuDimension dimension = skuDimensionRepository.findBySkuId(skuId)
                .orElseThrow(() -> new ResourceNotFoundException("Dimension not found for SKU ID: " + skuId));

        return skuDimensionMapper.toResponse(dimension);
    }

    @Override
    public SkuDimensionResponse updateDimension(Long skuId, SkuDimensionRequest request) {
        log.info("📦 Updating dimension for SKU: {}", skuId);

        SkuDimension dimension = skuDimensionRepository.findBySkuId(skuId)
                .orElseThrow(() -> new ResourceNotFoundException("Dimension not found for SKU ID: " + skuId));

        skuDimensionMapper.updateEntity(dimension, request);

        SkuDimension updatedDimension = skuDimensionRepository.save(dimension);
        log.info("✅ Dimension updated for SKU: {}", skuId);

        return skuDimensionMapper.toResponse(updatedDimension);
    }

    @Override
    public void deleteDimensionBySkuId(Long skuId) {
        log.info("📦 Deleting dimension for SKU: {}", skuId);

        if (!skuDimensionRepository.existsBySkuId(skuId)) {
            throw new ResourceNotFoundException("Dimension not found for SKU ID: " + skuId);
        }

        skuDimensionRepository.deleteBySkuId(skuId);
        log.info("✅ Dimension deleted for SKU: {}", skuId);
    }
}