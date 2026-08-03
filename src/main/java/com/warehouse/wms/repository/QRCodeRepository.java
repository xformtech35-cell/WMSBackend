// ====== FILE: src/main/java/com/warehouse/wms/repository/QRCodeRepository.java ======
package com.warehouse.wms.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.warehouse.wms.constant.QRStatus;
import com.warehouse.wms.entity.QRCode;

@Repository
public interface QRCodeRepository extends JpaRepository<QRCode, Long> {

    Optional<QRCode> findByQrId(String qrId);

    Optional<QRCode> findByQrCode(String qrCode);

    Optional<QRCode> findByBarcode(String barcode);

    List<QRCode> findByPutawayTaskId(Long putawayTaskId);

    List<QRCode> findByBinId(String binId);

    List<QRCode> findByGrnNumber(String grnNumber);

    List<QRCode> findByItemCodeAndStatus(String itemCode, QRStatus status);

    List<QRCode> findByStatus(QRStatus status);

    @Query("SELECT q FROM QRCode q WHERE q.status = :status AND q.createdAt < :expiryTime")
    List<QRCode> findExpiredQRCodes(@Param("status") QRStatus status,
                                     @Param("expiryTime") LocalDateTime expiryTime);

    @Modifying
    @Transactional
    @Query("UPDATE QRCode q SET q.status = :status, q.scannedBy = :scannedBy, " +
           "q.scannedAt = :scannedAt, q.scanCount = q.scanCount + 1 WHERE q.qrCode = :qrCode")
    int markQRCodeAsScanned(@Param("qrCode") String qrCode,
                             @Param("status") QRStatus status,
                             @Param("scannedBy") String scannedBy,
                             @Param("scannedAt") LocalDateTime scannedAt);

    @Modifying
    @Transactional
    @Query("UPDATE QRCode q SET q.printedBy = :printedBy, q.printedAt = :printedAt, " +
           "q.printCopies = :printCopies, q.status = :status WHERE q.id = :id")
    int markQRCodeAsPrinted(@Param("id") Long id, @Param("printedBy") String printedBy,
                             @Param("printedAt") LocalDateTime printedAt,
                             @Param("printCopies") Integer printCopies,
                             @Param("status") QRStatus status);

    @Query("SELECT COUNT(q) FROM QRCode q WHERE q.status = :status AND q.labelType = :labelType")
    Long countByStatusAndLabelType(@Param("status") QRStatus status, 
                                    @Param("labelType") String labelType);

    @Query("SELECT q FROM QRCode q WHERE q.putawayTaskId = :putawayTaskId ORDER BY q.createdAt DESC")
    List<QRCode> findByPutawayTaskIdOrderByCreatedAtDesc(@Param("putawayTaskId") Long putawayTaskId);
}