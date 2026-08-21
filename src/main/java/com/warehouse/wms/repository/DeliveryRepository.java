package com.warehouse.wms.repository;

import com.warehouse.wms.entity.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, Long> {

    Optional<Delivery> findByDeliveryNumber(String deliveryNumber);

    Optional<Delivery> findByShipmentNumber(String shipmentNumber);

    List<Delivery> findBySoNumber(String soNumber);

    List<Delivery> findByDeliveryStatus(String deliveryStatus);

    @Modifying
    @Transactional
    @Query("UPDATE Delivery d SET d.deliveryStatus = :deliveryStatus WHERE d.deliveryNumber = :deliveryNumber")
    void updateDeliveryStatus(@Param("deliveryNumber") String deliveryNumber, @Param("deliveryStatus") String deliveryStatus);
}