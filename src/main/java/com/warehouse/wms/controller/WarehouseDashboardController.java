package com.warehouse.wms.controller;

import com.warehouse.wms.dto.response.WarehouseDashboardResponse;
import com.warehouse.wms.service.WarehouseDashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard/warehouse")
@RequiredArgsConstructor
@Slf4j
public class WarehouseDashboardController {

    private final WarehouseDashboardService warehouseDashboardService;

    @GetMapping
    public ResponseEntity<WarehouseDashboardResponse> getDashboard() {
        log.info("GET /api/dashboard/warehouse - Getting Combined Dashboard Data");
        return ResponseEntity.ok(warehouseDashboardService.getDashboardData());
    }

    @GetMapping("/date-range")
    public ResponseEntity<WarehouseDashboardResponse> getDashboardByDateRange(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        log.info("GET /api/dashboard/warehouse/date-range - Getting Dashboard for Date Range");
        return ResponseEntity.ok(warehouseDashboardService.getDashboardDataByDateRange(startDate, endDate));
    }
}