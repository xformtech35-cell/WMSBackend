package com.warehouse.wms.repository;

import com.warehouse.wms.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByCustomerCode(String customerCode);

    Optional<Customer> findByEmail(String email);

    Optional<Customer> findByPhone(String phone);

    List<Customer> findByStatus(String status);

    Page<Customer> findByStatus(String status, Pageable pageable);

    List<Customer> findByCustomerType(String customerType);

    List<Customer> findByCity(String city);

    List<Customer> findByState(String state);

    @Query("SELECT c FROM Customer c WHERE " +
           "(:search IS NULL OR " +
           "LOWER(c.customerCode) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.customerName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.companyName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.phone) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.mobile) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.city) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Customer> searchCustomers(@Param("search") String search, Pageable pageable);

    @Query("SELECT c FROM Customer c WHERE " +
           "(:customerCode IS NULL OR c.customerCode LIKE %:customerCode%) AND " +
           "(:customerName IS NULL OR LOWER(c.customerName) LIKE LOWER(CONCAT('%', :customerName, '%'))) AND " +
           "(:companyName IS NULL OR LOWER(c.companyName) LIKE LOWER(CONCAT('%', :companyName, '%'))) AND " +
           "(:email IS NULL OR LOWER(c.email) LIKE LOWER(CONCAT('%', :email, '%'))) AND " +
           "(:phone IS NULL OR c.phone LIKE %:phone%) AND " +
           "(:mobile IS NULL OR c.mobile LIKE %:mobile%) AND " +
           "(:city IS NULL OR LOWER(c.city) LIKE LOWER(CONCAT('%', :city, '%'))) AND " +
           "(:state IS NULL OR LOWER(c.state) LIKE LOWER(CONCAT('%', :state, '%'))) AND " +
           "(:country IS NULL OR LOWER(c.country) LIKE LOWER(CONCAT('%', :country, '%'))) AND " +
           "(:status IS NULL OR c.status = :status) AND " +
           "(:customerType IS NULL OR c.customerType = :customerType) AND " +
           "(:isVerified IS NULL OR c.isVerified = :isVerified) AND " +
           "(:isBlacklisted IS NULL OR c.isBlacklisted = :isBlacklisted) AND " +
           "(:minTotalSpent IS NULL OR c.totalSpent >= :minTotalSpent) AND " +
           "(:maxTotalSpent IS NULL OR c.totalSpent <= :maxTotalSpent)")
    Page<Customer> findByFilters(
            @Param("customerCode") String customerCode,
            @Param("customerName") String customerName,
            @Param("companyName") String companyName,
            @Param("email") String email,
            @Param("phone") String phone,
            @Param("mobile") String mobile,
            @Param("city") String city,
            @Param("state") String state,
            @Param("country") String country,
            @Param("status") String status,
            @Param("customerType") String customerType,
            @Param("isVerified") Boolean isVerified,
            @Param("isBlacklisted") Boolean isBlacklisted,
            @Param("minTotalSpent") Double minTotalSpent,
            @Param("maxTotalSpent") Double maxTotalSpent,
            Pageable pageable);

    @Query("SELECT COUNT(c) FROM Customer c WHERE c.status = 'ACTIVE'")
    Long countActiveCustomers();

    @Query("SELECT COUNT(c) FROM Customer c WHERE c.isBlacklisted = true")
    Long countBlacklistedCustomers();

    @Query("SELECT COUNT(c) FROM Customer c WHERE c.isVerified = true")
    Long countVerifiedCustomers();

    @Query("SELECT COALESCE(SUM(c.totalSpent), 0) FROM Customer c")
    Double getTotalRevenue();

    @Query("SELECT COALESCE(SUM(c.totalOrders), 0) FROM Customer c")
    Integer getTotalOrders();

    @Query("SELECT COALESCE(AVG(c.totalSpent / NULLIF(c.totalOrders, 0)), 0) FROM Customer c WHERE c.totalOrders > 0")
    Double getAverageOrderValue();

    @Query("SELECT c.customerType, COUNT(c) FROM Customer c GROUP BY c.customerType")
    List<Object[]> countByCustomerType();

    @Query("SELECT c.loyaltyTier, COUNT(c) FROM Customer c WHERE c.loyaltyTier IS NOT NULL GROUP BY c.loyaltyTier")
    List<Object[]> countByLoyaltyTier();

    boolean existsByCustomerCode(String customerCode);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);
}