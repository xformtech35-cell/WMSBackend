// ====== FILE: src/main/java/com/warehouse/wms/repository/LevelRepository.java ======
package com.warehouse.wms.repository;

import com.warehouse.wms.entity.Level;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LevelRepository extends JpaRepository<Level, Long> {

    Optional<Level> findByLevelId(String levelId);

    List<Level> findByRackId(Long rackId);

    List<Level> findByRackIdAndIsActiveTrue(Long rackId);

    Page<Level> findByRackId(Long rackId, Pageable pageable);

    @Query("SELECT l FROM Level l WHERE l.rack.id = :rackId ORDER BY l.levelNumber ASC")
    List<Level> findByRackIdOrderByLevelNumberAsc(@Param("rackId") Long rackId);

    boolean existsByLevelId(String levelId);

    long countByRackId(Long rackId);
}