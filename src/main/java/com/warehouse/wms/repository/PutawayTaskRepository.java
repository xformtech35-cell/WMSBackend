// ====== FILE: src/main/java/com/warehouse/wms/repository/PutawayTaskRepository.java ======
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

import com.warehouse.wms.constant.PutawayStatus;
import com.warehouse.wms.entity.PutawayTask;

@Repository
public interface PutawayTaskRepository extends JpaRepository<PutawayTask, Long> {

    Optional<PutawayTask> findByTaskNumber(String taskNumber);

    Optional<PutawayTask> findByGrnNumber(String grnNumber);

    List<PutawayTask> findByStatus(PutawayStatus status);

    List<PutawayTask> findByAssignedToAndStatus(String assignedTo, PutawayStatus status);

    List<PutawayTask> findByAssignedTo(String assignedTo);

    @Query("SELECT p FROM PutawayTask p WHERE p.status IN :statuses")
    List<PutawayTask> findByStatusIn(@Param("statuses") List<PutawayStatus> statuses);

    @Query("SELECT p FROM PutawayTask p WHERE p.createdAt BETWEEN :startDate AND :endDate")
    List<PutawayTask> findByCreatedDateRange(@Param("startDate") LocalDateTime startDate,
                                              @Param("endDate") LocalDateTime endDate);

    @Query("SELECT p FROM PutawayTask p WHERE p.confirmedAt IS NOT NULL " +
           "AND p.confirmedAt BETWEEN :startDate AND :endDate")
    List<PutawayTask> findConfirmedBetween(@Param("startDate") LocalDateTime startDate,
                                            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(p) FROM PutawayTask p WHERE p.status = :status")
    Long countByStatus(@Param("status") PutawayStatus status);

    @Query("SELECT p FROM PutawayTask p WHERE p.warehouseId = :warehouseId AND p.status = :status")
    List<PutawayTask> findByWarehouseAndStatus(@Param("warehouseId") String warehouseId,
                                                @Param("status") PutawayStatus status);

    @Query("SELECT p FROM PutawayTask p WHERE p.assignedTo IS NULL AND p.status = :status")
    List<PutawayTask> findUnassignedTasks(@Param("status") PutawayStatus status);

    Page<PutawayTask> findByStatus(PutawayStatus status, Pageable pageable);

    Page<PutawayTask> findByAssignedTo(String assignedTo, Pageable pageable);
}