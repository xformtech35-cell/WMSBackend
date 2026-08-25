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

import com.warehouse.wms.entity.PickConfirmation;

@Repository
public interface PickConfirmationRepository extends JpaRepository<PickConfirmation, Long> {

    Optional<PickConfirmation> findByConfirmationNumber(String confirmationNumber);

    List<PickConfirmation> findByPickTaskNumber(String pickTaskNumber);

    List<PickConfirmation> findBySoNumber(String soNumber);

    List<PickConfirmation> findByStatus(String status);

    @Modifying
    @Transactional
    @Query("UPDATE PickConfirmation pc SET pc.status = :status WHERE pc.confirmationNumber = :confirmationNumber")
    void updateStatus(@Param("confirmationNumber") String confirmationNumber, @Param("status") String status);
    
    
    
    @Query("SELECT pc FROM PickConfirmation pc WHERE " +
            "(:confirmationNumber IS NULL OR pc.confirmationNumber LIKE %:confirmationNumber%) AND " +
            "(:pickTaskNumber IS NULL OR pc.pickTaskNumber LIKE %:pickTaskNumber%) AND " +
            "(:pickListNumber IS NULL OR pc.pickListNumber LIKE %:pickListNumber%) AND " +
            "(:soNumber IS NULL OR pc.soNumber LIKE %:soNumber%) AND " +
            "(:itemCode IS NULL OR pc.itemCode LIKE %:itemCode%) AND " +
            "(:itemName IS NULL OR LOWER(pc.itemName) LIKE LOWER(CONCAT('%', :itemName, '%'))) AND " +
            "(:confirmedBy IS NULL OR LOWER(pc.confirmedBy) LIKE LOWER(CONCAT('%', :confirmedBy, '%'))) AND " +
            "(:status IS NULL OR pc.status = :status) AND " +
            "(:barcode IS NULL OR pc.barcode LIKE %:barcode%) AND " +
            "(:startDate IS NULL OR pc.createdAt >= :startDate) AND " +
            "(:endDate IS NULL OR pc.createdAt <= :endDate) AND " +
            "(:startConfirmedDate IS NULL OR pc.confirmedDate >= :startConfirmedDate) AND " +
            "(:endConfirmedDate IS NULL OR pc.confirmedDate <= :endConfirmedDate) AND " +
            "(:minPickedQuantity IS NULL OR pc.pickedQuantity >= :minPickedQuantity) AND " +
            "(:maxPickedQuantity IS NULL OR pc.pickedQuantity <= :maxPickedQuantity) AND " +
            "(:minShortQuantity IS NULL OR pc.shortQuantity >= :minShortQuantity) AND " +
            "(:maxShortQuantity IS NULL OR pc.shortQuantity <= :maxShortQuantity)")
     Page<PickConfirmation> findByFilters(
             @Param("confirmationNumber") String confirmationNumber,
             @Param("pickTaskNumber") String pickTaskNumber,
             @Param("pickListNumber") String pickListNumber,
             @Param("soNumber") String soNumber,
             @Param("itemCode") String itemCode,
             @Param("itemName") String itemName,
             @Param("confirmedBy") String confirmedBy,
             @Param("status") String status,
             @Param("barcode") String barcode,
             @Param("startDate") LocalDateTime startDate,
             @Param("endDate") LocalDateTime endDate,
             @Param("startConfirmedDate") LocalDateTime startConfirmedDate,
             @Param("endConfirmedDate") LocalDateTime endConfirmedDate,
             @Param("minPickedQuantity") Integer minPickedQuantity,
             @Param("maxPickedQuantity") Integer maxPickedQuantity,
             @Param("minShortQuantity") Integer minShortQuantity,
             @Param("maxShortQuantity") Integer maxShortQuantity,
             Pageable pageable);

     // ====== SEARCH ======
     @Query("SELECT pc FROM PickConfirmation pc WHERE " +
            "LOWER(pc.confirmationNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(pc.pickTaskNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(pc.pickListNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(pc.soNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(pc.itemCode) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(pc.itemName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(pc.confirmedBy) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(pc.status) LIKE LOWER(CONCAT('%', :search, '%'))")
     Page<PickConfirmation> searchPickConfirmations(@Param("search") String search, Pageable pageable);
}