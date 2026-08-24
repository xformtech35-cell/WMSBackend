package com.warehouse.wms.repository;

import com.warehouse.wms.entity.StockReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockReservationRepository extends JpaRepository<StockReservation, Long> {

    Optional<StockReservation> findByReservationNumber(String reservationNumber);

    List<StockReservation> findBySoNumber(String soNumber);

    List<StockReservation> findByItemCodeAndStatus(String itemCode, String status);

    List<StockReservation> findByStatus(String status);
    
    List<StockReservation> findBySalesOrderItemId(Long salesOrderItemId);


    @Modifying
    @Transactional
    @Query("UPDATE StockReservation sr SET sr.status = :status WHERE sr.reservationNumber = :reservationNumber")
    void updateStatus(@Param("reservationNumber") String reservationNumber, @Param("status") String status);

    @Modifying
    @Transactional
    @Query("UPDATE StockReservation sr SET sr.reservedQuantity = :reservedQuantity WHERE sr.id = :id")
    void updateReservedQuantity(@Param("id") Long id, @Param("reservedQuantity") Integer reservedQuantity);

    @Query("SELECT COALESCE(SUM(sr.reservedQuantity), 0) FROM StockReservation sr WHERE sr.itemCode = :itemCode AND sr.status = 'RESERVED'")
    Integer getTotalReservedQuantityByItem(@Param("itemCode") String itemCode);
}