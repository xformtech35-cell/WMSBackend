package com.warehouse.wms.repository;

import com.warehouse.wms.entity.Uom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UomRepository extends JpaRepository<Uom, Long> {

    boolean existsByCode(String code);

    // Search and Filter with Pagination
    @Query("SELECT u FROM Uom u WHERE " +
           "(:keyword IS NULL OR LOWER(u.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(u.code) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:active IS NULL OR u.active = :active)")
    Page<Uom> searchUoms(@Param("keyword") String keyword, 
                         @Param("active") Boolean active, 
                         Pageable pageable);
}