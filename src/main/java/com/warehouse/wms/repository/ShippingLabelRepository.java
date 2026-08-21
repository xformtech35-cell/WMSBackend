package com.warehouse.wms.repository;

import com.warehouse.wms.entity.ShippingLabel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShippingLabelRepository extends JpaRepository<ShippingLabel, Long> {

    Optional<ShippingLabel> findByLabelNumber(String labelNumber);

    Optional<ShippingLabel> findByTrackingNumber(String trackingNumber);

    List<ShippingLabel> findByPackageNumber(String packageNumber);

    List<ShippingLabel> findBySoNumber(String soNumber);

    List<ShippingLabel> findByLabelStatus(String labelStatus);

    @Modifying
    @Transactional
    @Query("UPDATE ShippingLabel sl SET sl.labelStatus = :labelStatus WHERE sl.labelNumber = :labelNumber")
    void updateLabelStatus(@Param("labelNumber") String labelNumber, @Param("labelStatus") String labelStatus);

    @Modifying
    @Transactional
    @Query("UPDATE ShippingLabel sl SET sl.trackingNumber = :trackingNumber WHERE sl.labelNumber = :labelNumber")
    void updateTrackingNumber(@Param("labelNumber") String labelNumber, @Param("trackingNumber") String trackingNumber);
}