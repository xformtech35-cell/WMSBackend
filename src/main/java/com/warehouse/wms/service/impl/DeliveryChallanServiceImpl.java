package com.warehouse.wms.service.impl;

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

import com.warehouse.wms.dto.request.DeliveryChallanRequest;
import com.warehouse.wms.dto.request.PackageRequests;
import com.warehouse.wms.dto.response.DeliveryChallanResponse;
import com.warehouse.wms.dto.response.DeliveryChallanSummaryResponse;
import com.warehouse.wms.dto.response.PackageResponses;
import com.warehouse.wms.entity.DeliveryChallan;
import com.warehouse.wms.entity.DeliveryChallanPackage;
import com.warehouse.wms.entity.PackageInfo;
import com.warehouse.wms.entity.SalesOrder;
import com.warehouse.wms.exception.BusinessException;
import com.warehouse.wms.exception.ResourceNotFoundException;
import com.warehouse.wms.repository.DeliveryChallanPackageRepository;
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
    private final DeliveryChallanPackageRepository deliveryChallanPackageRepository;
    private final PackageInfoRepository packageInfoRepository;
    private final SalesOrderRepository salesOrderRepository;

    // ====== CREATE DELIVERY CHALLAN ======

@Override
public DeliveryChallanResponse createDeliveryChallan(DeliveryChallanRequest request) {
    log.info("Creating Delivery Challan");

    // Generate challan number
    String challanNumber = generateChallanNumber();

    // Calculate totals from packages
    int totalPackages = request.getPackages().size();
    int totalQuantity = request.getPackages().stream()
            .mapToInt(p -> p.getDispatchedQuantity() != null ? p.getDispatchedQuantity() : 0)
            .sum();
    double totalWeight = request.getPackages().stream()
            .mapToDouble(p -> p.getWeight() != null ? p.getWeight() : 0)
            .sum();
    double totalVolume = request.getPackages().stream()
            .mapToDouble(p -> p.getVolume() != null ? p.getVolume() : 0)
            .sum();

    // Get SO Number from first package (all packages should have same SO)
    String soNumber = request.getPackages().stream()
            .findFirst()
            .map(PackageRequests::getSoNumber)
            .orElseThrow(() -> new BusinessException("SO Number is required in packages"));

    // Create Delivery Challan
    DeliveryChallan challan = DeliveryChallan.builder()
            .challanNumber(challanNumber)
            .soNumber(soNumber)
            .shipmentNumber(request.getShipmentNumber())
            .transporter(request.getTransporter())
            .vehicleNumber(request.getVehicleNumber())
            .driverName(request.getDriverName())
            .driverPhone(request.getDriverPhone())
            .totalPackages(totalPackages)
            .totalQuantity(totalQuantity)
            .totalWeight(totalWeight)
            .totalVolume(totalVolume)
            .status("CREATED")
            .remarks(request.getRemarks())
            .createdBy(request.getCreatedBy() != null ? request.getCreatedBy() : "SYSTEM")
            .build();

    DeliveryChallan savedChallan = deliveryChallanRepository.save(challan);

    // Create Packages
    List<DeliveryChallanPackage> packages = new ArrayList<>();
    for (PackageRequests pkgReq : request.getPackages()) {
        // Verify package exists
        PackageInfo packageInfo = packageInfoRepository.findByPackageNumber(pkgReq.getPackageNumber())
                .orElse(null);

        DeliveryChallanPackage pkg = DeliveryChallanPackage.builder()
                .challanNumber(challanNumber)
                .soNumber(pkgReq.getSoNumber() != null ? pkgReq.getSoNumber() : soNumber)
                .packageNumber(pkgReq.getPackageNumber())
                .packageBarcode(pkgReq.getPackageBarcode() != null ? pkgReq.getPackageBarcode() : 
                        packageInfo != null ? packageInfo.getPackageBarcode() : null)
                .customerCode(pkgReq.getCustomerCode())
                .customerName(pkgReq.getCustomerName())
                .customerAddress(pkgReq.getCustomerAddress())
                .customerGst(pkgReq.getCustomerGst())
                .customerPhone(pkgReq.getCustomerPhone())
                .invoiceNumber(pkgReq.getInvoiceNumber())
                .orderDate(pkgReq.getOrderDate())
                .dispatchDate(pkgReq.getDispatchDate() != null ? pkgReq.getDispatchDate() : LocalDateTime.now())
                .expectedDeliveryDate(pkgReq.getExpectedDeliveryDate())
                .itemCode(pkgReq.getItemCode())
                .itemName(pkgReq.getItemName())
                .uom(pkgReq.getUom() != null ? pkgReq.getUom() : "EA")
                .orderedQuantity(pkgReq.getOrderedQuantity() != null ? pkgReq.getOrderedQuantity() : 0)
                .dispatchedQuantity(pkgReq.getDispatchedQuantity() != null ? pkgReq.getDispatchedQuantity() : 0)
                .deliveredQuantity(pkgReq.getDeliveredQuantity() != null ? pkgReq.getDeliveredQuantity() : 0)
                .shortQuantity(pkgReq.getShortQuantity() != null ? pkgReq.getShortQuantity() : 0)
                .batchNumber(pkgReq.getBatchNumber())
                .serialNumbers(pkgReq.getSerialNumbers())
                .unitPrice(pkgReq.getUnitPrice() != null ? pkgReq.getUnitPrice() : 0.0)
                .totalPrice(pkgReq.getTotalPrice() != null ? pkgReq.getTotalPrice() : 0.0)
                .weight(pkgReq.getWeight() != null ? pkgReq.getWeight() : 0.0)
                .volume(pkgReq.getVolume() != null ? pkgReq.getVolume() : 0.0)
                .status("PENDING")
                .remarks(pkgReq.getRemarks())
                .deliveryChallan(savedChallan)
                .build();
        packages.add(deliveryChallanPackageRepository.save(pkg));
    }

    log.info("Delivery Challan created: {}", challanNumber);
    return buildDeliveryChallanResponse(savedChallan, packages);
}


@Override
public DeliveryChallanResponse updateDeliveryChallan(String challanNumber, DeliveryChallanRequest request) {
    log.info("Updating Delivery Challan: {}", challanNumber);

    // Check if challan exists
    DeliveryChallan existingChallan = deliveryChallanRepository.findByChallanNumber(challanNumber)
            .orElseThrow(() -> new ResourceNotFoundException("Delivery Challan not found: " + challanNumber));

    // Check if challan can be updated (only CREATED or PRINTED status allowed)
    if (!existingChallan.getStatus().equals("CREATED") && !existingChallan.getStatus().equals("PRINTED")) {
        throw new BusinessException("Cannot update challan in status: " + existingChallan.getStatus());
    }

    // Validate packages exist
    if (request.getPackages() == null || request.getPackages().isEmpty()) {
        throw new BusinessException("At least one package is required");
    }

    // Get SO Number from first package
    String soNumber = request.getPackages().stream()
            .findFirst()
            .map(PackageRequests::getSoNumber)
            .orElse(existingChallan.getSoNumber());

    // Calculate totals
    int totalPackages = request.getPackages().size();
    int totalQuantity = request.getPackages().stream()
            .mapToInt(p -> p.getDispatchedQuantity() != null ? p.getDispatchedQuantity() : 0)
            .sum();
    double totalWeight = request.getPackages().stream()
            .mapToDouble(p -> p.getWeight() != null ? p.getWeight() : 0)
            .sum();
    double totalVolume = request.getPackages().stream()
            .mapToDouble(p -> p.getVolume() != null ? p.getVolume() : 0)
            .sum();

    // Update Delivery Challan
    existingChallan.setSoNumber(soNumber);
    existingChallan.setShipmentNumber(request.getShipmentNumber());
    existingChallan.setTransporter(request.getTransporter());
    existingChallan.setVehicleNumber(request.getVehicleNumber());
    existingChallan.setDriverName(request.getDriverName());
    existingChallan.setDriverPhone(request.getDriverPhone());
    existingChallan.setTotalPackages(totalPackages);
    existingChallan.setTotalQuantity(totalQuantity);
    existingChallan.setTotalWeight(totalWeight);
    existingChallan.setTotalVolume(totalVolume);
    existingChallan.setRemarks(request.getRemarks());
    existingChallan.setUpdatedBy(request.getCreatedBy() != null ? request.getCreatedBy() : "SYSTEM");
    existingChallan.setUpdatedAt(LocalDateTime.now());

    DeliveryChallan updatedChallan = deliveryChallanRepository.save(existingChallan);

    // Delete existing packages
    List<DeliveryChallanPackage> existingPackages = deliveryChallanPackageRepository.findByChallanNumber(challanNumber);
    deliveryChallanPackageRepository.deleteAll(existingPackages);

    // Create new packages
    List<DeliveryChallanPackage> packages = new ArrayList<>();
    for (PackageRequests pkgReq : request.getPackages()) {
        // Verify package exists
        PackageInfo packageInfo = packageInfoRepository.findByPackageNumber(pkgReq.getPackageNumber())
                .orElse(null);

        String packageSoNumber = pkgReq.getSoNumber() != null ? pkgReq.getSoNumber() : soNumber;

        DeliveryChallanPackage pkg = DeliveryChallanPackage.builder()
                .challanNumber(challanNumber)
                .soNumber(packageSoNumber)
                .packageNumber(pkgReq.getPackageNumber())
                .packageBarcode(pkgReq.getPackageBarcode() != null ? pkgReq.getPackageBarcode() : 
                        packageInfo != null ? packageInfo.getPackageBarcode() : null)
                .customerCode(pkgReq.getCustomerCode())
                .customerName(pkgReq.getCustomerName())
                .customerAddress(pkgReq.getCustomerAddress())
                .customerGst(pkgReq.getCustomerGst())
                .customerPhone(pkgReq.getCustomerPhone())
                .invoiceNumber(pkgReq.getInvoiceNumber())
                .orderDate(pkgReq.getOrderDate())
                .dispatchDate(pkgReq.getDispatchDate() != null ? pkgReq.getDispatchDate() : LocalDateTime.now())
                .expectedDeliveryDate(pkgReq.getExpectedDeliveryDate())
                .itemCode(pkgReq.getItemCode())
                .itemName(pkgReq.getItemName())
                .uom(pkgReq.getUom() != null ? pkgReq.getUom() : "EA")
                .orderedQuantity(pkgReq.getOrderedQuantity() != null ? pkgReq.getOrderedQuantity() : 0)
                .dispatchedQuantity(pkgReq.getDispatchedQuantity() != null ? pkgReq.getDispatchedQuantity() : 0)
                .deliveredQuantity(pkgReq.getDeliveredQuantity() != null ? pkgReq.getDeliveredQuantity() : 0)
                .shortQuantity(pkgReq.getShortQuantity() != null ? pkgReq.getShortQuantity() : 0)
                .batchNumber(pkgReq.getBatchNumber())
                .serialNumbers(pkgReq.getSerialNumbers())
                .unitPrice(pkgReq.getUnitPrice() != null ? pkgReq.getUnitPrice() : 0.0)
                .totalPrice(pkgReq.getTotalPrice() != null ? pkgReq.getTotalPrice() : 0.0)
                .weight(pkgReq.getWeight() != null ? pkgReq.getWeight() : 0.0)
                .volume(pkgReq.getVolume() != null ? pkgReq.getVolume() : 0.0)
                .status("PENDING")
                .remarks(pkgReq.getRemarks())
                .deliveryChallan(updatedChallan)
                .build();
        packages.add(deliveryChallanPackageRepository.save(pkg));
    }

    log.info("Delivery Challan updated: {}", challanNumber);
    return buildDeliveryChallanResponse(updatedChallan, packages);
}




    // ====== GET DELIVERY CHALLAN BY NUMBER ======

    @Override
    public DeliveryChallanResponse getDeliveryChallanByNumber(String challanNumber) {
        DeliveryChallan challan = deliveryChallanRepository.findByChallanNumber(challanNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery Challan not found: " + challanNumber));

        List<DeliveryChallanPackage> packages = deliveryChallanPackageRepository.findByChallanNumber(challanNumber);
        return buildDeliveryChallanResponse(challan, packages);
    }

    // ====== GET ALL DELIVERY CHALLANS ======

    @Override
    public Page<DeliveryChallanResponse> getAllDeliveryChallans(Pageable pageable) {
        return deliveryChallanRepository.findAll(pageable)
                .map(challan -> buildDeliveryChallanResponse(challan,
                        deliveryChallanPackageRepository.findByChallanNumber(challan.getChallanNumber())));
    }

    // ====== GET ALL WITH FILTERS ======

    @Override
    public Page<DeliveryChallanResponse> getAllDeliveryChallansWithFilters(
            String challanNumber,
            String shipmentNumber,
            String transporter,
            String vehicleNumber,
            String status,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable) {

        log.info("Fetching delivery challans with filters");

        Page<DeliveryChallan> challanPage = deliveryChallanRepository.findByFilters(
                challanNumber, shipmentNumber, transporter,
                vehicleNumber, status, startDate, endDate, pageable);

        return challanPage.map(challan -> buildDeliveryChallanResponse(challan,
                deliveryChallanPackageRepository.findByChallanNumber(challan.getChallanNumber())));
    }

    // ====== SEARCH ======

    @Override
    public Page<DeliveryChallanResponse> searchDeliveryChallans(String search, Pageable pageable) {
        log.info("Searching delivery challans with keyword: {}", search);
        return deliveryChallanRepository.searchDeliveryChallans(search, pageable)
                .map(challan -> buildDeliveryChallanResponse(challan,
                        deliveryChallanPackageRepository.findByChallanNumber(challan.getChallanNumber())));
    }

    // ====== GET BY SO NUMBER ======



    // ====== GET BY STATUS ======

    @Override
    public List<DeliveryChallanResponse> getDeliveryChallansByStatus(String status) {
        List<DeliveryChallan> challans = deliveryChallanRepository.findByStatus(status);
        return challans.stream()
                .map(challan -> buildDeliveryChallanResponse(challan,
                        deliveryChallanPackageRepository.findByChallanNumber(challan.getChallanNumber())))
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

        // If status is DELIVERED, update packages
        if ("DELIVERED".equals(status)) {
            List<DeliveryChallanPackage> packages = deliveryChallanPackageRepository.findByChallanNumber(challanNumber);
            for (DeliveryChallanPackage pkg : packages) {
                pkg.setDeliveredQuantity(pkg.getDispatchedQuantity());
                pkg.setStatus("DELIVERED");
                deliveryChallanPackageRepository.save(pkg);
            }
        }

        log.info("Delivery Challan status updated: {}", challanNumber);
        return buildDeliveryChallanResponse(updated,
                deliveryChallanPackageRepository.findByChallanNumber(challanNumber));
    }

    // ====== PRINT ======

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
                deliveryChallanPackageRepository.findByChallanNumber(challanNumber));
    }

    // ====== MARK AS DISPATCHED ======

    @Override
    public DeliveryChallanResponse markAsDispatched(String challanNumber) {
        log.info("Marking Delivery Challan as dispatched: {}", challanNumber);
        return updateDeliveryChallanStatus(challanNumber, "DISPATCHED");
    }

    // ====== MARK AS DELIVERED ======

    @Override
    public DeliveryChallanResponse markAsDelivered(String challanNumber) {
        log.info("Marking Delivery Challan as delivered: {}", challanNumber);
        return updateDeliveryChallanStatus(challanNumber, "DELIVERED");
    }

    // ====== CANCEL ======

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
                deliveryChallanPackageRepository.findByChallanNumber(challanNumber));
    }

    // ====== GENERATE PDF ======

  

    // ====== GET SUMMARY ======

    @Override
    public DeliveryChallanSummaryResponse getDeliveryChallanSummary() {
        log.info("Getting delivery challan summary");

        long totalChallans = deliveryChallanRepository.count();
        long created = deliveryChallanRepository.countByStatus("CREATED");
        long printed = deliveryChallanRepository.countByStatus("PRINTED");
        long dispatched = deliveryChallanRepository.countByStatus("DISPATCHED");
        long delivered = deliveryChallanRepository.countByStatus("DELIVERED");
        long cancelled = deliveryChallanRepository.countByStatus("CANCELLED");

        Integer totalQuantity = deliveryChallanRepository.getTotalQuantity();
        Double totalWeight = deliveryChallanRepository.getTotalWeight();

        return DeliveryChallanSummaryResponse.builder()
                .totalChallans(totalChallans)
                .created(created)
                .printed(printed)
                .dispatched(dispatched)
                .delivered(delivered)
                .cancelled(cancelled)
                .totalQuantity(totalQuantity != null ? totalQuantity : 0)
                .totalWeight(totalWeight != null ? totalWeight : 0.0)
                .build();
    }

    // ====== DELETE ======

    @Override
    public void deleteDeliveryChallan(String challanNumber) {
        log.info("Deleting Delivery Challan: {}", challanNumber);

        DeliveryChallan challan = deliveryChallanRepository.findByChallanNumber(challanNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery Challan not found: " + challanNumber));

        if (!challan.getStatus().equals("CREATED") && !challan.getStatus().equals("CANCELLED")) {
            throw new BusinessException("Cannot delete challan in status: " + challan.getStatus());
        }

        List<DeliveryChallanPackage> packages = deliveryChallanPackageRepository.findByChallanNumber(challanNumber);
        deliveryChallanPackageRepository.deleteAll(packages);

        deliveryChallanRepository.delete(challan);

        log.info("Delivery Challan deleted: {}", challanNumber);
    }

    // ====== HELPER METHODS ======

    private String generateChallanNumber() {
        return "DC-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) +
                "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    private void validateStatusTransition(String currentStatus, String newStatus) {
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

    private DeliveryChallanResponse buildDeliveryChallanResponse(DeliveryChallan challan, List<DeliveryChallanPackage> packages) {
        List<PackageResponses> packageResponses = packages.stream()
                .map(pkg -> PackageResponses.builder()
                        .id(pkg.getId())
                        .soNumber(pkg.getSoNumber())
                        .packageNumber(pkg.getPackageNumber())
                        .packageBarcode(pkg.getPackageBarcode())
                        .customerCode(pkg.getCustomerCode())
                        .customerName(pkg.getCustomerName())
                        .customerAddress(pkg.getCustomerAddress())
                        .customerGst(pkg.getCustomerGst())
                        .customerPhone(pkg.getCustomerPhone())
                        .invoiceNumber(pkg.getInvoiceNumber())
                        .orderDate(pkg.getOrderDate())
                        .dispatchDate(pkg.getDispatchDate())
                        .expectedDeliveryDate(pkg.getExpectedDeliveryDate())
                        .itemCode(pkg.getItemCode())
                        .itemName(pkg.getItemName())
                        .uom(pkg.getUom())
                        .orderedQuantity(pkg.getOrderedQuantity())
                        .dispatchedQuantity(pkg.getDispatchedQuantity())
                        .deliveredQuantity(pkg.getDeliveredQuantity())
                        .shortQuantity(pkg.getShortQuantity())
                        .batchNumber(pkg.getBatchNumber())
                        .serialNumbers(pkg.getSerialNumbers())
                        .unitPrice(pkg.getUnitPrice())
                        .totalPrice(pkg.getTotalPrice())
                        .weight(pkg.getWeight())
                        .volume(pkg.getVolume())
                        .status(pkg.getStatus())
                        .remarks(pkg.getRemarks())
                        .build())
                .collect(Collectors.toList());

        return DeliveryChallanResponse.builder()
                .id(challan.getId())
                .challanNumber(challan.getChallanNumber())
                .shipmentNumber(challan.getShipmentNumber())
                .transporter(challan.getTransporter())
                .vehicleNumber(challan.getVehicleNumber())
                .driverName(challan.getDriverName())
                .driverPhone(challan.getDriverPhone())
                .totalPackages(challan.getTotalPackages())
                .totalQuantity(challan.getTotalQuantity())
                .totalWeight(challan.getTotalWeight())
                .totalVolume(challan.getTotalVolume())
                .status(challan.getStatus())
                .remarks(challan.getRemarks())
                .createdBy(challan.getCreatedBy())
                .createdAt(challan.getCreatedAt())
                .updatedAt(challan.getUpdatedAt())
                .packages(packageResponses)
                .build();
    }

    // ====== PDF HELPER METHODS ======

   
}