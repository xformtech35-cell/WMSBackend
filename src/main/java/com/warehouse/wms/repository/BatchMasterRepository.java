package com.warehouse.wms.repository;

import com.warehouse.wms.entity.BatchMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BatchMasterRepository extends JpaRepository<BatchMaster, Long>, JpaSpecificationExecutor<BatchMaster> {
    
    Optional<BatchMaster> findByBatchCode(String batchCode);
    
    boolean existsByBatchCode(String batchCode);
}