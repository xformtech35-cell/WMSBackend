package com.warehouse.wms.repository;

import com.warehouse.wms.entity.DeliveryChallanPackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface DeliveryChallanPackageRepository extends JpaRepository<DeliveryChallanPackage, Long> {

    List<DeliveryChallanPackage> findByChallanNumber(String challanNumber);

    List<DeliveryChallanPackage> findByPackageNumber(String packageNumber);

    List<DeliveryChallanPackage> findByCustomerCode(String customerCode);

    @Modifying
    @Transactional
    @Query("UPDATE DeliveryChallanPackage dcp SET dcp.deliveredQuantity = :deliveredQuantity, dcp.status = :status WHERE dcp.id = :id")
    void updateDeliveredQuantityAndStatus(@Param("id") Long id, 
                                          @Param("deliveredQuantity") Integer deliveredQuantity, 
                                          @Param("status") String status);

    @Query("SELECT COALESCE(SUM(dcp.dispatchedQuantity), 0) FROM DeliveryChallanPackage dcp WHERE dcp.challanNumber = :challanNumber")
    Integer getTotalDispatchedQuantity(@Param("challanNumber") String challanNumber);

    @Query("SELECT COALESCE(SUM(dcp.deliveredQuantity), 0) FROM DeliveryChallanPackage dcp WHERE dcp.challanNumber = :challanNumber")
    Integer getTotalDeliveredQuantity(@Param("challanNumber") String challanNumber);
}