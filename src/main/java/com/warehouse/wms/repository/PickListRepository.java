package com.warehouse.wms.repository;

import com.warehouse.wms.entity.PickList;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface PickListRepository extends JpaRepository<PickList, Long> {

    Optional<PickList> findByPickListNumber(String pickListNumber);

    List<PickList> findBySoNumber(String soNumber);

    List<PickList> findByStatus(String status);

    Page<PickList> findByStatus(String status, Pageable pageable);

    @Modifying
    @Transactional
    @Query("UPDATE PickList pl SET pl.status = :status WHERE pl.pickListNumber = :pickListNumber")
    void updateStatus(@Param("pickListNumber") String pickListNumber, @Param("status") String status);

    @Modifying
    @Transactional
    @Query("UPDATE PickList pl SET pl.completedDate = :completedDate WHERE pl.id = :id")
    void updateCompletedDate(@Param("id") Long id, @Param("completedDate") java.time.LocalDateTime completedDate);

    @Query("SELECT COALESCE(SUM(pl.totalQuantity), 0) FROM PickList pl WHERE pl.status = 'COMPLETED'")
    Integer getTotalPickedQuantity();
}