package com.warehouse.wms.repository;

import com.warehouse.wms.entity.ReturnDispatch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReturnDispatchRepository extends JpaRepository<ReturnDispatch, Long> {
    
    /**
     * Find dispatch by dispatch number
     */
    Optional<ReturnDispatch> findByDispatchNumber(String dispatchNumber);
    
    /**
     * Find dispatches by return order ID
     */
    List<ReturnDispatch> findByReturnOrderId(Long returnOrderId);
    
    /**
     * Find dispatches by status
     */
    List<ReturnDispatch> findByStatus(ReturnDispatch.DispatchStatus status);
    
    /**
     * Find active dispatches for an order
     */
    @Query("SELECT d FROM ReturnDispatch d WHERE d.returnOrder.id = :orderId AND d.status != 'RECEIVED'")
    List<ReturnDispatch> findActiveDispatchesByOrder(@Param("orderId") Long orderId);
    
    /**
     * Count dispatches by number prefix for generating sequence
     */
    @Query("SELECT COUNT(d) FROM ReturnDispatch d WHERE d.dispatchNumber LIKE CONCAT(:prefix, '%')")
    Long countByDispatchNumberStartingWith(@Param("prefix") String prefix);
    
    /**
     * Find dispatches by date range
     */
    List<ReturnDispatch> findByDispatchDateBetween(LocalDate startDate, LocalDate endDate);
    
    /**
     * Find dispatches by transporter
     */
    List<ReturnDispatch> findByTransporterNameContainingIgnoreCase(String transporterName);
    
    /**
     * Find dispatches with POD pending
     */
    @Query("SELECT d FROM ReturnDispatch d WHERE d.status IN ('CREATED', 'IN_TRANSIT') AND d.podReceived = false")
    List<ReturnDispatch> findDispatchesWithPODPending();
    
    /**
     * Count dispatches by transport mode
     */
    @Query("SELECT d.transportMode, COUNT(d) FROM ReturnDispatch d GROUP BY d.transportMode")
    List<Object[]> countByTransportMode();
    
    /**
     * Find dispatches by order with pagination
     */
    @Query("SELECT d FROM ReturnDispatch d WHERE d.returnOrder.id = :orderId")
    Page<ReturnDispatch> findByReturnOrderId(@Param("orderId") Long orderId, Pageable pageable);
    
    /**
     * Find dispatches by status and date
     */
    List<ReturnDispatch> findByStatusAndDispatchDateBefore(ReturnDispatch.DispatchStatus status, LocalDate date);
    
    /**
     * Find dispatches by LR or AWB number
     */
    @Query("SELECT d FROM ReturnDispatch d WHERE d.lrNumber = :trackingNumber OR d.awbNumber = :trackingNumber")
    Optional<ReturnDispatch> findByTrackingNumber(@Param("trackingNumber") String trackingNumber);
}