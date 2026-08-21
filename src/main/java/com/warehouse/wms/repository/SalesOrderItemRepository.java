package com.warehouse.wms.repository;

import com.warehouse.wms.entity.SalesOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface SalesOrderItemRepository extends JpaRepository<SalesOrderItem, Long> {

    List<SalesOrderItem> findBySoNumber(String soNumber);

    List<SalesOrderItem> findByItemCode(String itemCode);

    @Modifying
    @Transactional
    @Query("UPDATE SalesOrderItem soi SET soi.reservedQuantity = :reservedQuantity WHERE soi.id = :id")
    void updateReservedQuantity(@Param("id") Long id, @Param("reservedQuantity") Integer reservedQuantity);

    @Modifying
    @Transactional
    @Query("UPDATE SalesOrderItem soi SET soi.pickedQuantity = :pickedQuantity WHERE soi.id = :id")
    void updatePickedQuantity(@Param("id") Long id, @Param("pickedQuantity") Integer pickedQuantity);

    @Query("SELECT COALESCE(SUM(soi.orderedQuantity), 0) FROM SalesOrderItem soi WHERE soi.soNumber = :soNumber")
    Integer getTotalQuantityBySoNumber(@Param("soNumber") String soNumber);
}