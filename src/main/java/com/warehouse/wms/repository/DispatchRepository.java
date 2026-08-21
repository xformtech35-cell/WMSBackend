package com.warehouse.wms.repository;

import com.warehouse.wms.entity.Dispatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface DispatchRepository extends JpaRepository<Dispatch, Long> {

    Optional<Dispatch> findByDispatchNumber(String dispatchNumber);

    Optional<Dispatch> findByShipmentNumber(String shipmentNumber);

    List<Dispatch> findBySoNumber(String soNumber);

    List<Dispatch> findByStatus(String status);

    @Modifying
    @Transactional
    @Query("UPDATE Dispatch d SET d.status = :status WHERE d.dispatchNumber = :dispatchNumber")
    void updateStatus(@Param("dispatchNumber") String dispatchNumber, @Param("status") String status);

    @Modifying
    @Transactional
    @Query("UPDATE Dispatch d SET d.shipmentNumber = :shipmentNumber WHERE d.dispatchNumber = :dispatchNumber")
    void updateShipmentNumber(@Param("dispatchNumber") String dispatchNumber, @Param("shipmentNumber") String shipmentNumber);
}