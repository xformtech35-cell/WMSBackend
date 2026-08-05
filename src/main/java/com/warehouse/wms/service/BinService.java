// ====== FILE: src/main/java/com/warehouse/wms/service/BinService.java ======
package com.warehouse.wms.service;

import com.warehouse.wms.dto.BinCreateRequest;
import com.warehouse.wms.dto.BinResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface BinService {

    // ====== Create ======
    BinResponse createBin(BinCreateRequest request);

    // ====== Read ======
    BinResponse getBinById(Long id);

    BinResponse getBinByBarcode(String barcode);

    Page<BinResponse> getAllBins(Pageable pageable, String search, Long rackId);

    List<BinResponse> getBinsByRack(Long rackId);

    List<BinResponse> getAvailableBins(Long rackId, BigDecimal requiredVolume, BigDecimal requiredWeight);

    // ====== Update ======
    BinResponse updateBin(Long id, BinCreateRequest request);

    BinResponse occupyBinSpace(Long id, BigDecimal volume, BigDecimal weight);

    BinResponse releaseBinSpace(Long id, BigDecimal volume, BigDecimal weight);

    BinResponse updateBinStatus(Long id, String status);

    // ====== Delete ======
    void deleteBin(Long id);

    void deleteBinByBarcode(String barcode);
}