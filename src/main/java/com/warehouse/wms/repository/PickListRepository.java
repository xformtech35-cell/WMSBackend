package com.warehouse.wms.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.warehouse.wms.entity.PickList;

@Repository
public interface PickListRepository extends JpaRepository<PickList, Long> {

    Optional<PickList> findByPickListNumber(String pickListNumber);

    List<PickList> findBySoNumber(String soNumber);

    List<PickList> findByStatus(String status);

    Page<PickList> findByStatus(String status, Pageable pageable);

    @Modifying
    @Transactional
    @Query("UPDATE PickList pl SET pl.status = :status WHERE pl.pickListNumber = :pickListNumber")
    void updateStatus(@Param("pickListNumber") String pickListNumber, @Param("status") String status);

    @Modifying
    @Transactional
    @Query("UPDATE PickList pl SET pl.completedDate = :completedDate WHERE pl.id = :id")
    void updateCompletedDate(@Param("id") Long id, @Param("completedDate") java.time.LocalDateTime completedDate);

    @Query("SELECT COALESCE(SUM(pl.totalQuantity), 0) FROM PickList pl WHERE pl.status = 'COMPLETED'")
    Integer getTotalPickedQuantity();
    
    

    // ====== ADVANCED FILTERS ======
    @Query("SELECT DISTINCT pl FROM PickList pl " +
           "LEFT JOIN PickListItem pli ON pl.pickListNumber = pli.pickListNumber " +
           "WHERE " +
           "(:pickListNumber IS NULL OR pl.pickListNumber LIKE %:pickListNumber%) AND " +
           "(:soNumber IS NULL OR pl.soNumber LIKE %:soNumber%) AND " +
           "(:warehouseId IS NULL OR pl.warehouseId = :warehouseId) AND " +
           "(:status IS NULL OR pl.status = :status) AND " +
           "(:priority IS NULL OR pl.priority = :priority) AND " +
           "(:assignedTo IS NULL OR pl.assignedTo LIKE %:assignedTo%) AND " +
           "(:createdBy IS NULL OR pl.createdBy = :createdBy) AND " +
           "(:startDate IS NULL OR pl.createdAt >= :startDate) AND " +
           "(:endDate IS NULL OR pl.createdAt <= :endDate) AND " +
           "(:startCreatedDate IS NULL OR pl.createdAt >= :startCreatedDate) AND " +
           "(:endCreatedDate IS NULL OR pl.createdAt <= :endCreatedDate) AND " +
           "(:startCompletedDate IS NULL OR pl.completedDate >= :startCompletedDate) AND " +
           "(:endCompletedDate IS NULL OR pl.completedDate <= :endCompletedDate) AND " +
           "(:minTotalItems IS NULL OR pl.totalItems >= :minTotalItems) AND " +
           "(:maxTotalItems IS NULL OR pl.totalItems <= :maxTotalItems) AND " +
           "(:minTotalQuantity IS NULL OR pl.totalQuantity >= :minTotalQuantity) AND " +
           "(:maxTotalQuantity IS NULL OR pl.totalQuantity <= :maxTotalQuantity) AND " +
           "(:itemCode IS NULL OR pli.itemCode = :itemCode)")
    Page<PickList> findByFilters(
            @Param("pickListNumber") String pickListNumber,
            @Param("soNumber") String soNumber,
            @Param("warehouseId") String warehouseId,
            @Param("status") String status,
            @Param("priority") String priority,
            @Param("assignedTo") String assignedTo,
            @Param("createdBy") String createdBy,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("startCreatedDate") LocalDateTime startCreatedDate,
            @Param("endCreatedDate") LocalDateTime endCreatedDate,
            @Param("startCompletedDate") LocalDateTime startCompletedDate,
            @Param("endCompletedDate") LocalDateTime endCompletedDate,
            @Param("minTotalItems") Integer minTotalItems,
            @Param("maxTotalItems") Integer maxTotalItems,
            @Param("minTotalQuantity") Integer minTotalQuantity,
            @Param("maxTotalQuantity") Integer maxTotalQuantity,
            @Param("itemCode") String itemCode,
            Pageable pageable);

    // ====== SEARCH ======
    @Query("SELECT DISTINCT pl FROM PickList pl " +
           "LEFT JOIN PickListItem pli ON pl.pickListNumber = pli.pickListNumber " +
           "WHERE " +
           "LOWER(pl.pickListNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(pl.soNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(pl.warehouseId) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(pl.status) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(pl.assignedTo) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(pl.priority) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(pli.itemCode) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(pli.itemName) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<PickList> searchPickLists(@Param("search") String search, Pageable pageable);
    
    
    
}