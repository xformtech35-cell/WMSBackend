package com.warehouse.wms.controller;

import com.warehouse.wms.dto.request.CustomerRequest;
import com.warehouse.wms.dto.response.CustomerResponse;
import com.warehouse.wms.dto.response.CustomerSummaryResponse;
import com.warehouse.wms.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@Slf4j
public class CustomerController {

    private final CustomerService customerService;

    // ====== CREATE ======
    @PostMapping
    public ResponseEntity<CustomerResponse> createCustomer(@Valid @RequestBody CustomerRequest request) {
        log.info("POST /api/customers - Creating customer");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(customerService.createCustomer(request));
    }

    // ====== GET BY ID ======
    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponse> getCustomerById(@PathVariable Long id) {
        log.info("GET /api/customers/{} - Getting customer by id", id);
        return ResponseEntity.ok(customerService.getCustomerById(id));
    }

    // ====== GET BY CODE ======
    @GetMapping("/code/{customerCode}")
    public ResponseEntity<CustomerResponse> getCustomerByCode(@PathVariable String customerCode) {
        log.info("GET /api/customers/code/{} - Getting customer by code", customerCode);
        return ResponseEntity.ok(customerService.getCustomerByCode(customerCode));
    }

    // ====== GET BY EMAIL ======
    @GetMapping("/email/{email}")
    public ResponseEntity<CustomerResponse> getCustomerByEmail(@PathVariable String email) {
        log.info("GET /api/customers/email/{} - Getting customer by email", email);
        return ResponseEntity.ok(customerService.getCustomerByEmail(email));
    }

    // ====== GET BY PHONE ======
    @GetMapping("/phone/{phone}")
    public ResponseEntity<CustomerResponse> getCustomerByPhone(@PathVariable String phone) {
        log.info("GET /api/customers/phone/{} - Getting customer by phone", phone);
        return ResponseEntity.ok(customerService.getCustomerByPhone(phone));
    }

    // ====== GET ALL WITH FILTERS ======
    @GetMapping
    public ResponseEntity<Page<CustomerResponse>> getAllCustomers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String customerCode,
            @RequestParam(required = false) String customerName,
            @RequestParam(required = false) String companyName,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String mobile,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String customerType,
            @RequestParam(required = false) Boolean isVerified,
            @RequestParam(required = false) Boolean isBlacklisted,
            @RequestParam(required = false) Double minTotalSpent,
            @RequestParam(required = false) Double maxTotalSpent,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        log.info("GET /api/customers - Getting all customers with filters");

        if (search != null && !search.isEmpty()) {
            return ResponseEntity.ok(customerService.searchCustomers(search, pageable));
        }

        Page<CustomerResponse> response = customerService.filterCustomers(
                customerCode, customerName, companyName, email,
                phone, mobile, city, state, country,
                status, customerType, isVerified, isBlacklisted,
                minTotalSpent, maxTotalSpent, pageable);

        return ResponseEntity.ok(response);
    }

    // ====== GET BY STATUS ======
    @GetMapping("/status/{status}")
    public ResponseEntity<List<CustomerResponse>> getCustomersByStatus(@PathVariable String status) {
        log.info("GET /api/customers/status/{} - Getting customers by status", status);
        return ResponseEntity.ok(customerService.getCustomersByStatus(status));
    }

    // ====== GET BY TYPE ======
    @GetMapping("/type/{customerType}")
    public ResponseEntity<List<CustomerResponse>> getCustomersByType(@PathVariable String customerType) {
        log.info("GET /api/customers/type/{} - Getting customers by type", customerType);
        return ResponseEntity.ok(customerService.getCustomersByType(customerType));
    }

    // ====== GET BY CITY ======
    @GetMapping("/city/{city}")
    public ResponseEntity<List<CustomerResponse>> getCustomersByCity(@PathVariable String city) {
        log.info("GET /api/customers/city/{} - Getting customers by city", city);
        return ResponseEntity.ok(customerService.getCustomersByCity(city));
    }

    // ====== GET BY STATE ======
    @GetMapping("/state/{state}")
    public ResponseEntity<List<CustomerResponse>> getCustomersByState(@PathVariable String state) {
        log.info("GET /api/customers/state/{} - Getting customers by state", state);
        return ResponseEntity.ok(customerService.getCustomersByState(state));
    }

    // ====== UPDATE ======
    @PutMapping("/{id}")
    public ResponseEntity<CustomerResponse> updateCustomer(
            @PathVariable Long id,
            @Valid @RequestBody CustomerRequest request) {
        log.info("PUT /api/customers/{} - Updating customer", id);
        return ResponseEntity.ok(customerService.updateCustomer(id, request));
    }

    // ====== UPDATE BY CODE ======
    @PutMapping("/code/{customerCode}")
    public ResponseEntity<CustomerResponse> updateCustomerByCode(
            @PathVariable String customerCode,
            @Valid @RequestBody CustomerRequest request) {
        log.info("PUT /api/customers/code/{} - Updating customer by code", customerCode);
        return ResponseEntity.ok(customerService.updateCustomerByCode(customerCode, request));
    }

    // ====== DELETE ======
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        log.info("DELETE /api/customers/{} - Deleting customer", id);
        customerService.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }

    // ====== DELETE BY CODE ======
    @DeleteMapping("/code/{customerCode}")
    public ResponseEntity<Void> deleteCustomerByCode(@PathVariable String customerCode) {
        log.info("DELETE /api/customers/code/{} - Deleting customer by code", customerCode);
        customerService.deleteCustomerByCode(customerCode);
        return ResponseEntity.noContent().build();
    }

    // ====== STATUS MANAGEMENT ======
    @PatchMapping("/{customerCode}/activate")
    public ResponseEntity<CustomerResponse> activateCustomer(@PathVariable String customerCode) {
        log.info("PATCH /api/customers/{}/activate - Activating customer", customerCode);
        return ResponseEntity.ok(customerService.activateCustomer(customerCode));
    }

    @PatchMapping("/{customerCode}/deactivate")
    public ResponseEntity<CustomerResponse> deactivateCustomer(@PathVariable String customerCode) {
        log.info("PATCH /api/customers/{}/deactivate - Deactivating customer", customerCode);
        return ResponseEntity.ok(customerService.deactivateCustomer(customerCode));
    }

    @PatchMapping("/{customerCode}/block")
    public ResponseEntity<CustomerResponse> blockCustomer(
            @PathVariable String customerCode,
            @RequestParam String reason) {
        log.info("PATCH /api/customers/{}/block - Blocking customer", customerCode);
        return ResponseEntity.ok(customerService.blockCustomer(customerCode, reason));
    }

    @PatchMapping("/{customerCode}/unblock")
    public ResponseEntity<CustomerResponse> unblockCustomer(@PathVariable String customerCode) {
        log.info("PATCH /api/customers/{}/unblock - Unblocking customer", customerCode);
        return ResponseEntity.ok(customerService.unblockCustomer(customerCode));
    }

    @PatchMapping("/{customerCode}/verify")
    public ResponseEntity<CustomerResponse> verifyCustomer(@PathVariable String customerCode) {
        log.info("PATCH /api/customers/{}/verify - Verifying customer", customerCode);
        return ResponseEntity.ok(customerService.verifyCustomer(customerCode));
    }

    // ====== LOYALTY ======
    @PatchMapping("/{customerCode}/loyalty-points")
    public ResponseEntity<CustomerResponse> updateLoyaltyPoints(
            @PathVariable String customerCode,
            @RequestParam Integer points) {
        log.info("PATCH /api/customers/{}/loyalty-points - Updating loyalty points", customerCode);
        return ResponseEntity.ok(customerService.updateLoyaltyPoints(customerCode, points));
    }

    @PatchMapping("/{customerCode}/loyalty-tier")
    public ResponseEntity<CustomerResponse> updateLoyaltyTier(@PathVariable String customerCode) {
        log.info("PATCH /api/customers/{}/loyalty-tier - Updating loyalty tier", customerCode);
        return ResponseEntity.ok(customerService.updateLoyaltyTier(customerCode));
    }

    // ====== SUMMARY ======
    @GetMapping("/summary")
    public ResponseEntity<CustomerSummaryResponse> getCustomerSummary() {
        log.info("GET /api/customers/summary - Getting customer summary");
        return ResponseEntity.ok(customerService.getCustomerSummary());
    }

    // ====== BULK OPERATIONS ======
    @PatchMapping("/bulk/activate")
    public ResponseEntity<Void> bulkActivateCustomers(@RequestBody List<String> customerCodes) {
        log.info("PATCH /api/customers/bulk/activate - Bulk activating customers");
        customerService.bulkActivateCustomers(customerCodes);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/bulk/deactivate")
    public ResponseEntity<Void> bulkDeactivateCustomers(@RequestBody List<String> customerCodes) {
        log.info("PATCH /api/customers/bulk/deactivate - Bulk deactivating customers");
        customerService.bulkDeactivateCustomers(customerCodes);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/bulk")
    public ResponseEntity<Void> bulkDeleteCustomers(@RequestBody List<Long> ids) {
        log.info("DELETE /api/customers/bulk - Bulk deleting customers");
        customerService.bulkDeleteCustomers(ids);
        return ResponseEntity.noContent().build();
    }
}