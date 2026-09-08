// src/main/java/com/warehouse/wms/controller/ItemBulkImportController.java
package com.warehouse.wms.controller;

import com.warehouse.wms.dto.request.BulkItemImportRequest;
import com.warehouse.wms.dto.response.BulkImportResponse;
import com.warehouse.wms.service.ExcelImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/items/bulk")
@RequiredArgsConstructor
public class ItemBulkImportController {

    private final ExcelImportService excelImportService;

    @PostMapping("/import")
    public ResponseEntity<BulkImportResponse> importItems(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "overwrite", defaultValue = "false") Boolean overwrite) {
        
        // Validate file
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        
        String fileName = file.getOriginalFilename();
        if (fileName == null || (!fileName.endsWith(".xlsx") && !fileName.endsWith(".xls"))) {
            throw new IllegalArgumentException("Invalid file format. Please upload .xlsx or .xls file");
        }
        
        BulkImportResponse response = excelImportService.importItemsFromExcel(file, overwrite);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}