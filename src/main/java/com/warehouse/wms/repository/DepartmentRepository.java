package com.warehouse.wms.repository;

import com.warehouse.wms.entity.Department;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {

    boolean existsByCode(String code);

    Optional<Department> findByCode(String code);

    // Search & Filter with Pagination
    @Query("SELECT d FROM Department d WHERE " +
           "(:keyword IS NULL OR LOWER(d.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(d.code) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(d.headOfDepartment) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:location IS NULL OR LOWER(d.location) LIKE LOWER(CONCAT('%', :location, '%'))) " +
           "AND (:isActive IS NULL OR d.isActive = :isActive)")
    Page<Department> searchDepartments(@Param("keyword") String keyword,
                                       @Param("location") String location,
                                       @Param("isActive") Boolean isActive,
                                       Pageable pageable);
}