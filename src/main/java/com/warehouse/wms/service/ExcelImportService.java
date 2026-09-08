// src/main/java/com/warehouse/wms/service/ExcelImportService.java
package com.warehouse.wms.service;

import com.warehouse.wms.dto.response.BulkImportResponse;
import org.springframework.web.multipart.MultipartFile;

public interface ExcelImportService {
    BulkImportResponse importItemsFromExcel(MultipartFile file, Boolean overwriteExisting);
}