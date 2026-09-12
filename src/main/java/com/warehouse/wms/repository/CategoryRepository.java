package com.warehouse.wms.repository;

import com.warehouse.wms.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    boolean existsByCode(String code);

    // Search, Filter and Pagination
    @Query("SELECT c FROM Category c WHERE " +
           "(:keyword IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(c.code) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:active IS NULL OR c.active = :active) " +
           "AND (:parentId IS NULL OR c.parent.id = :parentId)")
    Page<Category> searchCategories(@Param("keyword") String keyword,
                                    @Param("active") Boolean active,
                                    @Param("parentId") Long parentId,
                                    Pageable pageable);
}