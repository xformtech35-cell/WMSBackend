package com.warehouse.wms.repository;

import com.warehouse.wms.entity.PickTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface PickTaskRepository extends JpaRepository<PickTask, Long> {

    Optional<PickTask> findByPickTaskNumber(String pickTaskNumber);

    List<PickTask> findByPickListNumber(String pickListNumber);

    List<PickTask> findByStatus(String status);

    List<PickTask> findByPickerId(String pickerId);

    @Modifying
    @Transactional
    @Query("UPDATE PickTask pt SET pt.status = :status, pt.pickedQuantity = :pickedQuantity, pt.scanTime = :scanTime, pt.isScanned = true, pt.pickerId = :pickerId, pt.pickerName = :pickerName WHERE pt.pickTaskNumber = :pickTaskNumber")
    void completePickTask(@Param("pickTaskNumber") String pickTaskNumber,
                         @Param("status") String status,
                         @Param("pickedQuantity") Integer pickedQuantity,
                         @Param("scanTime") java.time.LocalDateTime scanTime,
                         @Param("pickerId") String pickerId,
                         @Param("pickerName") String pickerName);

    @Modifying
    @Transactional
    @Query("UPDATE PickTask pt SET pt.pickedQuantity = :pickedQuantity WHERE pt.pickTaskNumber = :pickTaskNumber")
    void updatePickedQuantity(@Param("pickTaskNumber") String pickTaskNumber, @Param("pickedQuantity") Integer pickedQuantity);

    @Query("SELECT COALESCE(SUM(pt.pickedQuantity), 0) FROM PickTask pt WHERE pt.pickListNumber = :pickListNumber")
    Integer getTotalPickedQuantityByPickList(@Param("pickListNumber") String pickListNumber);
}