package com.warehouse.wms.repository;

import com.warehouse.wms.entity.InboundLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InboundLineRepository extends JpaRepository<InboundLine, Long> {
    
    List<InboundLine> findByInboundId(Long inboundId);
    
    List<InboundLine> findByInboundIdAndQualityStatus(Long inboundId, String qualityStatus);
}