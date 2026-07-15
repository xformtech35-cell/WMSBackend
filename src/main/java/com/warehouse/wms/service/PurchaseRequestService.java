package com.warehouse.wms.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.warehouse.wms.dto.CreatePurchaseRequestDTO;
import com.warehouse.wms.dto.ItemReceiptDTO;
import com.warehouse.wms.dto.PurchaseRequestDTO;
import com.warehouse.wms.dto.PurchaseRequestFilterDTO;
import com.warehouse.wms.dto.PurchaseRequestItemDTO;
import com.warehouse.wms.dto.ReceiveItemDTO;
import com.warehouse.wms.dto.StatusUpdateRequestDTO;
import com.warehouse.wms.entity.Item;
import com.warehouse.wms.entity.ItemReceipt;
import com.warehouse.wms.entity.Priority;
import com.warehouse.wms.entity.PurchaseRequest;
import com.warehouse.wms.entity.PurchaseRequestItem;
import com.warehouse.wms.entity.RequestStatus;
import com.warehouse.wms.entity.Supplier;
import com.warehouse.wms.exception.ResourceNotFoundException;
import com.warehouse.wms.repository.ItemRepository;
import com.warehouse.wms.repository.PurchaseRequestItemRepository;
import com.warehouse.wms.repository.PurchaseRequestRepository;
import com.warehouse.wms.repository.SupplierRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PurchaseRequestService {

    private final PurchaseRequestRepository purchaseRequestRepository;
    private final PurchaseRequestItemRepository purchaseRequestItemRepository;
    private final SupplierRepository supplierRepository;
    private final ItemRepository itemRepository;

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
        
        // Get count of PRs for today
        Long count = purchaseRequestRepository.countByPrNumberStartingWith(prefix);
        int nextSequence = count.intValue() + 1;
        
        String prNumber = String.format("%s-%04d", prefix, nextSequence);
        
        // Safety check: If this PR number already exists, increment until we find a unique one
        while (purchaseRequestRepository.existsByPrNumber(prNumber)) {
            nextSequence++;
            prNumber = String.format("%s-%04d", prefix, nextSequence);
        }
        
        log.info("Generated PR Number: {}", prNumber);
        return prNumber;
    }

    // ============ CREATE ============
    
    @Transactional
    public PurchaseRequestDTO createPurchaseRequest(CreatePurchaseRequestDTO requestDTO) {
        log.info("Creating purchase request for: {}", requestDTO.getRequestedBy());
        
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
        
        // Set supplier if provided
        if (requestDTO.getSupplierId() != null) {
            Supplier supplier = supplierRepository.findById(requestDTO.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + requestDTO.getSupplierId()));
            purchaseRequest.setSupplier(supplier);
        }
        
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
            item.setItemBarcode(itemDTO.getItemBarcode());
            item.setReceivedQuantity(0);
            item.setPendingQuantity(itemDTO.getRequestedQty());
            item.setItemStatus("PENDING");
            item.setReceipts(new ArrayList<>());
            
            // Link to existing Item if provided
            if (itemDTO.getItemId() != null) {
                Item existingItem = itemRepository.findById(itemDTO.getItemId()).orElse(null);
                if (existingItem != null) {
                    item.setItem(existingItem);
                }
            }
            
            item.setPurchaseRequest(purchaseRequest);
            purchaseRequest.addItem(item);
        }
        
        purchaseRequest = purchaseRequestRepository.save(purchaseRequest);
        
        log.info("Purchase request created with PR Number: {}", purchaseRequest.getPrNumber());
        return convertToDTO(purchaseRequest);
    }

    // ============ READ ============
    
    public PurchaseRequestDTO getPurchaseRequestById(Long id) {
        PurchaseRequest purchaseRequest = purchaseRequestRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Purchase request not found with id: " + id));
        return convertToDTO(purchaseRequest);
    }
    
    public Page<PurchaseRequestDTO> getAllPurchaseRequests(String status, String priority,
                                                            LocalDate startDate, LocalDate endDate,
                                                            Pageable pageable) {
        RequestStatus statusEnum = status != null ? RequestStatus.valueOf(status) : null;
        Priority priorityEnum = priority != null ? Priority.valueOf(priority) : null;

        Page<PurchaseRequest> requests = purchaseRequestRepository.findWithFilters(
            statusEnum, priorityEnum, startDate, endDate, pageable);

        return requests.map(this::convertToDTO);
    }
    
    public List<PurchaseRequestDTO> getPurchaseRequestsByUser(String requestedBy) {
        List<PurchaseRequest> requests = purchaseRequestRepository.findByRequestedBy(requestedBy);
        return requests.stream().map(this::convertToDTO).collect(Collectors.toList());
    }
    
    public List<PurchaseRequestDTO> getPurchaseRequestsByUserId(Long userId) {
        List<PurchaseRequest> requests = purchaseRequestRepository.findByCreatedBy(userId);
        return requests.stream().map(this::convertToDTO).collect(Collectors.toList());
    }
    
    public List<PurchaseRequestItemDTO> getItemsByPurchaseRequestId(Long purchaseRequestId) {
        PurchaseRequest purchaseRequest = purchaseRequestRepository.findById(purchaseRequestId)
            .orElseThrow(() -> new ResourceNotFoundException("Purchase request not found with id: " + purchaseRequestId));
        
        return purchaseRequest.getItems().stream()
            .map(this::convertItemToDTO)
            .collect(Collectors.toList());
    }
    
    public PurchaseRequestItemDTO getItemById(Long itemId) {
        PurchaseRequestItem item = purchaseRequestItemRepository.findById(itemId)
            .orElseThrow(() -> new ResourceNotFoundException("Item not found with id: " + itemId));
        return convertItemToDTO(item);
    }

    // ============ UPDATE ============
    
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
            item.setDescription(itemDTO.getDescription());
            item.setUom(itemDTO.getUom());
            item.setRequestedQty(itemDTO.getRequestedQty());
            item.setCurrentStock(itemDTO.getCurrentStock());
            item.setReason(itemDTO.getReason());
            item.setItemBarcode(itemDTO.getItemBarcode());
            item.setReceivedQuantity(0);
            item.setPendingQuantity(itemDTO.getRequestedQty());
            item.setItemStatus("PENDING");
            item.setReceipts(new ArrayList<>());
            
            // Link to existing Item if provided
            if (itemDTO.getItemId() != null) {
                Item existingItem = itemRepository.findById(itemDTO.getItemId()).orElse(null);
                if (existingItem != null) {
                    item.setItem(existingItem);
                }
            }
            
            item.setPurchaseRequest(purchaseRequest);
            purchaseRequest.addItem(item);
        }

        purchaseRequest = purchaseRequestRepository.save(purchaseRequest);

        log.info("Purchase request updated: {}", id);
        return convertToDTO(purchaseRequest);
    }

    // ============ SUBMIT ============
    
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

    // ============ APPROVE ============
    
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

    // ============ RECEIVE ============
    
    @Transactional
    public PurchaseRequestItemDTO receiveItem(Long itemId, ReceiveItemDTO receiveDTO) {
        log.info("Receiving item: {}", itemId);
        log.info("Received quantity: {}", receiveDTO.getReceivedQuantity());
        log.info("Quality status: {}", receiveDTO.getQualityStatus());
        
        String qualityStatus = receiveDTO.getQualityStatus().trim().toUpperCase();

        if (!List.of("GOOD", "PARTIAL", "REJECTED").contains(qualityStatus)) {
            throw new IllegalStateException("Quality status must be GOOD, PARTIAL, or REJECTED");
        }

        PurchaseRequestItem item = purchaseRequestItemRepository.findById(itemId)
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

        // Update stock in Item entity if linked and quality is GOOD
        if (item.getItem() != null && "GOOD".equals(qualityStatus)) {
            Item inventoryItem = item.getItem();
            int newStock = inventoryItem.getCurrentStock() + receiveDTO.getReceivedQuantity();
            inventoryItem.setCurrentStock(newStock);
            itemRepository.save(inventoryItem);
            log.info("Updated stock for item: {} to {}", inventoryItem.getItemCode(), newStock);
        }

        purchaseRequestItemRepository.save(item);
        updatePurchaseRequestStatus(item.getPurchaseRequest().getId());

        log.info("Item received successfully: {}", itemId);
        return convertItemToDTO(item);
    }

    // ============ DELETE ============
    
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

    // ============ STATISTICS ============
    
    public Object getStatistics() {
        long draftCount = purchaseRequestRepository.countByStatus(RequestStatus.DRAFT);
        long submittedCount = purchaseRequestRepository.countByStatus(RequestStatus.SUBMITTED);
        long approvedCount = purchaseRequestRepository.countByStatus(RequestStatus.APPROVED);
        long rejectedCount = purchaseRequestRepository.countByStatus(RequestStatus.REJECTED);
        long completedCount = purchaseRequestRepository.countByStatus(RequestStatus.COMPLETED);
        long partialCount = purchaseRequestRepository.countByStatus(RequestStatus.PARTIAL);

        return java.util.Map.of(
            "draft", draftCount,
            "submitted", submittedCount,
            "approved", approvedCount,
            "rejected", rejectedCount,
            "completed", completedCount,
            "partial", partialCount,
            "total", draftCount + submittedCount + approvedCount + rejectedCount + completedCount + partialCount
        );
    }

    // ============ HELPER METHODS ============
    
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

        purchaseRequestRepository.save(purchaseRequest);
    }

    // ============ CONVERSION METHODS ============

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
        dto.setAprovalRemarks(entity.getAprovalRemarks());
        

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
            .itemBarcode(entity.getItemBarcode())
            .receivedQuantity(entity.getReceivedQuantity())
            .pendingQuantity(entity.getPendingQuantity())
            .itemStatus(entity.getItemStatus())
            .itemId(entity.getItem() != null ? entity.getItem().getId() : null)
            .receipts(receiptDTOs)
            .build();
    }

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
    
    
    
    
    @Transactional
    public PurchaseRequestDTO updateStatus(Long id, StatusUpdateRequestDTO statusUpdateRequest, Long userId) {
        log.info("Updating status for purchase request: {} to {}", id, statusUpdateRequest.getStatus());
        
        PurchaseRequest purchaseRequest = purchaseRequestRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Purchase request not found with id: " + id));
        
        RequestStatus newStatus = statusUpdateRequest.getStatus();
        RequestStatus currentStatus = purchaseRequest.getStatus();
        
        // Validate status transition
        validateStatusTransition(currentStatus, newStatus);
        
        // Execute status change
        switch (newStatus) {
            case PENDING:
                purchaseRequest.setStatus(RequestStatus.PENDING);
                break;
                
            case SUBMITTED:
                purchaseRequest.setStatus(RequestStatus.SUBMITTED);
                purchaseRequest.setSubmittedAt(LocalDateTime.now());
                break;
                
            case APPROVED:
                purchaseRequest.setStatus(RequestStatus.APPROVED);
                purchaseRequest.setApprovedAt(LocalDateTime.now());
                break;
                
            case REJECTED:
                purchaseRequest.setStatus(RequestStatus.REJECTED);
                purchaseRequest.setApprovedAt(LocalDateTime.now());
                purchaseRequest.setRejectionReason(
                    statusUpdateRequest.getRejectionReason() != null ? 
                    statusUpdateRequest.getRejectionReason() : 
                    "No reason provided"
                );
                break;
                
            case IN_PROGRESS:
                purchaseRequest.setStatus(RequestStatus.IN_PROGRESS);
                break;
                
            case PARTIAL:
                purchaseRequest.setStatus(RequestStatus.PARTIAL);
                break;
                
            case COMPLETED:
                purchaseRequest.setStatus(RequestStatus.COMPLETED);
                break;
                
        
                
            case DRAFT:
                purchaseRequest.setStatus(RequestStatus.DRAFT);
                purchaseRequest.setSubmittedAt(null);
                purchaseRequest.setApprovedAt(null);
                purchaseRequest.setRejectionReason(null);
                break;
                
            default:
                throw new IllegalStateException("Unsupported status: " + newStatus);
        }
        
        // Add remarks if provided
        if (statusUpdateRequest.getRemarks() != null) {
            String newRemarks =  statusUpdateRequest.getRemarks();
            purchaseRequest.setAprovalRemarks(newRemarks);
        }
        
        purchaseRequest = purchaseRequestRepository.save(purchaseRequest);
        log.info("Purchase request status updated: {} -> {}", currentStatus, newStatus);
        
        return convertToDTO(purchaseRequest);
    }

    private void validateStatusTransition(RequestStatus currentStatus, RequestStatus newStatus) {
        if (currentStatus == newStatus) {
            throw new IllegalStateException("Purchase request is already in " + newStatus + " status");
        }
        
        Map<RequestStatus, List<RequestStatus>> allowedTransitions = new HashMap<>();
        
        allowedTransitions.put(RequestStatus.DRAFT, Arrays.asList(
            RequestStatus.PENDING,
            RequestStatus.SUBMITTED
        ));
        
        allowedTransitions.put(RequestStatus.PENDING, Arrays.asList(
                RequestStatus.APPROVED,
	
            RequestStatus.SUBMITTED
        ));
        
        allowedTransitions.put(RequestStatus.SUBMITTED, Arrays.asList(
            RequestStatus.APPROVED,
            RequestStatus.REJECTED
        ));
        
        allowedTransitions.put(RequestStatus.APPROVED, Arrays.asList(
            RequestStatus.IN_PROGRESS,
            RequestStatus.COMPLETED
        ));
        
        allowedTransitions.put(RequestStatus.IN_PROGRESS, Arrays.asList(
            RequestStatus.PARTIAL,
            RequestStatus.COMPLETED
        ));
        
        allowedTransitions.put(RequestStatus.PARTIAL, Arrays.asList(
            RequestStatus.IN_PROGRESS,
            RequestStatus.COMPLETED
        ));
        
        allowedTransitions.put(RequestStatus.REJECTED, Arrays.asList(
            RequestStatus.DRAFT
        ));
        
        allowedTransitions.put(RequestStatus.COMPLETED, Arrays.asList(
            RequestStatus.DRAFT
        ));
    
        
        List<RequestStatus> allowed = allowedTransitions.get(currentStatus);
        if (allowed == null || !allowed.contains(newStatus)) {
            throw new IllegalArgumentException(
                "Cannot transition from " + currentStatus + " to " + newStatus + 
                ". Allowed transitions: " + allowed
            );
        }
    }

    
    
    
    public Page<PurchaseRequestDTO> filterPurchaseRequests(PurchaseRequestFilterDTO filter, Pageable pageable) {
        log.info("Filtering purchase requests with: {}", filter);
        
        Page<PurchaseRequest> requests = purchaseRequestRepository.filterPurchaseRequests(
            filter.getStatus(),
            filter.getStatuses(),
            filter.getPriority(),
            filter.getPriorities(),
            filter.getStartDate(),
            filter.getEndDate(),
            filter.getPrDateFrom(),
            filter.getPrDateTo(),
            filter.getRequiredDateFrom(),
            filter.getRequiredDateTo(),
            filter.getPrNumber(),
            filter.getRequestedBy(),
            filter.getDepartment(),
            filter.getWarehouse(),
            filter.getRemarks(),
            filter.getItemCode(),
            filter.getItemName(),
            filter.getHasSupplier(),
            filter.getHasItems(),
            filter.getCreatedFrom(),
            filter.getCreatedTo(),
            filter.getSearchTerm(),
            pageable
        );
        
        return requests.map(this::convertToDTO);
    }
    
}