// src/main/java/com/warehouse/wms/service/impl/ExcelImportServiceImpl.java
package com.warehouse.wms.service.impl;

import com.warehouse.wms.dto.response.BulkImportResponse;
import com.warehouse.wms.entity.Item;
import com.warehouse.wms.repository.ItemRepository;
import com.warehouse.wms.service.ExcelImportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExcelImportServiceImpl implements ExcelImportService {

    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public BulkImportResponse importItemsFromExcel(MultipartFile file, Boolean overwriteExisting) {
        BulkImportResponse.BulkImportResponseBuilder responseBuilder = BulkImportResponse.builder();
        List<BulkImportResponse.ImportError> errors = new ArrayList<>();
        List<Item> itemsToSave = new ArrayList<>();
        int successCount = 0;
        int failureCount = 0;

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            int totalRows = sheet.getPhysicalNumberOfRows() - 1; // Exclude header row
            
            // Get header row to map columns
            Row headerRow = sheet.getRow(0);
            Map<String, Integer> columnIndexMap = getColumnIndexMap(headerRow);

            // Process each row
            for (int rowIndex = 1; rowIndex <= totalRows; rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) continue;

                try {
                    Item item = mapRowToItem(row, columnIndexMap);
                    
                    // Validate item
                    List<String> validationErrors = validateItem(item);
                    
                    if (!validationErrors.isEmpty()) {
                        failureCount++;
                        errors.add(BulkImportResponse.ImportError.builder()
                                .rowNumber(rowIndex + 1)
                                .itemCode(item.getItemCode())
                                .errorMessage(String.join(", ", validationErrors))
                                .build());
                        continue;
                    }

                    // Check for existing item
                    Optional<Item> existingItem = itemRepository.findByItemCode(item.getItemCode());
                    
                    if (existingItem.isPresent()) {
                        if (Boolean.TRUE.equals(overwriteExisting)) {
                            // Update existing item
                            Item existing = existingItem.get();
                            updateExistingItem(existing, item);
                            itemsToSave.add(existing);
                            successCount++;
                        } else {
                            failureCount++;
                            errors.add(BulkImportResponse.ImportError.builder()
                                    .rowNumber(rowIndex + 1)
                                    .itemCode(item.getItemCode())
                                    .errorMessage("Item already exists. Use overwrite flag to update.")
                                    .build());
                        }
                    } else {
                        itemsToSave.add(item);
                        successCount++;
                    }

                } catch (Exception e) {
                    log.error("Error processing row {}: {}", rowIndex + 1, e.getMessage());
                    failureCount++;
                    errors.add(BulkImportResponse.ImportError.builder()
                            .rowNumber(rowIndex + 1)
                            .errorMessage("Processing error: " + e.getMessage())
                            .build());
                }
            }

            // Save all items
            if (!itemsToSave.isEmpty()) {
                itemRepository.saveAll(itemsToSave);
            }

            responseBuilder
                    .totalRecords(totalRows)
                    .successCount(successCount)
                    .failureCount(failureCount)
                    .errors(errors)
                    .summary(Map.of(
                            "created", itemsToSave.stream().filter(i -> i.getId() == null).count(),
                            "updated", itemsToSave.stream().filter(i -> i.getId() != null).count()
                    ));

        } catch (Exception e) {
            log.error("Error processing Excel file: {}", e.getMessage());
            throw new RuntimeException("Failed to process Excel file: " + e.getMessage());
        }

        return responseBuilder.build();
    }

    private Map<String, Integer> getColumnIndexMap(Row headerRow) {
        Map<String, Integer> columnMap = new HashMap<>();
        for (Cell cell : headerRow) {
            String columnName = cell.getStringCellValue().trim().toLowerCase();
            columnMap.put(columnName, cell.getColumnIndex());
        }
        return columnMap;
    }

    private Item mapRowToItem(Row row, Map<String, Integer> columnIndexMap) {
        Item item = new Item();
        
        // Required fields
        item.setItemCode(getCellValueAsString(row, columnIndexMap.get("item_code")));
        item.setItemName(getCellValueAsString(row, columnIndexMap.get("item_name")));
        item.setUom(getCellValueAsString(row, columnIndexMap.get("uom")));
        
        // Optional fields
        item.setDescription(getCellValueAsString(row, columnIndexMap.get("description")));
        item.setGstRate(getCellValueAsDouble(row, columnIndexMap.get("gst_rate")));
        item.setGstHsnCode(getCellValueAsString(row, columnIndexMap.get("gst_hsn_code")));
        item.setGstSacCode(getCellValueAsString(row, columnIndexMap.get("gst_sac_code")));
        item.setIsGstApplicable(getCellValueAsBoolean(row, columnIndexMap.get("is_gst_applicable"), true));
        item.setCgstRate(getCellValueAsDouble(row, columnIndexMap.get("cgst_rate")));
        item.setSgstRate(getCellValueAsDouble(row, columnIndexMap.get("sgst_rate")));
        item.setIgstRate(getCellValueAsDouble(row, columnIndexMap.get("igst_rate")));
        item.setUnitPrice(getCellValueAsDouble(row, columnIndexMap.get("unit_price")));
        item.setCurrentStock(getCellValueAsInteger(row, columnIndexMap.get("current_stock"), 0));
        item.setMinStockLevel(getCellValueAsInteger(row, columnIndexMap.get("min_stock_level"), 0));
        item.setReorderLevel(getCellValueAsInteger(row, columnIndexMap.get("reorder_level"), 0));
        item.setIsActive(getCellValueAsBoolean(row, columnIndexMap.get("is_active"), true));
        item.setCategory(getCellValueAsString(row, columnIndexMap.get("category")));
        item.setBrand(getCellValueAsString(row, columnIndexMap.get("brand")));
        item.setSupplierId(getCellValueAsLong(row, columnIndexMap.get("supplier_id")));
        item.setNotes(getCellValueAsString(row, columnIndexMap.get("notes")));
        
        return item;
    }

    private void updateExistingItem(Item existing, Item newItem) {
        existing.setItemName(newItem.getItemName());
        existing.setDescription(newItem.getDescription());
        existing.setUom(newItem.getUom());
        existing.setGstRate(newItem.getGstRate());
        existing.setGstHsnCode(newItem.getGstHsnCode());
        existing.setGstSacCode(newItem.getGstSacCode());
        existing.setIsGstApplicable(newItem.getIsGstApplicable());
        existing.setCgstRate(newItem.getCgstRate());
        existing.setSgstRate(newItem.getSgstRate());
        existing.setIgstRate(newItem.getIgstRate());
        existing.setUnitPrice(newItem.getUnitPrice());
        existing.setCurrentStock(newItem.getCurrentStock());
        existing.setMinStockLevel(newItem.getMinStockLevel());
        existing.setReorderLevel(newItem.getReorderLevel());
        existing.setIsActive(newItem.getIsActive());
        existing.setCategory(newItem.getCategory());
        existing.setBrand(newItem.getBrand());
        existing.setSupplierId(newItem.getSupplierId());
        existing.setNotes(newItem.getNotes());
    }

    private List<String> validateItem(Item item) {
        List<String> errors = new ArrayList<>();
        
        if (item.getItemCode() == null || item.getItemCode().trim().isEmpty()) {
            errors.add("Item Code is required");
        }
        
        if (item.getItemName() == null || item.getItemName().trim().isEmpty()) {
            errors.add("Item Name is required");
        }
        
        if (item.getUom() == null || item.getUom().trim().isEmpty()) {
            errors.add("UOM is required");
        }
        
        if (item.getGstRate() != null && (item.getGstRate() < 0 || item.getGstRate() > 100)) {
            errors.add("GST Rate must be between 0 and 100");
        }
        
        if (item.getUnitPrice() != null && item.getUnitPrice() < 0) {
            errors.add("Unit Price cannot be negative");
        }
        
        if (item.getCurrentStock() != null && item.getCurrentStock() < 0) {
            errors.add("Current Stock cannot be negative");
        }
        
        return errors;
    }

    // Helper methods for cell value extraction
    private String getCellValueAsString(Row row, Integer columnIndex) {
        if (columnIndex == null) return null;
        Cell cell = row.getCell(columnIndex);
        if (cell == null) return null;
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                return String.valueOf(cell.getNumericCellValue());
            default:
                return null;
        }
    }

    private Double getCellValueAsDouble(Row row, Integer columnIndex) {
        if (columnIndex == null) return null;
        Cell cell = row.getCell(columnIndex);
        if (cell == null) return null;
        
        try {
            switch (cell.getCellType()) {
                case NUMERIC:
                    return cell.getNumericCellValue();
                case STRING:
                    String str = cell.getStringCellValue().trim();
                    return str.isEmpty() ? null : Double.parseDouble(str);
                default:
                    return null;
            }
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer getCellValueAsInteger(Row row, Integer columnIndex, Integer defaultValue) {
        if (columnIndex == null) return defaultValue;
        Cell cell = row.getCell(columnIndex);
        if (cell == null) return defaultValue;
        
        try {
            switch (cell.getCellType()) {
                case NUMERIC:
                    return (int) cell.getNumericCellValue();
                case STRING:
                    String str = cell.getStringCellValue().trim();
                    return str.isEmpty() ? defaultValue : Integer.parseInt(str);
                default:
                    return defaultValue;
            }
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private Boolean getCellValueAsBoolean(Row row, Integer columnIndex, Boolean defaultValue) {
        if (columnIndex == null) return defaultValue;
        Cell cell = row.getCell(columnIndex);
        if (cell == null) return defaultValue;
        
        try {
            switch (cell.getCellType()) {
                case BOOLEAN:
                    return cell.getBooleanCellValue();
                case STRING:
                    String str = cell.getStringCellValue().trim().toLowerCase();
                    return "yes".equals(str) || "true".equals(str) || "1".equals(str) || "y".equals(str);
                case NUMERIC:
                    return cell.getNumericCellValue() != 0;
                default:
                    return defaultValue;
            }
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private Long getCellValueAsLong(Row row, Integer columnIndex) {
        if (columnIndex == null) return null;
        Cell cell = row.getCell(columnIndex);
        if (cell == null) return null;
        
        try {
            switch (cell.getCellType()) {
                case NUMERIC:
                    return (long) cell.getNumericCellValue();
                case STRING:
                    String str = cell.getStringCellValue().trim();
                    return str.isEmpty() ? null : Long.parseLong(str);
                default:
                    return null;
            }
        } catch (NumberFormatException e) {
            return null;
        }
    }
}