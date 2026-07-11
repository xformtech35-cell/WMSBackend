package com.warehouse.wms.scheduler;

import com.warehouse.wms.entity.Bin;
import com.warehouse.wms.entity.CountTask;
import com.warehouse.wms.entity.Sku;
import com.warehouse.wms.repository.BinRepository;
import com.warehouse.wms.repository.CountTaskRepository;
import com.warehouse.wms.repository.SkuRepository;
import com.warehouse.wms.service.CycleCountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class CycleCountScheduler {

    private final JdbcTemplate jdbc;
    private final SkuRepository skuRepository;
    private final BinRepository binRepository;
    private final CycleCountService cycleCountService;

    @Scheduled(cron = "0 0 2 * * SUN") // Every Sunday at 2:00 AM
    public void runABCClassificationAndSchedule() {
        log.info("[CycleCountScheduler] Running ABC classification rules...");

        // 1. Calculate velocities (popularity) over the last 90 days
        String query = "SELECT sol.sku_id, SUM(sol.quantity) as velocity " +
                "FROM wms_sales_order_line sol " +
                "JOIN wms_sales_order so ON sol.sales_order_id = so.id " +
                "WHERE so.order_date >= DATE_SUB(CURDATE(), INTERVAL 90 DAY) " +
                "GROUP BY sol.sku_id " +
                "ORDER BY velocity DESC";

        List<Map<String, Object>> results = jdbc.queryForList(query);
        
        List<Long> rankedSkuIds = results.stream()
                .map(r -> (Long) r.get("sku_id"))
                .toList();

        List<Sku> allSkus = skuRepository.findAll();
        int total = allSkus.size();
        if (total == 0) return;

        Map<Long, String> classifications = new HashMap<>();
        
        // Match ranks
        int idx = 0;
        for (Long skuId : rankedSkuIds) {
            double percentile = (double) idx / total;
            if (percentile <= 0.20) {
                classifications.put(skuId, "A");
            } else if (percentile <= 0.50) {
                classifications.put(skuId, "B");
            } else {
                classifications.put(skuId, "C");
            }
            idx++;
        }

        // Add any remaining unranked SKUs as Class C
        for (Sku sku : allSkus) {
            if (!classifications.containsKey(sku.getId())) {
                classifications.put(sku.getId(), "C");
            }
        }

        log.info("[CycleCountScheduler] ABC classification summary: {}", classifications.values().stream().collect(
                java.util.stream.Collectors.groupingBy(s -> s, java.util.stream.Collectors.counting())
        ));

        // 2. Schedule counts based on class
        // Weekly (always schedule 'A' items)
        List<Bin> allBins = binRepository.findAll();
        if (allBins.isEmpty()) return;

        int tasksCreated = 0;
        for (Map.Entry<Long, String> entry : classifications.entrySet()) {
            Long skuId = entry.getKey();
            String abc = entry.getValue();

            // Schedule count for 'A' items, B items on 1st Sunday of the month, C items on 1st Sunday of quarter
            boolean scheduleThisWeek = "A".equals(abc) 
                    || ("B".equals(abc) && java.time.LocalDate.now().getDayOfMonth() <= 7)
                    || ("C".equals(abc) && java.time.LocalDate.now().getDayOfMonth() <= 7 && java.time.LocalDate.now().getMonthValue() % 3 == 1);

            if (scheduleThisWeek) {
                // Find a bin containing this SKU if possible
                Bin targetBin = allBins.stream().findFirst().orElse(null);
                cycleCountService.createAdHocTask(null, targetBin != null ? targetBin.getId() : null, skuId, null);
                tasksCreated++;
            }
        }
        
        log.info("[CycleCountScheduler] Cycle counting scheduling complete. Generated {} counts.", tasksCreated);
    }
}
