package com.warehouse.wms.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.warehouse.wms.dto.request.PurchaseReturnRequestDTO;
import com.warehouse.wms.dto.response.PurchaseReturnLineResponseDTO;
import com.warehouse.wms.dto.response.PurchaseReturnResponseDTO;
import com.warehouse.wms.entity.PurchaseReturn;
import com.warehouse.wms.entity.PurchaseReturnLine;
import com.warehouse.wms.exception.ResourceNotFoundException;
import com.warehouse.wms.repository.InboundLineRepository;
import com.warehouse.wms.repository.InboundRepository;
import com.warehouse.wms.repository.PurchaseOrderRepository;
import com.warehouse.wms.repository.PurchaseReturnLineRepository;
import com.warehouse.wms.repository.PurchaseReturnRepository;
import com.warehouse.wms.repository.SupplierRepository;
import com.warehouse.wms.service.PurchaseReturnService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PurchaseReturnServiceImpl implements PurchaseReturnService {

    private final PurchaseReturnRepository purchaseReturnRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final InboundRepository inboundRepository;
    private final SupplierRepository supplierRepository;
    private final InboundLineRepository inboundLineRepository;
    
    private final PurchaseReturnLineRepository purchaseReturnLineRepository;


    private static final String RETURN_NUMBER_PREFIX = "PR-";

    @Override
    public PurchaseReturnResponseDTO createPurchaseReturn(PurchaseReturnRequestDTO request) {
        log.info("Creating purchase return for supplier: {}", request.getSupplierName());
        
        PurchaseReturn purchaseReturn = new PurchaseReturn();
        purchaseReturn.setReturnNumber(generateReturnNumber());
        purchaseReturn.setReturnDate(request.getReturnDate() != null ? request.getReturnDate() : LocalDate.now());
        purchaseReturn.setPoNumber(request.getPoNumber());
        purchaseReturn.setGrnNumber(request.getGrnNumber());
        purchaseReturn.setInvoiceNumber(request.getInvoiceNumber());
        purchaseReturn.setRockArea(request.getRockArea());
        purchaseReturn.setSupplierName(request.getSupplierName());
        purchaseReturn.setSupplierCode(request.getSupplierCode());
        purchaseReturn.setReason(request.getReason());
        purchaseReturn.setReturnType(request.getReturnType());
        purchaseReturn.setStatus(request.getStatus() != null ? request.getStatus() : PurchaseReturn.ReturnStatus.PENDING);
        purchaseReturn.setRemarks(request.getRemarks());
        
        // Set relationships
        if (request.getSupplierId() != null) {
            purchaseReturn.setSupplier(supplierRepository.findById(request.getSupplierId())
                    .orElseThrow(() -> new ResourceNotFoundException("Supplier not found")));
        }
        if (request.getPurchaseOrderId() != null) {
            purchaseReturn.setPurchaseOrder(purchaseOrderRepository.findById(request.getPurchaseOrderId())
                    .orElseThrow(() -> new ResourceNotFoundException("Purchase Order not found")));
        }
        if (request.getInboundId() != null) {
            purchaseReturn.setInbound(inboundRepository.findById(request.getInboundId())
                    .orElseThrow(() -> new ResourceNotFoundException("Inbound not found")));
        }
        
        // Add lines
        if (request.getLines() != null) {
            for (var lineRequest : request.getLines()) {
                PurchaseReturnLine line = new PurchaseReturnLine();
                line.setItemCode(lineRequest.getItemCode());
                line.setItemName(lineRequest.getItemName());
                line.setUom(lineRequest.getUom());
                line.setReturnQuantity(lineRequest.getReturnQuantity());
                line.setUnitPrice(lineRequest.getUnitPrice());
                line.setTotalAmount(lineRequest.getTotalAmount() != null ? 
                        lineRequest.getTotalAmount() : lineRequest.getReturnQuantity() * lineRequest.getUnitPrice());
                line.setOriginalQuantity(lineRequest.getOriginalQuantity());
                line.setReceivedQuantity(lineRequest.getReceivedQuantity());
                line.setReason(lineRequest.getReason());
                line.setBatchNumber(lineRequest.getBatchNumber());
                line.setExpiryDate(lineRequest.getExpiryDate());
                line.setRemarks(lineRequest.getRemarks());
                
                if (lineRequest.getInboundLineId() != null) {
                    line.setInboundLine(inboundLineRepository.findById(lineRequest.getInboundLineId())
                            .orElseThrow(() -> new ResourceNotFoundException("Inbound line not found")));
                }
                
                purchaseReturn.addLine(line);
            }
        }
        
        PurchaseReturn saved = purchaseReturnRepository.save(purchaseReturn);
        log.info("Purchase return created with ID: {} and Number: {}", saved.getId(), saved.getReturnNumber());
        return mapToResponseDTO(saved);
    }

    @Override
    public PurchaseReturnResponseDTO updatePurchaseReturn(Long id, PurchaseReturnRequestDTO request) {
        log.info("Updating purchase return with ID: {}", id);
        
        PurchaseReturn purchaseReturn = purchaseReturnRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase Return not found with ID: " + id));
        
        // Can only update if status is PENDING
        if (purchaseReturn.getStatus() != PurchaseReturn.ReturnStatus.PENDING) {
            throw new IllegalStateException("Cannot update purchase return with status: " + purchaseReturn.getStatus());
        }
        
        purchaseReturn.setPoNumber(request.getPoNumber());
        purchaseReturn.setGrnNumber(request.getGrnNumber());
        purchaseReturn.setInvoiceNumber(request.getInvoiceNumber());
        purchaseReturn.setSupplierName(request.getSupplierName());
        purchaseReturn.setSupplierCode(request.getSupplierCode());
        purchaseReturn.setReason(request.getReason());
        purchaseReturn.setReturnType(request.getReturnType());
        purchaseReturn.setRemarks(request.getRemarks());
        
        // Update lines (simple approach - clear and re-add)
        purchaseReturn.getLines().clear();
        if (request.getLines() != null) {
            for (var lineRequest : request.getLines()) {
                PurchaseReturnLine line = new PurchaseReturnLine();
                line.setItemCode(lineRequest.getItemCode());
                line.setItemName(lineRequest.getItemName());
                line.setUom(lineRequest.getUom());
                line.setReturnQuantity(lineRequest.getReturnQuantity());
                line.setUnitPrice(lineRequest.getUnitPrice());
                line.setRejectedArea(lineRequest.getRejectedArea());
                line.setTotalAmount(lineRequest.getTotalAmount() != null ? 
                        lineRequest.getTotalAmount() : lineRequest.getReturnQuantity() * lineRequest.getUnitPrice());
                line.setOriginalQuantity(lineRequest.getOriginalQuantity());
                line.setReceivedQuantity(lineRequest.getReceivedQuantity());
                line.setReason(lineRequest.getReason());
                line.setBatchNumber(lineRequest.getBatchNumber());
                line.setExpiryDate(lineRequest.getExpiryDate());
                line.setRemarks(lineRequest.getRemarks());
                purchaseReturn.addLine(line);
            }
        }
        purchaseReturn.updateTotals();
        
        PurchaseReturn updated = purchaseReturnRepository.save(purchaseReturn);
        log.info("Purchase return updated with ID: {}", updated.getId());
        return mapToResponseDTO(updated);
    }

    @Override
    public PurchaseReturnResponseDTO getPurchaseReturnById(Long id) {
        log.info("Fetching purchase return with ID: {}", id);
        PurchaseReturn purchaseReturn = purchaseReturnRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase Return not found with ID: " + id));
        return mapToResponseDTO(purchaseReturn);
    }

    @Override
    public PurchaseReturnResponseDTO getPurchaseReturnByNumber(String returnNumber) {
        log.info("Fetching purchase return with Number: {}", returnNumber);
        PurchaseReturn purchaseReturn = purchaseReturnRepository.findByReturnNumber(returnNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase Return not found with Number: " + returnNumber));
        return mapToResponseDTO(purchaseReturn);
    }

    @Override
    public Page<PurchaseReturnResponseDTO> getAllPurchaseReturns(Pageable pageable) {
        log.info("Fetching all purchase returns");
        return purchaseReturnRepository.findAll(pageable)
                .map(this::mapToResponseDTO);
    }

    @Override
    public Page<PurchaseReturnResponseDTO> searchPurchaseReturns(String status, String supplierName, String searchTerm, Pageable pageable) {
        log.info("Searching purchase returns with filters");
        PurchaseReturn.ReturnStatus returnStatus = status != null ? 
                PurchaseReturn.ReturnStatus.valueOf(status) : null;
        return purchaseReturnRepository.searchPurchaseReturns(returnStatus, supplierName, searchTerm, pageable)
                .map(this::mapToResponseDTO);
    }

    @Override
    public List<PurchaseReturnResponseDTO> getPurchaseReturnsBySupplier(Long supplierId) {
        log.info("Fetching purchase returns for supplier ID: {}", supplierId);
        return purchaseReturnRepository.findBySupplierId(supplierId)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PurchaseReturnResponseDTO> getPurchaseReturnsByStatus(String status) {
        log.info("Fetching purchase returns with status: {}", status);
        PurchaseReturn.ReturnStatus returnStatus = PurchaseReturn.ReturnStatus.valueOf(status);
        return purchaseReturnRepository.findByStatus(returnStatus)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public PurchaseReturnResponseDTO approvePurchaseReturn(Long id, Long approvedBy) {
        log.info("Approving purchase return with ID: {} by user: {}", id, approvedBy);
        PurchaseReturn purchaseReturn = purchaseReturnRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase Return not found with ID: " + id));
        
        if (purchaseReturn.getStatus() != PurchaseReturn.ReturnStatus.PENDING) {
            throw new IllegalStateException("Can only approve PENDING purchase returns");
        }
        
        purchaseReturn.setStatus(PurchaseReturn.ReturnStatus.APPROVED);
        purchaseReturn.setApprovedBy(approvedBy);
        purchaseReturn.setApprovedDate(LocalDateTime.now());
        
        PurchaseReturn updated = purchaseReturnRepository.save(purchaseReturn);
        log.info("Purchase return approved: {}", updated.getReturnNumber());
        return mapToResponseDTO(updated);
    }

    @Override
    public PurchaseReturnResponseDTO rejectPurchaseReturn(Long id, Long rejectedBy, String rejectionReason) {
        log.info("Rejecting purchase return with ID: {} by user: {}", id, rejectedBy);
        PurchaseReturn purchaseReturn = purchaseReturnRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase Return not found with ID: " + id));
        
        if (purchaseReturn.getStatus() != PurchaseReturn.ReturnStatus.PENDING) {
            throw new IllegalStateException("Can only reject PENDING purchase returns");
        }
        
        purchaseReturn.setStatus(PurchaseReturn.ReturnStatus.REJECTED);
        purchaseReturn.setRejectedBy(rejectedBy);
        purchaseReturn.setRejectedDate(LocalDateTime.now());
        purchaseReturn.setRejectionReason(rejectionReason);
        
        PurchaseReturn updated = purchaseReturnRepository.save(purchaseReturn);
        log.info("Purchase return rejected: {}", updated.getReturnNumber());
        return mapToResponseDTO(updated);
    }

    @Override
    public PurchaseReturnResponseDTO shipPurchaseReturn(Long id, Long shippedBy, String trackingNumber) {
        log.info("Shipping purchase return with ID: {} by user: {}", id, shippedBy);
        PurchaseReturn purchaseReturn = purchaseReturnRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase Return not found with ID: " + id));
        
        if (purchaseReturn.getStatus() != PurchaseReturn.ReturnStatus.APPROVED) {
            throw new IllegalStateException("Can only ship APPROVED purchase returns");
        }
        
        purchaseReturn.setStatus(PurchaseReturn.ReturnStatus.SHIPPED);
        purchaseReturn.setShippedBy(shippedBy);
        purchaseReturn.setShippedDate(LocalDateTime.now());
        purchaseReturn.setTrackingNumber(trackingNumber);
        
        PurchaseReturn updated = purchaseReturnRepository.save(purchaseReturn);
        log.info("Purchase return shipped: {}", updated.getReturnNumber());
        return mapToResponseDTO(updated);
    }

    @Override
    public PurchaseReturnResponseDTO completePurchaseReturn(Long id) {
        log.info("Completing purchase return with ID: {}", id);
        PurchaseReturn purchaseReturn = purchaseReturnRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase Return not found with ID: " + id));
        
        if (purchaseReturn.getStatus() != PurchaseReturn.ReturnStatus.SHIPPED) {
            throw new IllegalStateException("Can only complete SHIPPED purchase returns");
        }
        
        purchaseReturn.setStatus(PurchaseReturn.ReturnStatus.COMPLETED);
        
        PurchaseReturn updated = purchaseReturnRepository.save(purchaseReturn);
        log.info("Purchase return completed: {}", updated.getReturnNumber());
        return mapToResponseDTO(updated);
    }

    @Override
    public PurchaseReturnResponseDTO cancelPurchaseReturn(Long id) {
        log.info("Cancelling purchase return with ID: {}", id);
        PurchaseReturn purchaseReturn = purchaseReturnRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase Return not found with ID: " + id));
        
        if (purchaseReturn.getStatus() == PurchaseReturn.ReturnStatus.COMPLETED) {
            throw new IllegalStateException("Cannot cancel COMPLETED purchase return");
        }
        
        purchaseReturn.setStatus(PurchaseReturn.ReturnStatus.CANCELLED);
        
        PurchaseReturn updated = purchaseReturnRepository.save(purchaseReturn);
        log.info("Purchase return cancelled: {}", updated.getReturnNumber());
        return mapToResponseDTO(updated);
    }

    @Override
    public void deletePurchaseReturn(Long id) {
        log.info("Deleting purchase return with ID: {}", id);
        PurchaseReturn purchaseReturn = purchaseReturnRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase Return not found with ID: " + id));
        
        if (purchaseReturn.getStatus() != PurchaseReturn.ReturnStatus.PENDING) {
            throw new IllegalStateException("Can only delete PENDING purchase returns");
        }
        
        purchaseReturnRepository.delete(purchaseReturn);
        log.info("Purchase return deleted with ID: {}", id);
    }

    @Override
    public long getCountByStatus(String status) {
        PurchaseReturn.ReturnStatus returnStatus = PurchaseReturn.ReturnStatus.valueOf(status);
        return purchaseReturnRepository.countByStatus(returnStatus);
    }

    private String generateReturnNumber() {
        Long count = purchaseReturnRepository.countByReturnNumberStartingWith(RETURN_NUMBER_PREFIX);
        return RETURN_NUMBER_PREFIX + String.format("%08d", count + 1);
    }

    private PurchaseReturnResponseDTO mapToResponseDTO(PurchaseReturn purchaseReturn) {
        PurchaseReturnResponseDTO.PurchaseReturnResponseDTOBuilder builder = PurchaseReturnResponseDTO.builder()
                .id(purchaseReturn.getId())
                .returnNumber(purchaseReturn.getReturnNumber())
                .returnDate(purchaseReturn.getReturnDate())
                .poNumber(purchaseReturn.getPoNumber())
                .grnNumber(purchaseReturn.getGrnNumber())
                .invoiceNumber(purchaseReturn.getInvoiceNumber())
                .supplierName(purchaseReturn.getSupplierName())
                .supplierCode(purchaseReturn.getSupplierCode())
                .reason(purchaseReturn.getReason())
                .returnType(purchaseReturn.getReturnType())
                .status(purchaseReturn.getStatus())
                .totalAmount(purchaseReturn.getTotalAmount())
                .totalQuantity(purchaseReturn.getTotalQuantity())
                .remarks(purchaseReturn.getRemarks())
                .rejectionReason(purchaseReturn.getRejectionReason())
                .trackingNumber(purchaseReturn.getTrackingNumber())
                .createdAt(purchaseReturn.getCreatedAt());
        
        if (purchaseReturn.getSupplier() != null) {
            builder.supplierId(purchaseReturn.getSupplier().getId());
        }
        if (purchaseReturn.getApprovedDate() != null) {
            builder.approvedDate(purchaseReturn.getApprovedDate());
        }
        
        if (purchaseReturn.getLines() != null) {
            builder.lines(purchaseReturn.getLines().stream()
                    .map(this::mapLineToResponseDTO)
                    .collect(Collectors.toList()));
        }
        
        return builder.build();
    }
    
    private PurchaseReturnLineResponseDTO mapLineToResponseDTO(PurchaseReturnLine line) {
        return PurchaseReturnLineResponseDTO.builder()
                .id(line.getId())
                .itemCode(line.getItemCode())
                .itemName(line.getItemName())
                .uom(line.getUom())
                .rejectedArea(line.getRejectedArea())
                .returnQuantity(line.getReturnQuantity())
                .unitPrice(line.getUnitPrice())
                .totalAmount(line.getTotalAmount())
                .reason(line.getReason())
                .batchNumber(line.getBatchNumber())
                .expiryDate(line.getExpiryDate())
                .remarks(line.getRemarks())
                .build();
    }
    
    
    
    @Override
    public PurchaseReturnLineResponseDTO updateRejectedArea(Long lineId, String rejectedArea) {
        log.info("Updating rejected area for line ID: {} to: {}", lineId, rejectedArea);
        
        PurchaseReturnLine line = purchaseReturnLineRepository.findById(lineId)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase return line not found with ID: " + lineId));
        
        // Check if purchase return is editable
        if (line.getPurchaseReturn().getStatus() != PurchaseReturn.ReturnStatus.PENDING) {
            throw new IllegalStateException("Cannot update rejected area when purchase return is not in PENDING status");
        }
        
        line.setRejectedArea(rejectedArea);
        
        PurchaseReturnLine updated = purchaseReturnLineRepository.save(line);
        log.info("Rejected area updated for line ID: {}", updated.getId());
        return mapLineToResponseDTO(updated);
    }
    
    
    

}