// ====== FILE: src/main/java/com/warehouse/wms/specification/InventoryStockSpecification.java ======
package com.warehouse.wms.service.impl;

import com.warehouse.wms.constant.InventoryStatus;
import com.warehouse.wms.entity.InventoryStock;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class InventoryStockSpecification {

    public static Specification<InventoryStock> filterBy(
            String search,
            String itemCode,
            String itemName,
            InventoryStatus status,
            String warehouseId,
            String zone,
            String aisle,
            String rack,
            String level,
            String binId,
            String batchNumber,
            String grnNumber,
            Boolean isAvailable,
            Boolean isAllocated,
            Boolean isFrozen,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Integer minQuantity,
            Integer maxQuantity) {
        
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Search across multiple fields
            if (StringUtils.hasText(search)) {
                String searchPattern = "%" + search.toLowerCase() + "%";
                Predicate searchPredicate = criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("itemCode")), searchPattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("itemName")), searchPattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("inventoryNumber")), searchPattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("batchNumber")), searchPattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("binId")), searchPattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("grnNumber")), searchPattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("warehouseId")), searchPattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("qrCodeValue")), searchPattern)
                );
                predicates.add(searchPredicate);
            }

            // Exact match filters
            if (StringUtils.hasText(itemCode)) {
                predicates.add(criteriaBuilder.equal(root.get("itemCode"), itemCode));
            }

            if (StringUtils.hasText(itemName)) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("itemName")), 
                        "%" + itemName.toLowerCase() + "%"
                ));
            }

            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }

            if (StringUtils.hasText(warehouseId)) {
                predicates.add(criteriaBuilder.equal(root.get("warehouseId"), warehouseId));
            }

            if (StringUtils.hasText(zone)) {
                predicates.add(criteriaBuilder.equal(root.get("zone"), zone));
            }

            if (StringUtils.hasText(aisle)) {
                predicates.add(criteriaBuilder.equal(root.get("aisle"), aisle));
            }

            if (StringUtils.hasText(rack)) {
                predicates.add(criteriaBuilder.equal(root.get("rack"), rack));
            }

            if (StringUtils.hasText(level)) {
                predicates.add(criteriaBuilder.equal(root.get("level"), level));
            }

            if (StringUtils.hasText(binId)) {
                predicates.add(criteriaBuilder.equal(root.get("binId"), binId));
            }

            if (StringUtils.hasText(batchNumber)) {
                predicates.add(criteriaBuilder.equal(root.get("batchNumber"), batchNumber));
            }

            if (StringUtils.hasText(grnNumber)) {
                predicates.add(criteriaBuilder.equal(root.get("grnNumber"), grnNumber));
            }

            // Boolean filters
            if (isAvailable != null) {
                predicates.add(criteriaBuilder.equal(root.get("isAvailable"), isAvailable));
            }

            if (isAllocated != null) {
                predicates.add(criteriaBuilder.equal(root.get("isAllocated"), isAllocated));
            }

            if (isFrozen != null) {
                predicates.add(criteriaBuilder.equal(root.get("isFrozen"), isFrozen));
            }

            // Quantity range filters
            if (minQuantity != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("quantity"), minQuantity));
            }
            if (maxQuantity != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("quantity"), maxQuantity));
            }

            // Date range filters (using receivedDate or createdAt)
            if (startDate != null || endDate != null) {
                if (startDate != null && endDate != null) {
                    predicates.add(criteriaBuilder.between(root.get("receivedDate"), startDate, endDate));
                } else if (startDate != null) {
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("receivedDate"), startDate));
                } else if (endDate != null) {
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("receivedDate"), endDate));
                }
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}