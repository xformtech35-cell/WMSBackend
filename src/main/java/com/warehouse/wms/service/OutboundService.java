package com.warehouse.wms.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.warehouse.wms.dto.request.DeliveryRequest;
import com.warehouse.wms.dto.request.DispatchRequest;
import com.warehouse.wms.dto.request.PackageRequest;
import com.warehouse.wms.dto.request.PickConfirmationRequest;
import com.warehouse.wms.dto.request.PickListRequest;
import com.warehouse.wms.dto.request.PickTaskRequest;
import com.warehouse.wms.dto.request.SalesOrderRequest;
import com.warehouse.wms.dto.request.ShipmentConfirmationRequest;
import com.warehouse.wms.dto.response.DeliveryResponse;
import com.warehouse.wms.dto.response.DispatchResponse;
import com.warehouse.wms.dto.response.PackageResponse;
import com.warehouse.wms.dto.response.PickConfirmationResponse;
import com.warehouse.wms.dto.response.PickListResponse;
import com.warehouse.wms.dto.response.PickTaskResponse;
import com.warehouse.wms.dto.response.SalesOrderResponse;
import com.warehouse.wms.dto.response.ShipmentConfirmationResponse;
import com.warehouse.wms.dto.response.ShippingLabelResponse;
import com.warehouse.wms.dto.response.StockReservationResponse;
import com.warehouse.wms.entity.StockReservation;

public interface OutboundService {

    // Sales Order
    SalesOrderResponse createSalesOrder(SalesOrderRequest request);
    SalesOrderResponse getSalesOrderByNumber(String soNumber);
   // Page<SalesOrderResponse> getAllSalesOrders(Pageable pageable);
    
    
    Page<SalesOrderResponse> getAllSalesOrdersWithFilters(
            String search,
            String soNumber,
            String customerCode,
            String customerName,
            String warehouseId,
            String status,
            String priority,
            LocalDateTime startDate,
            LocalDateTime endDate,
            LocalDateTime startCreatedDate,
            LocalDateTime endCreatedDate,
            LocalDateTime startDeliveryDate,
            LocalDateTime endDeliveryDate,
            Integer minQuantity,
            Integer maxQuantity,
            String shippingMethod,
            String createdBy,
            Pageable pageable);
    
    
    SalesOrderResponse updateSalesOrderStatus(String soNumber, String status);
    void cancelSalesOrder(String soNumber);

    // Stock Reservation
    StockReservation reserveStock(String soNumber);
    StockReservationResponse getReservationByNumber(String reservationNumber);
    void releaseReservation(String reservationNumber);

    // Pick List
    PickListResponse createPickList(PickListRequest request);
    PickListResponse getPickListByNumber(String pickListNumber);
    Page<PickListResponse> getAllPickLists(Pageable pageable);
    PickListResponse updatePickListStatus(String pickListNumber, String status);

    // Pick Task
    PickTaskResponse createPickTask(PickTaskRequest request);
    PickTaskResponse getPickTaskByNumber(String pickTaskNumber);
    List<PickTaskResponse> getPickTasksByPickList(String pickListNumber);
    PickTaskResponse scanPickTask(String pickTaskNumber, String pickerId, String pickerName);

    // Pick Confirmation
    PickConfirmationResponse confirmPick(PickConfirmationRequest request);
    PickConfirmationResponse getConfirmationByNumber(String confirmationNumber);

    // Package
    PackageResponse createPackage(PackageRequest request);
    PackageResponse getPackageByNumber(String packageNumber);
    PackageResponse getPackageByBarcode(String packageBarcode);
    void updatePackageStatus(String packageNumber, String status);

    // Shipping Label
    ShippingLabelResponse generateShippingLabel(String packageNumber);
    ShippingLabelResponse getShippingLabelByNumber(String labelNumber);
    void updateShippingLabelStatus(String labelNumber, String status);

    // Dispatch
    DispatchResponse createDispatch(DispatchRequest request);
    DispatchResponse getDispatchByNumber(String dispatchNumber);
    DispatchResponse updateDispatchStatus(String dispatchNumber, String status);

    // Shipment Confirmation
    ShipmentConfirmationResponse confirmShipment(ShipmentConfirmationRequest request);
    ShipmentConfirmationResponse getShipmentByNumber(String shipmentNumber);
    ShipmentConfirmationResponse updateShipmentStatus(String shipmentNumber, String status);

    // Delivery
    DeliveryResponse confirmDelivery(DeliveryRequest request);
    DeliveryResponse getDeliveryByNumber(String deliveryNumber);
    DeliveryResponse updateDeliveryStatus(String deliveryNumber, String status);
}