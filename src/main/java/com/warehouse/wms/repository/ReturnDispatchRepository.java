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
    
    
    
    
    @Query("""
    	    SELECT DISTINCT d FROM ReturnDispatch d
    	    LEFT JOIN d.returnOrder o
    	    LEFT JOIN d.items i
    	    WHERE (:dispatchNumber IS NULL OR LOWER(d.dispatchNumber) LIKE LOWER(CONCAT('%', :dispatchNumber, '%')))
    	      AND (:returnOrderId IS NULL OR o.id = :returnOrderId)
    	      AND (:vroNumber IS NULL OR LOWER(o.vroNumber) LIKE LOWER(CONCAT('%', :vroNumber, '%')))
    	      AND (:supplierName IS NULL OR LOWER(o.supplierName) LIKE LOWER(CONCAT('%', :supplierName, '%')))
    	      AND (:supplierCode IS NULL OR LOWER(o.supplierCode) LIKE LOWER(CONCAT('%', :supplierCode, '%')))
    	      AND (:transportMode IS NULL OR d.transportMode = :transportMode)
    	      AND (:transporterName IS NULL OR LOWER(d.transporterName) LIKE LOWER(CONCAT('%', :transporterName, '%')))
    	      AND (:transportCompany IS NULL OR LOWER(d.transportCompany) LIKE LOWER(CONCAT('%', :transportCompany, '%')))
    	      AND (:vehicleNumber IS NULL OR LOWER(d.vehicleNumber) LIKE LOWER(CONCAT('%', :vehicleNumber, '%')))
    	      AND (:driverName IS NULL OR LOWER(d.driverName) LIKE LOWER(CONCAT('%', :driverName, '%')))
    	      AND (:driverPhone IS NULL OR LOWER(d.driverPhone) LIKE LOWER(CONCAT('%', :driverPhone, '%')))
    	      AND (:lrNumber IS NULL OR LOWER(d.lrNumber) LIKE LOWER(CONCAT('%', :lrNumber, '%')))
    	      AND (:awbNumber IS NULL OR LOWER(d.awbNumber) LIKE LOWER(CONCAT('%', :awbNumber, '%')))
    	      AND (:returnChallanNumber IS NULL OR LOWER(d.returnChallanNumber) LIKE LOWER(CONCAT('%', :returnChallanNumber, '%')))
    	      AND (:status IS NULL OR d.status = :status)
    	      AND (:podReceived IS NULL OR d.podReceived = :podReceived)
    	      AND (:dispatchFromDate IS NULL OR d.dispatchDate >= :dispatchFromDate)
    	      AND (:dispatchToDate IS NULL OR d.dispatchDate <= :dispatchToDate)
    	      AND (:podFromDate IS NULL OR d.podDate >= :podFromDate)
    	      AND (:podToDate IS NULL OR d.podDate <= :podToDate)
    	      AND (:minWeight IS NULL OR d.totalWeight >= :minWeight)
    	      AND (:maxWeight IS NULL OR d.totalWeight <= :maxWeight)
    	      AND (:minVolume IS NULL OR d.totalVolume >= :minVolume)
    	      AND (:maxVolume IS NULL OR d.totalVolume <= :maxVolume)
    	      AND (:itemCode IS NULL OR EXISTS (
    	            SELECT 1 FROM ReturnDispatchItem di
    	            WHERE di.dispatch = d AND LOWER(di.itemCode) LIKE LOWER(CONCAT('%', :itemCode, '%'))
    	      ))
    	      AND (:itemName IS NULL OR EXISTS (
    	            SELECT 1 FROM ReturnDispatchItem dn
    	            WHERE dn.dispatch = d AND LOWER(dn.itemName) LIKE LOWER(CONCAT('%', :itemName, '%'))
    	      ))
    	      AND (:searchTerm IS NULL OR (
    	            LOWER(d.dispatchNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
    	         OR LOWER(d.lrNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
    	         OR LOWER(d.awbNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
    	         OR LOWER(d.vehicleNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
    	         OR LOWER(d.driverName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
    	         OR LOWER(d.transporterName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
    	         OR LOWER(o.vroNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
    	         OR LOWER(o.supplierName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
    	      ))
    	    """)
    	Page<ReturnDispatch> findAllWithFilters(
    	        @Param("dispatchNumber") String dispatchNumber,
    	        @Param("returnOrderId") Long returnOrderId,
    	        @Param("vroNumber") String vroNumber,
    	        @Param("supplierName") String supplierName,
    	        @Param("supplierCode") String supplierCode,
    	        @Param("transportMode") ReturnDispatch.TransportMode transportMode,
    	        @Param("transporterName") String transporterName,
    	        @Param("transportCompany") String transportCompany,
    	        @Param("vehicleNumber") String vehicleNumber,
    	        @Param("driverName") String driverName,
    	        @Param("driverPhone") String driverPhone,
    	        @Param("lrNumber") String lrNumber,
    	        @Param("awbNumber") String awbNumber,
    	        @Param("returnChallanNumber") String returnChallanNumber,
    	        @Param("status") ReturnDispatch.DispatchStatus status,
    	        @Param("podReceived") Boolean podReceived,
    	        @Param("dispatchFromDate") LocalDate dispatchFromDate,
    	        @Param("dispatchToDate") LocalDate dispatchToDate,
    	        @Param("podFromDate") LocalDate podFromDate,
    	        @Param("podToDate") LocalDate podToDate,
    	        @Param("minWeight") Double minWeight,
    	        @Param("maxWeight") Double maxWeight,
    	        @Param("minVolume") Double minVolume,
    	        @Param("maxVolume") Double maxVolume,
    	        @Param("itemCode") String itemCode,
    	        @Param("itemName") String itemName,
    	        @Param("searchTerm") String searchTerm,
    	        Pageable pageable
    	);
}