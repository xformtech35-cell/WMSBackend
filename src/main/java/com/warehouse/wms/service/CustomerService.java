package com.warehouse.wms.service;

import com.warehouse.wms.dto.request.CustomerRequest;
import com.warehouse.wms.dto.response.CustomerResponse;
import com.warehouse.wms.dto.response.CustomerSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CustomerService {

    // ====== CRUD Operations ======
    CustomerResponse createCustomer(CustomerRequest request);
    CustomerResponse getCustomerById(Long id);
    CustomerResponse getCustomerByCode(String customerCode);
    CustomerResponse getCustomerByEmail(String email);
    CustomerResponse getCustomerByPhone(String phone);
    Page<CustomerResponse> getAllCustomers(Pageable pageable);
    CustomerResponse updateCustomer(Long id, CustomerRequest request);
    CustomerResponse updateCustomerByCode(String customerCode, CustomerRequest request);
    void deleteCustomer(Long id);
    void deleteCustomerByCode(String customerCode);

    // ====== Status Management ======
    CustomerResponse activateCustomer(String customerCode);
    CustomerResponse deactivateCustomer(String customerCode);
    CustomerResponse blockCustomer(String customerCode, String reason);
    CustomerResponse unblockCustomer(String customerCode);
    CustomerResponse verifyCustomer(String customerCode);

    // ====== Search & Filters ======
    Page<CustomerResponse> searchCustomers(String search, Pageable pageable);
    Page<CustomerResponse> filterCustomers(
            String customerCode,
            String customerName,
            String companyName,
            String email,
            String phone,
            String mobile,
            String city,
            String state,
            String country,
            String status,
            String customerType,
            Boolean isVerified,
            Boolean isBlacklisted,
            Double minTotalSpent,
            Double maxTotalSpent,
            Pageable pageable);

    List<CustomerResponse> getCustomersByStatus(String status);
    List<CustomerResponse> getCustomersByType(String customerType);
    List<CustomerResponse> getCustomersByCity(String city);
    List<CustomerResponse> getCustomersByState(String state);

    // ====== Summary & Dashboard ======
    CustomerSummaryResponse getCustomerSummary();

    // ====== Bulk Operations ======
    void bulkActivateCustomers(List<String> customerCodes);
    void bulkDeactivateCustomers(List<String> customerCodes);
    void bulkDeleteCustomers(List<Long> ids);

    // ====== Loyalty ======
    CustomerResponse updateLoyaltyPoints(String customerCode, Integer points);
    CustomerResponse updateLoyaltyTier(String customerCode);

    // ====== Statistics ======
    void updateCustomerStatistics(String customerCode, Double orderAmount);
}