package com.warehouse.wms.repository;

import com.warehouse.wms.entity.PackageInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface PackageInfoRepository extends JpaRepository<PackageInfo, Long> {

    Optional<PackageInfo> findByPackageNumber(String packageNumber);

    Optional<PackageInfo> findByPackageBarcode(String packageBarcode);

    List<PackageInfo> findBySoNumber(String soNumber);

    List<PackageInfo> findByStatus(String status);

    @Modifying
    @Transactional
    @Query("UPDATE PackageInfo p SET p.status = :status WHERE p.packageNumber = :packageNumber")
    void updateStatus(@Param("packageNumber") String packageNumber, @Param("status") String status);

    @Query("SELECT COUNT(p) FROM PackageInfo p WHERE p.soNumber = :soNumber")
    Integer getPackageCountBySoNumber(@Param("soNumber") String soNumber);
}