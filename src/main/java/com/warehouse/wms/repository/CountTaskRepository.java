package com.warehouse.wms.repository;

import com.warehouse.wms.entity.CountTask;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CountTaskRepository extends JpaRepository<CountTask, Long> {
    List<CountTask> findByAssignedToIdAndStatusNot(Long userId, CountTask.CountTaskStatus status);
    List<CountTask> findByStatus(CountTask.CountTaskStatus status);
}
