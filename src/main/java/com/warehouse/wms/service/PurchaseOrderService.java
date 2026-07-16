package com.warehouse.wms.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.warehouse.wms.dto.CreatePurchaseOrderDTO;
import com.warehouse.wms.dto.CreatePurchaseOrderLineDTO;
import com.warehouse.wms.dto.PurchaseOrderDTO;
import com.warehouse.wms.dto.PurchaseOrderFilterDTO;
import com.warehouse.wms.dto.PurchaseOrderLineDTO;
import com.warehouse.wms.dto.StatusUpdateRequestDTOPO;
import com.warehouse.wms.entity.PurchaseOrder;
import com.warehouse.wms.entity.PurchaseOrderLine;
import com.warehouse.wms.entity.PurchaseOrderStatus;
import com.warehouse.wms.entity.PurchaseRequest;
import com.warehouse.wms.entity.RequestStatus;
import com.warehouse.wms.entity.Supplier;
import com.warehouse.wms.exception.ResourceNotFoundException;
import com.warehouse.wms.repository.PurchaseOrderRepository;
import com.warehouse.wms.repository.PurchaseRequestRepository;
import com.warehouse.wms.repository.SupplierRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PurchaseOrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseRequestRepository purchaseRequestRepository;

    private final SupplierRepository supplierRepository;
    private static final String PO_PREFIX = "PO";

    // ============ GENERATE PO NUMBER ============
    
    private String generatePONumber() {
        LocalDate now = LocalDate.now();
        String year = String.valueOf(now.getYear());
        String monthDay = String.format("%02d%02d", now.getMonthValue(), now.getDayOfMonth());
        String datePart = String.format("%s-%s", year, monthDay);
        String prefix = String.format("%s-%s", PO_PREFIX, datePart);
        
        Long count = purchaseOrderRepository.countByPoNumberStartingWith(prefix);
        int nextSequence = count.intValue() + 1;
        
        String poNumber = String.format("%s-%04d", prefix, nextSequence);
        
        while (purchaseOrderRepository.existsByPoNumber(poNumber)) {
            nextSequence++;
            poNumber = String.format("%s-%04d", prefix, nextSequence);
        }
        
        return poNumber;
    }

    // ============ CREATE ============
    
    @Transactional
    public PurchaseOrderDTO createPurchaseOrder(CreatePurchaseOrderDTO requestDTO, Long userId) {
        log.info("Creating purchase order for user: {}", userId);
        
        PurchaseOrder purchaseOrder = new PurchaseOrder();
        purchaseOrder.setPoNumber(generatePONumber());
        purchaseOrder.setPoDate(requestDTO.getPoDate());
        purchaseOrder.setExpectedArrivalDate(requestDTO.getExpectedArrivalDate());
        purchaseOrder.setStatus(PurchaseOrderStatus.DRAFT);
        purchaseOrder.setDiscountAmount(requestDTO.getDiscountAmount() != null ? requestDTO.getDiscountAmount() : 0.0);
        purchaseOrder.setShippingCharges(requestDTO.getShippingCharges() != null ? requestDTO.getShippingCharges() : 0.0);
        purchaseOrder.setRemarks(requestDTO.getRemarks());
        purchaseOrder.setTermsAndConditions(requestDTO.getTermsAndConditions());
       
        if (requestDTO.getPurchaseRequestId() != null) {
            PurchaseRequest purchaseRequest = purchaseRequestRepository.findById(requestDTO.getPurchaseRequestId())
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Purchase request not found with id: " + requestDTO.getPurchaseRequestId()
                ));
            
            purchaseOrder.setPurchaseRequestId(requestDTO.getPurchaseRequestId());
            purchaseOrder.setPurchaseRequestNumber(purchaseRequest.getPrNumber());

            // Optional: Validate purchase request status (e.g., should be APPROVED)
            if (purchaseRequest.getStatus() != RequestStatus.APPROVED) {
                throw new IllegalStateException(
                    "Cannot create purchase order from purchase request with status: " + 
                    purchaseRequest.getStatus() + ". Purchase request must be APPROVED."
                );
            }
            
        }
        purchaseOrder.setCreatedBy(userId);
        
        // Set supplier details
        if (requestDTO.getSupplierId() != null) {
            Supplier supplier = supplierRepository.findById(requestDTO.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + requestDTO.getSupplierId()));
            purchaseOrder.setSupplier(supplier);
            purchaseOrder.setSupplierName(supplier.getName());
            purchaseOrder.setSupplierEmail(supplier.getEmail());
            purchaseOrder.setSupplierPhone(supplier.getPhone());
        } else {
            purchaseOrder.setSupplierName(requestDTO.getSupplierName());
            purchaseOrder.setSupplierEmail(requestDTO.getSupplierEmail());
            purchaseOrder.setSupplierPhone(requestDTO.getSupplierPhone());
        }
        
        purchaseOrder.setShippingAddress(requestDTO.getShippingAddress());
        purchaseOrder.setBillingAddress(requestDTO.getBillingAddress());
        
        purchaseOrder = purchaseOrderRepository.save(purchaseOrder);
        
        // Add lines
        for (CreatePurchaseOrderLineDTO lineDTO : requestDTO.getLines()) {
            PurchaseOrderLine line = createLineFromDTO(lineDTO);
            line.setPurchaseOrder(purchaseOrder);
            purchaseOrder.addLine(line);
        }
        
        // Calculate totals
        purchaseOrder.calculateTotals();
        purchaseOrder = purchaseOrderRepository.save(purchaseOrder);
        
        log.info("Purchase order created with PO Number: {}", purchaseOrder.getPoNumber());
        return convertToDTO(purchaseOrder);
    }

    // ============ READ ============
    
    public PurchaseOrderDTO getPurchaseOrderById(Long id) {
        PurchaseOrder purchaseOrder = purchaseOrderRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Purchase order not found with id: " + id));
        return convertToDTO(purchaseOrder);
    }
    
    public PurchaseOrderDTO getPurchaseOrderByPoNumber(String poNumber) {
        // You need to add this method to repository or use findAll and filter
        List<PurchaseOrder> orders = purchaseOrderRepository.findAll();
        PurchaseOrder purchaseOrder = orders.stream()
            .filter(po -> po.getPoNumber().equals(poNumber))
            .findFirst()
            .orElseThrow(() -> new ResourceNotFoundException("Purchase order not found with PO number: " + poNumber));
        return convertToDTO(purchaseOrder);
    }
    
    public Page<PurchaseOrderDTO> filterPurchaseOrders(PurchaseOrderFilterDTO filter, Pageable pageable) {
        Page<PurchaseOrder> orders = purchaseOrderRepository.filterPurchaseOrders(
            filter.getStatus(),
            filter.getStatuses(),
            filter.getPoDateFrom(),
            filter.getPoDateTo(),
            filter.getExpectedArrivalFrom(),
            filter.getExpectedArrivalTo(),
            filter.getPoNumber(),
            filter.getSupplierName(),
            filter.getItemCode(),
            filter.getItemName(),
            filter.getSupplierId(),
            filter.getPurchaseRequestId(),
            filter.getMinAmount(),
            filter.getMaxAmount(),
            filter.getSearchTerm(),
            pageable
        );
        return orders.map(this::convertToDTO);
    }
    
    public List<PurchaseOrderDTO> getPurchaseOrdersBySupplier(Long supplierId) {
        List<PurchaseOrder> orders = purchaseOrderRepository.findBySupplierId(supplierId);
        return orders.stream().map(this::convertToDTO).collect(Collectors.toList());
    }
    
    public List<PurchaseOrderDTO> getPurchaseOrdersByPR(Long purchaseRequestId) {
        List<PurchaseOrder> orders = purchaseOrderRepository.findByPurchaseRequestId(purchaseRequestId);
        return orders.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    // ============ UPDATE ============
    
    @Transactional
    public PurchaseOrderDTO updatePurchaseOrder(Long id, CreatePurchaseOrderDTO requestDTO, Long userId) {
        log.info("Updating purchase order: {}", id);
        
        PurchaseOrder purchaseOrder = purchaseOrderRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Purchase order not found with id: " + id));
        
        if (purchaseOrder.getStatus() != PurchaseOrderStatus.DRAFT) {
            throw new IllegalStateException("Only draft orders can be updated");
        }
        
        // Update fields
        purchaseOrder.setPoDate(requestDTO.getPoDate());
        purchaseOrder.setExpectedArrivalDate(requestDTO.getExpectedArrivalDate());
        purchaseOrder.setDiscountAmount(requestDTO.getDiscountAmount() != null ? requestDTO.getDiscountAmount() : 0.0);
        purchaseOrder.setShippingCharges(requestDTO.getShippingCharges() != null ? requestDTO.getShippingCharges() : 0.0);
        purchaseOrder.setRemarks(requestDTO.getRemarks());
        purchaseOrder.setTermsAndConditions(requestDTO.getTermsAndConditions());
        purchaseOrder.setPurchaseRequestId(requestDTO.getPurchaseRequestId());
        
        // Update supplier
        if (requestDTO.getSupplierId() != null) {
            Supplier supplier = supplierRepository.findById(requestDTO.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found"));
            purchaseOrder.setSupplier(supplier);
            purchaseOrder.setSupplierName(supplier.getName());
            purchaseOrder.setSupplierEmail(supplier.getEmail());
            purchaseOrder.setSupplierPhone(supplier.getPhone());
        }
        
        purchaseOrder.setShippingAddress(requestDTO.getShippingAddress());
        purchaseOrder.setBillingAddress(requestDTO.getBillingAddress());
        
        // Clear and rebuild lines
        purchaseOrder.getLines().clear();
        for (CreatePurchaseOrderLineDTO lineDTO : requestDTO.getLines()) {
            PurchaseOrderLine line = createLineFromDTO(lineDTO);
            line.setPurchaseOrder(purchaseOrder);
            purchaseOrder.addLine(line);
        }
        
        purchaseOrder.calculateTotals();
        purchaseOrder = purchaseOrderRepository.save(purchaseOrder);
        
        log.info("Purchase order updated: {}", id);
        return convertToDTO(purchaseOrder);
    }

    // ============ STATUS MANAGEMENT ============
    
    @Transactional
    public PurchaseOrderDTO updateStatus(Long id, StatusUpdateRequestDTOPO statusUpdateRequest, Long userId) {
        log.info("Updating status for purchase order: {} to {}", id, statusUpdateRequest.getStatus());
        
        PurchaseOrder purchaseOrder = purchaseOrderRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Purchase order not found with id: " + id));
        
        PurchaseOrderStatus newStatus = statusUpdateRequest.getStatus();
        PurchaseOrderStatus currentStatus = purchaseOrder.getStatus();
        
        validateStatusTransition(currentStatus, newStatus);
        
        switch (newStatus) {
            case SUBMITTED:
                purchaseOrder.setStatus(PurchaseOrderStatus.SUBMITTED);
                purchaseOrder.setSubmittedAt(LocalDateTime.now());
                break;
            case APPROVED:
                purchaseOrder.setStatus(PurchaseOrderStatus.APPROVED);
                purchaseOrder.setApprovedAt(LocalDateTime.now());
                purchaseOrder.setApprovedBy(userId);
                break;
            case REJECTED:
                purchaseOrder.setStatus(PurchaseOrderStatus.REJECTED);
                purchaseOrder.setRejectionReason(statusUpdateRequest.getRejectionReason());
                break;
            case IN_PROGRESS:
                purchaseOrder.setStatus(PurchaseOrderStatus.IN_PROGRESS);
                break;
            case PARTIAL:
                purchaseOrder.setStatus(PurchaseOrderStatus.PARTIAL);
                break;
            case SHIPPED:
                purchaseOrder.setStatus(PurchaseOrderStatus.SHIPPED);
                break;
            case DELIVERED:
                purchaseOrder.setStatus(PurchaseOrderStatus.DELIVERED);
                purchaseOrder.setDeliveredAt(LocalDateTime.now());
                break;
            case COMPLETED:
                purchaseOrder.setStatus(PurchaseOrderStatus.COMPLETED);
                break;
            case CANCELLED:
                purchaseOrder.setStatus(PurchaseOrderStatus.CANCELLED);
                break;
            case CLOSED:
                purchaseOrder.setStatus(PurchaseOrderStatus.CLOSED);
                break;
            case DRAFT:
                purchaseOrder.setStatus(PurchaseOrderStatus.DRAFT);
                purchaseOrder.setSubmittedAt(null);
                purchaseOrder.setApprovedAt(null);
                purchaseOrder.setRejectionReason(null);
                break;
            default:
                throw new IllegalStateException("Unsupported status: " + newStatus);
        }
        
        if (statusUpdateRequest.getRemarks() != null) {
            String currentRemarks = purchaseOrder.getRemarks();
            String newRemarks = currentRemarks != null ? 
                currentRemarks + "\n[Status Update: " + newStatus + "] " + statusUpdateRequest.getRemarks() : 
                "[Status Update: " + newStatus + "] " + statusUpdateRequest.getRemarks();
            purchaseOrder.setRemarks(newRemarks);
        }
        
        purchaseOrder = purchaseOrderRepository.save(purchaseOrder);
        log.info("Purchase order status updated: {} -> {}", currentStatus, newStatus);
        
        return convertToDTO(purchaseOrder);
    }
    
    private void validateStatusTransition(PurchaseOrderStatus currentStatus, PurchaseOrderStatus newStatus) {
        if (currentStatus == newStatus) {
            throw new IllegalStateException("Purchase order is already in " + newStatus + " status");
        }
        
        Map<PurchaseOrderStatus, List<PurchaseOrderStatus>> allowedTransitions = new HashMap<>();
        
        allowedTransitions.put(PurchaseOrderStatus.DRAFT, Arrays.asList(
            PurchaseOrderStatus.SUBMITTED, PurchaseOrderStatus.CANCELLED
        ));
        allowedTransitions.put(PurchaseOrderStatus.SUBMITTED, Arrays.asList(
            PurchaseOrderStatus.APPROVED, PurchaseOrderStatus.REJECTED, PurchaseOrderStatus.CANCELLED
        ));
        allowedTransitions.put(PurchaseOrderStatus.APPROVED, Arrays.asList(
            PurchaseOrderStatus.IN_PROGRESS, PurchaseOrderStatus.SHIPPED, 
            PurchaseOrderStatus.COMPLETED, PurchaseOrderStatus.CANCELLED
        ));
        allowedTransitions.put(PurchaseOrderStatus.IN_PROGRESS, Arrays.asList(
            PurchaseOrderStatus.PARTIAL, PurchaseOrderStatus.COMPLETED, 
            PurchaseOrderStatus.CANCELLED
        ));
        allowedTransitions.put(PurchaseOrderStatus.PARTIAL, Arrays.asList(
            PurchaseOrderStatus.IN_PROGRESS, PurchaseOrderStatus.COMPLETED, 
            PurchaseOrderStatus.CANCELLED
        ));
        allowedTransitions.put(PurchaseOrderStatus.SHIPPED, Arrays.asList(
            PurchaseOrderStatus.DELIVERED, PurchaseOrderStatus.PARTIAL, 
            PurchaseOrderStatus.COMPLETED, PurchaseOrderStatus.CANCELLED
        ));
        allowedTransitions.put(PurchaseOrderStatus.DELIVERED, Arrays.asList(
            PurchaseOrderStatus.COMPLETED, PurchaseOrderStatus.CLOSED
        ));
        allowedTransitions.put(PurchaseOrderStatus.COMPLETED, Arrays.asList(
            PurchaseOrderStatus.CLOSED, PurchaseOrderStatus.DRAFT
        ));
        allowedTransitions.put(PurchaseOrderStatus.REJECTED, Arrays.asList(
            PurchaseOrderStatus.DRAFT
        ));
        allowedTransitions.put(PurchaseOrderStatus.CANCELLED, Arrays.asList(
            PurchaseOrderStatus.DRAFT
        ));
        allowedTransitions.put(PurchaseOrderStatus.CLOSED, Arrays.asList(
            PurchaseOrderStatus.DRAFT
        ));
        
        List<PurchaseOrderStatus> allowed = allowedTransitions.get(currentStatus);
        if (allowed == null || !allowed.contains(newStatus)) {
            throw new IllegalArgumentException(
                "Cannot transition from " + currentStatus + " to " + newStatus + 
                ". Allowed transitions: " + allowed
            );
        }
    }

    // ============ DELETE ============
    
    @Transactional
    public void deletePurchaseOrder(Long id) {
        PurchaseOrder purchaseOrder = purchaseOrderRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Purchase order not found"));
        
        if (purchaseOrder.getStatus() != PurchaseOrderStatus.DRAFT) {
            throw new IllegalStateException("Only draft orders can be deleted");
        }
        
        purchaseOrderRepository.delete(purchaseOrder);
        log.info("Purchase order deleted: {}", id);
    }

    // ============ RECEIVE ============
    
    @Transactional
    public PurchaseOrderLineDTO receiveLine(Long lineId, Integer receivedQuantity) {
        // Implementation for receiving items against PO
        // Similar to PR receiveItem method
        return null; // Placeholder
    }

    // ============ STATISTICS ============
    
    public Object getStatistics() {
        long draftCount = purchaseOrderRepository.findByStatus(PurchaseOrderStatus.DRAFT).size();
        long submittedCount = purchaseOrderRepository.findByStatus(PurchaseOrderStatus.SUBMITTED).size();
        long approvedCount = purchaseOrderRepository.findByStatus(PurchaseOrderStatus.APPROVED).size();
        long completedCount = purchaseOrderRepository.findByStatus(PurchaseOrderStatus.COMPLETED).size();
        long cancelledCount = purchaseOrderRepository.findByStatus(PurchaseOrderStatus.CANCELLED).size();
        
        return Map.of(
            "draft", draftCount,
            "submitted", submittedCount,
            "approved", approvedCount,
            "completed", completedCount,
            "cancelled", cancelledCount,
            "total", draftCount + submittedCount + approvedCount + completedCount + cancelledCount
        );
    }

    // ============ HELPER METHODS ============
    
    private PurchaseOrderLine createLineFromDTO(CreatePurchaseOrderLineDTO dto) {
        PurchaseOrderLine line = new PurchaseOrderLine();
        line.setItemCode(dto.getItemCode());
        line.setItemName(dto.getItemName());
        line.setDescription(dto.getDescription());
        line.setHsnCode(dto.getHsnCode());
        line.setUom(dto.getUom());
        line.setQuantity(dto.getQuantity());
        line.setUnitPrice(dto.getUnitPrice());
        line.setGstRate(dto.getGstRate());
        line.setSgstRate(dto.getSgstRate());
        line.setCgstRate(dto.getCgstRate());
        line.setIgstRate(dto.getIgstRate());
        line.setDiscountPercentage(dto.getDiscountPercentage());
        line.setReceivedQuantity(0);
        line.setPendingQuantity(dto.getQuantity());
        line.setLineStatus("PENDING");
        line.calculatePrice();
        return line;
    }
    
    private PurchaseOrderDTO convertToDTO(PurchaseOrder entity) {
        PurchaseOrderDTO dto = PurchaseOrderDTO.builder()
            .id(entity.getId())
            .poNumber(entity.getPoNumber())
            .poDate(entity.getPoDate())
            .expectedArrivalDate(entity.getExpectedArrivalDate())
            .status(entity.getStatus())
            .subtotal(entity.getSubtotal())
            .totalGst(entity.getTotalGst())
            .grandTotal(entity.getGrandTotal())
            .discountAmount(entity.getDiscountAmount())
            .shippingCharges(entity.getShippingCharges())
            .remarks(entity.getRemarks())
            .termsAndConditions(entity.getTermsAndConditions())
            .supplierId(entity.getSupplier() != null ? entity.getSupplier().getId() : null)
            .supplierName(entity.getSupplierName())
            .supplierEmail(entity.getSupplierEmail())
            .supplierPhone(entity.getSupplierPhone())
            .shippingAddress(entity.getShippingAddress())
            .billingAddress(entity.getBillingAddress())
            .purchaseRequestId(entity.getPurchaseRequestId())
            .createdBy(entity.getCreatedBy())
            .approvedBy(entity.getApprovedBy())
            .approvedAt(entity.getApprovedAt())
            .submittedAt(entity.getSubmittedAt())
            .deliveredAt(entity.getDeliveredAt())
            .rejectionReason(entity.getRejectionReason())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .purchaseRequestNumber(entity.getPurchaseRequestNumber())
            .build();
        
        if (entity.getLines() != null) {
            dto.setLines(entity.getLines().stream()
                .map(this::convertLineToDTO)
                .collect(Collectors.toList()));
        }
        
        return dto;
    }
    
    private PurchaseOrderLineDTO convertLineToDTO(PurchaseOrderLine entity) {
        return PurchaseOrderLineDTO.builder()
            .id(entity.getId())
            .itemCode(entity.getItemCode())
            .itemName(entity.getItemName())
            .description(entity.getDescription())
            .hsnCode(entity.getHsnCode())
            .uom(entity.getUom())
            .quantity(entity.getQuantity())
            .gstRate(entity.getGstRate())
            .sgstRate(entity.getSgstRate())
            .cgstRate(entity.getCgstRate())
            .igstRate(entity.getIgstRate())
            .unitPrice(entity.getUnitPrice())
            .discountPercentage(entity.getDiscountPercentage())
            .discountAmount(entity.getDiscountAmount())
            .totalPrice(entity.getTotalPrice())
            .gstAmount(entity.getGstAmount())
            .totalWithGst(entity.getTotalWithGst())
            .receivedQuantity(entity.getReceivedQuantity())
            .pendingQuantity(entity.getPendingQuantity())
            .lineStatus(entity.getLineStatus())
            .itemId(entity.getItem() != null ? entity.getItem().getId() : null)
            .build();
    }
}