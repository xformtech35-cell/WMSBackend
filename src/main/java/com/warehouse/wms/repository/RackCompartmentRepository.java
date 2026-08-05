// ====== FILE: src/main/java/com/warehouse/wms/repository/RackCompartmentRepository.java ======
package com.warehouse.wms.repository;

import com.warehouse.wms.entity.RackCompartment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface RackCompartmentRepository extends JpaRepository<RackCompartment, Long> {

    // ====== Basic Queries ======
    
    Optional<RackCompartment> findByCompartmentId(String compartmentId);
    
    List<RackCompartment> findByRackId(Long rackId);
    
    List<RackCompartment> findByRackIdAndIsActiveTrue(Long rackId);
    
    List<RackCompartment> findByIsActiveTrue();
    
    List<RackCompartment> findByTrolleyId(Long trolleyId);
    
    List<RackCompartment> findBySalesOrderId(Long salesOrderId);
    
    // ====== Paginated Queries ======
    
    Page<RackCompartment> findByRackId(Long rackId, Pageable pageable);
    
    Page<RackCompartment> findByIsActiveTrue(Pageable pageable);
    
    @Query("SELECT rc FROM RackCompartment rc WHERE rc.rack.id = :rackId AND rc.isActive = true")
    Page<RackCompartment> findActiveByRackId(@Param("rackId") Long rackId, Pageable pageable);
    
    // ====== Search Queries ======
    
    @Query("SELECT rc FROM RackCompartment rc WHERE rc.isActive = true AND " +
           "(LOWER(rc.compartmentId) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<RackCompartment> searchCompartments(@Param("search") String search, Pageable pageable);
    
    @Query("SELECT rc FROM RackCompartment rc WHERE rc.rack.id = :rackId AND " +
           "(LOWER(rc.compartmentId) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<RackCompartment> searchCompartmentsByRack(@Param("rackId") Long rackId, 
                                                    @Param("search") String search, 
                                                    Pageable pageable);
    
    // ====== Capacity Queries ======
    
    @Query("SELECT rc FROM RackCompartment rc WHERE rc.rack.id = :rackId " +
           "AND rc.availableCapacity >= :requiredCapacity AND rc.isActive = true " +
           "ORDER BY rc.level, rc.position")
    List<RackCompartment> findAvailableCompartments(@Param("rackId") Long rackId,
                                                     @Param("requiredCapacity") Integer requiredCapacity);
    
    @Query("SELECT SUM(rc.availableCapacity) FROM RackCompartment rc WHERE rc.rack.id = :rackId AND rc.isActive = true")
    Integer getTotalAvailableCapacityByRack(@Param("rackId") Long rackId);
    
    // ====== Count Queries ======
    
    long countByRackId(Long rackId);
    
    @Query("SELECT COUNT(rc) FROM RackCompartment rc WHERE rc.rack.id = :rackId AND rc.isActive = true")
    long countActiveByRackId(@Param("rackId") Long rackId);
    
    // ====== Existence Checks ======
    
    boolean existsByCompartmentId(String compartmentId);
    
    boolean existsByCompartmentIdAndIdNot(String compartmentId, Long id);
    
    // ====== Update Queries ======
    
    @Modifying
    @Transactional
    @Query("UPDATE RackCompartment rc SET rc.isActive = :isActive WHERE rc.id = :id")
    int updateActiveStatus(@Param("id") Long id, @Param("isActive") Boolean isActive);
    
    @Modifying
    @Transactional
    @Query("UPDATE RackCompartment rc SET rc.usedCapacity = rc.usedCapacity + :quantity, " +
           "rc.availableCapacity = rc.availableCapacity - :quantity " +
           "WHERE rc.id = :id AND rc.availableCapacity >= :quantity")
    int allocateCapacity(@Param("id") Long id, @Param("quantity") Integer quantity);
    
    @Modifying
    @Transactional
    @Query("UPDATE RackCompartment rc SET rc.usedCapacity = rc.usedCapacity - :quantity, " +
           "rc.availableCapacity = rc.availableCapacity + :quantity " +
           "WHERE rc.id = :id AND rc.usedCapacity >= :quantity")
    int releaseCapacity(@Param("id") Long id, @Param("quantity") Integer quantity);
}