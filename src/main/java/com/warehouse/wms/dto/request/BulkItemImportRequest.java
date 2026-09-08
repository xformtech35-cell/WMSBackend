// src/main/java/com/warehouse/wms/dto/request/BulkItemImportRequest.java
package com.warehouse.wms.dto.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class BulkItemImportRequest {
    private MultipartFile file;
    private Boolean overwriteExisting = false;
}