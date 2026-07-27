package com.warehouse.wms.repository;

import com.warehouse.wms.entity.InspectionImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface InspectionImageRepository extends JpaRepository<InspectionImage, Long> {
    
    List<InspectionImage> findByInboundLineIdAndIsDeletedFalse(Long lineId);
    
    List<InspectionImage> findByInboundIdAndIsDeletedFalse(Long inboundId);
    
    List<InspectionImage> findByInboundLineId(Long lineId);
    
    void deleteByInboundLineId(Long lineId);
}