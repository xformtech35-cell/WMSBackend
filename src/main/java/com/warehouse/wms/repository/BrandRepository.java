package com.warehouse.wms.repository;

import com.warehouse.wms.entity.Brand;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BrandRepository extends JpaRepository<Brand, Long> {

    boolean existsByCode(String code);

    // Search & Filter with Pagination
    @Query("SELECT b FROM Brand b WHERE " +
           "(:keyword IS NULL OR LOWER(b.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(b.code) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:active IS NULL OR b.active = :active)")
    Page<Brand> searchBrands(@Param("keyword") String keyword,
                             @Param("active") Boolean active,
                             Pageable pageable);
}