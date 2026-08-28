package com.warehouse.wms.service;

import com.warehouse.wms.dto.request.PurchaseReturnRequestDTO;
import com.warehouse.wms.dto.response.PurchaseReturnResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface PurchaseReturnService {
    PurchaseReturnResponseDTO createPurchaseReturn(PurchaseReturnRequestDTO request);
    PurchaseReturnResponseDTO updatePurchaseReturn(Long id, PurchaseReturnRequestDTO request);
    PurchaseReturnResponseDTO getPurchaseReturnById(Long id);
    PurchaseReturnResponseDTO getPurchaseReturnByNumber(String returnNumber);
    Page<PurchaseReturnResponseDTO> getAllPurchaseReturns(Pageable pageable);
    Page<PurchaseReturnResponseDTO> searchPurchaseReturns(String status, String supplierName, String searchTerm, Pageable pageable);
    List<PurchaseReturnResponseDTO> getPurchaseReturnsBySupplier(Long supplierId);
    List<PurchaseReturnResponseDTO> getPurchaseReturnsByStatus(String status);
    PurchaseReturnResponseDTO approvePurchaseReturn(Long id, Long approvedBy);
    PurchaseReturnResponseDTO rejectPurchaseReturn(Long id, Long rejectedBy, String rejectionReason);
    PurchaseReturnResponseDTO shipPurchaseReturn(Long id, Long shippedBy, String trackingNumber);
    PurchaseReturnResponseDTO completePurchaseReturn(Long id);
    PurchaseReturnResponseDTO cancelPurchaseReturn(Long id);
    void deletePurchaseReturn(Long id);
    long getCountByStatus(String status);
}