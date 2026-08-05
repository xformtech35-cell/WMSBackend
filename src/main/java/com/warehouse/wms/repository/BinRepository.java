// ====== FILE: src/main/java/com/warehouse/wms/repository/BinRepository.java ======
package com.warehouse.wms.repository;

import com.warehouse.wms.entity.Bin;
import com.warehouse.wms.entity.Bin.BinStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface BinRepository extends JpaRepository<Bin, Long> {

    // ====== Basic Queries ======
    
    Optional<Bin> findByBarcode(String barcode);
    
    List<Bin> findByRackId(Long rackId);
    
    List<Bin> findByStatus(BinStatus status);
    
    List<Bin> findByRackIdAndStatus(Long rackId, BinStatus status);
    
    // ====== Paginated Queries ======
    
    Page<Bin> findByRackId(Long rackId, Pageable pageable);
    
    Page<Bin> findByStatus(BinStatus status, Pageable pageable);
    
    // ====== Search Queries ======
    
    @Query("SELECT b FROM Bin b WHERE LOWER(b.barcode) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Bin> searchBins(@Param("search") String search, Pageable pageable);
    
    // ====== Capacity Queries ======
    
    @Query("SELECT b FROM Bin b WHERE b.rack.id = :rackId AND b.status = 'AVAILABLE' " +
           "AND (b.volumeCm3 - b.occupiedVolumeCm3) >= :requiredVolume " +
           "AND (b.maxWeightG - b.occupiedWeightG) >= :requiredWeight")
    List<Bin> findAvailableBins(@Param("rackId") Long rackId,
                                @Param("requiredVolume") BigDecimal requiredVolume,
                                @Param("requiredWeight") BigDecimal requiredWeight);
    
    // ====== Count Queries ======
    
    long countByRackId(Long rackId);
    
    @Query("SELECT COUNT(b) FROM Bin b WHERE b.rack.id = :rackId AND b.status = 'AVAILABLE'")
    long countAvailableByRackId(@Param("rackId") Long rackId);
    
    // ====== Existence Checks ======
    
    boolean existsByBarcode(String barcode);
    
    boolean existsByBarcodeAndIdNot(String barcode, Long id);
    
    // ====== Update Queries ======
    
    @Modifying
    @Transactional
    @Query("UPDATE Bin b SET b.occupiedVolumeCm3 = b.occupiedVolumeCm3 + :volume, " +
           "b.occupiedWeightG = b.occupiedWeightG + :weight, " +
           "b.status = CASE WHEN (b.volumeCm3 - b.occupiedVolumeCm3 - :volume) <= 0 THEN 'FULL' ELSE 'AVAILABLE' END " +
           "WHERE b.id = :id")
    int occupyBinSpace(@Param("id") Long id, 
                       @Param("volume") BigDecimal volume, 
                       @Param("weight") BigDecimal weight);
    
    @Modifying
    @Transactional
    @Query("UPDATE Bin b SET b.occupiedVolumeCm3 = b.occupiedVolumeCm3 - :volume, " +
           "b.occupiedWeightG = b.occupiedWeightG - :weight, " +
           "b.status = 'AVAILABLE' " +
           "WHERE b.id = :id")
    int releaseBinSpace(@Param("id") Long id, 
                        @Param("volume") BigDecimal volume, 
                        @Param("weight") BigDecimal weight);
    
    @Modifying
    @Transactional
    @Query("UPDATE Bin b SET b.status = :status WHERE b.id = :id")
    int updateBinStatus(@Param("id") Long id, @Param("status") BinStatus status);
}