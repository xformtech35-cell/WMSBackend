package com.warehouse.wms.repository;

import com.warehouse.wms.entity.CountLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CountLineRepository extends JpaRepository<CountLine, Long> {
    List<CountLine> findByCountTaskId(Long countTaskId);
}
