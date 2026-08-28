package com.warehouse.wms.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.warehouse.wms.dto.CreateInboundDTO;
import com.warehouse.wms.dto.GateEntryDTO;
import com.warehouse.wms.dto.GoodsReceivingDTO;
import com.warehouse.wms.dto.GoodsReceivingItemDTO;
import com.warehouse.wms.dto.ImageWithUrlDTO;
import com.warehouse.wms.dto.InboundDTO;
import com.warehouse.wms.dto.InboundFilterDTO;
import com.warehouse.wms.dto.InboundImageDTO;
import com.warehouse.wms.dto.InboundLineDTO;
import com.warehouse.wms.dto.InspectionImageDTO;
import com.warehouse.wms.dto.ItemImageGroupDTO;
import com.warehouse.wms.dto.QualityInspectionApprovalDTO;
import com.warehouse.wms.dto.QualityInspectionDTO;
import com.warehouse.wms.dto.QualityInspectionItemDTO;
import com.warehouse.wms.dto.UnloadingDTO;
import com.warehouse.wms.dto.request.PurchaseReturnLineRequestDTO;
import com.warehouse.wms.dto.request.PurchaseReturnRequestDTO;
import com.warehouse.wms.dto.response.PurchaseReturnResponseDTO;
import com.warehouse.wms.entity.Inbound;
import com.warehouse.wms.entity.InboundLine;
import com.warehouse.wms.entity.InboundStage;
import com.warehouse.wms.entity.InboundStatus;
import com.warehouse.wms.entity.InspectionImage;
import com.warehouse.wms.entity.Item;
import com.warehouse.wms.entity.PurchaseOrder;
import com.warehouse.wms.entity.PurchaseOrderLine;
import com.warehouse.wms.entity.PurchaseReturn;
import com.warehouse.wms.entity.Rock;
import com.warehouse.wms.exception.ResourceNotFoundException;
import com.warehouse.wms.mapper.RockMapper;
import com.warehouse.wms.repository.InboundLineRepository;
import com.warehouse.wms.repository.InboundRepository;
import com.warehouse.wms.repository.ItemRepository;
import com.warehouse.wms.repository.PurchaseOrderLineRepository;
import com.warehouse.wms.repository.PurchaseOrderRepository;
import com.warehouse.wms.repository.RockRepository;
import com.warehouse.wms.repository.SupplierRepository;
import com.warehouse.wms.service.impl.PurchaseReturnServiceImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
    private final RockMapper rockMapper;
    private final PurchaseReturnServiceImpl purchaseReturnServiceImpl; // Add this

    private final RockRepository rockRepository;


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
 // ====== FILE: src/main/java/com/warehouse/wms/service/impl/InboundServiceImpl.java ======

    @Transactional
    public InboundDTO unloading(Long inboundId, UnloadingDTO unloadingDTO) {
        log.info("Unloading for inbound: {}", inboundId);

        Inbound inbound = inboundRepository.findById(inboundId)
                .orElseThrow(() -> new ResourceNotFoundException("Inbound not found"));

        // ✅ Update Rock if rockId is provided
        if (unloadingDTO.getRockId() != null) 
        {
        	
            Rock rock = rockRepository.findById(unloadingDTO.getRockId())
                    .orElseThrow(() -> new ResourceNotFoundException("Rock not found with ID: " + unloadingDTO.getRockId()));
            inbound.setRock(rock);
            log.info("✅ Rock assigned to inbound: {}", rock.getRockId());
        }

        // Update unloading details
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

    // ✅ Initialize rejected items list
    List<QualityInspectionItemDTO> rejectedItems = new ArrayList<>();
    List<QualityInspectionItemDTO> acceptedItems = new ArrayList<>();

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
        line.setRemainingQuantity(itemDTO.getAcceptedQuantity());
        line.setRejectedQuantity(itemDTO.getRejectedQuantity());
        line.setQualityStatus(itemDTO.getQualityStatus());
        line.setReason(itemDTO.getReason());
        line.setRemarks(itemDTO.getRemarks());
        inboundLineRepository.save(line);

        // ✅ Track rejected items
        if (itemDTO.getRejectedQuantity() != null && itemDTO.getRejectedQuantity() > 0) {
            rejectedItems.add(itemDTO);
            log.info("❌ Rejected: {} - Qty: {}", itemDTO.getItemName(), itemDTO.getRejectedQuantity());
        } else {
            acceptedItems.add(itemDTO);
            log.info("✅ Accepted: {} - Qty: {}", itemDTO.getItemName(), itemDTO.getAcceptedQuantity());
        }

        // Handle images if present
        if (itemDTO.getImageFiles() != null && !itemDTO.getImageFiles().isEmpty()) {
            imageService.deleteImagesByLineId(line.getId());
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
            .allMatch(item -> "GOOD".equals(item.getQualityStatus()) || "ACCEPTED".equals(item.getQualityStatus()));
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
    
    // ✅ LOG SUMMARY
    log.info("📊 Inspection Summary - Accepted: {}, Rejected: {}", 
            acceptedItems.size(), rejectedItems.size());
    
    // ✅ AUTO CREATE PURCHASE RETURN FOR REJECTED ITEMS
    if (!rejectedItems.isEmpty()) {
        try {
            log.info("🚀 Creating purchase return for {} rejected items", rejectedItems.size());
            PurchaseReturnResponseDTO purchaseReturn = createPurchaseReturnFromRejectedItems(inbound, rejectedItems);
            log.info("✅ Auto-created purchase return: {} for inbound: {}", 
                    purchaseReturn.getReturnNumber(), inbound.getInboundNumber());
        } catch (Exception e) {
            log.error("❌ Failed to auto-create purchase return for inbound: {}", inbound.getInboundNumber(), e);
            // Optionally: You might want to throw the exception or handle it differently
        }
    } else {
        log.info("ℹ️ No rejected items found, skipping purchase return creation");
    }

    return convertToDTO(inbound);
}

/**
 * Creates a purchase return from rejected items during quality inspection
 */
private PurchaseReturnResponseDTO createPurchaseReturnFromRejectedItems(
        Inbound inbound, 
        List<QualityInspectionItemDTO> rejectedItems) {
    
    log.info("Creating purchase return for inbound: {} with {} rejected items", 
            inbound.getInboundNumber(), rejectedItems.size());

    // Build purchase return request
    PurchaseReturnRequestDTO request = PurchaseReturnRequestDTO.builder()
            .returnDate(LocalDate.now())
            .poNumber(inbound.getPoNumber())
            .grnNumber(inbound.getGrnNumber())
            .invoiceNumber(inbound.getInvoiceNumber())
            .supplierName(inbound.getSupplierName())
            .supplierId(inbound.getSupplier() != null ? inbound.getSupplier().getId() : null)
            .inboundId(inbound.getId())
            .purchaseOrderId(inbound.getPurchaseOrder() != null ? inbound.getPurchaseOrder().getId() : null)
            .returnType(PurchaseReturn.ReturnType.QUALITY_ISSUE)
            .status(PurchaseReturn.ReturnStatus.PENDING)
            .reason("Auto-created from quality inspection - Rejected items")
            .remarks("Auto-generated purchase return for rejected items during quality inspection")
            .lines(new ArrayList<>())
            .build();

    // Add rejected items as return lines
    for (QualityInspectionItemDTO itemDTO : rejectedItems) {
        // Find the corresponding inbound line for more details
        InboundLine inboundLine = inboundLineRepository.findById(itemDTO.getLineId())
                .orElse(null);

        Double unitPrice = 0.0;
        if (inboundLine != null) {
            if (inboundLine.getPurchaseOrderLine() != null && 
                inboundLine.getPurchaseOrderLine().getUnitPrice() != null) {
                unitPrice = inboundLine.getPurchaseOrderLine().getUnitPrice();
            } else if (inboundLine.getItem() != null && 
                       inboundLine.getItem().getUnitPrice() != null) {
                unitPrice = inboundLine.getItem().getUnitPrice();
            }
        }

        PurchaseReturnLineRequestDTO lineRequest = PurchaseReturnLineRequestDTO.builder()
                .itemCode(itemDTO.getItemCode())
                .itemName(itemDTO.getItemName())
                .uom(inboundLine != null ? inboundLine.getUom() : "NOS")
                .returnQuantity(itemDTO.getRejectedQuantity())
                .unitPrice(unitPrice)
                .totalAmount(itemDTO.getRejectedQuantity() * unitPrice)
                .originalQuantity(inboundLine != null ? inboundLine.getOrderedQuantity() : 0)
                .receivedQuantity(inboundLine != null ? inboundLine.getReceivedQuantity() : 0)
                .reason(itemDTO.getReason() != null ? itemDTO.getReason() : "Rejected during quality inspection")
                .remarks(itemDTO.getRemarks())
                .inboundLineId(itemDTO.getLineId())
                .build();
        
        request.getLines().add(lineRequest);
        log.info("📦 Added return line: {} - Qty: {}", itemDTO.getItemName(), itemDTO.getRejectedQuantity());
    }

    // Create the purchase return
    return purchaseReturnServiceImpl.createPurchaseReturn(request);
}
    
    
    
//    private PurchaseReturnResponseDTO createPurchaseReturnFromRejectedItems(
//            Inbound inbound, 
//            List<QualityInspectionItemDTO> rejectedItems) {
//        
//        log.info("Creating purchase return for inbound: {} with {} rejected items", 
//                inbound.getInboundNumber(), rejectedItems.size());
//
//        // Build purchase return request
//        PurchaseReturnRequestDTO request = PurchaseReturnRequestDTO.builder()
//                .returnDate(LocalDate.now())
//                .poNumber(inbound.getPoNumber())
//                .grnNumber(inbound.getGrnNumber())
//                .invoiceNumber(inbound.getInvoiceNumber())
//                .supplierName(inbound.getSupplierName())
//                .supplierId(inbound.getSupplier() != null ? inbound.getSupplier().getId() : null)
//                .inboundId(inbound.getId())
//                .purchaseOrderId(inbound.getPurchaseOrder() != null ? inbound.getPurchaseOrder().getId() : null)
//                .returnType(PurchaseReturn.ReturnType.QUALITY_ISSUE)
//                .status(PurchaseReturn.ReturnStatus.PENDING)
//                .reason("Auto-created from quality inspection - Rejected items")
//                .remarks("Auto-generated purchase return for rejected items during quality inspection")
//                .lines(new ArrayList<>())
//                .build();
//
//        // Add rejected items as return lines
//        for (QualityInspectionItemDTO itemDTO : rejectedItems) {
//            // Find the corresponding inbound line for more details
//            InboundLine inboundLine = inboundLineRepository.findById(itemDTO.getLineId())
//                    .orElse(null);
//
//            PurchaseReturnLineRequestDTO lineRequest = PurchaseReturnLineRequestDTO.builder()
//                    .itemCode(itemDTO.getItemCode())
//                    .itemName(itemDTO.getItemName())
//                    .uom(inboundLine != null ? inboundLine.getUom() : "NOS")
//                    .returnQuantity(itemDTO.getRejectedQuantity())
//                    .unitPrice(inboundLine != null ? getItemUnitPrice(inboundLine) : 0.0)
//                    .totalAmount(itemDTO.getRejectedQuantity() * (inboundLine != null ? getItemUnitPrice(inboundLine) : 0.0))
//                    .originalQuantity(inboundLine != null ? inboundLine.getOrderedQuantity() : 0)
//                    .receivedQuantity(inboundLine != null ? inboundLine.getReceivedQuantity() : 0)
//                    .reason(itemDTO.getReason() != null ? itemDTO.getReason() : "Rejected during quality inspection")
//                    .remarks(itemDTO.getRemarks())
//                    .inboundLineId(itemDTO.getLineId())
//                    .build();
//            
//            request.getLines().add(lineRequest);
//        }
//
//        // Create the purchase return
//        return purchaseReturnService.createPurchaseReturn(request);
//    }

    /**
     * Helper method to get unit price from inbound line
     */
    private Double getItemUnitPrice(InboundLine line) {
        // Try to get from purchase order line
        if (line.getPurchaseOrderLine() != null && line.getPurchaseOrderLine().getUnitPrice() != null) {
            return line.getPurchaseOrderLine().getUnitPrice();
        }
        // Try to get from item
        if (line.getItem() != null && line.getItem().getUnitPrice() != null) {
            return line.getItem().getUnitPrice();
        }
        // Default
        return 0.0;
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
        
        
        
        
        
        // ✅ ADD ROCK MAPPING
        if (entity.getRock() != null) {
            dto.setRock(rockMapper.toResponse(entity.getRock()));
        }

      
        
        
        

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
                .barcodeGenerate(entity.getBarcodeGenerate())
                .fullpath(entity.getFullpath())
                .warehouseId(entity.getWarehouseId())
                .zone(entity.getZone())
                .aisle(entity.getAisle())
                .rack(entity.getRack())
                .level(entity.getLevel())
                .binId(entity.getBinId())
                .taskAssinged(entity.getTaskAssinged())
                .orderedQuantity(entity.getOrderedQuantity())
                .receivedQuantity(entity.getReceivedQuantity())
                .pendingQuantity(entity.getPendingQuantity())
                .totalQuantity(entity.getTotalQuantity())
                .acceptedQuantity(entity.getAcceptedQuantity())
                .remainingQuantity(entity.getRemainingQuantity())
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

// ====== FILE: src/main/java/com/warehouse/wms/service/impl/InboundServiceImpl.java ======


 @Transactional(readOnly = true)
public Page<InboundDTO> getInboundsByGrnStatusApproved(String search, Boolean barcodeGenerate, Boolean taskAssigned, Pageable pageable) {
    log.info("Fetching inbounds with GRN status APPROVED - search: {}, barcodeGenerate: {}, taskAssigned: {}, page: {}, size: {}", 
             search, barcodeGenerate, taskAssigned, pageable.getPageNumber(), pageable.getPageSize());
    
    String searchTerm = (search != null && !search.trim().isEmpty()) ? search.trim() : null;
    Page<Inbound> inbounds = null;
    
    // Check which filters are applied
    boolean hasSearch = searchTerm != null;
    boolean hasBarcodeGenerate = barcodeGenerate != null && barcodeGenerate;
    boolean hasTaskAssigned = taskAssigned != null && taskAssigned;
    
    if (hasSearch && hasBarcodeGenerate && hasTaskAssigned) {
        inbounds = inboundRepository.findByGrnStatusAndAllLinesBothFlags("APPROVED", searchTerm, pageable);
    } else if (hasSearch && hasBarcodeGenerate) {
        inbounds = inboundRepository.findByGrnStatusAndAllLinesBarcodeGenerated("APPROVED", searchTerm, pageable);
    } else if (hasSearch && hasTaskAssigned) {
        inbounds = inboundRepository.findByGrnStatusAndAllLinesTaskAssigned("APPROVED", searchTerm, pageable);
    } else if (hasSearch) {
        inbounds = inboundRepository.findByGrnStatusAndSearch("APPROVED", searchTerm, pageable);
    } else if (hasBarcodeGenerate && hasTaskAssigned) {
        inbounds = inboundRepository.findByGrnStatusAndAllLinesBothFlags("APPROVED", null, pageable);
    } else if (hasBarcodeGenerate) {
        inbounds = inboundRepository.findByGrnStatusAndAllLinesBarcodeGenerated("APPROVED", null, pageable);
    } else if (hasTaskAssigned) {
        inbounds = inboundRepository.findByGrnStatusAndAllLinesTaskAssigned("APPROVED", null, pageable);
    } else {
        inbounds = inboundRepository.findByGrnStatus("APPROVED", pageable);
    }
    
    // Filter out inbounds that have ALL lines REJECTED (completely rejected)
    // Keep inbounds that have at least one non-REJECTED line
    List<Inbound> filteredList = inbounds.getContent().stream()
        .filter(inbound -> {
            // Check if ALL lines are REJECTED
            boolean allLinesRejected = inbound.getLines().stream()
                .allMatch(line -> "REJECTED".equals(line.getQualityStatus()));
            // Keep only if NOT all lines are rejected
            return !allLinesRejected;
        })
        .map(inbound -> {
            // Remove REJECTED lines from the inbound
            List<InboundLine> nonRejectedLines = inbound.getLines().stream()
                .filter(line -> !"REJECTED".equals(line.getQualityStatus()))
                .collect(Collectors.toList());
            inbound.setLines(nonRejectedLines);
            return inbound;
        })
        .collect(Collectors.toList());
    
    Page<Inbound> filteredInbounds = new PageImpl<>(filteredList, pageable, filteredList.size());
    
    return filteredInbounds.map(this::convertToDTO);
}

}





