package com.warehouse.wms.repository;

import com.warehouse.wms.entity.DeliveryChallanItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface DeliveryChallanItemRepository extends JpaRepository<DeliveryChallanItem, Long> {

    List<DeliveryChallanItem> findByChallanNumber(String challanNumber);

    List<DeliveryChallanItem> findByItemCode(String itemCode);

    List<DeliveryChallanItem> findByStatus(String status);

    @Modifying
    @Transactional
    @Query("UPDATE DeliveryChallanItem dci SET dci.deliveredQuantity = :deliveredQuantity, dci.status = :status WHERE dci.id = :id")
    void updateDeliveredQuantityAndStatus(@Param("id") Long id, 
                                          @Param("deliveredQuantity") Integer deliveredQuantity, 
                                          @Param("status") String status);

    @Modifying
    @Transactional
    @Query("UPDATE DeliveryChallanItem dci SET dci.shortQuantity = :shortQuantity WHERE dci.id = :id")
    void updateShortQuantity(@Param("id") Long id, @Param("shortQuantity") Integer shortQuantity);

    @Query("SELECT COALESCE(SUM(dci.dispatchedQuantity), 0) FROM DeliveryChallanItem dci WHERE dci.challanNumber = :challanNumber")
    Integer getTotalDispatchedQuantity(@Param("challanNumber") String challanNumber);

    @Query("SELECT COALESCE(SUM(dci.deliveredQuantity), 0) FROM DeliveryChallanItem dci WHERE dci.challanNumber = :challanNumber")
    Integer getTotalDeliveredQuantity(@Param("challanNumber") String challanNumber);

    @Query("SELECT COALESCE(SUM(dci.shortQuantity), 0) FROM DeliveryChallanItem dci WHERE dci.challanNumber = :challanNumber")
    Integer getTotalShortQuantity(@Param("challanNumber") String challanNumber);
}