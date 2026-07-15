package com.warehouse.wms.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.warehouse.wms.entity.Supplier;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    Optional<Supplier> findByCode(String code);
    List<Supplier> findByIsActiveTrue();
    boolean existsByCode(String code);
    
    
    

    
    // Pageable find methods
    Page<Supplier> findByIsActive(Boolean isActive, Pageable pageable);
    
    // Exists methods
    
    // ============ SEARCH WITH SEARCH TERM ============
    
    @Query("SELECT s FROM Supplier s WHERE " +
           "(:searchTerm IS NULL OR :searchTerm = '' OR " +
           "LOWER(s.code) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(s.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(s.contactPerson) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(s.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "s.phone LIKE CONCAT('%', :searchTerm, '%') OR " +
           "LOWER(s.address) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(s.gstNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) AND " +
           "(:isActive IS NULL OR s.isActive = :isActive)")
    Page<Supplier> searchSuppliers(
            @Param("searchTerm") String searchTerm,
            @Param("isActive") Boolean isActive,
            Pageable pageable
    );
}