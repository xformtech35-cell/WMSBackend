package com.warehouse.wms.service;

import com.warehouse.wms.dto.response.WarehouseDashboardResponse;

public interface WarehouseDashboardService {
    WarehouseDashboardResponse getDashboardData();
    WarehouseDashboardResponse getDashboardDataByDateRange(String startDate, String endDate);
}