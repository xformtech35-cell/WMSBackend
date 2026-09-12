package com.warehouse.wms.repository;

import com.warehouse.wms.entity.Gst;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface GstRepository extends JpaRepository<Gst, Long> {

    boolean existsByCode(String code);

    // Search & Filter with Pagination
    @Query("SELECT g FROM Gst g WHERE " +
           "(:keyword IS NULL OR LOWER(g.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(g.code) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:rate IS NULL OR g.rate = :rate) " +
           "AND (:active IS NULL OR g.active = :active)")
    Page<Gst> searchGst(@Param("keyword") String keyword,
                        @Param("rate") BigDecimal rate,
                        @Param("active") Boolean active,
                        Pageable pageable);
}