package com.warehouse.wms.repository;

import com.warehouse.wms.entity.PurchaseReturn;
import com.warehouse.wms.entity.PurchaseReturn.ReturnStatus;
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
public interface PurchaseReturnRepository extends JpaRepository<PurchaseReturn, Long> {
    
    Optional<PurchaseReturn> findByReturnNumber(String returnNumber);
    
    List<PurchaseReturn> findBySupplierId(Long supplierId);
    
    List<PurchaseReturn> findByStatus(ReturnStatus status);
    
    List<PurchaseReturn> findByPoNumber(String poNumber);
    
    List<PurchaseReturn> findByGrnNumber(String grnNumber);
    
    @Query("SELECT pr FROM PurchaseReturn pr WHERE pr.returnDate BETWEEN :startDate AND :endDate")
    List<PurchaseReturn> findByReturnDateBetween(@Param("startDate") LocalDate startDate,
                                                  @Param("endDate") LocalDate endDate);
    
    @Query("SELECT pr FROM PurchaseReturn pr WHERE " +
           "(:status IS NULL OR pr.status = :status) AND " +
           "(:supplierName IS NULL OR pr.supplierName LIKE CONCAT('%', :supplierName, '%')) AND " +
           "(:searchTerm IS NULL OR " +
           "pr.returnNumber LIKE CONCAT('%', :searchTerm, '%') OR " +
           "pr.poNumber LIKE CONCAT('%', :searchTerm, '%') OR " +
           "pr.invoiceNumber LIKE CONCAT('%', :searchTerm, '%') OR " +
           "pr.supplierName LIKE CONCAT('%', :searchTerm, '%'))")
    Page<PurchaseReturn> searchPurchaseReturns(@Param("status") ReturnStatus status,
                                                @Param("supplierName") String supplierName,
                                                @Param("searchTerm") String searchTerm,
                                                Pageable pageable);
    
    @Query("SELECT COUNT(pr) FROM PurchaseReturn pr WHERE pr.status = :status")
    long countByStatus(@Param("status") ReturnStatus status);
    
    @Query("SELECT pr.status, COUNT(pr) FROM PurchaseReturn pr GROUP BY pr.status")
    List<Object[]> getStatusCounts();
    
    @Query("SELECT FUNCTION('MONTH', pr.returnDate), FUNCTION('YEAR', pr.returnDate), SUM(pr.totalAmount) " +
           "FROM PurchaseReturn pr WHERE pr.status = 'COMPLETED' " +
           "GROUP BY FUNCTION('YEAR', pr.returnDate), FUNCTION('MONTH', pr.returnDate) " +
           "ORDER BY FUNCTION('YEAR', pr.returnDate) DESC, FUNCTION('MONTH', pr.returnDate) DESC")
    List<Object[]> getMonthlyReturnAmounts();
    
    Long countByReturnNumberStartingWith(String prefix);
}