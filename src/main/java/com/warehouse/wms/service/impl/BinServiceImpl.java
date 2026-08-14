// ====== FILE: src/main/java/com/warehouse/wms/service/impl/BinServiceImpl.java ======
package com.warehouse.wms.service.impl;




import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.warehouse.wms.dto.BinCreateRequest;
import com.warehouse.wms.dto.response.BinResponse;
import com.warehouse.wms.entity.Bin;
import com.warehouse.wms.entity.Bin.BinStatus;
import com.warehouse.wms.entity.Level;
import com.warehouse.wms.exception.InvalidOperationException;
import com.warehouse.wms.exception.ResourceNotFoundException;
import com.warehouse.wms.mapper.BinMapper;
import com.warehouse.wms.repository.BinRepository;
import com.warehouse.wms.repository.LevelRepository;
import com.warehouse.wms.repository.RackRepository;
import com.warehouse.wms.service.BinBarcodeService;
import com.warehouse.wms.service.BinService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BinServiceImpl implements BinService {

    private final BinRepository binRepository;
    private final RackRepository rackRepository;
    private final BinMapper binMapper;
    private final LevelRepository levelRepository;  // ✅ ADD THIS
    private final BinBarcodeService binBarcodeService;

 // ====== FILE: src/main/java/com/warehouse/wms/service/impl/BinServiceImpl.java ======
 // Update the createBin method

 @Override
 public BinResponse createBin(BinCreateRequest request) {
     log.info("📦 Creating bin with barcode: {}", request.getBarcode());

     // Validate barcode uniqueness
     if (binRepository.existsByBarcode(request.getBarcode())) {
         throw new InvalidOperationException("Bin barcode already exists: " + request.getBarcode());
     }

     // ✅ Validate level exists (instead of rack)
     Level level = levelRepository.findById(request.getLevelId())
             .orElseThrow(() -> new ResourceNotFoundException("Level not found with ID: " + request.getLevelId()));

     // Create bin
     Bin bin = binMapper.toEntity(request);
     bin.setLevel(level);  // ✅ Set level
     bin.setRack(level.getRack());  // ✅ Set rack from level
     
     // Calculate volume
     BigDecimal volume = request.getLengthCm()
             .multiply(request.getWidthCm())
             .multiply(request.getHeightCm());
     bin.setVolumeCm3(volume);
     bin.setOccupiedVolumeCm3(BigDecimal.ZERO);
     bin.setOccupiedWeightG(BigDecimal.ZERO);
     bin.setStatus(BinStatus.AVAILABLE);

     Bin savedBin = binRepository.save(bin);
     log.info("✅ Bin created: {}", savedBin.getBarcode());
     
     
     binBarcodeService.generateBinBarcode(
             savedBin.getLevel().getRack().getAisle().getZone().getWarehouse().getWarehouseId(),
             savedBin.getLevel().getRack().getAisle().getZone().getZoneId(),
             savedBin.getLevel().getRack().getAisle().getAisleId(),
             savedBin.getLevel().getRack().getRackId(),
             savedBin.getLevel().getLevelId(),
             savedBin.getBarcode());

     return binMapper.toResponse(savedBin);
 }

    @Override
    public BinResponse getBinById(Long id) {
        Bin bin = binRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bin not found with ID: " + id));
        return binMapper.toResponse(bin);
    }

    @Override
    public BinResponse getBinByBarcode(String barcode) {
        Bin bin = binRepository.findByBarcode(barcode)
                .orElseThrow(() -> new ResourceNotFoundException("Bin not found: " + barcode));
        return binMapper.toResponse(bin);
    }

    @Override
    public Page<BinResponse> getAllBins(Pageable pageable, String search, Long rackId) {
        if (search != null && !search.isEmpty()) {
            return binRepository.searchBins(search, pageable)
                    .map(binMapper::toResponse);
        } else if (rackId != null) {
            return binRepository.findByRackId(rackId, pageable)
                    .map(binMapper::toResponse);
        }
        return binRepository.findAll(pageable)
                .map(binMapper::toResponse);
    }

    @Override
    public List<BinResponse> getBinsByRack(Long rackId) {
        return binRepository.findByRackId(rackId)
                .stream()
                .map(binMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<BinResponse> getAvailableBins(Long rackId, BigDecimal requiredVolume, BigDecimal requiredWeight) {
        List<Bin> bins = binRepository.findAvailableBins(rackId, requiredVolume, requiredWeight);
        return bins.stream()
                .map(binMapper::toResponse)
                .collect(Collectors.toList());
    }

   // ====== FILE: src/main/java/com/warehouse/wms/service/impl/BinServiceImpl.java ======

// ====== FILE: src/main/java/com/warehouse/wms/service/impl/BinServiceImpl.java ======

@Override
@Transactional
public BinResponse updateBin(Long id, BinCreateRequest request) {
    log.info("📦 Updating bin: {}", id);

    Bin bin = binRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Bin not found with ID: " + id));

    // ====== CHECK BARCODE UNIQUENESS (EXCLUDE CURRENT BIN) ======
    if (request.getBarcode() != null && !request.getBarcode().isEmpty()) {
        String newBarcode = request.getBarcode();
        String currentBarcode = bin.getBarcode();
        
        // Only check if the barcode is actually changing
        if (!newBarcode.equals(currentBarcode)) {
            // ✅ FIX: Check if barcode exists for ANY OTHER bin (exclude current)
            boolean exists = binRepository.existsByBarcodeAndIdNot(newBarcode, id);
            if (exists) {
                throw new InvalidOperationException("Bin barcode already exists: " + newBarcode);
            }
            bin.setBarcode(newBarcode);
        }
    }

    // ====== UPDATE LEVEL IF CHANGED ======
    if (request.getLevelId() != null) {
        // Only update if level is different from current
        if (bin.getLevel() == null || !request.getLevelId().equals(bin.getLevel().getId())) {
            Level newLevel = levelRepository.findById(request.getLevelId())
                    .orElseThrow(() -> new ResourceNotFoundException("Level not found with ID: " + request.getLevelId()));
            
            bin.setLevel(newLevel);
            bin.setRack(newLevel.getRack());
            log.debug("Updated bin level to: {} and rack to: {}", 
                newLevel.getLevelId(), 
                newLevel.getRack() != null ? newLevel.getRack().getRackId() : "null");
        }
    }

    // ====== UPDATE FIELDS WITH NULL HANDLING ======
    if (request.getLengthCm() != null) {
        bin.setLengthCm(request.getLengthCm());
    }
    if (request.getWidthCm() != null) {
        bin.setWidthCm(request.getWidthCm());
    }
    if (request.getHeightCm() != null) {
        bin.setHeightCm(request.getHeightCm());
    }
    if (request.getMaxWeightG() != null) {
        bin.setMaxWeightG(request.getMaxWeightG());
    }
    if (request.getMaxCapacity() != null) {
        bin.setMaxCapacity(request.getMaxCapacity());
    }
    if (request.getMinCapacity() != null) {
        bin.setMinCapacity(request.getMinCapacity());
    }
    if (request.getCapacityUnit() != null) {
        bin.setCapacityUnit(request.getCapacityUnit());
    }
    if (request.getUnit() != null) {
        bin.setUnit(request.getUnit());
    }
   

    // ====== RECALCULATE VOLUME ======
    if (request.getLengthCm() != null || request.getWidthCm() != null || request.getHeightCm() != null) {
        BigDecimal volume = bin.getLengthCm()
                .multiply(bin.getWidthCm())
                .multiply(bin.getHeightCm());
        bin.setVolumeCm3(volume);
        log.debug("Recalculated volume: {}", volume);
    }

    // ====== GENERATE BARCODE IMAGE IF MISSING ======
    try {
        if (bin.getBarcodeImage() == null && bin.getLevel() != null) {
            binBarcodeService.generateBinBarcode(
                    bin.getLevel().getRack().getAisle().getZone().getWarehouse().getWarehouseId(),
                    bin.getLevel().getRack().getAisle().getZone().getZoneId(),
                    bin.getLevel().getRack().getAisle().getAisleId(),
                    bin.getLevel().getRack().getRackId(),
                    bin.getLevel().getLevelId(),
                    bin.getBarcode());
            log.debug("Generated barcode image for bin: {}", bin.getBarcode());
        }
    } catch (Exception e) {
        log.warn("Failed to generate barcode image: {}", e.getMessage());
    }

    // ====== UPDATE BARCODE DATA ======
    String fullLocation = bin.getFullLocation();
    if (fullLocation != null && !fullLocation.equals(bin.getBarcodeData())) {
        bin.setBarcodeData(fullLocation);
    }

    Bin updatedBin = binRepository.save(bin);
    log.info("✅ Bin updated successfully: {}", updatedBin.getBarcode());

    return binMapper.toResponse(updatedBin);
}

    @Override
    public BinResponse occupyBinSpace(Long id, BigDecimal volume, BigDecimal weight) {
        log.info("📦 Occupying bin space: {} - volume: {}, weight: {}", id, volume, weight);

        Bin bin = binRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bin not found with ID: " + id));

        if (!bin.hasAvailableSpace(volume, weight)) {
            throw new InvalidOperationException("Insufficient space in bin: " + bin.getBarcode());
        }

        int updated = binRepository.occupyBinSpace(id, volume, weight);
        if (updated == 0) {
            throw new InvalidOperationException("Failed to occupy bin space.");
        }

        Bin updatedBin = binRepository.findById(id).get();
        log.info("✅ Bin space occupied: {}", updatedBin.getBarcode());

        return binMapper.toResponse(updatedBin);
    }

    @Override
    public BinResponse releaseBinSpace(Long id, BigDecimal volume, BigDecimal weight) {
        log.info("📦 Releasing bin space: {} - volume: {}, weight: {}", id, volume, weight);

        Bin bin = binRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bin not found with ID: " + id));

        int updated = binRepository.releaseBinSpace(id, volume, weight);
        if (updated == 0) {
            throw new InvalidOperationException("Failed to release bin space.");
        }

        Bin updatedBin = binRepository.findById(id).get();
        log.info("✅ Bin space released: {}", updatedBin.getBarcode());

        return binMapper.toResponse(updatedBin);
    }

    @Override
    public BinResponse updateBinStatus(Long id, String status) {
        log.info("📦 Updating bin status: {} to {}", id, status);

        Bin bin = binRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bin not found with ID: " + id));

        BinStatus binStatus = BinStatus.valueOf(status.toUpperCase());
        int updated = binRepository.updateBinStatus(id, binStatus);
        if (updated == 0) {
            throw new InvalidOperationException("Failed to update bin status.");
        }

        Bin updatedBin = binRepository.findById(id).get();
        log.info("✅ Bin status updated: {} -> {}", updatedBin.getBarcode(), status);

        return binMapper.toResponse(updatedBin);
    }

    @Override
    public void deleteBin(Long id) {
        log.info("📦 Deleting bin: {}", id);

        Bin bin = binRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bin not found with ID: " + id));

        bin.setIsActive(false);
        binRepository.save(bin);

        log.info("✅ Bin deactivated: {}", id);
    }

    @Override
    public void deleteBinByBarcode(String barcode) {
        log.info("📦 Deleting bin by barcode: {}", barcode);

        Bin bin = binRepository.findByBarcode(barcode)
                .orElseThrow(() -> new ResourceNotFoundException("Bin not found: " + barcode));

        bin.setIsActive(false);
        binRepository.save(bin);

        log.info("✅ Bin deactivated: {}", barcode);
    }
}