package com.warehouse.wms.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.warehouse.wms.dto.request.DispatchDTO;
import com.warehouse.wms.dto.request.PackingDTO;
import com.warehouse.wms.dto.request.PickListFilterDTO;
import com.warehouse.wms.dto.request.PickingDTO;
import com.warehouse.wms.dto.request.QCDTO;
import com.warehouse.wms.dto.request.ReturnOrderFilterDTO;
import com.warehouse.wms.dto.request.SettlementDTO;
import com.warehouse.wms.dto.request.VendorReceiptDTO;
import com.warehouse.wms.dto.request.VendorReturnOrderDTO;
import com.warehouse.wms.dto.request.VendorReturnRequestDTO;
import com.warehouse.wms.dto.response.DispatchResponseDTO;
import com.warehouse.wms.dto.response.PickListResponseDTO;
import com.warehouse.wms.dto.response.SettlementResponseDTO;
import com.warehouse.wms.dto.response.VendorReceiptResponseDTO;
import com.warehouse.wms.dto.response.VendorReturnOrderResponseDTO;
import com.warehouse.wms.dto.response.VendorReturnResponseDTO;

public interface VendorReturnService {
    
    // ========== Return Request Operations ==========
    VendorReturnResponseDTO createReturnRequest(VendorReturnRequestDTO request);
    VendorReturnResponseDTO updateReturnRequest(Long id, VendorReturnRequestDTO request);
    VendorReturnResponseDTO getReturnRequestById(Long id);
    VendorReturnResponseDTO getReturnRequestByNumber(String requestNumber);
    Page<VendorReturnResponseDTO> getAllReturnRequests(Pageable pageable);
    Page<VendorReturnResponseDTO> searchReturnRequests(String supplierName, String status, String searchTerm, Pageable pageable);
    VendorReturnResponseDTO submitReturnRequest(Long id);
    VendorReturnResponseDTO approveReturnRequest(Long id, Long approvedBy);
    VendorReturnResponseDTO rejectReturnRequest(Long id, Long rejectedBy, String rejectionReason);
    void deleteReturnRequest(Long id);
    
    // ========== Return Order Operations ==========
    VendorReturnOrderResponseDTO createReturnOrder(VendorReturnOrderDTO request);
    VendorReturnOrderResponseDTO updateReturnOrder(Long id, VendorReturnOrderDTO request);
    VendorReturnOrderResponseDTO getReturnOrderById(Long id);
    VendorReturnOrderResponseDTO getReturnOrderByNumber(String orderNumber);
    Page<VendorReturnOrderResponseDTO> getAllReturnOrders(Pageable pageable);
    Page<VendorReturnOrderResponseDTO> searchReturnOrders(String supplierName, String status, String searchTerm, Pageable pageable);
    VendorReturnOrderResponseDTO generatePickList(Long id,String assignTo);
    
    Page<PickListResponseDTO> searchPickLists(PickListFilterDTO filter, Pageable pageable);
    Page<PickListResponseDTO> getAllPickLists(Pageable pageable);

    VendorReturnOrderResponseDTO cancelReturnOrder(Long id);
    void deleteReturnOrder(Long id);
    
    
    Page<VendorReturnOrderResponseDTO> getAllReturnOrdersWithFilters(ReturnOrderFilterDTO filter, Pageable pageable);
    
    // ========== Warehouse Execution Operations ==========
    VendorReturnOrderResponseDTO performPicking(Long orderId, List<PickingDTO> pickingDetails);
    VendorReturnOrderResponseDTO performQC(Long orderId, List<QCDTO> qcDetails);
    VendorReturnOrderResponseDTO performPacking(Long orderId, List<PackingDTO> packingDetails);
    
    // ========== Dispatch Operations ==========
    DispatchResponseDTO createDispatch(DispatchDTO dispatchDTO);
    DispatchResponseDTO getDispatchById(Long id);
    DispatchResponseDTO confirmDispatch(Long id);
    DispatchResponseDTO uploadPOD(Long id, String podNumber, LocalDate podDate, String podDocumentPath);
    List<DispatchResponseDTO> getDispatchesByOrder(Long orderId);
    
    // ========== Receipt Operations ==========
    VendorReceiptResponseDTO createReceipt(VendorReceiptDTO receiptDTO);
    VendorReceiptResponseDTO getReceiptById(Long id);
    VendorReceiptResponseDTO acknowledgeReceipt(Long id, String acknowledgmentNumber);
    List<VendorReceiptResponseDTO> getReceiptsByOrder(Long orderId);
    
    // ========== Settlement Operations ==========
    SettlementResponseDTO createSettlement(SettlementDTO settlementDTO);
    SettlementResponseDTO getSettlementById(Long id);
    SettlementResponseDTO processSettlement(Long id);
    SettlementResponseDTO completeSettlement(Long id);
    List<SettlementResponseDTO> getSettlementsByOrder(Long orderId);
    
    // ========== Statistics ==========
    long getCountByStatus(String status);
    long getCountByRequestStatus(String status);
    Map<String, Long> getStatusCounts();
    Map<String, Long> getRequestStatusCounts();
}