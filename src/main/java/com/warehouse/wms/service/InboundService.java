package com.warehouse.wms.service;

import com.warehouse.wms.dto.*;
import com.warehouse.wms.entity.*;
import com.warehouse.wms.exception.ResourceNotFoundException;
import com.warehouse.wms.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InboundService {

    private final InboundRepository inboundRepository;
    private final InboundLineRepository inboundLineRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderLineRepository purchaseOrderLineRepository;
    private final SupplierRepository supplierRepository;
    private final ItemRepository itemRepository;
    private final ImageService imageService;

    private static final String INBOUND_PREFIX = "INB";

    // ============ Generate Inbound Number ============
    private String generateInboundNumber() {
        String dateStr = LocalDate.now().toString().replace("-", "");
        String prefix = INBOUND_PREFIX + "-" + dateStr + "-";
        Long count = inboundRepository.countByInboundNumberStartingWith(prefix);
        return prefix + String.format("%04d", count.intValue() + 1);
    }

    // ============ 1. CREATE INBOUND FROM PO ============
    @Transactional
    public InboundDTO createInbound(CreateInboundDTO requestDTO, Long userId) {
        log.info("Creating inbound from PO: {}", requestDTO.getPurchaseOrderId());

        PurchaseOrder po = purchaseOrderRepository.findById(requestDTO.getPurchaseOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Purchase Order not found"));

        // Check if inbound already exists
        if (inboundRepository.existsByPurchaseOrderId(po.getId())) {
            throw new IllegalStateException("Inbound already exists for PO: " + po.getPoNumber());
        }

        Inbound inbound = new Inbound();
        inbound.setInboundNumber(generateInboundNumber());
        inbound.setInboundDate(requestDTO.getInboundDate() != null ? requestDTO.getInboundDate() : LocalDate.now());
        inbound.setExpectedArrivalDate(requestDTO.getExpectedArrivalDate());
        inbound.setPoNumber(requestDTO.getPoNumber() != null ? requestDTO.getPoNumber() : po.getPoNumber());
        inbound.setInvoiceNumber(requestDTO.getInvoiceNumber());
        inbound.setDeliveryChallan(requestDTO.getDeliveryChallan());
        inbound.setSupplierName(requestDTO.getSupplierName() != null ? requestDTO.getSupplierName()
                : (po.getSupplier() != null ? po.getSupplier().getName() : null));
        inbound.setTrackingNumber(requestDTO.getTrackingNumber());
        inbound.setTrackingName(requestDTO.getTrackingName());
        inbound.setRemarks(requestDTO.getRemarks());
        inbound.setStatus(InboundStatus.PENDING);
        inbound.setStage(InboundStage.PENDING_INBOUND);
        inbound.setCreatedBy(userId);
        inbound.setPurchaseOrder(po);

        if (po.getSupplier() != null) {
            inbound.setSupplier(po.getSupplier());
        }

        inbound = inboundRepository.save(inbound);

        // Create inbound lines from PO lines
        for (PurchaseOrderLine poLine : po.getLines()) {
            InboundLine line = new InboundLine();
            line.setItemCode(poLine.getItemCode());
            line.setItemName(poLine.getItemName());
            line.setUom(poLine.getUom());
            line.setOrderedQuantity(poLine.getQuantity());
            line.setReceivedQuantity(0);
            line.setPendingQuantity(poLine.getQuantity());
            line.setTotalQuantity(poLine.getQuantity());
            line.setAcceptedQuantity(0);
            line.setRejectedQuantity(0);
            line.setQualityStatus("PENDING");
            line.setPurchaseOrderLine(poLine);
            line.setInbound(inbound);
            line.setItem(poLine.getItem());
            inbound.addLine(line);
        }

        inbound = inboundRepository.save(inbound);
        log.info("Inbound created: {}", inbound.getInboundNumber());

        return convertToDTO(inbound);
    }

    // ============ 2. GATE ENTRY ============
    @Transactional
    public InboundDTO gateEntry(Long inboundId, GateEntryDTO gateEntryDTO) {
        log.info("Gate entry for inbound: {}", inboundId);

        Inbound inbound = inboundRepository.findById(inboundId)
                .orElseThrow(() -> new ResourceNotFoundException("Inbound not found"));

        inbound.setGateEntryNumber("GE-" + inbound.getInboundNumber());
        inbound.setDriverName(gateEntryDTO.getDriverName());
        inbound.setDriverContact(gateEntryDTO.getDriverContact());
        inbound.setDriverId(gateEntryDTO.getDriverId());
        inbound.setTrackNumber(gateEntryDTO.getTrackNumber());
        inbound.setGateNumber(gateEntryDTO.getGateNumber());
        inbound.setApprovedBy(gateEntryDTO.getApprovedBy());
        inbound.setGateEntryDateTime(gateEntryDTO.getGateEntryDateTime() != null ? gateEntryDTO.getGateEntryDateTime()
                : LocalDateTime.now());
        inbound.setRemarks(gateEntryDTO.getRemarks());
        inbound.setStatus(InboundStatus.GATE_ENTRY);
        inbound.setStage(InboundStage.GATE_ENTRY);

        inbound = inboundRepository.save(inbound);
        log.info("Gate entry completed for inbound: {}", inbound.getInboundNumber());

        return convertToDTO(inbound);
    }

    // ============ 3. TRUCK UNLOADING ============
    @Transactional
    public InboundDTO unloading(Long inboundId, UnloadingDTO unloadingDTO) {
        log.info("Unloading for inbound: {}", inboundId);

        Inbound inbound = inboundRepository.findById(inboundId)
                .orElseThrow(() -> new ResourceNotFoundException("Inbound not found"));

        inbound.setBoxesUnloadedQuantity(unloadingDTO.getBoxesUnloadedQuantity());
        inbound.setBoxesInTruckQuantity(unloadingDTO.getBoxesInTruckQuantity());
        inbound.setUnloadedBy(unloadingDTO.getUnloadedBy());
        inbound.setUnloadingStartTime(LocalDateTime.now());
        inbound.setUnloadingEndTime(LocalDateTime.now());
        inbound.setRemarks(unloadingDTO.getRemarks());
        inbound.setStatus(InboundStatus.UNLOADING);
        inbound.setStage(InboundStage.UNLOADING);

        inbound = inboundRepository.save(inbound);
        log.info("Unloading completed for inbound: {}", inbound.getInboundNumber());

        return convertToDTO(inbound);
    }

    // ============ 4. GOODS RECEIVING ============
    @Transactional
    public InboundDTO goodsReceiving(Long inboundId, GoodsReceivingDTO receivingDTO) {
        log.info("Goods receiving for inbound: {}", inboundId);

        Inbound inbound = inboundRepository.findById(inboundId)
                .orElseThrow(() -> new ResourceNotFoundException("Inbound not found"));

        // Update lines with received quantities
        for (GoodsReceivingItemDTO itemDTO : receivingDTO.getItems()) {
            InboundLine line = inboundLineRepository.findById(itemDTO.getLineId())
                    .orElseThrow(() -> new ResourceNotFoundException("Line not found with id: " + itemDTO.getLineId()));

            line.setReceivedQuantity(itemDTO.getReceivedQuantity());
            line.setPendingQuantity(itemDTO.getPendingQuantity());
            line.setTotalQuantity(itemDTO.getTotalQuantity());
            line.setRemarks(itemDTO.getRemarks());
            inboundLineRepository.save(line);
        }

        inbound.setReceivedBy(receivingDTO.getReceivedBy());
        inbound.setReceivedDate(LocalDateTime.now());
        inbound.setRemarks(receivingDTO.getRemarks());
        inbound.setStatus(InboundStatus.RECEIVING);
        inbound.setStage(InboundStage.GOODS_RECEIVING);

        inbound = inboundRepository.save(inbound);
        log.info("Goods receiving completed for inbound: {}", inbound.getInboundNumber());

        return convertToDTO(inbound);
    }

    // ============ 5. QUALITY INSPECTION ============
    @Transactional
    public InboundDTO qualityInspection(Long inboundId, QualityInspectionDTO inspectionDTO) {
        log.info("Quality inspection for inbound: {}", inboundId);

        Inbound inbound = inboundRepository.findById(inboundId)
                .orElseThrow(() -> new ResourceNotFoundException("Inbound not found with id: " + inboundId));

        // Update lines with quality results and handle images
        for (QualityInspectionItemDTO itemDTO : inspectionDTO.getItems()) {
            InboundLine line = inboundLineRepository.findById(itemDTO.getLineId())
                    .orElseThrow(() -> new ResourceNotFoundException("Line not found with id: " + itemDTO.getLineId()));

            // Validate line belongs to inbound
            if (!line.getInbound().getId().equals(inboundId)) {
                throw new IllegalArgumentException("Line does not belong to this inbound");
            }

            // Update line details
            line.setAcceptedQuantity(itemDTO.getAcceptedQuantity());
            line.setRejectedQuantity(itemDTO.getRejectedQuantity());
            line.setQualityStatus(itemDTO.getQualityStatus());
            line.setReason(itemDTO.getReason());
            line.setRemarks(itemDTO.getRemarks());
            inboundLineRepository.save(line);

            // Handle images if present
            if (itemDTO.getImageFiles() != null && !itemDTO.getImageFiles().isEmpty()) {
                // Delete existing images for this line
                imageService.deleteImagesByLineId(line.getId());

                // Save new images
                imageService.saveInspectionImages(
                        inboundId,
                        line.getId(),
                        itemDTO.getImageFiles(),
                        inspectionDTO.getInspectedBy());
            }
        }

        // Update inbound header
        inbound.setInspectedBy(inspectionDTO.getInspectedBy());
        inbound.setInspectionDate(LocalDateTime.now());
        inbound.setQualityRemarks(inspectionDTO.getOverallRemarks());
        inbound.setStatus(InboundStatus.QUALITY_INSPECTION);
        inbound.setStage(InboundStage.QUALITY_INSPECTION);
        inbound.setApprovalStatus("PENDING");
        // Determine overall quality status
        boolean allAccepted = inspectionDTO.getItems().stream()
                .allMatch(item -> "GOOD".equals(item.getQualityStatus()));
        boolean anyRejected = inspectionDTO.getItems().stream()
                .anyMatch(item -> "REJECTED".equals(item.getQualityStatus()) ||
                        (item.getRejectedQuantity() != null && item.getRejectedQuantity() > 0));

        if (allAccepted) {
            inbound.setQualityStatus("GOOD");
        } else if (anyRejected) {
            inbound.setQualityStatus("PARTIAL");
        } else {
            inbound.setQualityStatus("GOOD");
        }

        inbound = inboundRepository.save(inbound);
        log.info("Quality inspection completed for inbound: {}", inbound.getInboundNumber());

        return convertToDTO(inbound);
    }

    // ============ 6. GENERATE GRN ============
    @Transactional
    public InboundDTO generateGRN(Long inboundId) {
        log.info("Generating GRN for inbound: {}", inboundId);

        Inbound inbound = inboundRepository.findById(inboundId)
                .orElseThrow(() -> new ResourceNotFoundException("Inbound not found"));

        // Generate GRN number
        String grnNumber = "GRN-" + inbound.getInboundNumber();
        inbound.setGrnNumber(grnNumber);
        inbound.setGrnDate(LocalDateTime.now());
        inbound.setGrnStatus("PENDING");
        inbound.setStatus(InboundStatus.GRN_PENDING);
        inbound.setStage(InboundStage.GRN_GENERATED);

        // Update inventory for accepted items
        for (InboundLine line : inbound.getLines()) {
            if (line.getAcceptedQuantity() > 0 && line.getItem() != null) {
                Item item = line.getItem();
                item.setCurrentStock(item.getCurrentStock() + line.getAcceptedQuantity());
                itemRepository.save(item);
            }
        }

        inbound = inboundRepository.save(inbound);
        log.info("GRN generated: {} for inbound: {}", grnNumber, inbound.getInboundNumber());

        return convertToDTO(inbound);
    }

    @Transactional
    public InboundDTO updateGRNStatus(Long inboundId, String newStatus) {
        log.info("Updating GRN status for inbound: {} to {}", inboundId, newStatus);

        Inbound inbound = inboundRepository.findById(inboundId)
                .orElseThrow(() -> new ResourceNotFoundException("Inbound not found with id: " + inboundId));

        // Validate current state
        if (inbound.getGrnNumber() == null) {
            throw new IllegalStateException("GRN must be generated before updating status");
        }

        // Store old status for audit
        String oldStatus = inbound.getGrnStatus();

        // Update ONLY the GRN status
        inbound.setGrnStatus(newStatus);

        // Optional: Update grnDate if status changes to APPROVED
        if ("APPROVED".equalsIgnoreCase(newStatus)) {
            inbound.setGrnApprovedDate(LocalDateTime.now());
            inbound.setStatus(InboundStatus.COMPLETED);

        } else if ("REJECTED".equalsIgnoreCase(newStatus)) {
            // If rejected, optionally reverse inventory
            revertInventoryIfRejected(inbound);
        }

        Inbound savedInbound = inboundRepository.save(inbound);

        log.info("GRN status updated from {} to {} for inbound: {}",
                oldStatus, newStatus, inbound.getInboundNumber());

        // Optional: Audit log

        return convertToDTO(savedInbound);
    }

    /**
     * Reverts inventory if GRN is rejected (optional business logic)
     */
    private void revertInventoryIfRejected(Inbound inbound) {
        for (InboundLine line : inbound.getLines()) {
            if (line.getAcceptedQuantity() > 0 && line.getItem() != null) {
                Item item = line.getItem();
                item.setCurrentStock(item.getCurrentStock() - line.getAcceptedQuantity());
                itemRepository.save(item);
            }
        }
        log.warn("Inventory reverted for rejected GRN: {}", inbound.getGrnNumber());
    }

    // ============ GET INBOUND BY ID ============
    public InboundDTO getInboundById(Long id) {
        Inbound inbound = inboundRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inbound not found with id: " + id));
        return convertToDTO(inbound);
    }

    // ============ GET INBOUND BY NUMBER ============
    public InboundDTO getInboundByNumber(String inboundNumber) {
        Inbound inbound = inboundRepository.findByInboundNumber(inboundNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Inbound not found with number: " + inboundNumber));
        return convertToDTO(inbound);
    }

    // ============ FILTER INBOUNDS ============
    public Page<InboundDTO> filterInbounds(InboundFilterDTO filter, Pageable pageable) {
        Page<Inbound> inbounds = inboundRepository.filterInbounds(
                // Status & Stage
                filter.getStatus(),
                filter.getStage(),
                filter.getApprovalStatus(),

                // Text Filters
                filter.getPoNumber(),
                filter.getSupplierName(),
                filter.getQualityStatus(),
                filter.getGrnStatus(),
                filter.getSearchTerm(),

                // Date Filters - Inbound Date
                filter.getInboundDateFrom(),
                filter.getInboundDateTo(),

                // Expected Arrival Date
                filter.getExpectedArrivalDateFrom(),
                filter.getExpectedArrivalDateTo(),

                // Gate Entry Date Time
                filter.getGateEntryDateTimeFrom(),
                filter.getGateEntryDateTimeTo(),

                // Unloading Start Time
                filter.getUnloadingStartTimeFrom(),
                filter.getUnloadingStartTimeTo(),

                // Received Date
                filter.getReceivedDateFrom(),
                filter.getReceivedDateTo(),

                // Inspection Date
                filter.getInspectionDateFrom(),
                filter.getInspectionDateTo(),

                // GRN Date
                filter.getGrnDateFrom(),
                filter.getGrnDateTo(),

                // Approval Date
                filter.getApprovalDateFrom(),
                filter.getApprovalDateTo(),

                // Quantity Filters
                filter.getMinBoxesUnloaded(),
                filter.getMaxBoxesUnloaded(),
                filter.getMinBoxesInTruck(),
                filter.getMaxBoxesInTruck(),

                pageable);
        return inbounds.map(this::convertToDTO);
    }

    // ============ GET ALL INBOUNDS ============
    public Page<InboundDTO> getAllInbounds(Pageable pageable) {
        return inboundRepository.findAll(pageable).map(this::convertToDTO);
    }

    // ============ GET BY STATUS ============
    public List<InboundDTO> getInboundsByStatus(InboundStatus status) {
        return inboundRepository.findByStatus(status).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // ============ CONVERSION METHODS ============

    private InboundDTO convertToDTO(Inbound entity) {
        InboundDTO dto = InboundDTO.builder()
                .id(entity.getId())
                .inboundNumber(entity.getInboundNumber())
                .inboundDate(entity.getInboundDate())
                .expectedArrivalDate(entity.getExpectedArrivalDate())
                .poNumber(entity.getPoNumber())
                .invoiceNumber(entity.getInvoiceNumber())
                .deliveryChallan(entity.getDeliveryChallan())
                .supplierName(entity.getSupplierName())
                .trackingNumber(entity.getTrackingNumber())
                .trackingName(entity.getTrackingName())
                .gateEntryNumber(entity.getGateEntryNumber())
                .driverName(entity.getDriverName())
                .driverContact(entity.getDriverContact())
                .driverId(entity.getDriverId())
                .trackNumber(entity.getTrackNumber())
                .gateNumber(entity.getGateNumber())
                .approvedBy(entity.getApprovedBy())
                .gateEntryDateTime(entity.getGateEntryDateTime())
                .boxesUnloadedQuantity(entity.getBoxesUnloadedQuantity())
                .unloadedBy(entity.getUnloadedBy())
                .unloadingStartTime(entity.getUnloadingStartTime())
                .unloadingEndTime(entity.getUnloadingEndTime())
                .receivedBy(entity.getReceivedBy())
                .receivedDate(entity.getReceivedDate())
                .inspectedBy(entity.getInspectedBy())
                .inspectionDate(entity.getInspectionDate())
                .qualityStatus(entity.getQualityStatus())
                .qualityRemarks(entity.getQualityRemarks())
                .grnNumber(entity.getGrnNumber())
                .grnDate(entity.getGrnDate())
                .grnApprovedDate(entity.getGrnApprovedDate())
                .grnStatus(entity.getGrnStatus())
                .status(entity.getStatus())
                .stage(entity.getStage())
                .remarks(entity.getRemarks())
                .createdBy(entity.getCreatedBy())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .approvalStatus(entity.getApprovalStatus())
                .approvalDate(entity.getApprovalDate())
                .approvalRemarks(entity.getApprovalRemarks())
                .approvedBy(entity.getApprovedBy())
                .build();

        if (entity.getLines() != null) {
            dto.setLines(entity.getLines().stream()
                    .map(this::convertLineToDTO)
                    .collect(Collectors.toList()));
        }

        return dto;
    }

    private InboundLineDTO convertLineToDTO(InboundLine entity) {
        InboundLineDTO.InboundLineDTOBuilder builder = InboundLineDTO.builder()
                .id(entity.getId())
                .itemCode(entity.getItemCode())
                .itemName(entity.getItemName())
                .uom(entity.getUom())
                .orderedQuantity(entity.getOrderedQuantity())
                .receivedQuantity(entity.getReceivedQuantity())
                .pendingQuantity(entity.getPendingQuantity())
                .totalQuantity(entity.getTotalQuantity())
                .acceptedQuantity(entity.getAcceptedQuantity())
                .rejectedQuantity(entity.getRejectedQuantity())
                .qualityStatus(entity.getQualityStatus())
                .reason(entity.getReason())
                .remarks(entity.getRemarks());

        // Get images for this line
        List<InspectionImage> images = imageService.getImagesByLineId(entity.getId());
        if (images != null && !images.isEmpty()) {
            List<InspectionImageDTO> imageDTOs = images.stream()
                    .map(this::convertToImageDTO)
                    .collect(Collectors.toList());
            builder.images(imageDTOs);
        }

        return builder.build();
    }

    private InspectionImageDTO convertToImageDTO(InspectionImage image) {
        return InspectionImageDTO.builder()
                .id(image.getId())
                .inboundLineId(image.getInboundLineId())
                .inboundId(image.getInboundId())
                .fileName(image.getFileName())
                .filePath(image.getFilePath())
                .fileSize(image.getFileSize())
                .fileType(image.getFileType())
                .fileExtension(image.getFileExtension())
                .uploadedBy(image.getUploadedBy())
                .remarks(image.getRemarks())
                .uploadedAt(image.getUploadedAt())
                .build();
    }

    public InboundImageDTO getInboundWithImages(Long inboundId) {
        Inbound inbound = inboundRepository.findById(inboundId)
                .orElseThrow(() -> new ResourceNotFoundException("Inbound not found with id: " + inboundId));

        InboundImageDTO.InboundImageDTOBuilder builder = InboundImageDTO.builder()
                .inboundId(inbound.getId())
                .inboundNumber(inbound.getInboundNumber())
                .supplierName(inbound.getSupplierName())
                .qualityStatus(inbound.getQualityStatus());

        List<ItemImageGroupDTO> itemGroups = new ArrayList<>();

        for (InboundLine line : inbound.getLines()) {
            List<InspectionImage> images = imageService.getImagesByLineId(line.getId());

            // FIXED: Using inline lambda to create ImageWithUrlDTO
            List<ImageWithUrlDTO> imageDTOs = images.stream()
                    .map(image -> {
                        String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().toUriString();
                        return ImageWithUrlDTO.builder()
                                .id(image.getId())
                                .fileName(image.getFileName())
                                .filePath(image.getFilePath())
                                .fullUrl(baseUrl + "/api/inbound/" + image.getInboundId() + "/image/" + image.getId()
                                        + "/view")
                                .thumbnailUrl(baseUrl + "/api/inbound/" + image.getInboundId() + "/image/"
                                        + image.getId() + "/thumbnail")
                                .downloadUrl(baseUrl + "/api/inbound/" + image.getInboundId() + "/image/"
                                        + image.getId() + "/download")
                                .fileSize(image.getFileSize())
                                .fileType(image.getFileType())
                                .uploadedAt(image.getUploadedAt())
                                .build();
                    })
                    .collect(Collectors.toList());

            ItemImageGroupDTO itemGroup = ItemImageGroupDTO.builder()
                    .lineId(line.getId())
                    .itemCode(line.getProductCode())
                    .itemName(line.getProductName())
                    .qualityStatus(line.getQualityStatus())
                    .acceptedQuantity(line.getAcceptedQuantity())
                    .rejectedQuantity(line.getRejectedQuantity())
                    .images(imageDTOs)
                    .build();

            itemGroups.add(itemGroup);
        }

        builder.items(itemGroups);
        return builder.build();
    }

    @Transactional
    public InboundDTO approveOrRejectQualityInspection(Long inboundId, QualityInspectionApprovalDTO approvalDTO) {
        log.info("Processing quality inspection approval for inbound: {}", inboundId);

        Inbound inbound = inboundRepository.findById(inboundId)
                .orElseThrow(() -> new ResourceNotFoundException("Inbound not found with id: " + inboundId));

        // Validate status
        if (inbound.getStatus() != InboundStatus.QUALITY_INSPECTION) {
            throw new IllegalStateException(
                    "Inbound is not in quality inspection stage. Current status: " + inbound.getStatus());
        }

        // Validate approval status
        String status = approvalDTO.getApprovalStatus();
        if (!"APPROVED".equals(status) && !"REJECTED".equals(status)) {
            throw new IllegalArgumentException("Approval status must be APPROVED or REJECTED");
        }

        // Update approval details
        inbound.setApprovalStatus(status);
        inbound.setApprovedBy(approvalDTO.getApprovedBy());
        inbound.setApprovalDate(LocalDateTime.now());
        inbound.setApprovalRemarks(approvalDTO.getApprovalRemarks());

        if ("REJECTED".equals(status)) {
            // Validate rejection reason
            if (approvalDTO.getRejectionReason() == null || approvalDTO.getRejectionReason().trim().isEmpty()) {
                throw new IllegalArgumentException("Rejection reason is required when rejecting");
            }
            inbound.setRejectionReason(approvalDTO.getRejectionReason());
            inbound.setStatus(InboundStatus.REJECTED);
            inbound.setStage(InboundStage.COMPLETED);
            log.warn("Quality inspection REJECTED for inbound: {} with reason: {}",
                    inbound.getInboundNumber(), approvalDTO.getRejectionReason());
        } else if ("APPROVED".equals(status)) {
            inbound.setRejectionReason(null);
            inbound.setStatus(InboundStatus.COMPLETED);
            inbound.setStage(InboundStage.COMPLETED);
            log.info("Quality inspection APPROVED for inbound: {}", inbound.getInboundNumber());

            // Update inventory for approved items
            updateInventoryForApprovedInbound(inbound);
        }

        inbound = inboundRepository.save(inbound);
        log.info("Quality inspection {} for inbound: {}", status, inbound.getInboundNumber());

        return convertToDTO(inbound);
    }

    private void updateInventoryForApprovedInbound(Inbound inbound) {
        log.info("Updating inventory for approved inbound: {}", inbound.getInboundNumber());

        for (InboundLine line : inbound.getLines()) {
            if (line.getAcceptedQuantity() != null && line.getAcceptedQuantity() > 0) {
                if (line.getItem() != null) {
                    Item item = line.getItem();
                    int currentStock = item.getCurrentStock() != null ? item.getCurrentStock() : 0;
                    item.setCurrentStock(currentStock + line.getAcceptedQuantity());
                    itemRepository.save(item);
                    log.info("Updated stock for item: {} by {}",
                            item.getItemCode(), line.getAcceptedQuantity());
                }
            }
        }

    }
    
    public InboundDTO getInboundByGrnNumber(String grnNumber) {
        log.info("Fetching inbound by GRN number: {}", grnNumber);
        Inbound inbound = inboundRepository.findByGrnNumber(grnNumber)
            .orElseThrow(() -> new RuntimeException("GRN not found: " + grnNumber));
        return convertToDTO(inbound);
    }
    
    
    
    @Transactional(readOnly = true)
    public InboundDTO getInboundByIds(Long id) {
        log.info("Fetching inbound by ID: {}", id);
        
        Inbound inbound = inboundRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inbound not found with id: " + id));
        
        return convertToDTO(inbound);
    }

    // ====== GET INBOUND BY GRN NUMBER ======
    @Transactional(readOnly = true)
    public InboundDTO getInboundByGrnNumbers(String grnNumber) {
        log.info("Fetching inbound by GRN number: {}", grnNumber);
        
        Inbound inbound = inboundRepository.findByGrnNumber(grnNumber)
            .orElseThrow(() -> new ResourceNotFoundException("GRN not found: " + grnNumber));
        
        return convertToDTO(inbound);
    }

    
    
    
}