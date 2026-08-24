package com.warehouse.wms.repository;

import com.warehouse.wms.entity.PickListItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface PickListItemRepository extends JpaRepository<PickListItem, Long> {

    List<PickListItem> findByPickListNumber(String pickListNumber);

    List<PickListItem> findByItemCode(String itemCode);

    List<PickListItem> findByStatus(String status);

    @Modifying
    @Transactional
    @Query("UPDATE PickListItem pli SET pli.pickedQuantity = :pickedQuantity, pli.status = :status WHERE pli.id = :id")
    void updatePickedQuantityAndStatus(@Param("id") Long id, @Param("pickedQuantity") Integer pickedQuantity, @Param("status") String status);

    @Modifying
    @Transactional
    @Query("UPDATE PickListItem pli SET pli.shortQuantity = :shortQuantity WHERE pli.id = :id")
    void updateShortQuantity(@Param("id") Long id, @Param("shortQuantity") Integer shortQuantity);

    @Query("SELECT COALESCE(SUM(pli.requiredQuantity), 0) FROM PickListItem pli WHERE pli.pickListNumber = :pickListNumber")
    Integer getTotalRequiredQuantity(@Param("pickListNumber") String pickListNumber);
    
    
    @Modifying
    @Transactional
    @Query("DELETE FROM PickListItem pli WHERE pli.pickListNumber = :pickListNumber")
    void deleteByPickListNumber(@Param("pickListNumber") String pickListNumber);
}