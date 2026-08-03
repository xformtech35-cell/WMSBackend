// ====== FILE: src/main/java/com/warehouse/wms/repository/PutawayLineRepository.java ======
package com.warehouse.wms.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.warehouse.wms.constant.PutawayLineStatus;
import com.warehouse.wms.entity.PutawayLine;

@Repository
public interface PutawayLineRepository extends JpaRepository<PutawayLine, Long> {

    List<PutawayLine> findByPutawayTaskId(Long putawayTaskId);

    List<PutawayLine> findByPutawayTaskIdAndStatus(Long putawayTaskId, PutawayLineStatus status);

    @Query("SELECT p FROM PutawayLine p WHERE p.putawayTask.taskNumber = :taskNumber")
    List<PutawayLine> findByTaskNumber(@Param("taskNumber") String taskNumber);

    @Query("SELECT p FROM PutawayLine p WHERE p.inboundLine.id = :inboundLineId")
    List<PutawayLine> findByInboundLineId(@Param("inboundLineId") Long inboundLineId);

    @Query("SELECT p FROM PutawayLine p WHERE p.itemCode = :itemCode AND p.status = :status")
    List<PutawayLine> findByItemCodeAndStatus(@Param("itemCode") String itemCode,
                                               @Param("status") PutawayLineStatus status);

    @Query("SELECT SUM(p.quantity) FROM PutawayLine p WHERE p.putawayTask.id = :taskId")
    Integer getTotalQuantityByTaskId(@Param("taskId") Long taskId);

    @Query("SELECT SUM(p.putawayQuantity) FROM PutawayLine p WHERE p.putawayTask.id = :taskId")
    Integer getPutawayQuantityByTaskId(@Param("taskId") Long taskId);

    @Modifying
    @Transactional
    @Query("UPDATE PutawayLine p SET p.status = :status, p.remarks = :remarks WHERE p.id = :id")
    void updateStatus(@Param("id") Long id, 
                      @Param("status") PutawayLineStatus status, 
                      @Param("remarks") String remarks);

    Optional<PutawayLine> findByQrCodeValue(String qrCodeValue);

    @Query("SELECT p FROM PutawayLine p WHERE p.actualBin = :binId AND p.status = :status")
    List<PutawayLine> findByActualBinAndStatus(@Param("binId") String binId,
                                                @Param("status") PutawayLineStatus status);
}