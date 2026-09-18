package com.warehouse.wms.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.warehouse.wms.entity.DeliveryReturn;

public interface DeliveryReturnRepository extends JpaRepository<DeliveryReturn, Long> {

    Optional<DeliveryReturn> findByReturnNumber(String returnNumber);

    List<DeliveryReturn> findByDeliveryId(Long deliveryId);

    List<DeliveryReturn> findByReturnStatus(String returnStatus);

    boolean existsByReturnNumber(String returnNumber);

    boolean existsByDeliveryIdAndReturnStatusNotIn(Long deliveryId, List<String> statuses);
    
    
    
    
//    Optional<DeliveryReturn> findByReturnNumber(String returnNumber);
//
//    List<DeliveryReturn> findByDeliveryId(Long deliveryId);
//
//    List<DeliveryReturn> findByReturnStatus(String returnStatus);
//
//    boolean existsByReturnNumber(String returnNumber);

    // ============================================================
    // SEARCH + FILTER
    // ============================================================
    @Query("""
        SELECT DISTINCT r FROM DeliveryReturn r
        LEFT JOIN r.items i
        WHERE (:search IS NULL OR (
                 LOWER(r.returnNumber)    LIKE LOWER(CONCAT('%', :search, '%')) OR
                 LOWER(r.deliveryNumber)  LIKE LOWER(CONCAT('%', :search, '%')) OR
                 LOWER(r.soNumber)        LIKE LOWER(CONCAT('%', :search, '%')) OR
                 LOWER(r.shipmentNumber)  LIKE LOWER(CONCAT('%', :search, '%')) OR
                 LOWER(r.customerCode)    LIKE LOWER(CONCAT('%', :search, '%')) OR
                 LOWER(r.customerName)    LIKE LOWER(CONCAT('%', :search, '%'))
              ))
          AND (:returnStatus     IS NULL OR r.returnStatus = :returnStatus)
          AND (:deliveryId       IS NULL OR r.deliveryId   = :deliveryId)
          AND (:deliveryNumber   IS NULL OR LOWER(r.deliveryNumber) = LOWER(:deliveryNumber))
          AND (:soNumber         IS NULL OR LOWER(r.soNumber)       = LOWER(:soNumber))
          AND (:shipmentNumber   IS NULL OR LOWER(r.shipmentNumber) = LOWER(:shipmentNumber))
          AND (:customerCode     IS NULL OR LOWER(r.customerCode)   = LOWER(:customerCode))
          AND (:customerName     IS NULL OR LOWER(r.customerName)   LIKE LOWER(CONCAT('%', :customerName, '%')))
          AND (:requestedBy      IS NULL OR LOWER(r.returnRequestedBy) = LOWER(:requestedBy))
          AND (:approvedBy       IS NULL OR LOWER(r.approvedBy)        = LOWER(:approvedBy))
          AND (:receivedBy       IS NULL OR LOWER(r.returnReceivedBy)  = LOWER(:receivedBy))
          AND (:itemReturnStatus IS NULL OR i.itemReturnStatus = :itemReturnStatus)
          AND (:itemCode         IS NULL OR LOWER(i.itemCode) LIKE LOWER(CONCAT('%', :itemCode, '%')))
          AND (:fromDate         IS NULL OR r.returnRequestedAt >= :fromDate)
          AND (:toDate           IS NULL OR r.returnRequestedAt <= :toDate)
        """)
    Page<DeliveryReturn> searchReturns(
            @Param("search")           String search,
            @Param("returnStatus")     String returnStatus,
            @Param("itemReturnStatus") String itemReturnStatus,
            @Param("deliveryId")       Long deliveryId,
            @Param("deliveryNumber")   String deliveryNumber,
            @Param("soNumber")         String soNumber,
            @Param("shipmentNumber")   String shipmentNumber,
            @Param("customerCode")     String customerCode,
            @Param("customerName")     String customerName,
            @Param("itemCode")         String itemCode,
            @Param("requestedBy")      String requestedBy,
            @Param("approvedBy")       String approvedBy,
            @Param("receivedBy")       String receivedBy,
            @Param("fromDate")         LocalDateTime fromDate,
            @Param("toDate")           LocalDateTime toDate,
            Pageable pageable
    );
}