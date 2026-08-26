package com.warehouse.wms.service.impl;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import com.warehouse.wms.dto.request.DeliveryChallanItemRequest;
import com.warehouse.wms.dto.request.DeliveryChallanRequest;
import com.warehouse.wms.dto.response.DeliveryChallanItemResponse;
import com.warehouse.wms.dto.response.DeliveryChallanResponse;
import com.warehouse.wms.dto.response.DeliveryChallanSummaryResponse;
import com.warehouse.wms.entity.DeliveryChallan;
import com.warehouse.wms.entity.DeliveryChallanItem;
import com.warehouse.wms.entity.PackageInfo;
import com.warehouse.wms.entity.SalesOrder;
import com.warehouse.wms.exception.BusinessException;
import com.warehouse.wms.exception.ResourceNotFoundException;
import com.warehouse.wms.repository.DeliveryChallanItemRepository;
import com.warehouse.wms.repository.DeliveryChallanRepository;
import com.warehouse.wms.repository.PackageInfoRepository;
import com.warehouse.wms.repository.SalesOrderRepository;
import com.warehouse.wms.service.DeliveryChallanService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DeliveryChallanServiceImpl implements DeliveryChallanService {

    private final DeliveryChallanRepository deliveryChallanRepository;
    private final DeliveryChallanItemRepository deliveryChallanItemRepository;
    private final PackageInfoRepository packageInfoRepository;
    private final SalesOrderRepository salesOrderRepository;

    // ====== CREATE DELIVERY CHALLAN ======

    @Override
    public DeliveryChallanResponse createDeliveryChallan(DeliveryChallanRequest request) {
        log.info("Creating Delivery Challan for SO: {}", request.getSoNumber());

        // Verify Package exists
        PackageInfo packageInfo = packageInfoRepository.findByPackageNumber(request.getPackageNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Package not found: " + request.getPackageNumber()));

        // Verify Sales Order exists
        SalesOrder salesOrder = salesOrderRepository.findBySoNumber(request.getSoNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Sales Order not found: " + request.getSoNumber()));

     

        // Generate challan number
        String challanNumber = generateChallanNumber();

        // Calculate totals
        int totalItems = request.getItems().size();
        int totalQuantity = request.getItems().stream().mapToInt(i -> i.getDispatchedQuantity() != null ? i.getDispatchedQuantity() : 0).sum();
        double totalWeight = request.getItems().stream().mapToDouble(i -> i.getWeight() != null ? i.getWeight() : 0).sum();
        double totalVolume = request.getItems().stream().mapToDouble(i -> i.getVolume() != null ? i.getVolume() : 0).sum();

        // Create Delivery Challan
        DeliveryChallan challan = DeliveryChallan.builder()
                .challanNumber(challanNumber)
                .soNumber(request.getSoNumber())
                .packageNumber(request.getPackageNumber())
                .shipmentNumber(request.getShipmentNumber())
                .customerCode(request.getCustomerCode() != null ? request.getCustomerCode() : salesOrder.getCustomerCode())
                .customerName(request.getCustomerName() != null ? request.getCustomerName() : salesOrder.getCustomerName())
                .customerAddress(request.getCustomerAddress() != null ? request.getCustomerAddress() : salesOrder.getDeliveryAddress())
                .customerGst(request.getCustomerGst())
                .customerPhone(request.getCustomerPhone())
                .invoiceNumber(request.getInvoiceNumber())
                .orderDate(request.getOrderDate() != null ? request.getOrderDate() : salesOrder.getOrderDate())
                .dispatchDate(request.getDispatchDate() != null ? request.getDispatchDate() : LocalDateTime.now())
                .expectedDeliveryDate(request.getExpectedDeliveryDate())
                .transporter(request.getTransporter())
                .vehicleNumber(request.getVehicleNumber())
                .driverName(request.getDriverName())
                .driverPhone(request.getDriverPhone())
                .totalItems(totalItems)
                .totalQuantity(totalQuantity)
                .totalWeight(totalWeight)
                .totalVolume(totalVolume)
                .status("CREATED")
                .remarks(request.getRemarks())
                .createdBy(request.getCreatedBy() != null ? request.getCreatedBy() : "SYSTEM")
                .build();

        DeliveryChallan savedChallan = deliveryChallanRepository.save(challan);

        // Create Challan Items
        List<DeliveryChallanItem> items = new ArrayList<>();
        for (DeliveryChallanItemRequest itemReq : request.getItems()) {
            DeliveryChallanItem item = DeliveryChallanItem.builder()
                    .challanNumber(challanNumber)
                    .itemCode(itemReq.getItemCode())
                    .itemName(itemReq.getItemName())
                    .uom(itemReq.getUom() != null ? itemReq.getUom() : "EA")
                    .orderedQuantity(itemReq.getOrderedQuantity() != null ? itemReq.getOrderedQuantity() : 0)
                    .dispatchedQuantity(itemReq.getDispatchedQuantity() != null ? itemReq.getDispatchedQuantity() : 0)
                    .deliveredQuantity(itemReq.getDeliveredQuantity() != null ? itemReq.getDeliveredQuantity() : 0)
                    .shortQuantity(itemReq.getShortQuantity() != null ? itemReq.getShortQuantity() : 0)
                    .batchNumber(itemReq.getBatchNumber())
                    .serialNumbers(itemReq.getSerialNumbers())
                    .unitPrice(itemReq.getUnitPrice() != null ? itemReq.getUnitPrice() : 0.0)
                    .totalPrice(itemReq.getTotalPrice() != null ? itemReq.getTotalPrice() : 0.0)
                    .weight(itemReq.getWeight() != null ? itemReq.getWeight() : 0.0)
                    .volume(itemReq.getVolume() != null ? itemReq.getVolume() : 0.0)
                    .status("PENDING")
                    .remarks(itemReq.getRemarks())
                    .deliveryChallan(savedChallan)
                    .build();
            items.add(deliveryChallanItemRepository.save(item));
        }

        log.info("Delivery Challan created: {}", challanNumber);
        return buildDeliveryChallanResponse(savedChallan, items);
    }

    // ====== GET DELIVERY CHALLAN BY NUMBER ======

    @Override
    public DeliveryChallanResponse getDeliveryChallanByNumber(String challanNumber) {
        DeliveryChallan challan = deliveryChallanRepository.findByChallanNumber(challanNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery Challan not found: " + challanNumber));

        List<DeliveryChallanItem> items = deliveryChallanItemRepository.findByChallanNumber(challanNumber);
        return buildDeliveryChallanResponse(challan, items);
    }

    // ====== GET ALL DELIVERY CHALLANS ======

    @Override
    public Page<DeliveryChallanResponse> getAllDeliveryChallans(Pageable pageable) {
        return deliveryChallanRepository.findAll(pageable)
                .map(challan -> buildDeliveryChallanResponse(challan,
                        deliveryChallanItemRepository.findByChallanNumber(challan.getChallanNumber())));
    }

    // ====== GET ALL WITH FILTERS ======

    @Override
    public Page<DeliveryChallanResponse> getAllDeliveryChallansWithFilters(
            String challanNumber,
            String soNumber,
            String packageNumber,
            String shipmentNumber,
            String customerCode,
            String customerName,
            String status,
            String transporter,
            String vehicleNumber,
            LocalDateTime startDate,
            LocalDateTime endDate,
            LocalDateTime startDispatchDate,
            LocalDateTime endDispatchDate,
            Pageable pageable) {

        log.info("Fetching delivery challans with filters");

        Page<DeliveryChallan> challanPage = deliveryChallanRepository.findByFilters(
                challanNumber, soNumber, packageNumber, shipmentNumber,
                customerCode, customerName, status, transporter, vehicleNumber,
                startDate, endDate, startDispatchDate, endDispatchDate, pageable);

        return challanPage.map(challan -> buildDeliveryChallanResponse(challan,
                deliveryChallanItemRepository.findByChallanNumber(challan.getChallanNumber())));
    }

    // ====== SEARCH DELIVERY CHALLANS ======

    @Override
    public Page<DeliveryChallanResponse> searchDeliveryChallans(String search, Pageable pageable) {
        log.info("Searching delivery challans with keyword: {}", search);
        return deliveryChallanRepository.searchDeliveryChallans(search, pageable)
                .map(challan -> buildDeliveryChallanResponse(challan,
                        deliveryChallanItemRepository.findByChallanNumber(challan.getChallanNumber())));
    }

    // ====== GET BY SO NUMBER ======

    @Override
    public List<DeliveryChallanResponse> getDeliveryChallansBySoNumber(String soNumber) {
        List<DeliveryChallan> challans = deliveryChallanRepository.findBySoNumber(soNumber);
        return challans.stream()
                .map(challan -> buildDeliveryChallanResponse(challan,
                        deliveryChallanItemRepository.findByChallanNumber(challan.getChallanNumber())))
                .collect(Collectors.toList());
    }

    // ====== GET BY STATUS ======

    @Override
    public List<DeliveryChallanResponse> getDeliveryChallansByStatus(String status) {
        List<DeliveryChallan> challans = deliveryChallanRepository.findByStatus(status);
        return challans.stream()
                .map(challan -> buildDeliveryChallanResponse(challan,
                        deliveryChallanItemRepository.findByChallanNumber(challan.getChallanNumber())))
                .collect(Collectors.toList());
    }

    // ====== UPDATE STATUS ======

    @Override
    public DeliveryChallanResponse updateDeliveryChallanStatus(String challanNumber, String status) {
        log.info("Updating Delivery Challan status: {} to {}", challanNumber, status);

        DeliveryChallan challan = deliveryChallanRepository.findByChallanNumber(challanNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery Challan not found: " + challanNumber));

        validateStatusTransition(challan.getStatus(), status);

        challan.setStatus(status);
        challan.setUpdatedBy("SYSTEM");
        challan.setUpdatedAt(LocalDateTime.now());

        DeliveryChallan updated = deliveryChallanRepository.save(challan);

        // If status is DELIVERED, update items
        if ("DELIVERED".equals(status)) {
            List<DeliveryChallanItem> items = deliveryChallanItemRepository.findByChallanNumber(challanNumber);
            for (DeliveryChallanItem item : items) {
                item.setDeliveredQuantity(item.getDispatchedQuantity());
                item.setStatus("DELIVERED");
                deliveryChallanItemRepository.save(item);
            }
        }

        log.info("Delivery Challan status updated: {}", challanNumber);
        return buildDeliveryChallanResponse(updated,
                deliveryChallanItemRepository.findByChallanNumber(challanNumber));
    }

    // ====== PRINT DELIVERY CHALLAN ======

    @Override
    public DeliveryChallanResponse printDeliveryChallan(String challanNumber) {
        log.info("Printing Delivery Challan: {}", challanNumber);

        DeliveryChallan challan = deliveryChallanRepository.findByChallanNumber(challanNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery Challan not found: " + challanNumber));

        if (!challan.getStatus().equals("CREATED") && !challan.getStatus().equals("PRINTED")) {
            throw new BusinessException("Cannot print challan in status: " + challan.getStatus());
        }

        challan.setStatus("PRINTED");
        challan.setUpdatedBy("SYSTEM");
        challan.setUpdatedAt(LocalDateTime.now());

        DeliveryChallan updated = deliveryChallanRepository.save(challan);

        log.info("Delivery Challan printed: {}", challanNumber);
        return buildDeliveryChallanResponse(updated,
                deliveryChallanItemRepository.findByChallanNumber(challanNumber));
    }

    // ====== MARK AS DELIVERED ======

    @Override
    public DeliveryChallanResponse markAsDelivered(String challanNumber) {
        log.info("Marking Delivery Challan as delivered: {}", challanNumber);
        return updateDeliveryChallanStatus(challanNumber, "DELIVERED");
    }

    // ====== CANCEL DELIVERY CHALLAN ======

    @Override
    public DeliveryChallanResponse cancelDeliveryChallan(String challanNumber) {
        log.info("Cancelling Delivery Challan: {}", challanNumber);

        DeliveryChallan challan = deliveryChallanRepository.findByChallanNumber(challanNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery Challan not found: " + challanNumber));

        if (challan.getStatus().equals("DELIVERED")) {
            throw new BusinessException("Cannot cancel a delivered challan");
        }

        challan.setStatus("CANCELLED");
        challan.setUpdatedBy("SYSTEM");
        challan.setUpdatedAt(LocalDateTime.now());

        DeliveryChallan updated = deliveryChallanRepository.save(challan);

        log.info("Delivery Challan cancelled: {}", challanNumber);
        return buildDeliveryChallanResponse(updated,
                deliveryChallanItemRepository.findByChallanNumber(challanNumber));
    }

    // ====== GENERATE PDF ======

    

    // ====== GENERATE HTML ======


    // ====== GET SUMMARY ======

    

    // ====== DELETE DELIVERY CHALLAN ======

    @Override
    public void deleteDeliveryChallan(String challanNumber) {
        log.info("Deleting Delivery Challan: {}", challanNumber);

        DeliveryChallan challan = deliveryChallanRepository.findByChallanNumber(challanNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery Challan not found: " + challanNumber));

        if (!challan.getStatus().equals("CREATED") && !challan.getStatus().equals("CANCELLED")) {
            throw new BusinessException("Cannot delete challan in status: " + challan.getStatus());
        }

        List<DeliveryChallanItem> items = deliveryChallanItemRepository.findByChallanNumber(challanNumber);
        deliveryChallanItemRepository.deleteAll(items);

        deliveryChallanRepository.delete(challan);

        log.info("Delivery Challan deleted: {}", challanNumber);
    }

    // ====== HELPER METHODS ======

    private String generateChallanNumber() {
        return "DC-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) +
                "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    private void validateStatusTransition(String currentStatus, String newStatus) {
        if (currentStatus.equals(newStatus)) {
            return;
        }

        Map<String, List<String>> validTransitions = new HashMap<>();
        validTransitions.put("CREATED", List.of("PRINTED", "CANCELLED"));
        validTransitions.put("PRINTED", List.of("DISPATCHED", "CANCELLED"));
        validTransitions.put("DISPATCHED", List.of("DELIVERED", "CANCELLED"));
        validTransitions.put("DELIVERED", List.of());
        validTransitions.put("CANCELLED", List.of());

        List<String> allowed = validTransitions.get(currentStatus);
        if (allowed == null || !allowed.contains(newStatus)) {
            throw new BusinessException("Cannot transition from " + currentStatus + " to " + newStatus);
        }
    }

    private DeliveryChallanResponse buildDeliveryChallanResponse(DeliveryChallan challan, List<DeliveryChallanItem> items) {
        List<DeliveryChallanItemResponse> itemResponses = items.stream()
                .map(item -> DeliveryChallanItemResponse.builder()
                        .id(item.getId())
                        .itemCode(item.getItemCode())
                        .itemName(item.getItemName())
                        .uom(item.getUom())
                        .orderedQuantity(item.getOrderedQuantity())
                        .dispatchedQuantity(item.getDispatchedQuantity())
                        .deliveredQuantity(item.getDeliveredQuantity())
                        .shortQuantity(item.getShortQuantity())
                        .batchNumber(item.getBatchNumber())
                        .serialNumbers(item.getSerialNumbers())
                        .unitPrice(item.getUnitPrice())
                        .totalPrice(item.getTotalPrice())
                        .weight(item.getWeight())
                        .volume(item.getVolume())
                        .status(item.getStatus())
                        .remarks(item.getRemarks())
                        .build())
                .collect(Collectors.toList());

        return DeliveryChallanResponse.builder()
                .id(challan.getId())
                .challanNumber(challan.getChallanNumber())
                .soNumber(challan.getSoNumber())
                .packageNumber(challan.getPackageNumber())
                .shipmentNumber(challan.getShipmentNumber())
                .customerCode(challan.getCustomerCode())
                .customerName(challan.getCustomerName())
                .customerAddress(challan.getCustomerAddress())
                .customerGst(challan.getCustomerGst())
                .customerPhone(challan.getCustomerPhone())
                .invoiceNumber(challan.getInvoiceNumber())
                .orderDate(challan.getOrderDate())
                .dispatchDate(challan.getDispatchDate())
                .expectedDeliveryDate(challan.getExpectedDeliveryDate())
                .transporter(challan.getTransporter())
                .vehicleNumber(challan.getVehicleNumber())
                .driverName(challan.getDriverName())
                .driverPhone(challan.getDriverPhone())
                .totalItems(challan.getTotalItems())
                .totalQuantity(challan.getTotalQuantity())
                .totalWeight(challan.getTotalWeight())
                .totalVolume(challan.getTotalVolume())
                .status(challan.getStatus())
                .remarks(challan.getRemarks())
                .createdBy(challan.getCreatedBy())
                .createdAt(challan.getCreatedAt())
                .updatedAt(challan.getUpdatedAt())
                .items(itemResponses)
                .build();
    }

    // ====== PDF HELPER METHODS ======

    
}