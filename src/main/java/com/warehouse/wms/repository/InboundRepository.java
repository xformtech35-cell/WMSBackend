package com.warehouse.wms.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.warehouse.wms.entity.Inbound;
import com.warehouse.wms.entity.InboundStage;
import com.warehouse.wms.entity.InboundStatus;

@Repository
public interface InboundRepository extends JpaRepository<Inbound, Long> {
    
    Optional<Inbound> findByInboundNumber(String inboundNumber);
    
    boolean existsByPurchaseOrderId(Long purchaseOrderId);
    
    List<Inbound> findByStatus(InboundStatus status);
    
    Page<Inbound> findByStatus(InboundStatus status, Pageable pageable);
    
    Long countByInboundNumberStartingWith(String prefix);
    
    @Query("SELECT i FROM Inbound i WHERE " +
           "(:status IS NULL OR i.status = :status) AND " +
           "(:stage IS NULL OR i.stage = :stage) AND " +
           "(:poNumber IS NULL OR i.poNumber LIKE CONCAT('%', :poNumber, '%')) AND " +
           "(:supplierName IS NULL OR i.supplierName LIKE CONCAT('%', :supplierName, '%')) AND " +
           "(:searchTerm IS NULL OR " +
           "i.inboundNumber LIKE CONCAT('%', :searchTerm, '%') OR " +
           "i.poNumber LIKE CONCAT('%', :searchTerm, '%') OR " +
           "i.supplierName LIKE CONCAT('%', :searchTerm, '%') OR " +
           "i.invoiceNumber LIKE CONCAT('%', :searchTerm, '%') OR " +
           "i.deliveryChallan LIKE CONCAT('%', :searchTerm, '%') OR " +
           "i.trackingNumber LIKE CONCAT('%', :searchTerm, '%'))")
    Page<Inbound> filterInbounds(
            @Param("status") InboundStatus status,
            @Param("stage") InboundStage stage,
            @Param("poNumber") String poNumber,
            @Param("supplierName") String supplierName,
            @Param("searchTerm") String searchTerm,
            Pageable pageable
    );
}