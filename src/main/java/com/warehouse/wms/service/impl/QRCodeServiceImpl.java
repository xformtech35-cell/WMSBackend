// ====== FILE: src/main/java/com/warehouse/wms/service/impl/QRCodeServiceImpl.java ======
package com.warehouse.wms.service.impl;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.warehouse.wms.constant.QRStatus;
import com.warehouse.wms.dto.request.QRCodeGenerateRequest;
import com.warehouse.wms.dto.request.QRCodePrintRequest;
import com.warehouse.wms.dto.response.QRCodeResponse;
import com.warehouse.wms.entity.InboundLine;
import com.warehouse.wms.entity.QRCode;
import com.warehouse.wms.exception.ResourceNotFoundException;
import com.warehouse.wms.mapper.QRCodeMapper;
import com.warehouse.wms.repository.InboundLineRepository;
import com.warehouse.wms.repository.QRCodeRepository;
import com.warehouse.wms.service.QRCodeService;
import com.warehouse.wms.util.BarcodeGenerator;
import com.warehouse.wms.util.QRCodeGenerator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class QRCodeServiceImpl implements QRCodeService {

    private final QRCodeRepository qrCodeRepository;
    
    private final InboundLineRepository inboundLineRepository;

    private final QRCodeMapper qrCodeMapper;
    private final QRCodeGenerator qrCodeGenerator;
    private final BarcodeGenerator barcodeGenerator;
    private final ObjectMapper objectMapper;

    private static final String QR_ID_PREFIX = "QR";
    private static final String BARCODE_PREFIX = "BAR";

    @Override
    public QRCodeResponse generateQRCode(QRCodeGenerateRequest request) {
        log.info("Generating QR Code for item: {}", request.getItemCode());

        // Generate QR Code and Barcode
        String qrCodeValue = generateQRCodeValue(request);
        String barcodeValue = generateBarcodeValue(request);
        String qrId = generateQRId();
        
        
        Long inboundLineId = request.getInboundLineId();
        if (inboundLineId != null) {
            InboundLine inboundLine = inboundLineRepository.findById(inboundLineId)
                    .orElseThrow(() -> new ResourceNotFoundException("Inbound Line not found with ID: " + inboundLineId));
            
            // Set barcodeGenerate to true
            inboundLine.setBarcodeGenerate(true);
            inboundLineRepository.save(inboundLine);
            log.info("✅ Barcode generate flag set to true for inbound line: {}", inboundLineId);
        }
        // Generate images
        String qrImage = qrCodeGenerator.generateQRCodeBase64(qrCodeValue, request.getItemCode());
        String barcodeImage = barcodeGenerator.generateBarcodeBase64(barcodeValue, request.getItemCode());

        // Build QR Data JSON
        Map<String, Object> qrData = buildQRData(request, qrCodeValue, barcodeValue);
        String qrDataJson = toJson(qrData);

        QRCode qrCode = QRCode.builder()
                .qrId(qrId)
                .qrCode(qrCodeValue)
                .qrImage(qrImage)
                .qrData(qrDataJson)
                .barcode(barcodeValue)
                .barcodeImage(barcodeImage)
                .qrType(request.getQrType())
                .labelLevel(request.getLabelLevel())
                .labelType(request.getLabelType())
                .grnNumber(request.getGrnNumber())
                .putawayTaskId(request.getPutawayTaskId())
                .putawayLineId(request.getPutawayLineId())
                .itemCode(request.getItemCode())
                .itemName(request.getItemName())
                .batchNumber(request.getBatchNumber())
                .quantity(request.getQuantity())
                .uom(request.getUom())
                .warehouseId(request.getWarehouseId())
                .zone(request.getZone())
                .aisle(request.getAisle())
                .rack(request.getRack())
                .shelf(request.getShelf())
                .binId(request.getBinId())
                .status(QRStatus.GENERATED)
                .generatedBy(request.getGeneratedBy())
                .templateName(request.getTemplateName())
                .labelFormat(request.getLabelFormat())
                .remarks(request.getRemarks())
                .build();

        QRCode savedQRCode = qrCodeRepository.save(qrCode);
        log.info("QR Code generated successfully with ID: {}", savedQRCode.getQrId());

        return qrCodeMapper.toResponse(savedQRCode);
    }

    @Override
    public List<QRCodeResponse> generateBatchQRCodes(List<QRCodeGenerateRequest> requests) {
        log.info("Generating batch QR Codes, count: {}", requests.size());
        List<QRCodeResponse> responses = new ArrayList<>();
        for (QRCodeGenerateRequest request : requests) {
            try {
                QRCodeResponse response = generateQRCode(request);
                responses.add(response);
            } catch (Exception e) {
                log.error("Error generating QR Code for request: {}", request, e);
                throw new RuntimeException("Failed to generate QR Code: " + e.getMessage());
            }
        }
        return responses;
    }

    @Override
    public QRCodeResponse getQRCodeById(Long id) {
        QRCode qrCode = qrCodeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("QR Code not found with ID: " + id));
        return qrCodeMapper.toResponse(qrCode);
    }

    @Override
    public QRCodeResponse getQRCodeByCode(String qrCode) {
        QRCode qrCodeEntity = qrCodeRepository.findByQrCode(qrCode)
                .orElseThrow(() -> new ResourceNotFoundException("QR Code not found: " + qrCode));
        return qrCodeMapper.toResponse(qrCodeEntity);
    }

    @Override
    public QRCodeResponse getQRCodeByBarcode(String barcode) {
        QRCode qrCode = qrCodeRepository.findByBarcode(barcode)
                .orElseThrow(() -> new ResourceNotFoundException("Barcode not found: " + barcode));
        return qrCodeMapper.toResponse(qrCode);
    }

    @Override
    public List<QRCodeResponse> getQRCodesByTaskId(Long taskId) {
        List<QRCode> qrCodes = qrCodeRepository.findByPutawayTaskId(taskId);
        return qrCodes.stream()
                .map(qrCodeMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<QRCodeResponse> getQRCodesByGrnNumber(String grnNumber) {
        List<QRCode> qrCodes = qrCodeRepository.findByGrnNumber(grnNumber);
        return qrCodes.stream()
                .map(qrCodeMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public QRCodeResponse printQRCode(QRCodePrintRequest request) {
        log.info("Printing QR Codes, count: {}", request.getQrCodeIds().size());

        List<QRCode> qrCodes = qrCodeRepository.findAllById(request.getQrCodeIds());
        
        for (QRCode qrCode : qrCodes) {
            qrCode.setStatus(QRStatus.PRINTED);
            qrCode.setPrintedBy(request.getPrintedBy());
            qrCode.setPrintedAt(LocalDateTime.now());
            qrCode.setPrintCopies(request.getPrintCopies());
        }

        List<QRCode> updatedQRCodes = qrCodeRepository.saveAll(qrCodes);
        
        if (!updatedQRCodes.isEmpty()) {
            return qrCodeMapper.toResponse(updatedQRCodes.get(0));
        }
        throw new RuntimeException("Failed to print QR Codes");
    }

    @Override
@Transactional
public QRCodeResponse scanQRCode(String qrCode, String scannedBy) {
    log.info("Scanning QR Code: {}", qrCode);
    
    // Validate inputs
    if (qrCode == null || qrCode.trim().isEmpty()) {
        throw new IllegalArgumentException("QR Code cannot be empty");
    }
    if (scannedBy == null || scannedBy.trim().isEmpty()) {
        throw new IllegalArgumentException("Scanned by cannot be empty");
    }
    
    QRCode qrCodeEntity = qrCodeRepository.findByQrCode(qrCode)
            .orElseThrow(() -> new ResourceNotFoundException("QR Code not found: " + qrCode));

    if (qrCodeEntity.getStatus() == QRStatus.USED) {
        throw new IllegalStateException("QR Code already used");
    }
    
    if (qrCodeEntity.getStatus() == QRStatus.EXPIRED) {
        throw new IllegalStateException("QR Code has expired");
    }

    // ✅ FIX: Handle null scanCount safely
    Integer currentScanCount = qrCodeEntity.getScanCount();
    if (currentScanCount == null) {
        currentScanCount = 0;
    }
    int newScanCount = currentScanCount + 1;

    qrCodeEntity.setStatus(QRStatus.SCANNED);
    qrCodeEntity.setScannedBy(scannedBy);
    qrCodeEntity.setScannedAt(LocalDateTime.now());
    qrCodeEntity.setScanCount(newScanCount);

    QRCode updatedQRCode = qrCodeRepository.save(qrCodeEntity);
    log.info("✅ QR Code scanned successfully: {} (Scan count: {})", qrCode, newScanCount);

    return qrCodeMapper.toResponse(updatedQRCode);
}
    
    
    @Override
    @Transactional
    public QRCodeResponse scanBarCode(String qrCode, String scannedBy) {
        log.info("Scanning QR Code: {}", qrCode);
        
        // Validate inputs
        if (qrCode == null || qrCode.trim().isEmpty()) {
            throw new IllegalArgumentException("QR Code cannot be empty");
        }
        if (scannedBy == null || scannedBy.trim().isEmpty()) {
            throw new IllegalArgumentException("Scanned by cannot be empty");
        }
        
        QRCode qrCodeEntity = qrCodeRepository.findByBarcode(qrCode)
                .orElseThrow(() -> new ResourceNotFoundException("QR Code not found: " + qrCode));

        if (qrCodeEntity.getStatus() == QRStatus.USED) {
            throw new IllegalStateException("QR Code already used");
        }
        
        if (qrCodeEntity.getStatus() == QRStatus.EXPIRED) {
            throw new IllegalStateException("QR Code has expired");
        }

        // ✅ FIX: Handle null scanCount safely
        Integer currentScanCount = qrCodeEntity.getScanCount();
        if (currentScanCount == null) {
            currentScanCount = 0;
        }
        int newScanCount = currentScanCount + 1;

        qrCodeEntity.setStatus(QRStatus.SCANNED);
        qrCodeEntity.setScannedBy(scannedBy);
        qrCodeEntity.setScannedAt(LocalDateTime.now());
        qrCodeEntity.setScanCount(newScanCount);

        QRCode updatedQRCode = qrCodeRepository.save(qrCodeEntity);
        log.info("✅ QR Code scanned successfully: {} (Scan count: {})", qrCode, newScanCount);

        return qrCodeMapper.toResponse(updatedQRCode);
    }

    @Override
    @Transactional
    public QRCodeResponse scanBarcode(String barcode, String scannedBy) {
        log.info("Scanning Barcode: {}", barcode);
        
        QRCode qrCode = qrCodeRepository.findByBarcode(barcode)
                .orElseThrow(() -> new ResourceNotFoundException("Barcode not found: " + barcode));

        if (qrCode.getStatus() == QRStatus.USED) {
            throw new IllegalStateException("Barcode already used");
        }

        qrCode.setStatus(QRStatus.SCANNED);
        qrCode.setScannedBy(scannedBy);
        qrCode.setScannedAt(LocalDateTime.now());
        qrCode.setScanCount(qrCode.getScanCount() + 1);

        QRCode updatedQRCode = qrCodeRepository.save(qrCode);
        log.info("Barcode scanned successfully: {}", barcode);

        return qrCodeMapper.toResponse(updatedQRCode);
    }

    @Override
    @Transactional
    public void updateQRCodeStatus(Long id, String status) {
        QRCode qrCode = qrCodeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("QR Code not found with ID: " + id));
        qrCode.setStatus(QRStatus.valueOf(status));
        qrCodeRepository.save(qrCode);
        log.info("QR Code status updated to: {}", status);
    }

    @Override
    public Page<QRCodeResponse> getAllQRCodes(Pageable pageable) {
        return qrCodeRepository.findAll(pageable)
                .map(qrCodeMapper::toResponse);
    }

    @Override
    public byte[] generateQRCodeImage(String data) {
        try {
            BufferedImage image = qrCodeGenerator.generateQRCodeImage(data, 300, 300);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Error generating QR Code image: {}", e.getMessage());
            throw new RuntimeException("Failed to generate QR Code image", e);
        }
    }

    @Override
    public byte[] generateBarcodeImage(String data) {
        try {
            BufferedImage image = barcodeGenerator.generateBarcodeImage(data, 300, 100);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Error generating Barcode image: {}", e.getMessage());
            throw new RuntimeException("Failed to generate Barcode image", e);
        }
    }

    @Override
    public String generateQRCodeBase64(String data) {
        return qrCodeGenerator.generateQRCodeBase64(data, "QR");
    }

    @Override
    public String generateBarcodeBase64(String data) {
        return barcodeGenerator.generateBarcodeBase64(data, "Barcode");
    }

    // Private Helper Methods

    private String generateQRCodeValue(QRCodeGenerateRequest request) {
        return String.format("%s-%s-%s-%d", 
            request.getItemCode(), 
            request.getGrnNumber() != null ? request.getGrnNumber() : "GRN",
            System.currentTimeMillis() % 10000,
            new Random().nextInt(1000));
    }

    private String generateBarcodeValue(QRCodeGenerateRequest request) {
        return String.format("%s-%s-%s", 
            BARCODE_PREFIX,
            request.getItemCode(),
            String.format("%08d", new Random().nextInt(99999999)));
    }

    private String generateQRId() {
        return String.format("%s-%d-%d", 
            QR_ID_PREFIX, 
            System.currentTimeMillis() % 1000000,
            new Random().nextInt(1000));
    }

    private Map<String, Object> buildQRData(QRCodeGenerateRequest request, String qrCode, String barcode) {
        Map<String, Object> data = new HashMap<>();
        data.put("qrId", qrCode);
        data.put("barcode", barcode);
        data.put("itemCode", request.getItemCode());
        data.put("itemName", request.getItemName());
        data.put("quantity", request.getQuantity());
        data.put("uom", request.getUom());
        data.put("grnNumber", request.getGrnNumber());
        data.put("warehouseId", request.getWarehouseId());
        data.put("binId", request.getBinId());
        data.put("batchNumber", request.getBatchNumber());
        data.put("labelType", request.getLabelType());
        data.put("labelLevel", request.getLabelLevel());
        data.put("generatedAt", LocalDateTime.now().toString());
        return data;
    }

    private String toJson(Map<String, Object> data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (Exception e) {
            log.error("Error converting QR data to JSON: {}", e.getMessage());
            return data.toString();
        }
    }
}