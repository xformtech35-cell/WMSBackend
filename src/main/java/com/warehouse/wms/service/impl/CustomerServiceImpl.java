package com.warehouse.wms.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.warehouse.wms.dto.request.CustomerRequest;
import com.warehouse.wms.dto.response.CustomerResponse;
import com.warehouse.wms.dto.response.CustomerSummaryResponse;
import com.warehouse.wms.dto.response.CustomerTypeCountResponse;
import com.warehouse.wms.dto.response.LoyaltyTierCountResponse;
import com.warehouse.wms.entity.Customer;
import com.warehouse.wms.exception.BusinessException;
import com.warehouse.wms.exception.ResourceNotFoundException;
import com.warehouse.wms.repository.CustomerRepository;
import com.warehouse.wms.service.CustomerService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;

    // ====== CRUD Operations ======

    @Override
    public CustomerResponse createCustomer(CustomerRequest request) {
        log.info("Creating customer: {}", request.getCustomerCode());

        // Validate uniqueness
        if (customerRepository.existsByCustomerCode(request.getCustomerCode())) {
            throw new BusinessException("Customer code already exists: " + request.getCustomerCode());
        }
        if (StringUtils.hasText(request.getEmail()) && customerRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email already exists: " + request.getEmail());
        }
        if (StringUtils.hasText(request.getPhone()) && customerRepository.existsByPhone(request.getPhone())) {
            throw new BusinessException("Phone already exists: " + request.getPhone());
        }

        Customer customer = buildCustomerFromRequest(request, new Customer());
        customer.setStatus("ACTIVE");
        customer.setTotalOrders(0);
        customer.setTotalSpent(0.0);
        customer.setAverageOrderValue(0.0);
        customer.setLoyaltyPoints(0);
        customer.setLoyaltyTier("BRONZE");
        customer.setIsVerified(false);
        customer.setIsBlacklisted(false);
        customer.setCreatedBy(request.getCreatedBy());

        Customer saved = customerRepository.save(customer);
        log.info("Customer created successfully: {}", saved.getCustomerCode());

        return buildCustomerResponse(saved);
    }

    @Override
    public CustomerResponse getCustomerById(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));
        return buildCustomerResponse(customer);
    }

    @Override
    public CustomerResponse getCustomerByCode(String customerCode) {
        Customer customer = customerRepository.findByCustomerCode(customerCode)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with code: " + customerCode));
        return buildCustomerResponse(customer);
    }

    @Override
    public CustomerResponse getCustomerByEmail(String email) {
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with email: " + email));
        return buildCustomerResponse(customer);
    }

    @Override
    public CustomerResponse getCustomerByPhone(String phone) {
        Customer customer = customerRepository.findByPhone(phone)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with phone: " + phone));
        return buildCustomerResponse(customer);
    }

    @Override
    public Page<CustomerResponse> getAllCustomers(Pageable pageable) {
        return customerRepository.findAll(pageable)
                .map(this::buildCustomerResponse);
    }

    @Override
    public CustomerResponse updateCustomer(Long id, CustomerRequest request) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));
        return updateCustomerInternal(customer, request);
    }

    @Override
    public CustomerResponse updateCustomerByCode(String customerCode, CustomerRequest request) {
        Customer customer = customerRepository.findByCustomerCode(customerCode)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with code: " + customerCode));
        return updateCustomerInternal(customer, request);
    }

    private CustomerResponse updateCustomerInternal(Customer customer, CustomerRequest request) {
        log.info("Updating customer: {}", customer.getCustomerCode());

        // Check email uniqueness
        if (StringUtils.hasText(request.getEmail()) && 
            !request.getEmail().equals(customer.getEmail()) && 
            customerRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email already exists: " + request.getEmail());
        }

        // Check phone uniqueness
        if (StringUtils.hasText(request.getPhone()) && 
            !request.getPhone().equals(customer.getPhone()) && 
            customerRepository.existsByPhone(request.getPhone())) {
            throw new BusinessException("Phone already exists: " + request.getPhone());
        }

        Customer updated = buildCustomerFromRequest(request, customer);
        updated.setUpdatedBy(request.getCreatedBy());

        Customer saved = customerRepository.save(updated);
        log.info("Customer updated successfully: {}", saved.getCustomerCode());

        return buildCustomerResponse(saved);
    }

    @Override
    public void deleteCustomer(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));
        customerRepository.delete(customer);
        log.info("Customer deleted: {}", customer.getCustomerCode());
    }

    @Override
    public void deleteCustomerByCode(String customerCode) {
        Customer customer = customerRepository.findByCustomerCode(customerCode)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with code: " + customerCode));
        customerRepository.delete(customer);
        log.info("Customer deleted: {}", customerCode);
    }

    // ====== Status Management ======

    @Override
    public CustomerResponse activateCustomer(String customerCode) {
        Customer customer = customerRepository.findByCustomerCode(customerCode)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + customerCode));
        customer.setStatus("ACTIVE");
        customer.setUpdatedBy("SYSTEM");
        Customer saved = customerRepository.save(customer);
        log.info("Customer activated: {}", customerCode);
        return buildCustomerResponse(saved);
    }

    @Override
    public CustomerResponse deactivateCustomer(String customerCode) {
        Customer customer = customerRepository.findByCustomerCode(customerCode)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + customerCode));
        customer.setStatus("INACTIVE");
        customer.setUpdatedBy("SYSTEM");
        Customer saved = customerRepository.save(customer);
        log.info("Customer deactivated: {}", customerCode);
        return buildCustomerResponse(saved);
    }

    @Override
    public CustomerResponse blockCustomer(String customerCode, String reason) {
        Customer customer = customerRepository.findByCustomerCode(customerCode)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + customerCode));
        customer.setStatus("BLOCKED");
        customer.setIsBlacklisted(true);
        customer.setBlacklistReason(reason);
        customer.setUpdatedBy("SYSTEM");
        Customer saved = customerRepository.save(customer);
        log.info("Customer blocked: {}", customerCode);
        return buildCustomerResponse(saved);
    }

    @Override
    public CustomerResponse unblockCustomer(String customerCode) {
        Customer customer = customerRepository.findByCustomerCode(customerCode)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + customerCode));
        customer.setStatus("ACTIVE");
        customer.setIsBlacklisted(false);
        customer.setBlacklistReason(null);
        customer.setUpdatedBy("SYSTEM");
        Customer saved = customerRepository.save(customer);
        log.info("Customer unblocked: {}", customerCode);
        return buildCustomerResponse(saved);
    }

    @Override
    public CustomerResponse verifyCustomer(String customerCode) {
        Customer customer = customerRepository.findByCustomerCode(customerCode)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + customerCode));
        customer.setIsVerified(true);
        customer.setUpdatedBy("SYSTEM");
        Customer saved = customerRepository.save(customer);
        log.info("Customer verified: {}", customerCode);
        return buildCustomerResponse(saved);
    }

    // ====== Search & Filters ======

    @Override
    public Page<CustomerResponse> searchCustomers(String search, Pageable pageable) {
        if (!StringUtils.hasText(search)) {
            return customerRepository.findAll(pageable).map(this::buildCustomerResponse);
        }
        return customerRepository.searchCustomers(search, pageable)
                .map(this::buildCustomerResponse);
    }

    @Override
    public Page<CustomerResponse> filterCustomers(
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
            Pageable pageable) {

        return customerRepository.findByFilters(
                customerCode, customerName, companyName, email,
                phone, mobile, city, state, country,
                status, customerType, isVerified, isBlacklisted,
                minTotalSpent, maxTotalSpent, pageable)
                .map(this::buildCustomerResponse);
    }

    @Override
    public List<CustomerResponse> getCustomersByStatus(String status) {
        return customerRepository.findByStatus(status)
                .stream()
                .map(this::buildCustomerResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<CustomerResponse> getCustomersByType(String customerType) {
        return customerRepository.findByCustomerType(customerType)
                .stream()
                .map(this::buildCustomerResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<CustomerResponse> getCustomersByCity(String city) {
        return customerRepository.findByCity(city)
                .stream()
                .map(this::buildCustomerResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<CustomerResponse> getCustomersByState(String state) {
        return customerRepository.findByState(state)
                .stream()
                .map(this::buildCustomerResponse)
                .collect(Collectors.toList());
    }

    // ====== Summary & Dashboard ======

    @Override
    public CustomerSummaryResponse getCustomerSummary() {
        log.info("Getting customer summary");

        return CustomerSummaryResponse.builder()
                .totalCustomers(customerRepository.count())
                .activeCustomers(customerRepository.countActiveCustomers())
                .inactiveCustomers(customerRepository.count() - customerRepository.countActiveCustomers())
                .blacklistedCustomers(customerRepository.countBlacklistedCustomers())
                .verifiedCustomers(customerRepository.countVerifiedCustomers())
                .totalRevenue(customerRepository.getTotalRevenue())
                .totalOrders(customerRepository.getTotalOrders())
                .averageOrderValue(customerRepository.getAverageOrderValue())
                .customerTypeCounts(getCustomerTypeCounts())
                .loyaltyTierCounts(getLoyaltyTierCounts())
                .build();
    }

    private List<CustomerTypeCountResponse> getCustomerTypeCounts() {
        List<Object[]> results = customerRepository.countByCustomerType();
        List<CustomerTypeCountResponse> counts = new ArrayList<>();
        for (Object[] result : results) {
            counts.add(CustomerTypeCountResponse.builder()
                    .customerType((String) result[0])
                    .count((Long) result[1])
                    .build());
        }
        return counts;
    }

    private List<LoyaltyTierCountResponse> getLoyaltyTierCounts() {
        List<Object[]> results = customerRepository.countByLoyaltyTier();
        List<LoyaltyTierCountResponse> counts = new ArrayList<>();
        for (Object[] result : results) {
            counts.add(LoyaltyTierCountResponse.builder()
                    .loyaltyTier((String) result[0])
                    .count((Long) result[1])
                    .build());
        }
        return counts;
    }

    // ====== Bulk Operations ======

    @Override
    public void bulkActivateCustomers(List<String> customerCodes) {
        for (String code : customerCodes) {
            try {
                activateCustomer(code);
            } catch (Exception e) {
                log.error("Failed to activate customer: {}", code, e);
            }
        }
    }

    @Override
    public void bulkDeactivateCustomers(List<String> customerCodes) {
        for (String code : customerCodes) {
            try {
                deactivateCustomer(code);
            } catch (Exception e) {
                log.error("Failed to deactivate customer: {}", code, e);
            }
        }
    }

    @Override
    public void bulkDeleteCustomers(List<Long> ids) {
        for (Long id : ids) {
            try {
                deleteCustomer(id);
            } catch (Exception e) {
                log.error("Failed to delete customer: {}", id, e);
            }
        }
    }

    // ====== Loyalty ======

    @Override
    public CustomerResponse updateLoyaltyPoints(String customerCode, Integer points) {
        Customer customer = customerRepository.findByCustomerCode(customerCode)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + customerCode));

        int currentPoints = customer.getLoyaltyPoints() != null ? customer.getLoyaltyPoints() : 0;
        customer.setLoyaltyPoints(currentPoints + points);
        customer.setUpdatedBy("SYSTEM");

        // Update loyalty tier
        updateLoyaltyTier(customer);

        Customer saved = customerRepository.save(customer);
        log.info("Loyalty points updated for {}: +{}", customerCode, points);
        return buildCustomerResponse(saved);
    }

    @Override
    public CustomerResponse updateLoyaltyTier(String customerCode) {
        Customer customer = customerRepository.findByCustomerCode(customerCode)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + customerCode));
        updateLoyaltyTier(customer);
        Customer saved = customerRepository.save(customer);
        return buildCustomerResponse(saved);
    }

    private void updateLoyaltyTier(Customer customer) {
        int points = customer.getLoyaltyPoints() != null ? customer.getLoyaltyPoints() : 0;
        String tier;
        if (points >= 10000) {
            tier = "PLATINUM";
        } else if (points >= 5000) {
            tier = "GOLD";
        } else if (points >= 2000) {
            tier = "SILVER";
        } else {
            tier = "BRONZE";
        }
        customer.setLoyaltyTier(tier);
    }

    // ====== Statistics ======

    @Override
    public void updateCustomerStatistics(String customerCode, Double orderAmount) {
        Customer customer = customerRepository.findByCustomerCode(customerCode)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + customerCode));

        int orders = customer.getTotalOrders() != null ? customer.getTotalOrders() : 0;
        double spent = customer.getTotalSpent() != null ? customer.getTotalSpent() : 0.0;

        customer.setTotalOrders(orders + 1);
        customer.setTotalSpent(spent + orderAmount);
        customer.setLastOrderDate(LocalDateTime.now());

        if (customer.getTotalOrders() > 0) {
            customer.setAverageOrderValue(customer.getTotalSpent() / customer.getTotalOrders());
        }

        // Calculate loyalty points (e.g., 1 point per 100 rupees)
        int pointsEarned = (int) (orderAmount / 100);
        updateLoyaltyPoints(customerCode, pointsEarned);

        customer.setUpdatedBy("SYSTEM");
        customerRepository.save(customer);
        log.info("Customer statistics updated: {}", customerCode);
    }

    // ====== Helper Methods ======

    private Customer buildCustomerFromRequest(CustomerRequest request, Customer customer) {
        customer.setCustomerCode(request.getCustomerCode());
        customer.setCustomerName(request.getCustomerName());
        customer.setCompanyName(request.getCompanyName());
        customer.setEmail(request.getEmail());
        customer.setPhone(request.getPhone());
        customer.setMobile(request.getMobile());
        customer.setAddressLine1(request.getAddressLine1());
        customer.setAddressLine2(request.getAddressLine2());
        customer.setCity(request.getCity());
        customer.setState(request.getState());
        customer.setPincode(request.getPincode());
        customer.setCountry(request.getCountry());
        customer.setGstNumber(request.getGstNumber());
        customer.setPanNumber(request.getPanNumber());
        customer.setTaxId(request.getTaxId());
        customer.setContactPerson(request.getContactPerson());
        customer.setContactDesignation(request.getContactDesignation());
        customer.setContactPhone(request.getContactPhone());
        customer.setContactEmail(request.getContactEmail());
        customer.setPaymentTerms(request.getPaymentTerms());
        customer.setCreditLimit(request.getCreditLimit());
        customer.setCreditDays(request.getCreditDays());
        customer.setDiscountPercentage(request.getDiscountPercentage());
        customer.setCustomerType(request.getCustomerType());
        customer.setIndustryType(request.getIndustryType());
        customer.setWebsite(request.getWebsite());
        customer.setNotes(request.getNotes());
        customer.setPreferredWarehouse(request.getPreferredWarehouse());
        customer.setDefaultShippingMethod(request.getDefaultShippingMethod());
        return customer;
    }

    private CustomerResponse buildCustomerResponse(Customer customer) {
        return CustomerResponse.builder()
                .id(customer.getId())
                .customerCode(customer.getCustomerCode())
                .customerName(customer.getCustomerName())
                .companyName(customer.getCompanyName())
                .email(customer.getEmail())
                .phone(customer.getPhone())
                .mobile(customer.getMobile())
                .addressLine1(customer.getAddressLine1())
                .addressLine2(customer.getAddressLine2())
                .city(customer.getCity())
                .state(customer.getState())
                .pincode(customer.getPincode())
                .country(customer.getCountry())
                .gstNumber(customer.getGstNumber())
                .panNumber(customer.getPanNumber())
                .taxId(customer.getTaxId())
                .contactPerson(customer.getContactPerson())
                .contactDesignation(customer.getContactDesignation())
                .contactPhone(customer.getContactPhone())
                .contactEmail(customer.getContactEmail())
                .paymentTerms(customer.getPaymentTerms())
                .creditLimit(customer.getCreditLimit())
                .creditDays(customer.getCreditDays())
                .discountPercentage(customer.getDiscountPercentage())
                .status(customer.getStatus())
                .customerType(customer.getCustomerType())
                .industryType(customer.getIndustryType())
                .website(customer.getWebsite())
                .notes(customer.getNotes())
                .preferredWarehouse(customer.getPreferredWarehouse())
                .defaultShippingMethod(customer.getDefaultShippingMethod())
                .lastOrderDate(customer.getLastOrderDate())
                .totalOrders(customer.getTotalOrders())
                .totalSpent(customer.getTotalSpent())
                .averageOrderValue(customer.getAverageOrderValue())
                .loyaltyPoints(customer.getLoyaltyPoints())
                .loyaltyTier(customer.getLoyaltyTier())
                .isVerified(customer.getIsVerified())
                .isBlacklisted(customer.getIsBlacklisted())
                .blacklistReason(customer.getBlacklistReason())
                .createdBy(customer.getCreatedBy())
                .createdAt(customer.getCreatedAt())
                .updatedAt(customer.getUpdatedAt())
                .build();
    }
}