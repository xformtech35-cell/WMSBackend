package com.warehouse.wms.service;

import com.warehouse.wms.dto.*;
import com.warehouse.wms.entity.*;
import com.warehouse.wms.exception.ResourceNotFoundException;
import com.warehouse.wms.repository.PurchaseRequestItemRepository;
import com.warehouse.wms.repository.PurchaseRequestRepository;
import com.warehouse.wms.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PurchaseRequestService {

    private final PurchaseRequestRepository purchaseRequestRepository;
    private final PurchaseRequestItemRepository itemRepository;
    private final SupplierRepository supplierRepository;

    // Generate PR Number
    private String generatePRNumber() {
        String year = String.valueOf(LocalDate.now().getYear());
        long count = purchaseRequestRepository.count() + 1;
        return String.format("PR-%s-%04d", year, count);
    }

    // Create new purchase request
    @Transactional
    public PurchaseRequestDTO createPurchaseRequest(CreatePurchaseRequestDTO requestDTO, Long userId) {
        log.info("Creating purchase request for user: {}", userId);

        PurchaseRequest purchaseRequest = new PurchaseRequest();
        purchaseRequest.setPrNumber(generatePRNumber());
        purchaseRequest.setRequestedDate(requestDTO.getRequestedDate());
        purchaseRequest.setRequiredDate(requestDTO.getRequiredDate());
        purchaseRequest.setPriority(Priority.valueOf(requestDTO.getPriority()));
        purchaseRequest.setStatus(RequestStatus.DRAFT);
        purchaseRequest.setNotes(requestDTO.getNotes());
        purchaseRequest.setCreatedBy(userId);

        // Set supplier if provided
        if (requestDTO.getSupplierId() != null) {
            Supplier supplier = supplierRepository.findById(requestDTO.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + requestDTO.getSupplierId()));
            purchaseRequest.setSupplier(supplier);
        }

        // Save purchase request first
        purchaseRequest = purchaseRequestRepository.save(purchaseRequest);

        // Add items
        for (PurchaseRequestItemDTO itemDTO : requestDTO.getItems()) {
            PurchaseRequestItem item = new PurchaseRequestItem();
            item.setItemCode(itemDTO.getItemCode());
            item.setItemName(itemDTO.getItemName());
            item.setItemBarcode(itemDTO.getItemBarcode());

            item.setBatchNo(itemDTO.getBatchNo());

            item.setQuantity(itemDTO.getQuantity());
            item.setUnit(itemDTO.getUnit());
            item.setUnitPrice(itemDTO.getUnitPrice());
            item.setTotal(itemDTO.getQuantity() * itemDTO.getUnitPrice());
            item.setRemarks(itemDTO.getRemarks());
            item.setReceivedQuantity(0);
            item.setPendingQuantity(itemDTO.getQuantity());
            item.setItemStatus("PENDING");
            item.setReceipts(new ArrayList<>());
            item.setPurchaseRequest(purchaseRequest);
            purchaseRequest.addItem(item);
        }

        // Calculate total amount
        purchaseRequest.calculateTotalAmount();

        // Save again with items
        purchaseRequest = purchaseRequestRepository.save(purchaseRequest);

        log.info("Purchase request created with ID: {}", purchaseRequest.getId());

        return convertToDTO(purchaseRequest);
    }

    // Receive items with quality check
    @Transactional
    public PurchaseRequestItemDTO receiveItem(Long itemId, ReceiveItemDTO receiveDTO) {
        log.info("Receiving item: {}", itemId);
        log.info("Received quantity: {}", receiveDTO.getReceivedQuantity());
        log.info("Quality status: {}", receiveDTO.getQualityStatus());
        String qualityStatus = receiveDTO.getQualityStatus().trim().toUpperCase();

        if (!List.of("GOOD", "PARTIAL", "REJECTED").contains(qualityStatus)) {
            throw new IllegalStateException("Quality status must be GOOD, PARTIAL, or REJECTED");
        }

        PurchaseRequestItem item = itemRepository.findById(itemId)
            .orElseThrow(() -> new ResourceNotFoundException("Item not found with id: " + itemId));

        log.info("Found item: {}, Pending quantity: {}", item.getItemName(), item.getPendingQuantity());

        // Validate receiving quantity
        int pendingQty = item.getPendingQuantity() != null ? item.getPendingQuantity() : item.getQuantity();
        if (receiveDTO.getReceivedQuantity() > pendingQty) {
            throw new IllegalStateException("Cannot receive more than pending quantity: " + pendingQty);
        }

        if (receiveDTO.getReceivedQuantity() == 0 && !"REJECTED".equals(qualityStatus)) {
            throw new IllegalStateException("Received quantity must be greater than 0");
        }

        if ("REJECTED".equals(qualityStatus) && receiveDTO.getReceivedQuantity() > 0) {
            throw new IllegalStateException("Rejected items cannot have received quantity greater than 0");
        }

        // Create receipt record
        ItemReceipt receipt = new ItemReceipt();
        receipt.setPurchaseRequestItem(item);
        receipt.setReceivedDate(LocalDateTime.now());
        receipt.setReceivedQuantity(receiveDTO.getReceivedQuantity());
        receipt.setDefectiveQuantity(receiveDTO.getDefectiveQuantity() != null ? receiveDTO.getDefectiveQuantity() : 0);
        receipt.setQualityStatus(qualityStatus);
        receipt.setRemarks(receiveDTO.getRemarks());
        receipt.setImages(receiveDTO.getImages() != null ? receiveDTO.getImages() : new ArrayList<>());

        // Add receipt to item
        if (item.getReceipts() == null) {
            item.setReceipts(new ArrayList<>());
        }
        item.getReceipts().add(receipt);

        // Calculate total received across all receipts
        int totalReceived = item.getReceipts().stream()
            .mapToInt(ItemReceipt::getReceivedQuantity)
            .sum();

        // Update quantities
        item.setReceivedQuantity(totalReceived);
        int pending = item.getQuantity() - totalReceived;
        item.setPendingQuantity(Math.max(pending, 0));

        // Update item status
        if (item.getPendingQuantity() == 0) {
            item.setItemStatus("COMPLETED");
        } else if (item.getReceivedQuantity() > 0) {
            item.setItemStatus("PARTIAL");
        } else if ("REJECTED".equals(qualityStatus)) {
            item.setItemStatus("REJECTED");
        } else {
            item.setItemStatus("PENDING");
        }

        itemRepository.save(item);

        // Update parent purchase request status
        updatePurchaseRequestStatus(item.getPurchaseRequest().getId());

        log.info("Item received successfully: {}", itemId);

        return convertItemToDTO(item);
    }

    // Update parent purchase request status based on all items
    private void updatePurchaseRequestStatus(Long prId) {
        PurchaseRequest purchaseRequest = purchaseRequestRepository.findById(prId)
            .orElseThrow(() -> new ResourceNotFoundException("Purchase request not found"));

        boolean allCompleted = purchaseRequest.getItems().stream()
            .allMatch(item -> "COMPLETED".equals(item.getItemStatus()));

        boolean anyReceived = purchaseRequest.getItems().stream()
            .anyMatch(item -> item.getReceivedQuantity() != null && item.getReceivedQuantity() > 0);

        boolean anyPending = purchaseRequest.getItems().stream()
            .anyMatch(item -> item.getPendingQuantity() != null && item.getPendingQuantity() > 0);

        if (allCompleted) {
            purchaseRequest.setStatus(RequestStatus.COMPLETED);
        } else if (anyReceived && anyPending) {
            purchaseRequest.setStatus(RequestStatus.PARTIAL);
        }
        // Keep existing status if no changes

        purchaseRequestRepository.save(purchaseRequest);
    }

    // Submit purchase request (change from DRAFT to SUBMITTED)
    @Transactional
    public PurchaseRequestDTO submitPurchaseRequest(Long id, Long userId) {
        log.info("Submitting purchase request: {}", id);

        PurchaseRequest purchaseRequest = purchaseRequestRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Purchase request not found with id: " + id));

        if (purchaseRequest.getStatus() != RequestStatus.DRAFT) {
            throw new IllegalStateException("Only draft requests can be submitted");
        }

        purchaseRequest.setStatus(RequestStatus.SUBMITTED);
        purchaseRequest.setSubmittedAt(LocalDateTime.now());
        purchaseRequest = purchaseRequestRepository.save(purchaseRequest);

        log.info("Purchase request submitted: {}", id);

        return convertToDTO(purchaseRequest);
    }

    // Approve purchase request
    @Transactional
    public PurchaseRequestDTO approvePurchaseRequest(Long id, Long userId, boolean approved, String rejectionReason) {
        log.info("Approving purchase request: {} with status: {}", id, approved);

        PurchaseRequest purchaseRequest = purchaseRequestRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Purchase request not found with id: " + id));

        if (purchaseRequest.getStatus() != RequestStatus.SUBMITTED) {
            throw new IllegalStateException("Only submitted requests can be approved/rejected");
        }

        if (approved) {
            purchaseRequest.setStatus(RequestStatus.APPROVED);
        } else {
            purchaseRequest.setStatus(RequestStatus.REJECTED);
            purchaseRequest.setRejectionReason(rejectionReason);
        }

        purchaseRequest.setApprovedBy(userId);
        purchaseRequest.setApprovedAt(LocalDateTime.now());
        purchaseRequest = purchaseRequestRepository.save(purchaseRequest);

        log.info("Purchase request {}: {}", id, approved ? "approved" : "rejected");

        return convertToDTO(purchaseRequest);
    }

    // Get all purchase requests with filters
    public Page<PurchaseRequestDTO> getAllPurchaseRequests(String status, String priority,
                                                            LocalDate startDate, LocalDate endDate,
                                                            Pageable pageable) {
        RequestStatus statusEnum = status != null ? RequestStatus.valueOf(status) : null;
        Priority priorityEnum = priority != null ? Priority.valueOf(priority) : null;

        Page<PurchaseRequest> requests = purchaseRequestRepository.findWithFilters(
            statusEnum, priorityEnum, startDate, endDate, pageable);

        return requests.map(this::convertToDTO);
    }

    // Get purchase request by ID
    public PurchaseRequestDTO getPurchaseRequestById(Long id) {
        PurchaseRequest purchaseRequest = purchaseRequestRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Purchase request not found with id: " + id));
        return convertToDTO(purchaseRequest);
    }

    // Update purchase request (only DRAFT status)
    @Transactional
    public PurchaseRequestDTO updatePurchaseRequest(Long id, CreatePurchaseRequestDTO requestDTO, Long userId) {
        log.info("Updating purchase request: {}", id);

        PurchaseRequest purchaseRequest = purchaseRequestRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Purchase request not found with id: " + id));

        if (purchaseRequest.getStatus() != RequestStatus.DRAFT) {
            throw new IllegalStateException("Only draft requests can be updated");
        }

        // Update basic info
        purchaseRequest.setRequestedDate(requestDTO.getRequestedDate());
        purchaseRequest.setRequiredDate(requestDTO.getRequiredDate());
        purchaseRequest.setPriority(Priority.valueOf(requestDTO.getPriority()));
        purchaseRequest.setNotes(requestDTO.getNotes());

        // Update supplier
        if (requestDTO.getSupplierId() != null) {
            Supplier supplier = supplierRepository.findById(requestDTO.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found"));
            purchaseRequest.setSupplier(supplier);
        } else {
            purchaseRequest.setSupplier(null);
        }

        // Clear existing items
        purchaseRequest.getItems().clear();

        // Add new items
        for (PurchaseRequestItemDTO itemDTO : requestDTO.getItems()) {
            PurchaseRequestItem item = new PurchaseRequestItem();
            item.setItemCode(itemDTO.getItemCode());
            item.setItemName(itemDTO.getItemName());
            item.setItemBarcode(itemDTO.getItemBarcode());

            item.setBatchNo(itemDTO.getBatchNo());

            item.setQuantity(itemDTO.getQuantity());
            item.setUnit(itemDTO.getUnit());
            item.setUnitPrice(itemDTO.getUnitPrice());
            item.setTotal(itemDTO.getQuantity() * itemDTO.getUnitPrice());
            item.setRemarks(itemDTO.getRemarks());
            item.setReceivedQuantity(0);
            item.setPendingQuantity(itemDTO.getQuantity());
            item.setItemStatus("PENDING");
            item.setReceipts(new ArrayList<>());
            item.setPurchaseRequest(purchaseRequest);
            purchaseRequest.addItem(item);
        }

        // Recalculate total
        purchaseRequest.calculateTotalAmount();

        purchaseRequest = purchaseRequestRepository.save(purchaseRequest);

        return convertToDTO(purchaseRequest);
    }

    // Delete purchase request (only DRAFT status)
    @Transactional
    public void deletePurchaseRequest(Long id) {
        log.info("Deleting purchase request: {}", id);

        PurchaseRequest purchaseRequest = purchaseRequestRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Purchase request not found"));

        if (purchaseRequest.getStatus() != RequestStatus.DRAFT) {
            throw new IllegalStateException("Only draft requests can be deleted");
        }

        purchaseRequestRepository.delete(purchaseRequest);

        log.info("Purchase request deleted: {}", id);
    }

    // Get purchase requests by user
    public List<PurchaseRequestDTO> getPurchaseRequestsByUser(Long userId) {
        List<PurchaseRequest> requests = purchaseRequestRepository.findByCreatedBy(userId);
        return requests.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    // Get statistics
    public Object getStatistics() {
        long draftCount = purchaseRequestRepository.countByStatus(RequestStatus.DRAFT);
        long submittedCount = purchaseRequestRepository.countByStatus(RequestStatus.SUBMITTED);
        long approvedCount = purchaseRequestRepository.countByStatus(RequestStatus.APPROVED);
        long rejectedCount = purchaseRequestRepository.countByStatus(RequestStatus.REJECTED);

        return java.util.Map.of(
            "draft", draftCount,
            "submitted", submittedCount,
            "approved", approvedCount,
            "rejected", rejectedCount,
            "total", draftCount + submittedCount + approvedCount + rejectedCount
        );
    }

    // ============ CONVERSION METHODS ============

    // Convert Entity to DTO
    private PurchaseRequestDTO convertToDTO(PurchaseRequest entity) {
        PurchaseRequestDTO dto = new PurchaseRequestDTO();
        dto.setId(entity.getId());
        dto.setPrNumber(entity.getPrNumber());
        dto.setRequestedDate(entity.getRequestedDate());
        dto.setRequiredDate(entity.getRequiredDate());
        dto.setPriority(entity.getPriority().name());
        dto.setStatus(entity.getStatus().name());
        dto.setNotes(entity.getNotes());
        dto.setTotalAmount(entity.getTotalAmount());
        dto.setSubmittedAt(entity.getSubmittedAt());
        dto.setApprovedAt(entity.getApprovedAt());
        dto.setRejectionReason(entity.getRejectionReason());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());

        if (entity.getSupplier() != null) {
            dto.setSupplierId(entity.getSupplier().getId());
            dto.setSupplierName(entity.getSupplier().getName());
        }

        if (entity.getItems() != null) {
            List<PurchaseRequestItemDTO> itemDTOs = entity.getItems().stream()
                .map(this::convertItemToDTO)
                .collect(Collectors.toList());
            dto.setItems(itemDTOs);
        }

        return dto;
    }

    // Convert Item Entity to DTO
    private PurchaseRequestItemDTO convertItemToDTO(PurchaseRequestItem entity) {
        List<ItemReceiptDTO> receiptDTOs = null;
        if (entity.getReceipts() != null && !entity.getReceipts().isEmpty()) {
            receiptDTOs = entity.getReceipts().stream()
                .map(this::convertReceiptToDTO)
                .collect(Collectors.toList());
        }

        return PurchaseRequestItemDTO.builder()
            .id(entity.getId())
            .itemCode(entity.getItemCode())
            .itemName(entity.getItemName())
            .itemBarcode(entity.getItemBarcode())

            .batchNo(entity.getBatchNo())

            .quantity(entity.getQuantity())
            .unit(entity.getUnit())
            .unitPrice(entity.getUnitPrice())
            .total(entity.getTotal())
            .remarks(entity.getRemarks())
            .receivedQuantity(entity.getReceivedQuantity())
            .pendingQuantity(entity.getPendingQuantity())
            .itemStatus(entity.getItemStatus())
            .receipts(receiptDTOs)
            .build();
    }

    // Convert Receipt Entity to DTO
    private ItemReceiptDTO convertReceiptToDTO(ItemReceipt entity) {
        return ItemReceiptDTO.builder()
            .id(entity.getId())
            .receivedDate(entity.getReceivedDate())
            .receivedQuantity(entity.getReceivedQuantity())
            .defectiveQuantity(entity.getDefectiveQuantity())
            .qualityStatus(entity.getQualityStatus())
            .remarks(entity.getRemarks())
            .images(entity.getImages() != null ? entity.getImages() : new ArrayList<>())
            .build();
    }
}
