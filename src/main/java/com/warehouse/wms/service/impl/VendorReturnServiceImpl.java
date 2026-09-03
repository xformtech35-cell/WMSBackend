package com.warehouse.wms.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.warehouse.wms.dto.request.DispatchDTO;
import com.warehouse.wms.dto.request.DispatchItemDTO;
import com.warehouse.wms.dto.request.PackingDTO;
import com.warehouse.wms.dto.request.PickListFilterDTO;
import com.warehouse.wms.dto.request.PickingDTO;
import com.warehouse.wms.dto.request.QCDTO;
import com.warehouse.wms.dto.request.ReturnOrderFilterDTO;
import com.warehouse.wms.dto.request.SettlementDTO;
import com.warehouse.wms.dto.request.VendorReceiptDTO;
import com.warehouse.wms.dto.request.VendorReceiptLineDTO;
import com.warehouse.wms.dto.request.VendorReturnOrderDTO;
import com.warehouse.wms.dto.request.VendorReturnOrderLineDTO;
import com.warehouse.wms.dto.request.VendorReturnRequestDTO;
import com.warehouse.wms.dto.request.VendorReturnRequestLineDTO;
import com.warehouse.wms.dto.response.DispatchItemResponseDTO;
import com.warehouse.wms.dto.response.DispatchResponseDTO;
import com.warehouse.wms.dto.response.PickListItemDTO;
import com.warehouse.wms.dto.response.PickListResponseDTO;
import com.warehouse.wms.dto.response.SettlementResponseDTO;
import com.warehouse.wms.dto.response.VendorReceiptLineResponseDTO;
import com.warehouse.wms.dto.response.VendorReceiptResponseDTO;
import com.warehouse.wms.dto.response.VendorReturnLineResponseDTO;
import com.warehouse.wms.dto.response.VendorReturnOrderLineResponseDTO;
import com.warehouse.wms.dto.response.VendorReturnOrderResponseDTO;
import com.warehouse.wms.dto.response.VendorReturnResponseDTO;
import com.warehouse.wms.entity.ReturnDispatch;
import com.warehouse.wms.entity.ReturnDispatchItem;
import com.warehouse.wms.entity.ReturnSettlement;
import com.warehouse.wms.entity.VendorReceipt;
import com.warehouse.wms.entity.VendorReceiptLine;
import com.warehouse.wms.entity.VendorReturnOrder;
import com.warehouse.wms.entity.VendorReturnOrderLine;
import com.warehouse.wms.entity.VendorReturnRequest;
import com.warehouse.wms.entity.VendorReturnRequestLine;
import com.warehouse.wms.exception.ResourceNotFoundException;
import com.warehouse.wms.repository.ReturnDispatchRepository;
import com.warehouse.wms.repository.ReturnSettlementRepository;
import com.warehouse.wms.repository.SupplierRepository;
import com.warehouse.wms.repository.VendorReceiptRepository;
import com.warehouse.wms.repository.VendorReturnOrderRepository;
import com.warehouse.wms.repository.VendorReturnRequestRepository;
import com.warehouse.wms.service.VendorReturnService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class VendorReturnServiceImpl implements VendorReturnService {

    private final VendorReturnRequestRepository requestRepository;
    private final VendorReturnOrderRepository orderRepository;
    private final ReturnDispatchRepository dispatchRepository;
    private final VendorReceiptRepository receiptRepository;
    private final ReturnSettlementRepository settlementRepository;
    private final SupplierRepository supplierRepository;

    private static final String REQUEST_PREFIX = "VRR-";
    private static final String ORDER_PREFIX = "VRO-";
    private static final String DISPATCH_PREFIX = "VRD-";
    private static final String RECEIPT_PREFIX = "VRC-";
    private static final String SETTLEMENT_PREFIX = "VRS-";

    // ========== RETURN REQUEST OPERATIONS ==========

    @Override
    public VendorReturnResponseDTO createReturnRequest(VendorReturnRequestDTO request) {
        log.info("Creating return request for supplier: {}", request.getSupplierName());

        VendorReturnRequest returnRequest = new VendorReturnRequest();
        returnRequest.setReturnRequestNumber(generateRequestNumber());
        returnRequest.setRequestDate(request.getRequestDate() != null ? request.getRequestDate() : LocalDate.now());
        returnRequest.setPoNumber(request.getPoNumber());
        returnRequest.setGrnNumber(request.getGrnNumber());
        returnRequest.setInvoiceNumber(request.getInvoiceNumber());
        returnRequest.setSupplierName(request.getSupplierName());
        returnRequest.setSupplierCode(request.getSupplierCode());
        returnRequest.setReturnType(request.getReturnType());
        returnRequest.setReturnReason(request.getReturnReason());
        returnRequest.setPriority(request.getPriority() != null ? request.getPriority() : VendorReturnRequest.Priority.MEDIUM);
        returnRequest.setStatus(VendorReturnRequest.RequestStatus.PENDING_APPROVAL);
        returnRequest.setRemarks(request.getRemarks());
        
        if (request.getSupplierId() != null) {
            returnRequest.setSupplier(supplierRepository.findById(request.getSupplierId())
                    .orElseThrow(() -> new ResourceNotFoundException("Supplier not found")));
        }

        // Add lines
        if (request.getLines() != null) {
            for (VendorReturnRequestLineDTO lineRequest : request.getLines()) {
                VendorReturnRequestLine line = new VendorReturnRequestLine();
                line.setItemCode(lineRequest.getItemCode());
                line.setItemName(lineRequest.getItemName());
                line.setUom(lineRequest.getUom());
                line.setRequestedQuantity(lineRequest.getRequestedQuantity());
                line.setUnitPrice(lineRequest.getUnitPrice());
                line.setTotalAmount(lineRequest.getTotalAmount() != null ? 
                        lineRequest.getTotalAmount() : 
                        BigDecimal.valueOf(lineRequest.getRequestedQuantity()).multiply(lineRequest.getUnitPrice()));
                line.setOriginalQuantity(lineRequest.getOriginalQuantity());
                line.setReceivedQuantity(lineRequest.getReceivedQuantity());
                line.setBatchNumber(lineRequest.getBatchNumber());
                line.setSerialNumbers(lineRequest.getSerialNumbers());
                line.setExpiryDate(lineRequest.getExpiryDate());
                line.setReason(lineRequest.getReason());
                line.setRemarks(lineRequest.getRemarks());
                line.setInboundLineId(lineRequest.getInboundLineId());
                returnRequest.addLine(line);
            }
        }

        VendorReturnRequest saved = requestRepository.save(returnRequest);
        log.info("Return request created with ID: {} and Number: {}", saved.getId(), saved.getReturnRequestNumber());
        return mapToResponseDTO(saved);
    }

    @Override
    public VendorReturnResponseDTO updateReturnRequest(Long id, VendorReturnRequestDTO request) {
        log.info("Updating return request with ID: {}", id);
        
        VendorReturnRequest returnRequest = requestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Return request not found with ID: " + id));
        
        if (!returnRequest.isEditable()) {
            throw new IllegalStateException("Cannot update request in status: " + returnRequest.getStatus());
        }
        
        returnRequest.setPoNumber(request.getPoNumber());
        returnRequest.setGrnNumber(request.getGrnNumber());
        returnRequest.setInvoiceNumber(request.getInvoiceNumber());
        returnRequest.setSupplierName(request.getSupplierName());
        returnRequest.setSupplierCode(request.getSupplierCode());
        returnRequest.setReturnType(request.getReturnType());
        returnRequest.setReturnReason(request.getReturnReason());
        returnRequest.setPriority(request.getPriority());
        returnRequest.setRemarks(request.getRemarks());
        
        // Update lines (clear and re-add)
        returnRequest.getLines().clear();
        if (request.getLines() != null) {
            for (VendorReturnRequestLineDTO lineRequest : request.getLines()) {
                VendorReturnRequestLine line = new VendorReturnRequestLine();
                line.setItemCode(lineRequest.getItemCode());
                line.setItemName(lineRequest.getItemName());
                line.setUom(lineRequest.getUom());
                line.setRequestedQuantity(lineRequest.getRequestedQuantity());
                line.setUnitPrice(lineRequest.getUnitPrice());
                line.setTotalAmount(lineRequest.getTotalAmount() != null ? 
                        lineRequest.getTotalAmount() : 
                        BigDecimal.valueOf(lineRequest.getRequestedQuantity()).multiply(lineRequest.getUnitPrice()));
                line.setOriginalQuantity(lineRequest.getOriginalQuantity());
                line.setReceivedQuantity(lineRequest.getReceivedQuantity());
                line.setBatchNumber(lineRequest.getBatchNumber());
                line.setSerialNumbers(lineRequest.getSerialNumbers());
                line.setExpiryDate(lineRequest.getExpiryDate());
                line.setReason(lineRequest.getReason());
                line.setRemarks(lineRequest.getRemarks());
                line.setInboundLineId(lineRequest.getInboundLineId());
                returnRequest.addLine(line);
            }
        }
        
        VendorReturnRequest updated = requestRepository.save(returnRequest);
        log.info("Return request updated with ID: {}", updated.getId());
        return mapToResponseDTO(updated);
    }

    @Override
    public VendorReturnResponseDTO getReturnRequestById(Long id) {
        log.info("Fetching return request with ID: {}", id);
        VendorReturnRequest returnRequest = requestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Return request not found with ID: " + id));
        return mapToResponseDTO(returnRequest);
    }

    @Override
    public VendorReturnResponseDTO getReturnRequestByNumber(String requestNumber) {
        log.info("Fetching return request with Number: {}", requestNumber);
        VendorReturnRequest returnRequest = requestRepository.findByReturnRequestNumber(requestNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Return request not found with Number: " + requestNumber));
        return mapToResponseDTO(returnRequest);
    }

    @Override
    public Page<VendorReturnResponseDTO> getAllReturnRequests(Pageable pageable) {
        log.info("Fetching all return requests");
        return requestRepository.findAll(pageable)
                .map(this::mapToResponseDTO);
    }

    @Override
    public Page<VendorReturnResponseDTO> searchReturnRequests(String supplierName, String status, String searchTerm, Pageable pageable) {
        log.info("Searching return requests with filters");
        VendorReturnRequest.RequestStatus requestStatus = status != null ? 
                VendorReturnRequest.RequestStatus.valueOf(status) : null;
        return requestRepository.searchRequests(supplierName, requestStatus, searchTerm, pageable)
                .map(this::mapToResponseDTO);
    }

    @Override
    public VendorReturnResponseDTO submitReturnRequest(Long id) {
        log.info("Submitting return request with ID: {}", id);
        
        VendorReturnRequest request = requestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Return request not found"));
        
        if (request.getStatus() != VendorReturnRequest.RequestStatus.DRAFT) {
            throw new IllegalStateException("Only DRAFT requests can be submitted");
        }
        
        request.setStatus(VendorReturnRequest.RequestStatus.PENDING_APPROVAL);
        
        VendorReturnRequest submitted = requestRepository.save(request);
        return mapToResponseDTO(submitted);
    }

    @Override
    public VendorReturnResponseDTO approveReturnRequest(Long id, Long approvedBy) {
        log.info("Approving return request with ID: {} by user: {}", id, approvedBy);
        
        VendorReturnRequest request = requestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Return request not found"));
        
        if (request.getStatus() != VendorReturnRequest.RequestStatus.PENDING_APPROVAL) {
            throw new IllegalStateException("Only PENDING_APPROVAL requests can be approved");
        }
        
        request.setStatus(VendorReturnRequest.RequestStatus.APPROVED);
        request.setApprovedBy(approvedBy);
        request.setApprovedDate(LocalDateTime.now());
        
        // Create return order automatically
        VendorReturnOrder order = createReturnOrderFromRequest(request);
        orderRepository.save(order);
        
        VendorReturnRequest approved = requestRepository.save(request);
        return mapToResponseDTO(approved);
    }

    @Override
    public VendorReturnResponseDTO rejectReturnRequest(Long id, Long rejectedBy, String rejectionReason) {
        log.info("Rejecting return request with ID: {} by user: {}", id, rejectedBy);
        
        VendorReturnRequest request = requestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Return request not found"));
        
        if (request.getStatus() != VendorReturnRequest.RequestStatus.PENDING_APPROVAL) {
            throw new IllegalStateException("Only PENDING_APPROVAL requests can be rejected");
        }
        
        request.setStatus(VendorReturnRequest.RequestStatus.REJECTED);
        request.setRejectedBy(rejectedBy);
        request.setRejectedDate(LocalDateTime.now());
        request.setRejectionReason(rejectionReason);
        
        VendorReturnRequest rejected = requestRepository.save(request);
        return mapToResponseDTO(rejected);
    }

    @Override
    public void deleteReturnRequest(Long id) {
        log.info("Deleting return request with ID: {}", id);
        VendorReturnRequest request = requestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Return request not found"));
        
        if (request.getStatus() != VendorReturnRequest.RequestStatus.DRAFT && 
            request.getStatus() != VendorReturnRequest.RequestStatus.CANCELLED) {
            throw new IllegalStateException("Cannot delete request in status: " + request.getStatus());
        }
        
        requestRepository.delete(request);
        log.info("Return request deleted with ID: {}", id);
    }

    // ========== RETURN ORDER OPERATIONS ==========

    @Override
    public VendorReturnOrderResponseDTO createReturnOrder(VendorReturnOrderDTO request) {
        log.info("Creating return order for supplier: {}", request.getSupplierName());
        
        VendorReturnOrder order = new VendorReturnOrder();
        order.setVroNumber(generateOrderNumber());
        order.setOrderDate(request.getOrderDate() != null ? request.getOrderDate() : LocalDate.now());
        order.setExpectedReturnDate(request.getExpectedReturnDate());
        
        if (request.getReturnRequestId() != null) {
            VendorReturnRequest returnRequest = requestRepository.findById(request.getReturnRequestId())
                    .orElseThrow(() -> new ResourceNotFoundException("Return request not found"));
            order.setReturnRequest(returnRequest);
            order.setSupplier(returnRequest.getSupplier());
            order.setSupplierName(returnRequest.getSupplierName());
            order.setSupplierCode(returnRequest.getSupplierCode());
            order.setReturnType(returnRequest.getReturnType());
            order.setReturnReason(returnRequest.getReturnReason());
            order.setPriority(returnRequest.getPriority());
        } else {
            order.setSupplierName(request.getSupplierName());
            order.setSupplierCode(request.getSupplierCode());
            order.setReturnType(request.getReturnType());
            order.setReturnReason(request.getReturnReason());
            order.setPriority(request.getPriority() != null ? request.getPriority() : VendorReturnRequest.Priority.MEDIUM);
        }
        
        order.setStatus(VendorReturnOrder.OrderStatus.CREATED);
        order.setShippingAddress(request.getShippingAddress());
        order.setShippingMethod(request.getShippingMethod());
        
        // Add lines
        if (request.getLines() != null) {
            for (VendorReturnOrderLineDTO lineRequest : request.getLines()) {
                VendorReturnOrderLine line = new VendorReturnOrderLine();
                line.setItemCode(lineRequest.getItemCode());
                line.setItemName(lineRequest.getItemName());
                line.setUom(lineRequest.getUom());
                line.setRejectedArea(lineRequest.getRejectedArea());
                line.setOrderQuantity(lineRequest.getOrderQuantity());
                line.setBatchNumber(lineRequest.getBatchNumber());
                line.setSerialNumbers(lineRequest.getSerialNumbers());
                line.setExpiryDate(lineRequest.getExpiryDate());
                line.setUnitPrice(lineRequest.getUnitPrice());
                line.setTotalAmount(lineRequest.getTotalAmount() != null ? 
                        lineRequest.getTotalAmount() : 
                        BigDecimal.valueOf(lineRequest.getOrderQuantity()).multiply(lineRequest.getUnitPrice()));
                line.setPickLocation(lineRequest.getPickLocation());
                line.setPickSequence(lineRequest.getPickSequence());
                line.setReturnRequestLineId(lineRequest.getReturnRequestLineId());
                order.addLine(line);
            }
        }
        
        order.updateTotals();
        VendorReturnOrder saved = orderRepository.save(order);
        log.info("Return order created with ID: {} and Number: {}", saved.getId(), saved.getVroNumber());
        return mapToOrderResponseDTO(saved);
    }

    @Override
    public VendorReturnOrderResponseDTO updateReturnOrder(Long id, VendorReturnOrderDTO request) {
        log.info("Updating return order with ID: {}", id);
        
        VendorReturnOrder order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Return order not found with ID: " + id));
        
        if (!order.isEditable()) {
            throw new IllegalStateException("Cannot update order in status: " + order.getStatus());
        }
        
        order.setExpectedReturnDate(request.getExpectedReturnDate());
        order.setShippingAddress(request.getShippingAddress());
        order.setShippingMethod(request.getShippingMethod());
        order.setTrackingNumber(request.getTrackingNumber());
        order.setTrackingName(request.getTrackingName());
        
        // Update lines (clear and re-add)
        order.getLines().clear();
        if (request.getLines() != null) {
            for (VendorReturnOrderLineDTO lineRequest : request.getLines()) {
                VendorReturnOrderLine line = new VendorReturnOrderLine();
                line.setItemCode(lineRequest.getItemCode());
                line.setItemName(lineRequest.getItemName());
                line.setUom(lineRequest.getUom());
                line.setRejectedArea(lineRequest.getRejectedArea());
                line.setOrderQuantity(lineRequest.getOrderQuantity());
                line.setBatchNumber(lineRequest.getBatchNumber());
                line.setSerialNumbers(lineRequest.getSerialNumbers());
                line.setExpiryDate(lineRequest.getExpiryDate());
                line.setUnitPrice(lineRequest.getUnitPrice());
                line.setTotalAmount(lineRequest.getTotalAmount() != null ? 
                        lineRequest.getTotalAmount() : 
                        BigDecimal.valueOf(lineRequest.getOrderQuantity()).multiply(lineRequest.getUnitPrice()));
                line.setPickLocation(lineRequest.getPickLocation());
                line.setPickSequence(lineRequest.getPickSequence());
                line.setReturnRequestLineId(lineRequest.getReturnRequestLineId());
                order.addLine(line);
            }
        }
        order.updateTotals();
        
        VendorReturnOrder updated = orderRepository.save(order);
        log.info("Return order updated with ID: {}", updated.getId());
        return mapToOrderResponseDTO(updated);
    }

    @Override
    public VendorReturnOrderResponseDTO getReturnOrderById(Long id) {
        log.info("Fetching return order with ID: {}", id);
        VendorReturnOrder order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Return order not found with ID: " + id));
        return mapToOrderResponseDTO(order);
    }

    @Override
    public VendorReturnOrderResponseDTO getReturnOrderByNumber(String orderNumber) {
        log.info("Fetching return order with Number: {}", orderNumber);
        VendorReturnOrder order = orderRepository.findByVroNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Return order not found with Number: " + orderNumber));
        return mapToOrderResponseDTO(order);
    }

    @Override
    public Page<VendorReturnOrderResponseDTO> getAllReturnOrders(Pageable pageable) {
        log.info("Fetching all return orders");
        return orderRepository.findAll(pageable)
                .map(this::mapToOrderResponseDTO);
    }

    
    
    @Override
    public Page<VendorReturnOrderResponseDTO> getAllReturnOrdersWithFilters(ReturnOrderFilterDTO filter, Pageable pageable) {
        log.info("Fetching return orders with filters: {}", filter);
        
        if (filter == null) {
            return getAllReturnOrders(pageable);
        }
        
        // Check if only search term is provided
        if (hasOnlySearchTerm(filter)) {
            Page<VendorReturnOrder> orders = orderRepository.searchOrders(filter.getSearchTerm(), pageable);
            return orders.map(this::mapToOrderResponseDTO);
        }
        
        // Use full filters (without pickListStatus)
        Page<VendorReturnOrder> orders = orderRepository.findAllWithFiltersAndSearch(
                filter.getVroNumber(),
                filter.getSupplierName(),
                filter.getSupplierCode(),
                filter.getStatus(),
                filter.getPriority(),
                filter.getReturnType(),
                filter.getAssignTo(),
                filter.getPickListGenerated(),
                filter.getOrderFromDate(),
                filter.getOrderToDate(),
                filter.getExpectedFromDate(),
                filter.getExpectedToDate(),
                filter.getActualFromDate(),
                filter.getActualToDate(),
                filter.getMinQuantity(),
                filter.getMaxQuantity(),
                filter.getMinAmount(),
                filter.getMaxAmount(),
                filter.getSearchTerm(),
                pageable
        );
        
        return orders.map(this::mapToOrderResponseDTO);
    }

    /**
     * Check if filter only has search term (no other filters)
     */
    private boolean hasOnlySearchTerm(ReturnOrderFilterDTO filter) {
        return filter.getSearchTerm() != null && !filter.getSearchTerm().isEmpty()
                && filter.getVroNumber() == null
                && filter.getSupplierName() == null
                && filter.getSupplierCode() == null
                && filter.getStatus() == null
                && filter.getPriority() == null
                && filter.getReturnType() == null
                && filter.getAssignTo() == null
                && filter.getPickListGenerated() == null
                && filter.getOrderFromDate() == null
                && filter.getOrderToDate() == null
                && filter.getExpectedFromDate() == null
                && filter.getExpectedToDate() == null
                && filter.getActualFromDate() == null
                && filter.getActualToDate() == null
                && filter.getMinQuantity() == null
                && filter.getMaxQuantity() == null
                && filter.getMinAmount() == null
                && filter.getMaxAmount() == null;
    }
    
    
    
    
    @Override
    public Page<VendorReturnOrderResponseDTO> searchReturnOrders(String supplierName, String status, String searchTerm, Pageable pageable) {
        log.info("Searching return orders with filters");
        VendorReturnOrder.OrderStatus orderStatus = status != null ? 
                VendorReturnOrder.OrderStatus.valueOf(status) : null;
        return orderRepository.searchOrders(supplierName, orderStatus, searchTerm, pageable)
                .map(this::mapToOrderResponseDTO);
    }

    @Override
    public VendorReturnOrderResponseDTO generatePickList(Long id,String assignTo) {
        log.info("Generating pick list for order ID: {}", id);
        
        VendorReturnOrder order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Return order not found"));
        
        if (order.getStatus() != VendorReturnOrder.OrderStatus.CREATED) {
            throw new IllegalStateException("Only CREATED orders can generate pick list");
        }
        
        
        order.setAssignTo(assignTo);
        order.setStatus(VendorReturnOrder.OrderStatus.PENDING_PICKING);
        order.setPickListGenerated(true);
        order.setPickListGeneratedAt(LocalDateTime.now());
        
        // Assign pick locations and sequences
        int sequence = 1;
        for (VendorReturnOrderLine line : order.getLines()) {
            line.setPickSequence(sequence++);
            // In real implementation, get pick location from inventory
            line.setPickLocation("LOC-" + String.format("%03d", sequence));
        }
        
        VendorReturnOrder updated = orderRepository.save(order);
        log.info("Pick list generated for order: {}", updated.getVroNumber());
        return mapToOrderResponseDTO(updated);
    }

    @Override
    public VendorReturnOrderResponseDTO cancelReturnOrder(Long id) {
        log.info("Cancelling return order with ID: {}", id);
        
        VendorReturnOrder order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Return order not found"));
        
        if (order.getStatus() == VendorReturnOrder.OrderStatus.COMPLETED) {
            throw new IllegalStateException("Cannot cancel COMPLETED order");
        }
        
        order.setStatus(VendorReturnOrder.OrderStatus.CANCELLED);
        VendorReturnOrder updated = orderRepository.save(order);
        log.info("Return order cancelled: {}", updated.getVroNumber());
        return mapToOrderResponseDTO(updated);
    }

    @Override
    public void deleteReturnOrder(Long id) {
        log.info("Deleting return order with ID: {}", id);
        VendorReturnOrder order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Return order not found"));
        
        if (order.getStatus() != VendorReturnOrder.OrderStatus.CREATED && 
            order.getStatus() != VendorReturnOrder.OrderStatus.CANCELLED) {
            throw new IllegalStateException("Cannot delete order in status: " + order.getStatus());
        }
        
        orderRepository.delete(order);
        log.info("Return order deleted with ID: {}", id);
    }

    // ========== WAREHOUSE EXECUTION ==========

    @Override
    public VendorReturnOrderResponseDTO performPicking(Long orderId, List<PickingDTO> pickingDetails) {
        log.info("Performing picking for order ID: {}", orderId);
        
        if (pickingDetails == null || pickingDetails.isEmpty()) {
            throw new IllegalArgumentException("Picking details cannot be empty");
        }
        
        VendorReturnOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Return order not found"));
        
        if (!order.canPick()) {
            throw new IllegalStateException("Order is not in a state to perform picking. Current status: " + order.getStatus());
        }
        
        // Update picked quantities
        for (PickingDTO pick : pickingDetails) {
            VendorReturnOrderLine line = order.getLines().stream()
                    .filter(l -> l.getId().equals(pick.getLineId()))
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("Order line not found with ID: " + pick.getLineId()));
            
            // Validate pick quantity
            if (pick.getPickedQuantity() > line.getOrderQuantity()) {
                throw new IllegalArgumentException(
                    String.format("Picked quantity (%d) cannot exceed order quantity (%d) for item: %s", 
                        pick.getPickedQuantity(), line.getOrderQuantity(), line.getItemCode())
                );
            }
            
            line.setPickedQuantity(pick.getPickedQuantity());
            line.setStatus(VendorReturnOrderLine.LineStatus.PICKED);
        }
        
        order.setStatus(VendorReturnOrder.OrderStatus.PENDING_QC);
        order.setPickedBy(pickingDetails.get(0).getPickedBy());
        order.setPickedAt(LocalDateTime.now());
        
        VendorReturnOrder updated = orderRepository.save(order);
        log.info("Picking completed for order: {}", updated.getVroNumber());
        return mapToOrderResponseDTO(updated);
    }

    @Override
    public VendorReturnOrderResponseDTO performQC(Long orderId, List<QCDTO> qcDetails) {
        log.info("Performing QC for order ID: {}", orderId);
        
        if (qcDetails == null || qcDetails.isEmpty()) {
            throw new IllegalArgumentException("QC details cannot be empty");
        }
        
        VendorReturnOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Return order not found"));
        
        if (!order.canQC()) {
            throw new IllegalStateException("Order is not in a state to perform QC. Current status: " + order.getStatus());
        }
        
        boolean allPassed = true;
        List<String> failedItems = new ArrayList<>();
        
        for (QCDTO qc : qcDetails) {
            VendorReturnOrderLine line = order.getLines().stream()
                    .filter(l -> l.getId().equals(qc.getLineId()))
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("Order line not found with ID: " + qc.getLineId()));
            
            // Validate QC quantity
            if (qc.getQcQuantity() == null || qc.getQcQuantity() < 0) {
                throw new IllegalArgumentException("QC quantity cannot be null or negative");
            }
            
            if (qc.getQcQuantity() > line.getPickedQuantity()) {
                throw new IllegalArgumentException(
                    String.format("QC quantity (%d) cannot exceed picked quantity (%d) for item: %s", 
                        qc.getQcQuantity(), line.getPickedQuantity(), line.getItemCode())
                );
            }
            
            line.setQcQuantity(qc.getQcQuantity());
            line.setQcStatus(qc.getPassed() ? 
                    VendorReturnOrderLine.QCStatus.PASSED : 
                    VendorReturnOrderLine.QCStatus.FAILED);
            line.setQcRemarks(qc.getRemarks());
            
            if (qc.getPassed()) {
                line.setStatus(VendorReturnOrderLine.LineStatus.QC_PASSED);
            } else {
                line.setStatus(VendorReturnOrderLine.LineStatus.QC_FAILED);
                allPassed = false;
                failedItems.add(line.getItemCode());
            }
        }
        
        order.setQcVerifiedBy(qcDetails.get(0).getVerifiedBy());
        order.setQcVerifiedAt(LocalDateTime.now());
        
        if (allPassed) {
            order.setStatus(VendorReturnOrder.OrderStatus.QC_PASSED);
            // Auto move to packing
            order.setStatus(VendorReturnOrder.OrderStatus.PENDING_PACKING);
            log.info("All QC passed for order: {}. Moving to packing.", order.getVroNumber());
        } else {
            order.setStatus(VendorReturnOrder.OrderStatus.QC_FAILED);
            log.warn("QC failed for order: {}. Failed items: {}", order.getVroNumber(), String.join(", ", failedItems));
        }
        
        VendorReturnOrder updated = orderRepository.save(order);
        log.info("QC completed for order: {} with status: {}", updated.getVroNumber(), updated.getStatus());
        return mapToOrderResponseDTO(updated);
    }

    @Override
    public VendorReturnOrderResponseDTO performPacking(Long orderId, List<PackingDTO> packingDetails) {
        log.info("Performing packing for order ID: {}", orderId);
        
        if (packingDetails == null || packingDetails.isEmpty()) {
            throw new IllegalArgumentException("Packing details cannot be empty");
        }
        
        VendorReturnOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Return order not found"));
        
        if (!order.canPack()) {
            throw new IllegalStateException("Order is not in a state to perform packing. Current status: " + order.getStatus());
        }
        
        for (PackingDTO pack : packingDetails) {
            VendorReturnOrderLine line = order.getLines().stream()
                    .filter(l -> l.getId().equals(pack.getLineId()))
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("Order line not found with ID: " + pack.getLineId()));
            
            // Validate pack quantity
            if (pack.getPackedQuantity() > line.getQcQuantity()) {
                throw new IllegalArgumentException(
                    String.format("Packed quantity (%d) cannot exceed QC quantity (%d) for item: %s", 
                        pack.getPackedQuantity(), line.getQcQuantity(), line.getItemCode())
                );
            }
            
            line.setPackedQuantity(pack.getPackedQuantity());
            line.setPackBarcode(pack.getPackBarcode());
            line.setStatus(VendorReturnOrderLine.LineStatus.PACKED);
        }
        
        order.setPackedBy(packingDetails.get(0).getPackedBy());
        order.setPackedAt(LocalDateTime.now());
        order.setStatus(VendorReturnOrder.OrderStatus.PACKED);
        
        VendorReturnOrder updated = orderRepository.save(order);
        log.info("Packing completed for order: {}", updated.getVroNumber());
        return mapToOrderResponseDTO(updated);
    }

    // ========== DISPATCH OPERATIONS ==========

    @Override
    public DispatchResponseDTO createDispatch(DispatchDTO dispatchDTO) {
        log.info("Creating dispatch for order ID: {}", dispatchDTO.getReturnOrderId());
        
        VendorReturnOrder order = orderRepository.findById(dispatchDTO.getReturnOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Return order not found"));
        
        if (!order.canDispatch()) {
            throw new IllegalStateException("Order is not ready for dispatch. Current status: " + order.getStatus());
        }
        
        ReturnDispatch dispatch = new ReturnDispatch();
        dispatch.setDispatchNumber(generateDispatchNumber());
        dispatch.setDispatchDate(dispatchDTO.getDispatchDate() != null ? dispatchDTO.getDispatchDate() : LocalDate.now());
        dispatch.setDispatchTime(dispatchDTO.getDispatchTime() != null ? dispatchDTO.getDispatchTime() : LocalTime.now());
        dispatch.setReturnOrder(order);
        dispatch.setTransportMode(dispatchDTO.getTransportMode());
        dispatch.setTransporterName(dispatchDTO.getTransporterName());
        dispatch.setTransportCompany(dispatchDTO.getTransportCompany());
        dispatch.setVehicleNumber(dispatchDTO.getVehicleNumber());
        dispatch.setDriverName(dispatchDTO.getDriverName());
        dispatch.setDriverPhone(dispatchDTO.getDriverPhone());
        dispatch.setLrNumber(dispatchDTO.getLrNumber());
        dispatch.setAwbNumber(dispatchDTO.getAwbNumber());
        dispatch.setTrackingUrl(dispatchDTO.getTrackingUrl());
        dispatch.setReturnChallanNumber(dispatchDTO.getReturnChallanNumber());
        dispatch.setReturnChallanDate(dispatchDTO.getReturnChallanDate());
        dispatch.setTotalWeight(dispatchDTO.getTotalWeight());
        dispatch.setTotalVolume(dispatchDTO.getTotalVolume());
        dispatch.setStatus(ReturnDispatch.DispatchStatus.CREATED);
        
        // Add dispatch items
        if (dispatchDTO.getItems() != null) {
            int totalItems = 0;
            for (DispatchItemDTO itemDTO : dispatchDTO.getItems()) {
                ReturnDispatchItem item = new ReturnDispatchItem();
                item.setItemCode(itemDTO.getItemCode());
                item.setItemName(itemDTO.getItemName());
                item.setDispatchedQuantity(itemDTO.getDispatchedQuantity());
                item.setPackedQuantity(itemDTO.getPackedQuantity());
                item.setPackagingType(itemDTO.getPackagingType());
                item.setPackageCount(itemDTO.getPackageCount());
                item.setPackageWeight(itemDTO.getPackageWeight());
                item.setVroLineId(itemDTO.getVroLineId());
                dispatch.addItem(item);
                totalItems += itemDTO.getDispatchedQuantity();
            }
            dispatch.setTotalItems(totalItems);
        }
        
        // Update order
        order.setStatus(VendorReturnOrder.OrderStatus.DISPATCHED);
        order.setDispatchedAt(LocalDateTime.now());
        order.setDispatchNumber(dispatch.getDispatchNumber());
        
        ReturnDispatch saved = dispatchRepository.save(dispatch);
        orderRepository.save(order);
        
        log.info("Dispatch created with ID: {} and Number: {}", saved.getId(), saved.getDispatchNumber());
        return mapToDispatchResponseDTO(saved);
    }

    @Override
    public DispatchResponseDTO getDispatchById(Long id) {
        log.info("Fetching dispatch with ID: {}", id);
        ReturnDispatch dispatch = dispatchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dispatch not found with ID: " + id));
        return mapToDispatchResponseDTO(dispatch);
    }

    @Override
    public DispatchResponseDTO confirmDispatch(Long id) {
        log.info("Confirming dispatch with ID: {}", id);
        
        ReturnDispatch dispatch = dispatchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dispatch not found"));
        
        dispatch.setStatus(ReturnDispatch.DispatchStatus.IN_TRANSIT);
        
        VendorReturnOrder order = dispatch.getReturnOrder();
        order.setStatus(VendorReturnOrder.OrderStatus.IN_TRANSIT);
        order.setTrackingNumber(dispatch.getLrNumber() != null ? dispatch.getLrNumber() : dispatch.getAwbNumber());
        order.setTrackingName(dispatch.getTransporterName());
        
        ReturnDispatch updated = dispatchRepository.save(dispatch);
        orderRepository.save(order);
        
        log.info("Dispatch confirmed: {}", updated.getDispatchNumber());
        return mapToDispatchResponseDTO(updated);
    }

    @Override
    public DispatchResponseDTO uploadPOD(Long id, String podNumber, LocalDate podDate, String podDocumentPath) {
        log.info("Uploading POD for dispatch ID: {}", id);
        
        ReturnDispatch dispatch = dispatchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dispatch not found"));
        
        dispatch.setPodNumber(podNumber);
        dispatch.setPodDate(podDate);
        dispatch.setPodReceived(true);
        dispatch.setPodDocumentPath(podDocumentPath);
        dispatch.setStatus(ReturnDispatch.DispatchStatus.DELIVERED);
        
        ReturnDispatch updated = dispatchRepository.save(dispatch);
        log.info("POD uploaded for dispatch: {}", updated.getDispatchNumber());
        return mapToDispatchResponseDTO(updated);
    }

    @Override
    public List<DispatchResponseDTO> getDispatchesByOrder(Long orderId) {
        log.info("Fetching dispatches for order ID: {}", orderId);
        return dispatchRepository.findByReturnOrderId(orderId)
                .stream()
                .map(this::mapToDispatchResponseDTO)
                .collect(Collectors.toList());
    }

    // ========== RECEIPT OPERATIONS ==========

    @Override
    public VendorReceiptResponseDTO createReceipt(VendorReceiptDTO receiptDTO) {
        log.info("Creating receipt for order ID: {}", receiptDTO.getReturnOrderId());
        
        VendorReturnOrder order = orderRepository.findById(receiptDTO.getReturnOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Return order not found"));
        
        VendorReceipt receipt = new VendorReceipt();
        receipt.setReceiptNumber(generateReceiptNumber());
        receipt.setReceiptDate(receiptDTO.getReceiptDate() != null ? receiptDTO.getReceiptDate() : LocalDate.now());
        receipt.setReturnOrder(order);
        receipt.setSupplier(order.getSupplier());
        receipt.setSupplierName(order.getSupplierName());
        receipt.setReceivedBy(receiptDTO.getReceivedBy());
        receipt.setStatus(VendorReceipt.ReceiptStatus.PENDING);
        
        // Add receipt lines
        if (receiptDTO.getLines() != null) {
            for (VendorReceiptLineDTO lineDTO : receiptDTO.getLines()) {
                VendorReceiptLine line = new VendorReceiptLine();
                line.setItemCode(lineDTO.getItemCode());
                line.setItemName(lineDTO.getItemName());
                line.setDispatchedQuantity(lineDTO.getDispatchedQuantity());
                line.setReceivedQuantity(lineDTO.getReceivedQuantity());
                line.setAcceptedQuantity(lineDTO.getAcceptedQuantity() != null ? 
                        lineDTO.getAcceptedQuantity() : lineDTO.getReceivedQuantity());
                line.setRejectedQuantity(lineDTO.getRejectedQuantity() != null ? 
                        lineDTO.getRejectedQuantity() : 0);
                line.setShortQuantity(lineDTO.getShortQuantity() != null ? 
                        lineDTO.getShortQuantity() : 0);
                line.setDamagedQuantity(lineDTO.getDamagedQuantity() != null ? 
                        lineDTO.getDamagedQuantity() : 0);
                line.setRejectionReason(lineDTO.getRejectionReason());
                line.setDamagedRemarks(lineDTO.getDamagedRemarks());
                line.setVroLineId(lineDTO.getVroLineId());
                
                // Determine line status
                if (line.getAcceptedQuantity() == 0 && line.getRejectedQuantity() > 0) {
                    line.setStatus(VendorReceiptLine.LineReceiptStatus.FULLY_REJECTED);
                } else if (line.getAcceptedQuantity() > 0 && line.getRejectedQuantity() > 0) {
                    line.setStatus(VendorReceiptLine.LineReceiptStatus.PARTIALLY_ACCEPTED);
                } else {
                    line.setStatus(VendorReceiptLine.LineReceiptStatus.FULLY_ACCEPTED);
                }
                
                receipt.addLine(line);
            }
        }
        
        receipt.updateTotals();
        
        // Update order status
        order.setStatus(VendorReturnOrder.OrderStatus.RECEIVED);
        
        VendorReceipt saved = receiptRepository.save(receipt);
        orderRepository.save(order);
        
        log.info("Receipt created with ID: {} and Number: {}", saved.getId(), saved.getReceiptNumber());
        return mapToReceiptResponseDTO(saved);
    }

    @Override
    public VendorReceiptResponseDTO getReceiptById(Long id) {
        log.info("Fetching receipt with ID: {}", id);
        VendorReceipt receipt = receiptRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Receipt not found with ID: " + id));
        return mapToReceiptResponseDTO(receipt);
    }

    @Override
    public VendorReceiptResponseDTO acknowledgeReceipt(Long id, String acknowledgmentNumber) {
        log.info("Acknowledging receipt with ID: {}", id);
        
        VendorReceipt receipt = receiptRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Receipt not found"));
        
        receipt.setAcknowledgmentNumber(acknowledgmentNumber);
        receipt.setAcknowledgmentDate(LocalDate.now());
        
        // Check if all lines are fully accepted or rejected
        boolean allLinesProcessed = receipt.getLines().stream()
                .allMatch(line -> line.getStatus() != VendorReceiptLine.LineReceiptStatus.PARTIALLY_ACCEPTED);
        
        if (allLinesProcessed) {
            receipt.setStatus(VendorReceipt.ReceiptStatus.COMPLETED);
        } else {
            receipt.setStatus(VendorReceipt.ReceiptStatus.PARTIAL);
        }
        
        VendorReceipt updated = receiptRepository.save(receipt);
        log.info("Receipt acknowledged: {}", updated.getReceiptNumber());
        return mapToReceiptResponseDTO(updated);
    }

    @Override
    public List<VendorReceiptResponseDTO> getReceiptsByOrder(Long orderId) {
        log.info("Fetching receipts for order ID: {}", orderId);
        return receiptRepository.findByReturnOrderId(orderId)
                .stream()
                .map(this::mapToReceiptResponseDTO)
                .collect(Collectors.toList());
    }

    // ========== SETTLEMENT OPERATIONS ==========

    @Override
    public SettlementResponseDTO createSettlement(SettlementDTO settlementDTO) {
        log.info("Creating settlement for order ID: {}", settlementDTO.getReturnOrderId());
        
        VendorReturnOrder order = orderRepository.findById(settlementDTO.getReturnOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Return order not found"));
        
        ReturnSettlement settlement = new ReturnSettlement();
        settlement.setSettlementNumber(generateSettlementNumber());
        settlement.setSettlementType(settlementDTO.getSettlementType());
        settlement.setSettlementDate(settlementDTO.getSettlementDate() != null ? 
                settlementDTO.getSettlementDate() : LocalDate.now());
        settlement.setReturnOrder(order);
        settlement.setSettlementAmount(settlementDTO.getSettlementAmount());
        settlement.setStatus(ReturnSettlement.SettlementStatus.PENDING);
        settlement.setRemarks(settlementDTO.getRemarks());
        
        // Set specific settlement type details
        switch (settlementDTO.getSettlementType()) {
            case CREDIT_NOTE:
                settlement.setCreditNoteNumber(settlementDTO.getCreditNoteNumber());
                settlement.setCreditNoteDate(settlementDTO.getCreditNoteDate());
                settlement.setCreditNoteAmount(settlementDTO.getCreditNoteAmount());
                break;
            case REPLACEMENT:
                settlement.setReplacementOrderId(settlementDTO.getReplacementOrderId());
                settlement.setReplacementOrderNumber(settlementDTO.getReplacementOrderNumber());
                settlement.setReplacementQuantity(settlementDTO.getReplacementQuantity());
                break;
            case REFUND:
                settlement.setRefundReference(settlementDTO.getRefundReference());
                settlement.setRefundDate(settlementDTO.getRefundDate());
                settlement.setRefundAmount(settlementDTO.getRefundAmount());
                settlement.setRefundStatus(ReturnSettlement.RefundStatus.PENDING);
                break;
        }
        
        ReturnSettlement saved = settlementRepository.save(settlement);
        log.info("Settlement created with ID: {} and Number: {}", saved.getId(), saved.getSettlementNumber());
        return mapToSettlementResponseDTO(saved);
    }

    @Override
    public SettlementResponseDTO getSettlementById(Long id) {
        log.info("Fetching settlement with ID: {}", id);
        ReturnSettlement settlement = settlementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Settlement not found with ID: " + id));
        return mapToSettlementResponseDTO(settlement);
    }

    @Override
    public SettlementResponseDTO processSettlement(Long id) {
        log.info("Processing settlement with ID: {}", id);
        
        ReturnSettlement settlement = settlementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Settlement not found"));
        
        settlement.setStatus(ReturnSettlement.SettlementStatus.PROCESSING);
        
        // Handle different settlement types
        switch (settlement.getSettlementType()) {
            case REFUND:
                if (settlement.getRefundStatus() == ReturnSettlement.RefundStatus.PENDING) {
                    // Process refund logic here
                    settlement.setRefundStatus(ReturnSettlement.RefundStatus.PROCESSING);
                }
                break;
            case REPLACEMENT:
                // Process replacement logic here
                break;
            case CREDIT_NOTE:
                // Process credit note logic here
                break;
        }
        
        ReturnSettlement updated = settlementRepository.save(settlement);
        log.info("Settlement processing started: {}", updated.getSettlementNumber());
        return mapToSettlementResponseDTO(updated);
    }

    @Override
    public SettlementResponseDTO completeSettlement(Long id) {
        log.info("Completing settlement with ID: {}", id);
        
        ReturnSettlement settlement = settlementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Settlement not found"));
        
        settlement.setStatus(ReturnSettlement.SettlementStatus.COMPLETED);
        
        // Handle different settlement types completion
        switch (settlement.getSettlementType()) {
            case REFUND:
                settlement.setRefundStatus(ReturnSettlement.RefundStatus.COMPLETED);
                break;
            case REPLACEMENT:
                // Complete replacement logic here
                break;
            case CREDIT_NOTE:
                // Complete credit note logic here
                break;
        }
        
        // Update order status to completed
        VendorReturnOrder order = settlement.getReturnOrder();
        if (order != null) {
            order.setStatus(VendorReturnOrder.OrderStatus.COMPLETED);
            order.setActualReturnDate(LocalDate.now());
            orderRepository.save(order);
        }
        
        ReturnSettlement updated = settlementRepository.save(settlement);
        log.info("Settlement completed: {}", updated.getSettlementNumber());
        return mapToSettlementResponseDTO(updated);
    }

    @Override
    public List<SettlementResponseDTO> getSettlementsByOrder(Long orderId) {
        log.info("Fetching settlements for order ID: {}", orderId);
        return settlementRepository.findByReturnOrderId(orderId)
                .stream()
                .map(this::mapToSettlementResponseDTO)
                .collect(Collectors.toList());
    }

    // ========== STATISTICS ==========

    @Override
    public long getCountByStatus(String status) {
        log.info("Getting count by order status: {}", status);
        VendorReturnOrder.OrderStatus orderStatus = VendorReturnOrder.OrderStatus.valueOf(status);
        return orderRepository.countByStatus(orderStatus);
    }

    @Override
    public long getCountByRequestStatus(String status) {
        log.info("Getting count by request status: {}", status);
        VendorReturnRequest.RequestStatus requestStatus = VendorReturnRequest.RequestStatus.valueOf(status);
        return requestRepository.countByStatus(requestStatus);
    }

    @Override
    public Map<String, Long> getStatusCounts() {
        log.info("Getting order status counts");
        Map<String, Long> counts = new HashMap<>();
        for (VendorReturnOrder.OrderStatus status : VendorReturnOrder.OrderStatus.values()) {
            Long count = orderRepository.countByStatus(status);
            counts.put(status.name(), count != null ? count : 0L);
        }
        return counts;
    }

    @Override
    public Map<String, Long> getRequestStatusCounts() {
        log.info("Getting request status counts");
        Map<String, Long> counts = new HashMap<>();
        for (VendorReturnRequest.RequestStatus status : VendorReturnRequest.RequestStatus.values()) {
            Long count = requestRepository.countByStatus(status);
            counts.put(status.name(), count != null ? count : 0L);
        }
        return counts;
    }

    // ========== PRIVATE HELPER METHODS ==========

    private VendorReturnOrder createReturnOrderFromRequest(VendorReturnRequest request) {
        VendorReturnOrder order = new VendorReturnOrder();
        order.setVroNumber(generateOrderNumber());
        order.setOrderDate(LocalDate.now());
        order.setReturnRequest(request);
        order.setSupplier(request.getSupplier());
        order.setSupplierName(request.getSupplierName());
        order.setSupplierCode(request.getSupplierCode());
        order.setRockArea(request.getRockArea());
        order.setReturnType(request.getReturnType());
        order.setReturnReason(request.getReturnReason());
        order.setPriority(request.getPriority());
        order.setStatus(VendorReturnOrder.OrderStatus.CREATED);
        
        // Copy lines
        for (VendorReturnRequestLine requestLine : request.getLines()) {
            VendorReturnOrderLine orderLine = new VendorReturnOrderLine();
            orderLine.setItemCode(requestLine.getItemCode());
            orderLine.setItemName(requestLine.getItemName());
            orderLine.setUom(requestLine.getUom());
            orderLine.setOrderQuantity(requestLine.getRequestedQuantity());
            orderLine.setBatchNumber(requestLine.getBatchNumber());
            orderLine.setSerialNumbers(requestLine.getSerialNumbers());
            orderLine.setExpiryDate(requestLine.getExpiryDate());
            orderLine.setUnitPrice(requestLine.getUnitPrice());
            orderLine.setTotalAmount(requestLine.getTotalAmount());
            orderLine.setReturnRequestLineId(requestLine.getId());
            order.addLine(orderLine);
        }
        
        order.updateTotals();
        return order;
    }

    private String generateRequestNumber() {
        Long count = requestRepository.countByReturnRequestNumberStartingWith(REQUEST_PREFIX);
        return REQUEST_PREFIX + String.format("%08d", count + 1);
    }

    private String generateOrderNumber() {
        Long count = orderRepository.countByVroNumberStartingWith(ORDER_PREFIX);
        return ORDER_PREFIX + String.format("%08d", count + 1);
    }

    private String generateDispatchNumber() {
        Long count = dispatchRepository.countByDispatchNumberStartingWith(DISPATCH_PREFIX);
        return DISPATCH_PREFIX + String.format("%08d", count + 1);
    }

    private String generateReceiptNumber() {
        Long count = receiptRepository.countByReceiptNumberStartingWith(RECEIPT_PREFIX);
        return RECEIPT_PREFIX + String.format("%08d", count + 1);
    }

    private String generateSettlementNumber() {
        Long count = settlementRepository.countBySettlementNumberStartingWith(SETTLEMENT_PREFIX);
        return SETTLEMENT_PREFIX + String.format("%08d", count + 1);
    }

    // ========== MAPPING METHODS ==========

    /**
     * Map VendorReturnRequest to VendorReturnResponseDTO
     */
    private VendorReturnResponseDTO mapToResponseDTO(VendorReturnRequest request) {
        if (request == null) {
            return null;
        }
        
        return VendorReturnResponseDTO.builder()
                .id(request.getId())
                .returnRequestNumber(request.getReturnRequestNumber())
                .requestDate(request.getRequestDate())
                .poNumber(request.getPoNumber())
                .grnNumber(request.getGrnNumber())
                .invoiceNumber(request.getInvoiceNumber())
                .supplierName(request.getSupplierName())
                .supplierCode(request.getSupplierCode())
                .supplierId(request.getSupplier() != null ? request.getSupplier().getId() : null)
                .returnType(request.getReturnType())
                .returnReason(request.getReturnReason())
                .priority(request.getPriority())
                .status(request.getStatus())
                .approvedBy(request.getApprovedBy())
                .approvedDate(request.getApprovedDate())
                .rockArea(request.getRockArea())
                .rejectedBy(request.getRejectedBy())
                .rejectedDate(request.getRejectedDate())
                .rejectionReason(request.getRejectionReason())
                .remarks(request.getRemarks())
                .createdAt(request.getCreatedAt())
                .createdBy(request.getCreatedBy())
                .updatedAt(request.getUpdatedAt())
                .updatedBy(request.getUpdatedBy())
                .lines(request.getLines() != null ? request.getLines().stream()
                        .map(this::mapLineToResponseDTO)
                        .collect(Collectors.toList()) : null)
                .build();
    }

    /**
     * Map VendorReturnRequestLine to VendorReturnLineResponseDTO
     */
    private VendorReturnLineResponseDTO mapLineToResponseDTO(VendorReturnRequestLine line) {
        if (line == null) {
            return null;
        }
        
        return VendorReturnLineResponseDTO.builder()
                .id(line.getId())
                .itemCode(line.getItemCode())
                .itemName(line.getItemName())
                .uom(line.getUom())
                .requestedQuantity(line.getRequestedQuantity())
                .approvedQuantity(line.getApprovedQuantity())
                .actualReturnedQuantity(line.getActualReturnedQuantity())
                .originalQuantity(line.getOriginalQuantity())
                .receivedQuantity(line.getReceivedQuantity())
                .batchNumber(line.getBatchNumber())
                .serialNumbers(line.getSerialNumbers())
                .expiryDate(line.getExpiryDate())
                .unitPrice(line.getUnitPrice())
                .totalAmount(line.getTotalAmount())
                .reason(line.getReason())
                .remarks(line.getRemarks())
                .inboundLineId(line.getInboundLineId())
                .createdAt(line.getCreatedAt())
                .updatedAt(line.getUpdatedAt())
                .build();
    }

    /**
     * Map VendorReturnOrder to VendorReturnOrderResponseDTO
     */
    private VendorReturnOrderResponseDTO mapToOrderResponseDTO(VendorReturnOrder order) {
        if (order == null) {
            return null;
        }
        
        return VendorReturnOrderResponseDTO.builder()
                .id(order.getId())
                .vroNumber(order.getVroNumber())
                .orderDate(order.getOrderDate())
                .expectedReturnDate(order.getExpectedReturnDate())
                .actualReturnDate(order.getActualReturnDate())
                .returnRequestId(order.getReturnRequest() != null ? order.getReturnRequest().getId() : null)
                .returnRequestNumber(order.getReturnRequest() != null ? order.getReturnRequest().getReturnRequestNumber() : null)
                .supplierId(order.getSupplier() != null ? order.getSupplier().getId() : null)
                .supplierName(order.getSupplierName())
                .supplierCode(order.getSupplierCode())
                .rockArea(order.getRockArea())
                .returnType(order.getReturnType())
                .returnReason(order.getReturnReason())
                .priority(order.getPriority())
                .status(order.getStatus())
                .shippingAddress(order.getShippingAddress())
                .shippingMethod(order.getShippingMethod())
                .trackingNumber(order.getTrackingNumber())
                .trackingName(order.getTrackingName())
                .pickListGenerated(order.getPickListGenerated())
                .pickListGeneratedAt(order.getPickListGeneratedAt())
                .pickedBy(order.getPickedBy())
                .pickedAt(order.getPickedAt())
                .qcVerifiedBy(order.getQcVerifiedBy())
                .qcVerifiedAt(order.getQcVerifiedAt())
                .packedBy(order.getPackedBy())
                .packedAt(order.getPackedAt())
                .dispatchedBy(order.getDispatchedBy())
                .dispatchedAt(order.getDispatchedAt())
                .dispatchNumber(order.getDispatchNumber())
                .totalQuantity(order.getTotalQuantity())
                .totalAmount(order.getTotalAmount())
                .createdAt(order.getCreatedAt())
                .createdBy(order.getCreatedBy())
                .assignTo(order.getAssignTo())
                .updatedAt(order.getUpdatedAt())
                .updatedBy(order.getUpdatedBy())
                .lines(order.getLines() != null ? order.getLines().stream()
                        .map(this::mapOrderLineToResponseDTO)
                        .collect(Collectors.toList()) : null)
                .build();
    }

    /**
     * Map VendorReturnOrderLine to VendorReturnOrderLineResponseDTO
     */
    private VendorReturnOrderLineResponseDTO mapOrderLineToResponseDTO(VendorReturnOrderLine line) {
        if (line == null) {
            return null;
        }
        
        return VendorReturnOrderLineResponseDTO.builder()
                .id(line.getId())
                .returnRequestLineId(line.getReturnRequestLineId())
                .itemCode(line.getItemCode())
                .itemName(line.getItemName())
                .uom(line.getUom())
                .orderQuantity(line.getOrderQuantity())
                .pickedQuantity(line.getPickedQuantity())
                .qcQuantity(line.getQcQuantity())
                .packedQuantity(line.getPackedQuantity())
                .dispatchedQuantity(line.getDispatchedQuantity())
                .receivedQuantity(line.getReceivedQuantity())
                .batchNumber(line.getBatchNumber())
                .serialNumbers(line.getSerialNumbers())
                .expiryDate(line.getExpiryDate())
                .unitPrice(line.getUnitPrice())
                .totalAmount(line.getTotalAmount())
                .pickLocation(line.getPickLocation())
                .pickSequence(line.getPickSequence())
                .packBarcode(line.getPackBarcode())
                .qcStatus(line.getQcStatus())
                .qcRemarks(line.getQcRemarks())
                .status(line.getStatus())
                .createdAt(line.getCreatedAt())
                .updatedAt(line.getUpdatedAt())
                .build();
    }

    /**
     * Map ReturnDispatch to DispatchResponseDTO
     */
    private DispatchResponseDTO mapToDispatchResponseDTO(ReturnDispatch dispatch) {
        if (dispatch == null) {
            return null;
        }
        
        return DispatchResponseDTO.builder()
                .id(dispatch.getId())
                .dispatchNumber(dispatch.getDispatchNumber())
                .dispatchDate(dispatch.getDispatchDate())
                .dispatchTime(dispatch.getDispatchTime())
                .returnOrderId(dispatch.getReturnOrder() != null ? dispatch.getReturnOrder().getId() : null)
                .returnOrderNumber(dispatch.getReturnOrder() != null ? dispatch.getReturnOrder().getVroNumber() : null)
                .supplierName(dispatch.getReturnOrder() != null ? dispatch.getReturnOrder().getSupplierName() : null)
                .transportMode(dispatch.getTransportMode())
                .transportModeDisplayName(dispatch.getTransportMode() != null ? dispatch.getTransportMode().getDisplayName() : null)
                .transporterName(dispatch.getTransporterName())
                .transportCompany(dispatch.getTransportCompany())
                .vehicleNumber(dispatch.getVehicleNumber())
                .driverName(dispatch.getDriverName())
                .driverPhone(dispatch.getDriverPhone())
                .lrNumber(dispatch.getLrNumber())
                .awbNumber(dispatch.getAwbNumber())
                .trackingUrl(dispatch.getTrackingUrl())
                .returnChallanNumber(dispatch.getReturnChallanNumber())
                .returnChallanDate(dispatch.getReturnChallanDate())
                .podNumber(dispatch.getPodNumber())
                .podDate(dispatch.getPodDate())
                .podReceived(dispatch.getPodReceived())
                .podDocumentPath(dispatch.getPodDocumentPath())
                .status(dispatch.getStatus())
                .statusDisplayName(dispatch.getStatus() != null ? dispatch.getStatus().getDisplayName() : null)
                .totalItems(dispatch.getTotalItems())
                .totalWeight(dispatch.getTotalWeight())
                .totalVolume(dispatch.getTotalVolume())
                .createdAt(dispatch.getCreatedAt())
                .createdBy(dispatch.getCreatedBy())
                .updatedAt(dispatch.getUpdatedAt())
                .items(dispatch.getItems() != null ? dispatch.getItems().stream()
                        .map(this::mapDispatchItemToResponseDTO)
                        .collect(Collectors.toList()) : null)
                .build();
    }

    /**
     * Map ReturnDispatchItem to DispatchItemResponseDTO
     */
    private DispatchItemResponseDTO mapDispatchItemToResponseDTO(ReturnDispatchItem item) {
        if (item == null) {
            return null;
        }
        
        return DispatchItemResponseDTO.builder()
                .id(item.getId())
                .vroLineId(item.getVroLineId())
                .itemCode(item.getItemCode())
                .itemName(item.getItemName())
                .dispatchedQuantity(item.getDispatchedQuantity())
                .packedQuantity(item.getPackedQuantity())
                .packagingType(item.getPackagingType())
                .packageCount(item.getPackageCount())
                .packageWeight(item.getPackageWeight())
                .createdAt(item.getCreatedAt())
                .build();
    }

    /**
     * Map VendorReceipt to VendorReceiptResponseDTO
     */
    private VendorReceiptResponseDTO mapToReceiptResponseDTO(VendorReceipt receipt) {
        if (receipt == null) {
            return null;
        }
        
        return VendorReceiptResponseDTO.builder()
                .id(receipt.getId())
                .receiptNumber(receipt.getReceiptNumber())
                .receiptDate(receipt.getReceiptDate())
                .returnOrderId(receipt.getReturnOrder() != null ? receipt.getReturnOrder().getId() : null)
                .returnOrderNumber(receipt.getReturnOrder() != null ? receipt.getReturnOrder().getVroNumber() : null)
                .dispatchId(receipt.getDispatch() != null ? receipt.getDispatch().getId() : null)
                .dispatchNumber(receipt.getDispatch() != null ? receipt.getDispatch().getDispatchNumber() : null)
                .supplierId(receipt.getSupplier() != null ? receipt.getSupplier().getId() : null)
                .supplierName(receipt.getSupplierName())
                .receivedBy(receipt.getReceivedBy())
                .totalReceivedQuantity(receipt.getTotalReceivedQuantity())
                .totalAcceptedQuantity(receipt.getTotalAcceptedQuantity())
                .totalRejectedQuantity(receipt.getTotalRejectedQuantity())
                .totalShortQuantity(receipt.getTotalShortQuantity())
                .totalDamagedQuantity(receipt.getTotalDamagedQuantity())
                .status(receipt.getStatus())
                .statusDisplayName(receipt.getStatus() != null ? receipt.getStatus().getDisplayName() : null)
                .receiptDocumentPath(receipt.getReceiptDocumentPath())
                .acknowledgmentNumber(receipt.getAcknowledgmentNumber())
                .acknowledgmentDate(receipt.getAcknowledgmentDate())
                .createdAt(receipt.getCreatedAt())
                .updatedAt(receipt.getUpdatedAt())
                .lines(receipt.getLines() != null ? receipt.getLines().stream()
                        .map(this::mapReceiptLineToResponseDTO)
                        .collect(Collectors.toList()) : null)
                .build();
    }

    /**
     * Map VendorReceiptLine to VendorReceiptLineResponseDTO
     */
    private VendorReceiptLineResponseDTO mapReceiptLineToResponseDTO(VendorReceiptLine line) {
        if (line == null) {
            return null;
        }
        
        return VendorReceiptLineResponseDTO.builder()
                .id(line.getId())
                .vroLineId(line.getVroLineId())
                .itemCode(line.getItemCode())
                .itemName(line.getItemName())
                .dispatchedQuantity(line.getDispatchedQuantity())
                .receivedQuantity(line.getReceivedQuantity())
                .acceptedQuantity(line.getAcceptedQuantity())
                .rejectedQuantity(line.getRejectedQuantity())
                .shortQuantity(line.getShortQuantity())
                .damagedQuantity(line.getDamagedQuantity())
                .rejectionReason(line.getRejectionReason())
                .damagedRemarks(line.getDamagedRemarks())
                .status(line.getStatus())
                .statusDisplayName(line.getStatus() != null ? line.getStatus().getDisplayName() : null)
                .createdAt(line.getCreatedAt())
                .updatedAt(line.getUpdatedAt())
                .build();
    }

    /**
     * Map ReturnSettlement to SettlementResponseDTO
     */
    private SettlementResponseDTO mapToSettlementResponseDTO(ReturnSettlement settlement) {
        if (settlement == null) {
            return null;
        }
        
        return SettlementResponseDTO.builder()
                .id(settlement.getId())
                .settlementNumber(settlement.getSettlementNumber())
                .settlementType(settlement.getSettlementType())
                .settlementTypeDisplayName(settlement.getSettlementType() != null ? 
                        settlement.getSettlementType().getDisplayName() : null)
                .settlementDate(settlement.getSettlementDate())
                .returnOrderId(settlement.getReturnOrder() != null ? settlement.getReturnOrder().getId() : null)
                .returnOrderNumber(settlement.getReturnOrder() != null ? settlement.getReturnOrder().getVroNumber() : null)
                .receiptId(settlement.getReceipt() != null ? settlement.getReceipt().getId() : null)
                .receiptNumber(settlement.getReceipt() != null ? settlement.getReceipt().getReceiptNumber() : null)
                .settlementAmount(settlement.getSettlementAmount())
                .creditNoteNumber(settlement.getCreditNoteNumber())
                .creditNoteDate(settlement.getCreditNoteDate())
                .creditNoteAmount(settlement.getCreditNoteAmount())
                .replacementOrderId(settlement.getReplacementOrderId())
                .replacementOrderNumber(settlement.getReplacementOrderNumber())
                .replacementQuantity(settlement.getReplacementQuantity())
                .refundReference(settlement.getRefundReference())
                .refundDate(settlement.getRefundDate())
                .refundAmount(settlement.getRefundAmount())
                .refundStatus(settlement.getRefundStatus())
                .refundStatusDisplayName(settlement.getRefundStatus() != null ? 
                        settlement.getRefundStatus().getDisplayName() : null)
                .status(settlement.getStatus())
                .statusDisplayName(settlement.getStatus() != null ? 
                        settlement.getStatus().getDisplayName() : null)
                .remarks(settlement.getRemarks())
                .createdAt(settlement.getCreatedAt())
                .createdBy(settlement.getCreatedBy())
                .updatedAt(settlement.getUpdatedAt())
                .updatedBy(settlement.getUpdatedBy())
                .build();
    }
    
    
    
    
    @Override
    public Page<PickListResponseDTO> searchPickLists(PickListFilterDTO filter, Pageable pageable) {
        log.info("Searching pick lists with filters: {}", filter);
        
        if (filter == null) {
            return getAllPickLists(pageable);
        }
        
        // ✅ FIXED: Extract values from DTO and pass as individual parameters
        Page<VendorReturnOrder> orders = orderRepository.findPickListsWithAdvancedFilters(
                filter.getVroNumber(),
                filter.getAssignedTo(),
                filter.getSupplierName(),
                filter.getAssignedFromDate(),
                filter.getAssignedToDate(),
                filter.getPickedFromDate(),
                filter.getPickedToDate(),
                filter.getSearchTerm(),
                pageable
        );
        
        List<PickListResponseDTO> pickLists = orders.getContent().stream()
                .map(this::mapToPickListResponseDTO)
                .collect(Collectors.toList());
        
        return new PageImpl<>(pickLists, pageable, orders.getTotalElements());
    }

    
    @Override
    public Page<PickListResponseDTO> getAllPickLists(Pageable pageable) {
        log.info("Fetching all pick lists with pagination");
        
        Page<VendorReturnOrder> orders = orderRepository.findOrdersWithPickList(pageable);
        List<PickListResponseDTO> pickLists = orders.getContent().stream()
                .map(this::mapToPickListResponseDTO)
                .collect(Collectors.toList());
        
        return new PageImpl<>(pickLists, pageable, orders.getTotalElements());
    }
    
    
    private PickListResponseDTO mapToPickListResponseDTO(VendorReturnOrder order) {
        // Calculate progress
        int totalItems = order.getLines().size();
        int pickedItems = (int) order.getLines().stream()
                .filter(line -> line.getPickedQuantity() > 0)
                .count();
        int totalQuantity = order.getTotalQuantity();
        int pickedQuantity = order.getLines().stream()
                .mapToInt(VendorReturnOrderLine::getPickedQuantity)
                .sum();
        int remainingQuantity = totalQuantity - pickedQuantity;
        
        double progress = totalQuantity > 0 
                ? (double) pickedQuantity / totalQuantity * 100 
                : 0.0;
        
        return PickListResponseDTO.builder()
                .id(order.getId())
                .pickListNumber("PL-" + order.getVroNumber())
                .vroNumber(order.getVroNumber())
                .orderId(order.getId())
                .supplierName(order.getSupplierName())
                .supplierCode(order.getSupplierCode())
                .assignedTo(order.getAssignTo())
//                .status(order.getPickListStatus())
//                .statusDisplayName(getStatusDisplayName(order.getPickListStatus()))
                .totalItems(totalItems)
                .totalQuantity(totalQuantity)
                .pickedQuantity(pickedQuantity)
                .remainingQuantity(remainingQuantity)
                .pickingProgress(progress)
                .assignedAt(order.getPickListGeneratedAt())
                .pickedAt(order.getPickedAt())
                .completedAt(order.getPickedAt() != null ? order.getPickedAt() : null)
                .priority(order.getPriority() != null ? order.getPriority().name() : "MEDIUM")
                .createdAt(order.getCreatedAt())
                .items(order.getLines().stream()
                        .map(this::mapToPickListItemDTO)
                        .collect(Collectors.toList()))
                .build();
    }
    
    
    
    

    private PickListItemDTO mapToPickListItemDTO(VendorReturnOrderLine line) {
        int remainingQuantity = line.getOrderQuantity() - line.getPickedQuantity();
        
        return PickListItemDTO.builder()
                .id(line.getId())
                .lineId(line.getId())
                .itemCode(line.getItemCode())
                .itemName(line.getItemName())
                .uom(line.getUom())
                .orderQuantity(line.getOrderQuantity())
                .pickedQuantity(line.getPickedQuantity() != null ? line.getPickedQuantity() : 0)
                .remainingQuantity(remainingQuantity)
                .pickLocation(line.getPickLocation())
                .pickSequence(line.getPickSequence())
                .status(line.getStatus() != null ? line.getStatus().name() : "PENDING")
                .batchNumber(line.getBatchNumber())
                .serialNumbers(line.getSerialNumbers())
                .build();
    }

}