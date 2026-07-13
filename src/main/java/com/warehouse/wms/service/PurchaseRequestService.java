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

    private static final String PR_PREFIX = "PR";

    /**
     * Generates PR number in format: PR-YYYY-MMDD-SEQ
     * Example: PR-2026-0713-0001
     */
    private String generatePRNumber() {
        LocalDate now = LocalDate.now();
        String year = String.valueOf(now.getYear());
        String monthDay = String.format("%02d%02d", now.getMonthValue(), now.getDayOfMonth());
        String datePart = String.format("%s-%s", year, monthDay);
        String prefix = String.format("%s-%s", PR_PREFIX, datePart);
        
        // Get the latest sequence number for today
        Integer maxSequence = purchaseRequestRepository.findMaxSequenceForDate(datePart);
        
        // If maxSequence is null, start from 1
        int nextSequence = (maxSequence != null) ? maxSequence + 1 : 1;
        
        // Generate the PR number
        String prNumber = String.format("%s-%04d", prefix, nextSequence);
        
        // Safety check: If this PR number already exists, increment until we find a unique one
        while (purchaseRequestRepository.existsByPrNumber(prNumber)) {
            nextSequence++;
            prNumber = String.format("%s-%04d", prefix, nextSequence);
        }
        
        return prNumber;
    }

    // Create new purchase request
    @Transactional
    public PurchaseRequestDTO createPurchaseRequest(CreatePurchaseRequestDTO requestDTO) {
        log.info("Creating purchase request for: {}", requestDTO.getRequestedBy());
        
        // Create and populate purchase request
        PurchaseRequest purchaseRequest = new PurchaseRequest();
        purchaseRequest.setPrNumber(generatePRNumber());
        purchaseRequest.setPrDate(requestDTO.getPrDate());
        purchaseRequest.setRequestedBy(requestDTO.getRequestedBy());
        purchaseRequest.setDepartment(requestDTO.getDepartment());
        purchaseRequest.setWarehouse(requestDTO.getWarehouse());
        purchaseRequest.setPriority(requestDTO.getPriority());
        purchaseRequest.setRequiredDate(requestDTO.getRequiredDate());
        purchaseRequest.setRemarks(requestDTO.getRemarks());
        purchaseRequest.setStatus(RequestStatus.DRAFT);
        
        // Save purchase request first
        purchaseRequest = purchaseRequestRepository.save(purchaseRequest);
        
        // Add items
        for (PurchaseRequestItemDTO itemDTO : requestDTO.getItems()) {
            PurchaseRequestItem item = new PurchaseRequestItem();
            item.setItemCode(itemDTO.getItemCode());
            item.setItemName(itemDTO.getItemName());
            item.setDescription(itemDTO.getDescription());
            item.setUom(itemDTO.getUom());
            item.setRequestedQty(itemDTO.getRequestedQty());
            item.setCurrentStock(itemDTO.getCurrentStock());
            item.setReason(itemDTO.getReason());
            item.setPurchaseRequest(purchaseRequest);
            purchaseRequest.addItem(item);
        }
        
        // Save again with items
        purchaseRequest = purchaseRequestRepository.save(purchaseRequest);
        
        log.info("Purchase request created with PR Number: {}", purchaseRequest.getPrNumber());
        
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
        int pendingQty = item.getPendingQuantity() != null ? item.getPendingQuantity() : item.getRequestedQty();
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
        int pending = item.getRequestedQty() - totalReceived;
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
    public PurchaseRequestDTO submitPurchaseRequest(Long id) {
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
    public PurchaseRequestDTO approvePurchaseRequest(Long id, boolean approved, String rejectionReason) {
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
    public PurchaseRequestDTO updatePurchaseRequest(Long id, CreatePurchaseRequestDTO requestDTO) {
        log.info("Updating purchase request: {}", id);

        PurchaseRequest purchaseRequest = purchaseRequestRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Purchase request not found with id: " + id));

        if (purchaseRequest.getStatus() != RequestStatus.DRAFT) {
            throw new IllegalStateException("Only draft requests can be updated");
        }

        // Update basic info
        purchaseRequest.setPrDate(requestDTO.getPrDate());
        purchaseRequest.setRequestedBy(requestDTO.getRequestedBy());
        purchaseRequest.setDepartment(requestDTO.getDepartment());
        purchaseRequest.setWarehouse(requestDTO.getWarehouse());
        purchaseRequest.setPriority(requestDTO.getPriority());
        purchaseRequest.setRequiredDate(requestDTO.getRequiredDate());
        purchaseRequest.setRemarks(requestDTO.getRemarks());

        // Clear existing items
        purchaseRequest.getItems().clear();

        // Add new items
        for (PurchaseRequestItemDTO itemDTO : requestDTO.getItems()) {
            PurchaseRequestItem item = new PurchaseRequestItem();
            item.setItemCode(itemDTO.getItemCode());
            item.setItemName(itemDTO.getItemName());
            item.setDescription(itemDTO.getDescription());
            item.setUom(itemDTO.getUom());
            item.setRequestedQty(itemDTO.getRequestedQty());
            item.setCurrentStock(itemDTO.getCurrentStock());
            item.setReason(itemDTO.getReason());
            item.setPurchaseRequest(purchaseRequest);
            purchaseRequest.addItem(item);
        }

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
    public List<PurchaseRequestDTO> getPurchaseRequestsByUser(String requestedBy) {
        List<PurchaseRequest> requests = purchaseRequestRepository.findByRequestedBy(requestedBy);
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
        dto.setPrDate(entity.getPrDate());
        dto.setRequestedBy(entity.getRequestedBy());
        dto.setDepartment(entity.getDepartment());
        dto.setWarehouse(entity.getWarehouse());
        dto.setPriority(entity.getPriority());
        dto.setRequiredDate(entity.getRequiredDate());
        dto.setRemarks(entity.getRemarks());
        dto.setStatus(entity.getStatus());
        dto.setSubmittedAt(entity.getSubmittedAt());
        dto.setApprovedAt(entity.getApprovedAt());
        dto.setRejectionReason(entity.getRejectionReason());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());

        if (entity.getItems() != null) {
            List<PurchaseRequestItemDTO> itemDTOs = entity.getItems().stream()
                .map(this::convertItemToDTO)
                .collect(Collectors.toList());
            dto.setItems(itemDTOs);
        }

        return dto;
    }

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
            .description(entity.getDescription())
            .uom(entity.getUom())
            .requestedQty(entity.getRequestedQty())
            .currentStock(entity.getCurrentStock())
            .reason(entity.getReason())
            .receivedQuantity(entity.getReceivedQuantity())
            .pendingQuantity(entity.getPendingQuantity())
            .itemStatus(entity.getItemStatus())
            .receipts(receiptDTOs)  // Now this will work
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
    
    
    
  

    // Get purchase requests by user ID (if you have user entity)
    public List<PurchaseRequestDTO> getPurchaseRequestsByUserId(Long userId) {
        // If you have a User entity with name field
        // User user = userRepository.findById(userId).orElseThrow();
        // List<PurchaseRequest> requests = purchaseRequestRepository.findByRequestedBy(user.getName());
        // Or if you want to store userId instead of name, update the entity to have createdBy field
        return purchaseRequestRepository.findAll().stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
}