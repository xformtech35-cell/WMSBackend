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

    @Override
    public BinResponse updateBin(Long id, BinCreateRequest request) {
        log.info("📦 Updating bin: {}", id);

        Bin bin = binRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bin not found with ID: " + id));

        // Check uniqueness if barcode is changed
        if (!request.getBarcode().equals(bin.getBarcode()) &&
            binRepository.existsByBarcode(request.getBarcode())) {
            throw new InvalidOperationException("Bin barcode already exists: " + request.getBarcode());
        }

        // ✅ Update level if changed (instead of rack)
        if (request.getLevelId() != null && !request.getLevelId().equals(bin.getLevel().getId())) {
            Level newLevel = levelRepository.findById(request.getLevelId())
                    .orElseThrow(() -> new ResourceNotFoundException("Level not found with ID: " + request.getLevelId()));
            
            // Update bin's level
            bin.setLevel(newLevel);
            // Update bin's rack from the new level
            bin.setRack(newLevel.getRack());
        }

        // Update fields
        bin.setBarcode(request.getBarcode());
        bin.setLengthCm(request.getLengthCm());
        bin.setWidthCm(request.getWidthCm());
        bin.setHeightCm(request.getHeightCm());
        bin.setMaxWeightG(request.getMaxWeightG());
        
        // Recalculate volume
        BigDecimal volume = request.getLengthCm()
                .multiply(request.getWidthCm())
                .multiply(request.getHeightCm());
        bin.setVolumeCm3(volume);

        Bin updatedBin = binRepository.save(bin);
        log.info("✅ Bin updated: {}", updatedBin.getBarcode());

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