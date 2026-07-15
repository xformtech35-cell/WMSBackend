package com.warehouse.wms.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.warehouse.wms.entity.QuotationStatus;
import com.warehouse.wms.entity.VendorQuotation;

@Repository
public interface VendorQuotationRepository extends JpaRepository<VendorQuotation, Long> {
    
    List<VendorQuotation> findByRfqId(Long rfqId);
    
    List<VendorQuotation> findBySupplierId(Long supplierId);
    
    List<VendorQuotation> findByRfqIdAndStatus(Long rfqId, QuotationStatus status);
    
    Optional<VendorQuotation> findByQuotationNumber(String quotationNumber);
    
    @Query("SELECT vq FROM VendorQuotation vq WHERE vq.rfq.id = :rfqId ORDER BY vq.grandTotal ASC")
    List<VendorQuotation> findByRfqIdOrderByGrandTotalAsc(@Param("rfqId") Long rfqId);
    
//    @Query("SELECT vq FROM VendorQuotation vq WHERE vq.rfq.id = :rfqId AND vq.status = :status")
//    List<VendorQuotation> findByRfqIdAndStatus(@Param("rfqId") Long rfqId, @Param("status") QuotationStatus status);
    
    @Query("SELECT MIN(vq.grandTotal) FROM VendorQuotation vq WHERE vq.rfq.id = :rfqId")
    Double findMinGrandTotalByRfqId(@Param("rfqId") Long rfqId);
}