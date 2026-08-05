// ====== FILE: src/main/java/com/warehouse/wms/repository/SkuDimensionRepository.java ======
package com.warehouse.wms.repository;

import com.warehouse.wms.entity.SkuDimension;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface SkuDimensionRepository extends JpaRepository<SkuDimension, Long> {

    Optional<SkuDimension> findBySkuId(Long skuId);

    @Modifying
    @Transactional
    @Query("DELETE FROM SkuDimension sd WHERE sd.sku.id = :skuId")
    void deleteBySkuId(@Param("skuId") Long skuId);

    boolean existsBySkuId(Long skuId);
}