// ====== FILE: src/main/java/com/warehouse/wms/repository/TrolleyRepository.java ======
package com.warehouse.wms.repository;

import com.warehouse.wms.entity.Trolley;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TrolleyRepository extends JpaRepository<Trolley, Long> {

    // ====== Basic Queries ======
    
    Optional<Trolley> findByTrolleyIdentifier(String trolleyIdentifier);
    
    List<Trolley> findByStatus(String status);
    
    List<Trolley> findByIsActiveTrue();
    
    List<Trolley> findByStatusAndIsActiveTrue(String status);
    
    // ====== Type Queries ======
    
    List<Trolley> findByTrolleyType(String trolleyType);
    
    List<Trolley> findByTrolleyTypeAndIsActiveTrue(String trolleyType);
    
    // ====== Capacity Queries ======
    
    @Query("SELECT t FROM Trolley t WHERE t.capacity >= :minCapacity AND t.isActive = true")
    List<Trolley> findByMinCapacity(@Param("minCapacity") Integer minCapacity);
    
    @Query("SELECT t FROM Trolley t WHERE t.capacity >= :requiredWeight AND " +
           "(t.capacity - t.currentLoad) >= :requiredWeight AND " +
           "t.status = 'AVAILABLE' AND t.isActive = true")
    List<Trolley> findAvailableTrolleys(@Param("requiredWeight") Integer requiredWeight);
    
    @Query("SELECT t FROM Trolley t WHERE (t.capacity - t.currentLoad) > 0 AND t.isActive = true")
    List<Trolley> findTrolleysWithAvailableCapacity();
    
    // ====== Maintenance Queries ======
    
    @Query("SELECT t FROM Trolley t WHERE t.maintenanceDueDate <= :date AND t.isActive = true")
    List<Trolley> findTrolleysDueForMaintenance(@Param("date") LocalDateTime date);
    
    @Query("SELECT t FROM Trolley t WHERE t.maintenanceDueDate BETWEEN :startDate AND :endDate")
    List<Trolley> findTrolleysWithMaintenanceDueBetween(@Param("startDate") LocalDateTime startDate,
                                                        @Param("endDate") LocalDateTime endDate);
    
    // ====== Paginated Queries ======
    
    Page<Trolley> findByIsActiveTrue(Pageable pageable);
    
    Page<Trolley> findByStatus(String status, Pageable pageable);
    
    @Query("SELECT t FROM Trolley t WHERE t.isActive = true AND " +
           "(LOWER(t.trolleyIdentifier) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(t.name) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Trolley> searchTrolleys(@Param("search") String search, Pageable pageable);
    
    @Query("SELECT t FROM Trolley t WHERE t.isActive = true AND " +
           "(LOWER(t.trolleyIdentifier) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(t.name) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "t.status = :status")
    Page<Trolley> searchTrolleysByStatus(@Param("search") String search, 
                                         @Param("status") String status, 
                                         Pageable pageable);
    
    // ====== Count Queries ======
    
    long countByStatus(String status);
    
    @Query("SELECT t.status, COUNT(t) FROM Trolley t GROUP BY t.status")
    List<Object[]> countByStatusGroup();
    
    @Query("SELECT COUNT(t) FROM Trolley t WHERE t.isActive = true")
    long countActiveTrolleys();
    
    // ====== Existence Checks ======
    
    boolean existsByTrolleyIdentifier(String trolleyIdentifier);
    
    boolean existsByTrolleyIdentifierAndIdNot(String trolleyIdentifier, Long id);
    
    // ====== Update Queries ======
    
    @Modifying
    @Transactional
    @Query("UPDATE Trolley t SET t.status = :status WHERE t.id = :id")
    int updateStatus(@Param("id") Long id, @Param("status") String status);
    
    @Modifying
    @Transactional
    @Query("UPDATE Trolley t SET t.currentLoad = t.currentLoad + :weight, " +
           "t.lastUsedAt = CURRENT_TIMESTAMP WHERE t.id = :id")
    int addLoad(@Param("id") Long id, @Param("weight") Integer weight);
    
    @Modifying
    @Transactional
    @Query("UPDATE Trolley t SET t.currentLoad = t.currentLoad - :weight WHERE t.id = :id")
    int removeLoad(@Param("id") Long id, @Param("weight") Integer weight);
    
    @Modifying
    @Transactional
    @Query("UPDATE Trolley t SET t.isActive = :isActive WHERE t.id = :id")
    int updateActiveStatus(@Param("id") Long id, @Param("isActive") Boolean isActive);
}