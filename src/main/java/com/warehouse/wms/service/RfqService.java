package com.warehouse.wms.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.warehouse.wms.dto.CreateRfqDTO;
import com.warehouse.wms.dto.PurchaseOrderDTO;
import com.warehouse.wms.dto.RfqDTO;
import com.warehouse.wms.dto.RfqFilterDTO;
import com.warehouse.wms.dto.RfqItemDTO;
import com.warehouse.wms.dto.StatusUpdateRequestDTORfq;
import com.warehouse.wms.dto.VendorQuotationDTO;
import com.warehouse.wms.dto.VendorQuotationItemDTO;
import com.warehouse.wms.entity.PurchaseRequest;
import com.warehouse.wms.entity.PurchaseRequestItem;
import com.warehouse.wms.entity.QuotationStatus;
import com.warehouse.wms.entity.Rfq;
import com.warehouse.wms.entity.RfqItem;
import com.warehouse.wms.entity.RfqStatus;
import com.warehouse.wms.entity.Supplier;
import com.warehouse.wms.entity.VendorQuotation;
import com.warehouse.wms.entity.VendorQuotationItem;
import com.warehouse.wms.exception.ResourceNotFoundException;
import com.warehouse.wms.repository.PurchaseRequestItemRepository;
import com.warehouse.wms.repository.PurchaseRequestRepository;
import com.warehouse.wms.repository.RfqItemRepository;
import com.warehouse.wms.repository.RfqRepository;
import com.warehouse.wms.repository.SupplierRepository;
import com.warehouse.wms.repository.VendorQuotationRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RfqService {

    private final RfqRepository rfqRepository;
    private final RfqItemRepository rfqItemRepository;
    private final VendorQuotationRepository vendorQuotationRepository;
    private final PurchaseRequestRepository purchaseRequestRepository;
    private final SupplierRepository supplierRepository;
    private final PurchaseRequestItemRepository purchaseRequestItemRepository;

    private static final String RFQ_PREFIX = "RFQ";

    // Generate RFQ Number
    private String generateRfqNumber() {
        LocalDate now = LocalDate.now();
        String year = String.valueOf(now.getYear());
        String monthDay = String.format("%02d%02d", now.getMonthValue(), now.getDayOfMonth());
        String datePart = String.format("%s-%s", year, monthDay);
        String prefix = String.format("%s-%s", RFQ_PREFIX, datePart);
        
        Long count = rfqRepository.countByRfqNumberStartingWith(prefix);
        int nextSequence = count.intValue() + 1;
        
        String rfqNumber = String.format("%s-%04d", prefix, nextSequence);
        while (rfqRepository.existsByRfqNumber(rfqNumber)) {
            nextSequence++;
            rfqNumber = String.format("%s-%04d", prefix, nextSequence);
        }
        
        return rfqNumber;
    }

    // ============ CREATE RFQ FROM PR ============
    
    @Transactional
    public RfqDTO createRfqFromPR(CreateRfqDTO requestDTO, Long userId) {
        log.info("Creating RFQ from Purchase Request: {}", requestDTO.getPurchaseRequestId());
        
        // Get Purchase Request
        PurchaseRequest pr = purchaseRequestRepository.findById(requestDTO.getPurchaseRequestId())
            .orElseThrow(() -> new ResourceNotFoundException("Purchase Request not found"));
        
        // Create RFQ
        Rfq rfq = new Rfq();
        rfq.setRfqNumber(generateRfqNumber());
        rfq.setRfqDate(requestDTO.getRfqDate());
        rfq.setClosingDate(requestDTO.getClosingDate());
        rfq.setStatus(RfqStatus.DRAFT);
        rfq.setReferenceNumber(requestDTO.getReferenceNumber());
        rfq.setRemarks(requestDTO.getRemarks());
        rfq.setTermsAndConditions(requestDTO.getTermsAndConditions());
        rfq.setDeliveryTerms(requestDTO.getDeliveryTerms());
        rfq.setPaymentTerms(requestDTO.getPaymentTerms());
        rfq.setPurchaseRequest(pr);
        rfq.setCreatedBy(userId);
        
        rfq = rfqRepository.save(rfq);
        
        // Add items from PR
        if (requestDTO.getItems() != null && !requestDTO.getItems().isEmpty()) {
            for (RfqItemDTO itemDTO : requestDTO.getItems()) {
                RfqItem item = new RfqItem();
                item.setItemCode(itemDTO.getItemCode());
                item.setItemName(itemDTO.getItemName());
                item.setDescription(itemDTO.getDescription());
                item.setUom(itemDTO.getUom());
                item.setQuantity(itemDTO.getQuantity());
                item.setHsnCode(itemDTO.getHsnCode());
                item.setGstRate(itemDTO.getGstRate());
                item.setCgstRate(itemDTO.getCgstRate());
                item.setSgstRate(itemDTO.getSgstRate());
                item.setIgstRate(itemDTO.getIgstRate());
                item.setEstimatedUnitPrice(itemDTO.getEstimatedUnitPrice());
                item.setEstimatedTotal(itemDTO.getQuantity() * (itemDTO.getEstimatedUnitPrice() != null ? itemDTO.getEstimatedUnitPrice() : 0));
                item.setSpecifications(itemDTO.getSpecifications());
                item.setRfq(rfq);
                
                // Link to PR item if provided
                if (itemDTO.getPurchaseRequestItemId() != null) {
                    PurchaseRequestItem prItem = purchaseRequestItemRepository.findById(itemDTO.getPurchaseRequestItemId())
                        .orElse(null);
                    item.setPurchaseRequestItem(prItem);
                }
                
                rfq.addItem(item);
            }
        } else {
            // If no items provided, copy from PR
            for (PurchaseRequestItem prItem : pr.getItems()) {
                RfqItem item = new RfqItem();
                item.setItemCode(prItem.getItemCode());
                item.setItemName(prItem.getItemName());
                item.setDescription(prItem.getDescription());
                item.setUom(prItem.getUom());
                item.setQuantity(prItem.getRequestedQty().doubleValue());
                item.setRfq(rfq);
                item.setPurchaseRequestItem(prItem);
                rfq.addItem(item);
            }
        }
        
        // Create Vendor Quotations for selected suppliers
        if (requestDTO.getSupplierIds() != null && !requestDTO.getSupplierIds().isEmpty()) {
            for (Long supplierId : requestDTO.getSupplierIds()) {
                Supplier supplier = supplierRepository.findById(supplierId)
                    .orElseThrow(() -> new ResourceNotFoundException("Supplier not found: " + supplierId));
                
                VendorQuotation quotation = new VendorQuotation();
                quotation.setQuotationNumber("QUOT-" + supplier.getCode() + "-" + rfq.getRfqNumber());
                quotation.setQuotationDate(LocalDate.now());
                quotation.setStatus(QuotationStatus.PENDING);
                quotation.setSupplier(supplier);
                quotation.setRfq(rfq);
                
                // Create quotation items from RFQ items
                for (RfqItem rfqItem : rfq.getItems()) {
                    VendorQuotationItem qItem = new VendorQuotationItem();
                    qItem.setItemCode(rfqItem.getItemCode());
                    qItem.setItemName(rfqItem.getItemName());
                    qItem.setDescription(rfqItem.getDescription());
                    qItem.setUom(rfqItem.getUom());
                    qItem.setQuantity(rfqItem.getQuantity());
                    qItem.setGstRate(rfqItem.getGstRate());
                    qItem.setCgstRate(rfqItem.getCgstRate());
                    qItem.setSgstRate(rfqItem.getSgstRate());
                    qItem.setIgstRate(rfqItem.getIgstRate());
                    qItem.setRfqItem(rfqItem);
                    qItem.setVendorQuotation(quotation);
                    quotation.addItem(qItem);
                }
                
                rfq.addVendorQuotation(quotation);
            }
        }
        
        rfq = rfqRepository.save(rfq);
        log.info("RFQ created with number: {}", rfq.getRfqNumber());
        
        return convertToDTO(rfq);
    }

    // ============ ADD VENDOR QUOTATION ============
    
    @Transactional
    public VendorQuotationDTO addVendorQuotation(Long rfqId, VendorQuotationDTO quotationDTO) {
        log.info("Adding vendor quotation for RFQ: {}", rfqId);
        
        Rfq rfq = rfqRepository.findById(rfqId)
            .orElseThrow(() -> new ResourceNotFoundException("RFQ not found"));
        
        Supplier supplier = supplierRepository.findById(quotationDTO.getSupplierId())
            .orElseThrow(() -> new ResourceNotFoundException("Supplier not found"));
        
        VendorQuotation quotation = new VendorQuotation();
        quotation.setQuotationNumber(quotationDTO.getQuotationNumber() != null ? 
            quotationDTO.getQuotationNumber() : 
            "QUOT-" + supplier.getCode() + "-" + LocalDate.now().toString());
        quotation.setQuotationDate(quotationDTO.getQuotationDate() != null ? 
            quotationDTO.getQuotationDate() : LocalDate.now());
        quotation.setDeliveryDate(quotationDTO.getDeliveryDate());
        quotation.setValidTill(quotationDTO.getValidTill());
        quotation.setDiscountAmount(quotationDTO.getDiscountAmount() != null ? quotationDTO.getDiscountAmount() : 0.0);
        quotation.setShippingCharges(quotationDTO.getShippingCharges() != null ? quotationDTO.getShippingCharges() : 0.0);
        quotation.setRemarks(quotationDTO.getRemarks());
        quotation.setStatus(QuotationStatus.PENDING);
        quotation.setSupplier(supplier);
        quotation.setRfq(rfq);
        
        // Add items
        for (VendorQuotationItemDTO itemDTO : quotationDTO.getItems()) {
            VendorQuotationItem item = new VendorQuotationItem();
            item.setItemCode(itemDTO.getItemCode());
            item.setItemName(itemDTO.getItemName());
            item.setDescription(itemDTO.getDescription());
            item.setUom(itemDTO.getUom());
            item.setQuantity(itemDTO.getQuantity());
            item.setUnitPrice(itemDTO.getUnitPrice() != null ? itemDTO.getUnitPrice() : 0.0);
            item.setGstRate(itemDTO.getGstRate() != null ? itemDTO.getGstRate() : 0.0);
            item.setCgstRate(itemDTO.getCgstRate());
            item.setSgstRate(itemDTO.getSgstRate());
            item.setIgstRate(itemDTO.getIgstRate());
            item.setDiscountPercentage(itemDTO.getDiscountPercentage() != null ? itemDTO.getDiscountPercentage() : 0.0);
            
            item.calculatePrice();
            item.setVendorQuotation(quotation);
            
            // Link to RFQ item if possible
            if (rfq.getItems() != null) {
                rfq.getItems().stream()
                    .filter(rfqItem -> rfqItem.getItemCode().equals(itemDTO.getItemCode()))
                    .findFirst()
                    .ifPresent(rfqItem -> item.setRfqItem(rfqItem));
            }
            
            quotation.addItem(item);
        }
        
        quotation.calculateTotals();
        quotation = vendorQuotationRepository.save(quotation);
        
        // Update RFQ status
        rfq.setStatus(RfqStatus.IN_PROGRESS);
        rfqRepository.save(rfq);
        
        log.info("Vendor quotation added with number: {}", quotation.getQuotationNumber());
        return convertQuotationToDTO(quotation);
    }

    // ============ COMPARE QUOTATIONS ============
    
    @Transactional
    public List<VendorQuotationDTO> compareQuotations(Long rfqId) {
        log.info("Comparing quotations for RFQ: {}", rfqId);
        
        Rfq rfq = rfqRepository.findById(rfqId)
            .orElseThrow(() -> new ResourceNotFoundException("RFQ not found"));
        
        List<VendorQuotation> quotations = rfq.getVendorQuotations();
        
        // Sort by grand total
        quotations.sort((q1, q2) -> q1.getGrandTotal().compareTo(q2.getGrandTotal()));
        
        // Assign ranks
        int rank = 1;
        for (VendorQuotation q : quotations) {
            q.setRank(rank++);
            q.setStatus(QuotationStatus.COMPARED);
            vendorQuotationRepository.save(q);
        }
        
        return quotations.stream()
            .map(this::convertQuotationToDTO)
            .collect(Collectors.toList());
    }

    // ============ CONVERT TO PO ============
    
    @Transactional
    public PurchaseOrderDTO convertToPO(Long quotationId) {
        log.info("Converting quotation to PO: {}", quotationId);
        
        VendorQuotation quotation = vendorQuotationRepository.findById(quotationId)
            .orElseThrow(() -> new ResourceNotFoundException("Quotation not found"));
        
        // Create Purchase Order from quotation
        // This would call your PurchaseOrderService
        // ...
        
        quotation.setStatus(QuotationStatus.CONVERTED);
        vendorQuotationRepository.save(quotation);
        
        return null; // Return PO DTO
    }

    // ============ GET RFQ ============
    
    public RfqDTO getRfqById(Long id) {
        Rfq rfq = rfqRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("RFQ not found"));
        return convertToDTO(rfq);
    }

    // ============ CONVERSION METHODS ============
    
    private RfqDTO convertToDTO(Rfq entity) {
        RfqDTO dto = RfqDTO.builder()
            .id(entity.getId())
            .rfqNumber(entity.getRfqNumber())
            .rfqDate(entity.getRfqDate())
            .closingDate(entity.getClosingDate())
            .status(entity.getStatus())
            .referenceNumber(entity.getReferenceNumber())
            .remarks(entity.getRemarks())
            .termsAndConditions(entity.getTermsAndConditions())
            .deliveryTerms(entity.getDeliveryTerms())
            .paymentTerms(entity.getPaymentTerms())
            .purchaseRequestId(entity.getPurchaseRequest() != null ? entity.getPurchaseRequest().getId() : null)
            .prNumber(entity.getPurchaseRequest() != null ? entity.getPurchaseRequest().getPrNumber() : null)
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .build();
        
        if (entity.getItems() != null) {
            dto.setItems(entity.getItems().stream()
                .map(this::convertItemToDTO)
                .collect(Collectors.toList()));
        }
        
        if (entity.getVendorQuotations() != null) {
            dto.setVendorQuotations(entity.getVendorQuotations().stream()
                .map(this::convertQuotationToDTO)
                .collect(Collectors.toList()));
        }
        
        return dto;
    }
    
    private RfqItemDTO convertItemToDTO(RfqItem entity) {
        return RfqItemDTO.builder()
            .id(entity.getId())
            .itemCode(entity.getItemCode())
            .itemName(entity.getItemName())
            .description(entity.getDescription())
            .uom(entity.getUom())
            .quantity(entity.getQuantity())
            .hsnCode(entity.getHsnCode())
            .gstRate(entity.getGstRate())
            .cgstRate(entity.getCgstRate())
            .sgstRate(entity.getSgstRate())
            .igstRate(entity.getIgstRate())
            .estimatedUnitPrice(entity.getEstimatedUnitPrice())
            .estimatedTotal(entity.getEstimatedTotal())
            .specifications(entity.getSpecifications())
            .purchaseRequestItemId(entity.getPurchaseRequestItem() != null ? entity.getPurchaseRequestItem().getId() : null)
            .build();
    }
    
    private VendorQuotationDTO convertQuotationToDTO(VendorQuotation entity) {
        VendorQuotationDTO dto = VendorQuotationDTO.builder()
            .id(entity.getId())
            .quotationNumber(entity.getQuotationNumber())
            .quotationDate(entity.getQuotationDate())
            .deliveryDate(entity.getDeliveryDate())
            .validTill(entity.getValidTill())
            .subTotal(entity.getSubTotal())
            .gstTotal(entity.getGstTotal())
            .grandTotal(entity.getGrandTotal())
            .discountAmount(entity.getDiscountAmount())
            .shippingCharges(entity.getShippingCharges())
            .remarks(entity.getRemarks())
            .status(entity.getStatus())
            .rank(entity.getRank())
            .supplierId(entity.getSupplier().getId())
            .supplierName(entity.getSupplier().getName())
            .supplierCode(entity.getSupplier().getCode())
            .build();
        
        if (entity.getItems() != null) {
            dto.setItems(entity.getItems().stream()
                .map(this::convertQuotationItemToDTO)
                .collect(Collectors.toList()));
        }
        
        return dto;
    }
    
    private VendorQuotationItemDTO convertQuotationItemToDTO(VendorQuotationItem entity) {
        return VendorQuotationItemDTO.builder()
            .id(entity.getId())
            .itemCode(entity.getItemCode())
            .itemName(entity.getItemName())
            .description(entity.getDescription())
            .uom(entity.getUom())
            .quantity(entity.getQuantity())
            .unitPrice(entity.getUnitPrice())
            .gstRate(entity.getGstRate())
            .cgstRate(entity.getCgstRate())
            .sgstRate(entity.getSgstRate())
            .igstRate(entity.getIgstRate())
            .totalAmount(entity.getTotalAmount())
            .gstAmount(entity.getGstAmount())
            .totalWithGst(entity.getTotalWithGst())
            .discountPercentage(entity.getDiscountPercentage())
            .discountAmount(entity.getDiscountAmount())
            .build();
    }
    
 // Get RFQ by number
    public RfqDTO getRfqByNumber(String rfqNumber) {
        Rfq rfq = rfqRepository.findByRfqNumber(rfqNumber);
        if (rfq == null) {
            throw new ResourceNotFoundException("RFQ not found with number: " + rfqNumber);
        }
        return convertToDTO(rfq);
    }

    // Filter RFQs
    public Page<RfqDTO> filterRfqs(RfqFilterDTO filter, Pageable pageable) {
        Page<Rfq> rfqs = rfqRepository.filterRfqs(
            filter.getStatus(),
            filter.getStatuses(),
            filter.getRfqNumber(),
            filter.getPrNumber(),
            filter.getRfqDateFrom(),
            filter.getRfqDateTo(),
            filter.getClosingDateFrom(),
            filter.getClosingDateTo(),
            filter.getItemCode(),
            filter.getItemName(),
            filter.getSupplierId(),
            filter.getHasQuotations(),
            filter.getSearchTerm(),
            pageable
        );
        return rfqs.map(this::convertToDTO);
    }

    // Get RFQs by PR
    public List<RfqDTO> getRfqsByPR(Long prId) {
        List<Rfq> rfqs = rfqRepository.findByPurchaseRequestId(prId);
        return rfqs.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    // Get RFQ items
    public List<RfqItemDTO> getRfqItems(Long rfqId) {
        List<RfqItem> items = rfqItemRepository.findByRfqId(rfqId);
        return items.stream().map(this::convertItemToDTO).collect(Collectors.toList());
    }

    // Update RFQ status
    @Transactional
    public RfqDTO updateRfqStatus(Long id, StatusUpdateRequestDTORfq statusRequest) {
        Rfq rfq = rfqRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("RFQ not found"));
        rfq.setStatus(statusRequest.getStatus());
        if (statusRequest.getRemarks() != null) {
            rfq.setRemarks(statusRequest.getRemarks());
        }
        rfq = rfqRepository.save(rfq);
        return convertToDTO(rfq);
    }

    // Submit RFQ
    @Transactional
    public RfqDTO submitRfq(Long id) {
        Rfq rfq = rfqRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("RFQ not found"));
        rfq.setStatus(RfqStatus.SUBMITTED);
        rfq.setSubmittedAt(LocalDateTime.now());
        rfq = rfqRepository.save(rfq);
        return convertToDTO(rfq);
    }

    // Close RFQ
    @Transactional
    public RfqDTO closeRfq(Long id) {
        Rfq rfq = rfqRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("RFQ not found"));
        rfq.setStatus(RfqStatus.CLOSED);
        rfq = rfqRepository.save(rfq);
        return convertToDTO(rfq);
    }

    // Cancel RFQ
    @Transactional
    public RfqDTO cancelRfq(Long id) {
        Rfq rfq = rfqRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("RFQ not found"));
        rfq.setStatus(RfqStatus.CANCELLED);
        rfq = rfqRepository.save(rfq);
        return convertToDTO(rfq);
    }

    // Delete RFQ (only if DRAFT)
    @Transactional
    public void deleteRfq(Long id) {
        Rfq rfq = rfqRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("RFQ not found"));
        if (rfq.getStatus() != RfqStatus.DRAFT) {
            throw new IllegalStateException("Only DRAFT RFQs can be deleted");
        }
        rfqRepository.delete(rfq);
    }

    // Get RFQ statistics
    public Object getRfqStatistics() {
        long draftCount = rfqRepository.countByStatus(RfqStatus.DRAFT);
        long submittedCount = rfqRepository.countByStatus(RfqStatus.SUBMITTED);
        long inProgressCount = rfqRepository.countByStatus(RfqStatus.IN_PROGRESS);
        long completedCount = rfqRepository.countByStatus(RfqStatus.COMPLETED);
        long closedCount = rfqRepository.countByStatus(RfqStatus.CLOSED);
        long cancelledCount = rfqRepository.countByStatus(RfqStatus.CANCELLED);
        
        return Map.of(
            "draft", draftCount,
            "submitted", submittedCount,
            "inProgress", inProgressCount,
            "completed", completedCount,
            "closed", closedCount,
            "cancelled", cancelledCount,
            "total", draftCount + submittedCount + inProgressCount + completedCount + closedCount + cancelledCount
        );
    }

    // Approve quotation
    @Transactional
    public VendorQuotationDTO approveQuotation(Long quotationId) {
        VendorQuotation quotation = vendorQuotationRepository.findById(quotationId)
            .orElseThrow(() -> new ResourceNotFoundException("Quotation not found"));
        quotation.setStatus(QuotationStatus.APPROVED);
        quotation = vendorQuotationRepository.save(quotation);
        return convertQuotationToDTO(quotation);
    }

    // Reject quotation
    @Transactional
    public VendorQuotationDTO rejectQuotation(Long quotationId, String reason) {
        VendorQuotation quotation = vendorQuotationRepository.findById(quotationId)
            .orElseThrow(() -> new ResourceNotFoundException("Quotation not found"));
        quotation.setStatus(QuotationStatus.REJECTED);
        if (reason != null) {
            quotation.setRemarks(quotation.getRemarks() != null ? 
                quotation.getRemarks() + "\nRejected: " + reason : 
                "Rejected: " + reason);
        }
        quotation = vendorQuotationRepository.save(quotation);
        return convertQuotationToDTO(quotation);
    }

    // Update vendor quotation
    @Transactional
    public VendorQuotationDTO updateVendorQuotation(Long quotationId, VendorQuotationDTO quotationDTO) {
        VendorQuotation quotation = vendorQuotationRepository.findById(quotationId)
            .orElseThrow(() -> new ResourceNotFoundException("Quotation not found"));
        
        // Update basic fields
        quotation.setQuotationDate(quotationDTO.getQuotationDate());
        quotation.setDeliveryDate(quotationDTO.getDeliveryDate());
        quotation.setValidTill(quotationDTO.getValidTill());
        quotation.setDiscountAmount(quotationDTO.getDiscountAmount() != null ? quotationDTO.getDiscountAmount() : 0.0);
        quotation.setShippingCharges(quotationDTO.getShippingCharges() != null ? quotationDTO.getShippingCharges() : 0.0);
        quotation.setRemarks(quotationDTO.getRemarks());
        
        // Update items
        quotation.getItems().clear();
        for (VendorQuotationItemDTO itemDTO : quotationDTO.getItems()) {
            VendorQuotationItem item = new VendorQuotationItem();
            item.setItemCode(itemDTO.getItemCode());
            item.setItemName(itemDTO.getItemName());
            item.setDescription(itemDTO.getDescription());
            item.setUom(itemDTO.getUom());
            item.setQuantity(itemDTO.getQuantity());
            item.setUnitPrice(itemDTO.getUnitPrice() != null ? itemDTO.getUnitPrice() : 0.0);
            item.setGstRate(itemDTO.getGstRate() != null ? itemDTO.getGstRate() : 0.0);
            item.setCgstRate(itemDTO.getCgstRate());
            item.setSgstRate(itemDTO.getSgstRate());
            item.setIgstRate(itemDTO.getIgstRate());
            item.setDiscountPercentage(itemDTO.getDiscountPercentage() != null ? itemDTO.getDiscountPercentage() : 0.0);
            item.calculatePrice();
            item.setVendorQuotation(quotation);
            quotation.addItem(item);
        }
        
        quotation.calculateTotals();
        quotation = vendorQuotationRepository.save(quotation);
        
        return convertQuotationToDTO(quotation);
    }
}