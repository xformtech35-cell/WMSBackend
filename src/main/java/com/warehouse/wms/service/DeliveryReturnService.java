package com.warehouse.wms.service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.warehouse.wms.constant.ItemReturnStatus;
import com.warehouse.wms.constant.ReturnStatus;
import com.warehouse.wms.dto.request.CompleteReturnDto;
import com.warehouse.wms.dto.request.QcItemDto;
import com.warehouse.wms.dto.request.ReceiveItemDto;
import com.warehouse.wms.dto.request.ReturnApprovalDto;
import com.warehouse.wms.dto.request.ReturnFilterRequest;
import com.warehouse.wms.dto.request.ReturnItemDecisionDto;
import com.warehouse.wms.dto.request.ReturnItemRequestDto;
import com.warehouse.wms.dto.request.ReturnQcDto;
import com.warehouse.wms.dto.request.ReturnReceiveDto;
import com.warehouse.wms.dto.request.ReturnRequestDto;
import com.warehouse.wms.dto.response.ReturnItemResponseDto;
import com.warehouse.wms.dto.response.ReturnResponseDto;
import com.warehouse.wms.entity.Delivery;
import com.warehouse.wms.entity.DeliveryReturn;
import com.warehouse.wms.entity.DeliveryReturnItem;
import com.warehouse.wms.repository.DeliveryRepository;
import com.warehouse.wms.repository.DeliveryReturnRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryReturnService {

    private final DeliveryRepository deliveryRepository;
    private final DeliveryReturnRepository returnRepository;
    // private final InventoryService inventoryService;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    // ================================================================
    // 1. INITIATE MULTI-ITEM RETURN
    // ================================================================
    @Transactional
    public ReturnResponseDto initiateReturn(ReturnRequestDto req) {

        Delivery delivery = deliveryRepository.findById(req.getDeliveryId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Delivery not found: " + req.getDeliveryId()));

        if (!"DELIVERED".equalsIgnoreCase(delivery.getDeliveryStatus())
                && !"PARTIAL".equalsIgnoreCase(delivery.getDeliveryStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Return can only be initiated for DELIVERED/PARTIAL shipments");
        }

        // Prevent duplicate active return for same delivery
        List<String> activeStatuses = List.of(
                ReturnStatus.PENDING.name(),
                ReturnStatus.APPROVED.name(),
                ReturnStatus.IN_TRANSIT.name(),
                ReturnStatus.RECEIVED.name(),
                ReturnStatus.QC_PASSED.name(),
                ReturnStatus.QC_FAILED.name()
        );
        if (returnRepository.existsByDeliveryIdAndReturnStatusNotIn(
                delivery.getId(), activeStatuses)) {
            // ignore - allow additional returns after closure
        }
        boolean hasOpenReturn = returnRepository.findByDeliveryId(delivery.getId()).stream()
                .anyMatch(r -> activeStatuses.contains(r.getReturnStatus()));
        if (hasOpenReturn) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "An active return already exists for this delivery");
        }

        if (req.getItems() == null || req.getItems().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "At least one item is required");
        }

        DeliveryReturn ret = DeliveryReturn.builder()
                .deliveryId(delivery.getId())
                .deliveryNumber(delivery.getDeliveryNumber())
                .shipmentNumber(delivery.getShipmentNumber())
                .soNumber(delivery.getSoNumber())
                .customerCode(delivery.getCustomerCode())
                .customerName(delivery.getCustomerName())
                .returnNumber(generateReturnNumber())
                .returnStatus(ReturnStatus.PENDING.name())
                .returnReason(req.getReturnReason())
                .returnRequestedBy(req.getRequestedBy())
                .returnRequestedAt(LocalDateTime.now())
                .remarks(req.getRemarks())
                .build();

        for (ReturnItemRequestDto dto : req.getItems()) {
            if (dto.getReturnQty() == null || dto.getReturnQty() <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Return qty must be > 0 for item: " + dto.getItemCode());
            }
            DeliveryReturnItem item = DeliveryReturnItem.builder()
                    .deliveryReturn(ret)
                    .itemCode(dto.getItemCode())
                    .itemName(dto.getItemName())
                    .sku(dto.getSku())
                    .uom(dto.getUom())
                    .batchNumber(dto.getBatchNumber())
                    .deliveredQty(dto.getDeliveredQty() != null ? dto.getDeliveredQty() : 0)
                    .returnQty(dto.getReturnQty())
                    .returnReason(dto.getReturnReason() != null
                            ? dto.getReturnReason() : req.getReturnReason())
                    .remarks(dto.getRemarks())
                    .unitPrice(dto.getUnitPrice())
                    .itemReturnStatus(ItemReturnStatus.PENDING.name())
                    .qcStatus("PENDING")
                    .restocked(false)
                    .build();
            ret.getItems().add(item);
        }

        DeliveryReturn saved = returnRepository.save(ret);
        log.info("Return {} initiated for delivery {} ({} items)",
                saved.getReturnNumber(), delivery.getDeliveryNumber(), saved.getItems().size());

        return toResponse(saved);
    }

    // ================================================================
    // 2. APPROVE / REJECT (per-item)
    // ================================================================
    @Transactional
    public ReturnResponseDto approveReturn(ReturnApprovalDto req) {

        DeliveryReturn ret = findReturn(req.getReturnId());
        assertHeaderStatus(ret, ReturnStatus.PENDING);

        Map<Long, Boolean> decisions = new HashMap<>();
        if (req.getItems() != null) {
            for (ReturnItemDecisionDto d : req.getItems()) {
                decisions.put(d.getItemId(), d.getApproved());
            }
        }

        boolean anyApproved = false, anyRejected = false;

        for (DeliveryReturnItem item : ret.getItems()) {
            Boolean approved = decisions.getOrDefault(item.getId(), req.getApproved());
            if (Boolean.TRUE.equals(approved)) {
                item.setItemReturnStatus(ItemReturnStatus.APPROVED.name());
                anyApproved = true;
            } else {
                item.setItemReturnStatus(ItemReturnStatus.REJECTED.name());
                anyRejected = true;
            }
        }

        ret.setReturnStatus((!anyApproved && anyRejected)
                ? ReturnStatus.REJECTED.name()
                : ReturnStatus.APPROVED.name());
        ret.setApprovedBy(req.getApprovedBy());
        ret.setApprovedAt(LocalDateTime.now());
        if (req.getRemarks() != null) ret.setRemarks(req.getRemarks());

        DeliveryReturn saved = returnRepository.save(ret);
        log.info("Return {} -> {}", saved.getReturnNumber(), saved.getReturnStatus());
        return toResponse(saved);
    }

    // ================================================================
    // 3. RECEIVE
    // ================================================================
    @Transactional
    public ReturnResponseDto receiveReturn(ReturnReceiveDto req) {

        DeliveryReturn ret = findReturn(req.getReturnId());
        assertHeaderStatus(ret, ReturnStatus.APPROVED, ReturnStatus.IN_TRANSIT);

        Map<Long, Integer> receivedMap = new HashMap<>();
        if (req.getItems() != null) {
            for (ReceiveItemDto d : req.getItems()) {
                receivedMap.put(d.getItemId(), d.getReceivedQty());
            }
        }

        for (DeliveryReturnItem item : ret.getItems()) {
            if (!ItemReturnStatus.APPROVED.name().equals(item.getItemReturnStatus())) {
                continue; // skip rejected
            }
            Integer recvQty = receivedMap.get(item.getId());
            if (recvQty == null) recvQty = item.getReturnQty();
            if (recvQty > item.getReturnQty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Received qty cannot exceed return qty for " + item.getItemCode());
            }
            item.setReceivedQty(recvQty);
            item.setItemReturnStatus(ItemReturnStatus.RECEIVED.name());
            item.setQcStatus("PENDING");
        }

        ret.setReturnStatus(ReturnStatus.RECEIVED.name());
        ret.setReturnReceivedAt(LocalDateTime.now());
        ret.setReturnReceivedBy(req.getReceivedBy());
        if (req.getRemarks() != null) ret.setRemarks(req.getRemarks());

        DeliveryReturn saved = returnRepository.save(ret);
        log.info("Return {} received by {}", saved.getReturnNumber(), req.getReceivedBy());
        return toResponse(saved);
    }

    // ================================================================
    // 4. QUALITY CHECK (per-item)
    // ================================================================
    @Transactional
    public ReturnResponseDto runQualityCheck(ReturnQcDto req) {

        DeliveryReturn ret = findReturn(req.getReturnId());
        assertHeaderStatus(ret, ReturnStatus.RECEIVED);

        if (req.getItems() == null || req.getItems().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "QC items required");
        }

        Map<Long, QcItemDto> qcMap = req.getItems().stream()
                .collect(Collectors.toMap(QcItemDto::getItemId, d -> d));

        boolean allPassed = true, allFailed = true;

        for (DeliveryReturnItem item : ret.getItems()) {
            if (!ItemReturnStatus.RECEIVED.name().equals(item.getItemReturnStatus())) {
                continue;
            }
            QcItemDto qc = qcMap.get(item.getId());
            if (qc == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "QC decision missing for item: " + item.getItemCode());
            }

            int received = item.getReceivedQty() == null ? 0 : item.getReceivedQty();
            int accepted = qc.getAcceptedQty() != null ? qc.getAcceptedQty()
                    : (Boolean.TRUE.equals(qc.getPassed()) ? received : 0);
            int rejected = qc.getRejectedQty() != null ? qc.getRejectedQty()
                    : received - accepted;

            if (accepted + rejected != received) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Accepted + rejected must equal received for " + item.getItemCode());
            }

            item.setAcceptedQty(accepted);
            item.setRejectedQty(rejected);
            item.setQcStatus(Boolean.TRUE.equals(qc.getPassed()) ? "PASSED" : "FAILED");
            item.setQcRemarks(qc.getQcRemarks());
            item.setQcCheckedBy(req.getCheckedBy());
            item.setQcCheckedAt(LocalDateTime.now());

            if (Boolean.TRUE.equals(qc.getPassed())) {
                allFailed = false;
                item.setItemReturnStatus(ItemReturnStatus.QC_PASSED.name());

                if (Boolean.TRUE.equals(qc.getRestock())) {
                    // inventoryService.increaseStock(item.getItemCode(), accepted);
                    item.setRestocked(true);
                    item.setItemReturnStatus(ItemReturnStatus.RESTOCKED.name());
                }
                if (rejected > 0) {
                    // inventoryService.markScrap(item.getItemCode(), rejected);
                }
            } else {
                allPassed = false;
                item.setItemReturnStatus(ItemReturnStatus.QC_FAILED.name());
            }
        }

        if (allPassed) {
            ret.setReturnStatus(ReturnStatus.QC_PASSED.name());
        } else if (allFailed) {
            ret.setReturnStatus(ReturnStatus.QC_FAILED.name());
        } else {
            ret.setReturnStatus(ReturnStatus.QC_PASSED.name()); // mixed
        }

        DeliveryReturn saved = returnRepository.save(ret);
        log.info("QC done for return {}: {}", saved.getReturnNumber(), saved.getReturnStatus());
        return toResponse(saved);
    }

    // ================================================================
    // 5. COMPLETE
    // ================================================================
    @Transactional
    public ReturnResponseDto completeReturn(Long returnId, CompleteReturnDto req) {

        DeliveryReturn ret = findReturn(returnId);
        assertHeaderStatus(ret, ReturnStatus.QC_PASSED, ReturnStatus.QC_FAILED);

        ret.setReturnStatus(ReturnStatus.COMPLETED.name());
        ret.setCompletedAt(LocalDateTime.now());
        if (req != null) {
            if (req.getRemarks() != null) ret.setRemarks(req.getRemarks());
            if (req.getRefundAmount() != null) ret.setRefundAmount(req.getRefundAmount());
        }

        DeliveryReturn saved = returnRepository.save(ret);
        log.info("Return completed: {}", saved.getReturnNumber());
        return toResponse(saved);
    }

    // ================================================================
    // 6. CANCEL
    // ================================================================
    @Transactional
    public ReturnResponseDto cancelReturn(Long returnId) {

        DeliveryReturn ret = findReturn(returnId);
        assertHeaderStatus(ret, ReturnStatus.PENDING);

        ret.setReturnStatus(ReturnStatus.CANCELLED.name());
        for (DeliveryReturnItem item : ret.getItems()) {
            item.setItemReturnStatus(ItemReturnStatus.REJECTED.name());
        }

        DeliveryReturn saved = returnRepository.save(ret);
        log.info("Return cancelled: {}", saved.getReturnNumber());
        return toResponse(saved);
    }

    // ================================================================
    // 7. GET
    // ================================================================
    @Transactional(readOnly = true)
    public ReturnResponseDto getById(Long returnId) {
        return toResponse(findReturn(returnId));
    }

    @Transactional(readOnly = true)
    public List<ReturnResponseDto> listByDelivery(Long deliveryId) {
        return returnRepository.findByDeliveryId(deliveryId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ReturnResponseDto> listByStatus(String status) {
        return returnRepository.findByReturnStatus(status)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    // ================================================================
    // HELPERS
    // ================================================================
    private DeliveryReturn findReturn(Long id) {
        return returnRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Return not found: " + id));
    }

    private void assertHeaderStatus(DeliveryReturn r, ReturnStatus... allowed) {
        for (ReturnStatus s : allowed) {
            if (s.name().equals(r.getReturnStatus())) return;
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Invalid return state. Current: " + r.getReturnStatus());
    }

    private String generateReturnNumber() {
        String candidate;
        do {
            candidate = "RET-" + LocalDateTime.now().format(FMT)
                    + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        } while (returnRepository.existsByReturnNumber(candidate));
        return candidate;
    }

    private ReturnResponseDto toResponse(DeliveryReturn r) {
        List<ReturnItemResponseDto> itemDtos = r.getItems() == null ? List.of()
                : r.getItems().stream().map(i -> ReturnItemResponseDto.builder()
                        .itemId(i.getId())
                        .itemCode(i.getItemCode())
                        .itemName(i.getItemName())
                        .sku(i.getSku())
                        .uom(i.getUom())
                        .batchNumber(i.getBatchNumber())
                        .deliveredQty(i.getDeliveredQty())
                        .returnQty(i.getReturnQty())
                        .receivedQty(i.getReceivedQty())
                        .acceptedQty(i.getAcceptedQty())
                        .rejectedQty(i.getRejectedQty())
                        .itemReturnStatus(i.getItemReturnStatus())
                        .returnReason(i.getReturnReason())
                        .qcStatus(i.getQcStatus())
                        .qcRemarks(i.getQcRemarks())
                        .qcCheckedBy(i.getQcCheckedBy())
                        .qcCheckedAt(i.getQcCheckedAt())
                        .restocked(i.getRestocked())
                        .unitPrice(i.getUnitPrice())
                        .refundAmount(i.getRefundAmount())
                        .remarks(i.getRemarks())
                        .createdAt(i.getCreatedAt())
                        .updatedAt(i.getUpdatedAt())
                        .build()).collect(Collectors.toList());

        int totalReturn = itemDtos.stream().mapToInt(i -> nz(i.getReturnQty())).sum();
        int totalRecv   = itemDtos.stream().mapToInt(i -> nz(i.getReceivedQty())).sum();
        int totalAcc    = itemDtos.stream().mapToInt(i -> nz(i.getAcceptedQty())).sum();
        int totalRej    = itemDtos.stream().mapToInt(i -> nz(i.getRejectedQty())).sum();

        return ReturnResponseDto.builder()
                .returnId(r.getId())
                .returnNumber(r.getReturnNumber())
                .deliveryId(r.getDeliveryId())
                .deliveryNumber(r.getDeliveryNumber())
                .shipmentNumber(r.getShipmentNumber())
                .soNumber(r.getSoNumber())
                .customerCode(r.getCustomerCode())
                .customerName(r.getCustomerName())
                .returnStatus(r.getReturnStatus())
                .returnReason(r.getReturnReason())
                .returnRequestedBy(r.getReturnRequestedBy())
                .returnRequestedAt(r.getReturnRequestedAt())
                .approvedBy(r.getApprovedBy())
                .approvedAt(r.getApprovedAt())
                .returnReceivedBy(r.getReturnReceivedBy())
                .returnReceivedAt(r.getReturnReceivedAt())
                .completedAt(r.getCompletedAt())
                .refundAmount(r.getRefundAmount())
                .remarks(r.getRemarks())
                .totalReturnQty(totalReturn)
                .totalReceivedQty(totalRecv)
                .totalAcceptedQty(totalAcc)
                .totalRejectedQty(totalRej)
                .items(itemDtos)
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt())
                .build();
    }

    private int nz(Integer v) { return v == null ? 0 : v; }
    
    
    
    
    
    
    
    
    
    
    
    
    
   @Transactional(readOnly = true)
public Page<ReturnResponseDto> search(ReturnFilterRequest filter, Pageable pageable) {

    String search           = normalize(filter.getSearch());
    String returnStatus     = normalize(filter.getReturnStatus());
    String itemReturnStatus = normalize(filter.getItemReturnStatus());
    String deliveryNumber   = normalize(filter.getDeliveryNumber());
    String soNumber         = normalize(filter.getSoNumber());
    String shipmentNumber   = normalize(filter.getShipmentNumber());
    String customerCode     = normalize(filter.getCustomerCode());
    String customerName     = normalize(filter.getCustomerName());
    String itemCode         = normalize(filter.getItemCode());
    String requestedBy      = normalize(filter.getRequestedBy());
    String approvedBy       = normalize(filter.getApprovedBy());
    String receivedBy       = normalize(filter.getReceivedBy());

    LocalDateTime fromDate = filter.getFromDate() == null
            ? null : filter.getFromDate().atStartOfDay();
    LocalDateTime toDate = filter.getToDate() == null
            ? null : filter.getToDate().atTime(LocalTime.MAX);

    // Default sort if client didn't specify one
    if (pageable.getSort().isUnsorted()) {
        pageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "returnRequestedAt"));
    }

    Page<DeliveryReturn> page = returnRepository.searchReturns(
            search,
            returnStatus,
            itemReturnStatus,
            filter.getDeliveryId(),
            deliveryNumber,
            soNumber,
            shipmentNumber,
            customerCode,
            customerName,
            itemCode,
            requestedBy,
            approvedBy,
            receivedBy,
            fromDate,
            toDate,
            pageable
    );

    return page.map(this::toResponse);
}



    private String normalize(String v) {
        return (v == null || v.isBlank()) ? null : v.trim();
    }
}