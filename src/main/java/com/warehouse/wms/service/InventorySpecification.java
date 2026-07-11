package com.warehouse.wms.service;

import com.warehouse.wms.entity.Inventory;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class InventorySpecification {

    public static Specification<Inventory> withDynamicQuery(String search, String state, String warehouse) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // State filter
            if (StringUtils.hasText(state)) {
                try {
                    Inventory.InventoryState inventoryState = Inventory.InventoryState.valueOf(state.toUpperCase(Locale.ROOT));
                    predicates.add(criteriaBuilder.equal(root.get("state"), inventoryState));
                } catch (IllegalArgumentException e) {
                    // Ignore invalid state
                }
            }

            // Warehouse filter
            if (StringUtils.hasText(warehouse)) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("bin").get("rack").get("aisle").get("zone").get("warehouse").get("name")),
                        "%" + warehouse.toLowerCase(Locale.ROOT) + "%"
                ));
            }

            // Search filter
            if (StringUtils.hasText(search)) {
                String likePattern = "%" + search.toLowerCase(Locale.ROOT) + "%";
                Predicate searchPredicate = criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("itemCode")), likePattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("itemName")), likePattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("sku").get("skuCode")), likePattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("sku").get("description")), likePattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("batchNo")), likePattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("serialNo")), likePattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("bin").get("barcode")), likePattern)
                );
                predicates.add(searchPredicate);
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}