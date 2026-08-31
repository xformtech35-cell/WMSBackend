// ====== FILE: src/main/java/com/warehouse/wms/repository/WarehouseRepository.java ======
package com.warehouse.wms.repository;

import com.warehouse.wms.entity.Warehouse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {

    Optional<Warehouse> findByWarehouseId(String warehouseId);

    List<Warehouse> findByIsActiveTrue();

    @Query("SELECT w FROM Warehouse w WHERE w.isActive = true AND " +
           "(LOWER(w.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(w.warehouseId) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Warehouse> searchWarehouses(@Param("search") String search, Pageable pageable);

    boolean existsByWarehouseId(String warehouseId);
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
  List<Warehouse> findByNameContainingIgnoreCase(String name, Pageable pageable);
    
    Long countByNameContainingIgnoreCase(String name);
    
    List<Warehouse> findByWarehouseIdContainingIgnoreCase(String warehouseId, Pageable pageable);
    
    Long countByWarehouseIdContainingIgnoreCase(String warehouseId);
    
    List<Warehouse> findByIsActive(Boolean isActive, Pageable pageable);
    
    Long countByIsActive(Boolean isActive);
    

    
    @Query("SELECT COUNT(w) FROM Warehouse w WHERE " +
           "LOWER(w.name) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
           "LOWER(w.warehouseId) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
           "LOWER(w.location) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
           "LOWER(w.address) LIKE LOWER(CONCAT('%', :term, '%'))")
    Long countSearchWarehouses(@Param("term") String term);
}