package com.warehouse.wms.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.warehouse.wms.dto.request.BarcodeScanRequest;
import com.warehouse.wms.dto.request.DeliveryRequest;
import com.warehouse.wms.dto.request.DispatchRequest;
import com.warehouse.wms.dto.request.PackageRequest;
import com.warehouse.wms.dto.request.PickConfirmationRequest;
import com.warehouse.wms.dto.request.PickListRequest;
import com.warehouse.wms.dto.request.PickTaskRequest;
import com.warehouse.wms.dto.request.SalesOrderItemUpdateRequest;
import com.warehouse.wms.dto.request.SalesOrderRequest;
import com.warehouse.wms.dto.request.ShipmentConfirmationRequest;
import com.warehouse.wms.dto.response.BarcodeScanResponse;
import com.warehouse.wms.dto.response.DeliveryResponse;
import com.warehouse.wms.dto.response.DispatchResponse;
import com.warehouse.wms.dto.response.LabelImageResponse;
import com.warehouse.wms.dto.response.PackageResponse;
import com.warehouse.wms.dto.response.PickConfirmationResponse;
import com.warehouse.wms.dto.response.PickListResponse;
import com.warehouse.wms.dto.response.PickTaskResponse;
import com.warehouse.wms.dto.response.QrCodeResponses;
import com.warehouse.wms.dto.response.SalesOrderItemResponse;
import com.warehouse.wms.dto.response.SalesOrderResponse;
import com.warehouse.wms.dto.response.ShipmentConfirmationResponse;
import com.warehouse.wms.dto.response.ShippingLabelBarcodeResponse;
import com.warehouse.wms.dto.response.ShippingLabelResponse;
import com.warehouse.wms.dto.response.StockReservationResponse;
import com.warehouse.wms.entity.StockReservation;

public interface OutboundService {

    // ============================================================
    // ===================== SALES ORDER ===========================
    // ============================================================

    SalesOrderResponse createSalesOrder(SalesOrderRequest request);

    SalesOrderResponse getSalesOrderByNumber(String soNumber);

    Page<SalesOrderResponse> getAllSalesOrdersWithFilters(
            String search, String soNumber, String customerCode, String customerName,
            String warehouseId, String status, String priority,
            LocalDateTime startDate, LocalDateTime endDate,
            LocalDateTime startCreatedDate, LocalDateTime endCreatedDate,
            LocalDateTime startDeliveryDate, LocalDateTime endDeliveryDate,
            Integer minQuantity, Integer maxQuantity,
            String shippingMethod, String createdBy, Pageable pageable);

    SalesOrderResponse updateSalesOrder(String soNumber, SalesOrderRequest request);

    SalesOrderResponse updateSalesOrderStatus(String soNumber, String status);

    void deleteSalesOrder(String soNumber);

    void cancelSalesOrder(String soNumber);

    // ============================================================
    // =================== SALES ORDER ITEM ========================
    // ============================================================

    SalesOrderItemResponse getSalesOrderItemById(Long itemId);

    List<SalesOrderItemResponse> getSalesOrderItemsBySoNumber(String soNumber);

    SalesOrderItemResponse updateSalesOrderItem(Long itemId, SalesOrderItemUpdateRequest request);

    SalesOrderItemResponse updateSalesOrderItemQuantity(Long itemId, Integer quantity);

    SalesOrderItemResponse updateSalesOrderItemReservedQuantity(Long itemId, Integer quantity);

    SalesOrderItemResponse updateSalesOrderItemPickedQuantity(Long itemId, Integer quantity);

    SalesOrderItemResponse updateSalesOrderItemShippedQuantity(Long itemId, Integer quantity);

    SalesOrderItemResponse updateSalesOrderItemLocation(Long itemId, String sourceLocation);

    SalesOrderItemResponse updateSalesOrderItemBatch(Long itemId, String batchNumber);

    SalesOrderItemResponse updateSalesOrderItemName(Long itemId, String itemName);

    SalesOrderItemResponse updateSalesOrderItemUom(Long itemId, String uom);

    SalesOrderItemResponse updateSalesOrderItemCode(Long itemId, String itemCode);

    void deleteSalesOrderItem(Long itemId);

    void deleteSalesOrderItemsBySoNumber(String soNumber);

    // ============================================================
    // =================== STOCK RESERVATION =======================
    // ============================================================

    StockReservation reserveStock(String soNumber);

    StockReservationResponse getReservationByNumber(String reservationNumber);

    List<StockReservationResponse> getReservationsBySoNumber(String soNumber);

    void releaseReservation(String reservationNumber);

    void releaseAllReservations(String soNumber);

    // ============================================================
    // ===================== PICK LIST =============================
    // ============================================================

    PickListResponse createPickList(PickListRequest request);

    PickListResponse getPickListByNumber(String pickListNumber);

    Page<PickListResponse> getAllPickListsWithFilters(
            String pickListNumber, String soNumber, String warehouseId,
            String status, String priority, String assignedTo, String createdBy,
            LocalDateTime startDate, LocalDateTime endDate,
            LocalDateTime startCreatedDate, LocalDateTime endCreatedDate,
            LocalDateTime startCompletedDate, LocalDateTime endCompletedDate,
            Integer minTotalItems, Integer maxTotalItems,
            Integer minTotalQuantity, Integer maxTotalQuantity,
            String itemCode, Pageable pageable);

    Page<PickListResponse> searchPickLists(String search, Pageable pageable);

    PickListResponse updatePickListStatus(String pickListNumber, String status);

    void deletePickList(String pickListNumber);

    // ============================================================
    // ===================== PICK TASK =============================
    // ============================================================

    PickTaskResponse createPickTask(PickTaskRequest request);

    PickTaskResponse getPickTaskByNumber(String pickTaskNumber);

    List<PickTaskResponse> getPickTasksByPickList(String pickListNumber);

    Page<PickTaskResponse> getAllPickTasksWithFilters(
            String pickTaskNumber, String pickListNumber, String soNumber,
            String itemCode, String itemName, String status,
            String pickerId, String pickerName, String binId,
            String locationBarcode, String batchNumber, Boolean isScanned,
            LocalDateTime startDate, LocalDateTime endDate,
            LocalDateTime startScanDate, LocalDateTime endScanDate,
            Integer minRequiredQuantity, Integer maxRequiredQuantity,
            Integer minPickedQuantity, Integer maxPickedQuantity,
            String createdBy, Pageable pageable);

    Page<PickTaskResponse> searchPickTasks(String search, Pageable pageable);

    PickTaskResponse scanPickTask(String pickTaskNumber, String pickerId, String pickerName);

    PickTaskResponse updatePickTaskStatus(String pickTaskNumber, String status);

    void deletePickTask(String pickTaskNumber);

    // ============================================================
    // ================== PICK CONFIRMATION ========================
    // ============================================================

    PickConfirmationResponse confirmPick(PickConfirmationRequest request);

    PickConfirmationResponse getConfirmationByNumber(String confirmationNumber);
    
    
    
    Page<PickConfirmationResponse> getAllPickConfirmationsWithFilters(
            String confirmationNumber,
            String pickTaskNumber,
            String pickListNumber,
            String soNumber,
            String itemCode,
            String itemName,
            String confirmedBy,
            String status,
            String barcode,
            LocalDateTime startDate,
            LocalDateTime endDate,
            LocalDateTime startConfirmedDate,
            LocalDateTime endConfirmedDate,
            Integer minPickedQuantity,
            Integer maxPickedQuantity,
            Integer minShortQuantity,
            Integer maxShortQuantity,
            Pageable pageable);
    
    
    Page<PickConfirmationResponse> searchPickConfirmations(String search, Pageable pageable);


    // ============================================================
    // ===================== PACKAGE ===============================
    // ============================================================

    PackageResponse createPackage(PackageRequest request);

    PackageResponse getPackageByNumber(String packageNumber);

    PackageResponse getPackageByBarcode(String packageBarcode);

    void updatePackageStatus(String packageNumber, String status);

    void deletePackage(String packageNumber);
    
    

    Page<PackageResponse> getAllPackagesWithFilters(
            String packageNumber,
            String packageBarcode,
            String soNumber,
            String pickListNumber,
            String itemCode,
            String itemName,
            String packageType,
            String status,
            String packedBy,
            LocalDateTime startDate,
            LocalDateTime endDate,
            LocalDateTime startPackedDate,
            LocalDateTime endPackedDate,
            Double minWeight,
            Double maxWeight,
            Integer minQuantity,
            Integer maxQuantity,
            Pageable pageable);

    Page<PackageResponse> searchPackages(String search, Pageable pageable);


    // ============================================================
    // ================== SHIPPING LABEL ===========================
    // ============================================================

    ShippingLabelResponse generateShippingLabel(String packageNumber);

    ShippingLabelResponse getShippingLabelByNumber(String labelNumber);

    void updateShippingLabelStatus(String labelNumber, String status);
    
    
    Page<ShippingLabelResponse> getAllShippingLabelsWithFilters(
            String labelNumber,
            String packageNumber,
            String packageBarcode,
            String soNumber,
            String customerCode,
            String customerName,
            String itemCode,
            String itemName,
            String trackingNumber,
            String labelStatus,
            String shippingMethod,
            String printedBy,
            LocalDateTime startDate,
            LocalDateTime endDate,
            LocalDateTime startPrintedDate,
            LocalDateTime endPrintedDate,
            Double minWeight,
            Double maxWeight,
            Integer minQuantity,
            Integer maxQuantity,
            Pageable pageable);

    Page<ShippingLabelResponse> searchShippingLabels(String search, Pageable pageable);
    
    

    LabelImageResponse getShippingLabelImage(String labelNumber);

    QrCodeResponses getShippingLabelQr(String labelNumber);
    
    
    byte[] getShippingLabelImageAsPng(String labelNumber);
    byte[] getShippingLabelQRAsPng(String labelNumber);
    

    ShippingLabelBarcodeResponse getShippingLabelBarcode(String labelNumber);
    byte[] getShippingLabelBarcodeAsPng(String labelNumber);
    
    BarcodeScanResponse scanBarcode(BarcodeScanRequest request);


    // ============================================================
    // ===================== DISPATCH ==============================
    // ============================================================

    DispatchResponse createDispatch(DispatchRequest request);

    DispatchResponse getDispatchByNumber(String dispatchNumber);

    DispatchResponse updateDispatchStatus(String dispatchNumber, String status);

    
    Page<DispatchResponse> getAllDispatchesWithFilters(
            String dispatchNumber,
            String shipmentNumber,
            String soNumber,
            String packageNumber,
            String customerCode,
            String customerName,
            String transporter,
            String vehicleNumber,
            String driverName,
            String invoiceNumber,
            String deliveryChallan,
            String status,
            String dispatchedBy,
            LocalDateTime startDate,
            LocalDateTime endDate,
            LocalDateTime startDispatchDate,
            LocalDateTime endDispatchDate,
            Pageable pageable);

    Page<DispatchResponse> searchDispatches(String search, Pageable pageable);
    
    
    void deleteDispatch(String dispatchNumber);

    // ============================================================
    // ================ SHIPMENT CONFIRMATION ======================
    // ============================================================

    ShipmentConfirmationResponse confirmShipment(ShipmentConfirmationRequest request);

    ShipmentConfirmationResponse getShipmentByNumber(String shipmentNumber);

    ShipmentConfirmationResponse updateShipmentStatus(String shipmentNumber, String status);
    
    Page<ShipmentConfirmationResponse> getAllShipmentConfirmationsWithFilters(
            String shipmentNumber,
            String dispatchNumber,
            String soNumber,
            String packageNumber,
            String trackingNumber,
            String transporter,
            String shippingMethod,
            String vehicleNumber,
            String status,
            String confirmedBy,
            LocalDateTime startDate,
            LocalDateTime endDate,
            LocalDateTime startDispatchDate,
            LocalDateTime endDispatchDate,
            LocalDateTime startExpectedDelivery,
            LocalDateTime endExpectedDelivery,
            LocalDateTime startActualDelivery,
            LocalDateTime endActualDelivery,
            Pageable pageable);
    
    Page<ShipmentConfirmationResponse> searchShipmentConfirmations(String search, Pageable pageable);

    void deleteShipment(String shipmentNumber);

    // ============================================================
    // ===================== DELIVERY ==============================
    // ============================================================

    DeliveryResponse confirmDelivery(DeliveryRequest request);

    DeliveryResponse getDeliveryByNumber(String deliveryNumber);

    DeliveryResponse updateDeliveryStatus(String deliveryNumber, String status);

    void deleteDelivery(String deliveryNumber);
}