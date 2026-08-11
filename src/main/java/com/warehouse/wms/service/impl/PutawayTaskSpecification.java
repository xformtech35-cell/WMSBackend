// ====== FILE: src/main/java/com/warehouse/wms/specification/PutawayTaskSpecification.java ======
package com.warehouse.wms.service.impl;

import com.warehouse.wms.constant.PutawayStage;
import com.warehouse.wms.constant.PutawayStatus;
import com.warehouse.wms.entity.PutawayTask;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PutawayTaskSpecification {

    public static Specification<PutawayTask> filterBy(
            String search,
            PutawayStatus status,
            PutawayStage stage,
            String grnNumber,
            String assignedTo,
            LocalDateTime startDate,
            LocalDateTime endDate) {
        
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Search across multiple fields
            if (StringUtils.hasText(search)) {
                String searchPattern = "%" + search.toLowerCase() + "%";
                Predicate searchPredicate = criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("taskNumber")), searchPattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("grnNumber")), searchPattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("assignedTo")), searchPattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("assignedBy")), searchPattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("confirmationNumber")), searchPattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("warehouseId")), searchPattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("receivingArea")), searchPattern)
                );
                predicates.add(searchPredicate);
            }

            // Status filter
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }

            // Stage filter
            if (stage != null) {
                predicates.add(criteriaBuilder.equal(root.get("stage"), stage));
            }

            // GRN Number filter
            if (StringUtils.hasText(grnNumber)) {
                predicates.add(criteriaBuilder.equal(root.get("grnNumber"), grnNumber));
            }

            // Assigned To filter
            if (StringUtils.hasText(assignedTo)) {
                predicates.add(criteriaBuilder.equal(root.get("assignedTo"), assignedTo));
            }

            // Date range filter
            if (startDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), startDate));
            }
            if (endDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), endDate));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}