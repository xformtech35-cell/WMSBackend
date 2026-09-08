// src/main/java/com/warehouse/wms/dto/response/BulkImportResponse.java
package com.warehouse.wms.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class BulkImportResponse {
    private Integer totalRecords;
    private Integer successCount;
    private Integer failureCount;
    private List<ImportError> errors;
    private Map<String, Object> summary;
    
    @Data
    @Builder
    public static class ImportError {
        private Integer rowNumber;
        private String itemCode;
        private String field;
        private String errorMessage;
    }
}