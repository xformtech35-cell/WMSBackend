package com.warehouse.wms.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.warehouse.wms.entity.Item;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    
    // Basic CRUD methods are inherited
    
    // Find by item code
    Optional<Item> findByItemCode(String itemCode);
    
    // Check if item code exists
    boolean existsByItemCode(String itemCode);
    
    // Find by name containing (for search)
    List<Item> findByItemNameContaining(String itemName);
    
    // Find by category
    List<Item> findByCategory(String category);
    
    // Find by brand
    List<Item> findByBrand(String brand);
    
    // Find active items
    List<Item> findByIsActiveTrue();
    
    // Find items needing reorder
    List<Item> findByCurrentStockLessThanEqual(Integer reorderLevel);
    
    // Find GST applicable items
    @Query("SELECT i FROM Item i WHERE i.isGstApplicable = true")
    List<Item> findGstApplicableItems();
    
    // Advanced search with filters
    @Query("SELECT i FROM Item i WHERE " +
           "(:itemCode IS NULL OR i.itemCode LIKE CONCAT('%', :itemCode, '%')) AND " +
           "(:itemName IS NULL OR i.itemName LIKE CONCAT('%', :itemName, '%')) AND " +
           "(:category IS NULL OR i.category = :category) AND " +
           "(:brand IS NULL OR i.brand = :brand) AND " +
           "(:isActive IS NULL OR i.isActive = :isActive) AND " +
           "(:minPrice IS NULL OR i.unitPrice >= :minPrice) AND " +
           "(:maxPrice IS NULL OR i.unitPrice <= :maxPrice) AND " +
           "(:minStock IS NULL OR i.currentStock >= :minStock) AND " +
           "(:maxStock IS NULL OR i.currentStock <= :maxStock) AND " +
           "(:isGstApplicable IS NULL OR i.isGstApplicable = :isGstApplicable)")
    Page<Item> searchItems(@Param("itemCode") String itemCode,
                          @Param("itemName") String itemName,
                          @Param("category") String category,
                          @Param("brand") String brand,
                          @Param("isActive") Boolean isActive,
                          @Param("minPrice") Double minPrice,
                          @Param("maxPrice") Double maxPrice,
                          @Param("minStock") Integer minStock,
                          @Param("maxStock") Integer maxStock,
                          @Param("isGstApplicable") Boolean isGstApplicable,
                          Pageable pageable);
    
    // Count by category
    Long countByCategory(String category);
    
    // Count by brand
    Long countByBrand(String brand);
    
    // Find low stock items
    @Query("SELECT i FROM Item i WHERE i.currentStock <= i.minStockLevel AND i.isActive = true")
    List<Item> findLowStockItems();
    
    // Find items by supplier
    List<Item> findBySupplierId(Long supplierId);
    
    
    
    
    
    @Query("SELECT i FROM Item i WHERE " +
            "(:itemCode IS NULL OR i.itemCode LIKE %:itemCode%) AND " +
            "(:itemName IS NULL OR LOWER(i.itemName) LIKE LOWER(CONCAT('%', :itemName, '%'))) AND " +
            "(:category IS NULL OR LOWER(i.category) LIKE LOWER(CONCAT('%', :category, '%'))) AND " +
            "(:brand IS NULL OR LOWER(i.brand) LIKE LOWER(CONCAT('%', :brand, '%'))) AND " +
            "(:uom IS NULL OR LOWER(i.uom) = LOWER(:uom)) AND " +
            "(:isActive IS NULL OR i.isActive = :isActive) AND " +
            "(:minPrice IS NULL OR i.unitPrice >= :minPrice) AND " +
            "(:maxPrice IS NULL OR i.unitPrice <= :maxPrice) AND " +
            "(:minStock IS NULL OR i.currentStock >= :minStock) AND " +
            "(:maxStock IS NULL OR i.currentStock <= :maxStock) AND " +
            "(:supplierId IS NULL OR i.supplierId = :supplierId) AND " +
            "(:isGstApplicable IS NULL OR i.isGstApplicable = :isGstApplicable) AND " +
            "(:startDate IS NULL OR i.createdAt >= :startDate) AND " +
            "(:endDate IS NULL OR i.createdAt <= :endDate)")
     Page<Item> findByFilters(
             @Param("itemCode") String itemCode,
             @Param("itemName") String itemName,
             @Param("category") String category,
             @Param("brand") String brand,
             @Param("uom") String uom,
             @Param("isActive") Boolean isActive,
             @Param("minPrice") Double minPrice,
             @Param("maxPrice") Double maxPrice,
             @Param("minStock") Integer minStock,
             @Param("maxStock") Integer maxStock,
             @Param("supplierId") Long supplierId,
             @Param("isGstApplicable") Boolean isGstApplicable,
             @Param("startDate") LocalDateTime startDate,
             @Param("endDate") LocalDateTime endDate,
             Pageable pageable);

     // ====== Search ======
     @Query("SELECT i FROM Item i WHERE " +
            "LOWER(i.itemCode) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(i.itemName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(i.description) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(i.category) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(i.brand) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(i.uom) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(i.gstHsnCode) LIKE LOWER(CONCAT('%', :search, '%'))")
     Page<Item> searchItems(@Param("search") String search, Pageable pageable);
}