// ====== FILE: src/main/java/com/warehouse/wms/dto/QRGenerationRequest.java ======

package com.warehouse.wms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QRGenerationRequest {
    
    // Source - From GRN
    private String grnNumber;
    private Long inboundId;
    private List<String> itemCodes;  // Filter specific items
    
    // QR Settings
    private String qrType = "QR_CODE";
    private String labelLevel = "BOX";
    
    // Data Options
    private DataOptions dataOptions;
    
    // Design
    private DesignOptions design;
    
    // Label
    private LabelOptions label;
    
    // Settings
    private GenerationSettings settings;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataOptions {
        private Boolean includeItemCode = true;
        private Boolean includeItemName = true;
        private Boolean includeBatchNumber = true;
        private Boolean includeExpiryDate = true;
        private Boolean includeQuantity = true;
        private Boolean includeGRNNumber = true;
        private Boolean includePO = true;
        private Boolean includeSupplier = true;
        private Boolean includeSerialNumbers = true;
        private Boolean includeUOM = true;
        private Boolean includeQualityStatus = true;
        private Map<String, Object> customData;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DesignOptions {
        private String size = "Medium (3cm)";
        private String errorCorrection = "H (30% recovery)";
        private String color = "#000000";
        private String backgroundColor = "#FFFFFF";
        private Boolean includeLogo = false;
        private String logoUrl;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LabelOptions {
        private String template = "Standard (4x6 inches)";
        private Boolean showTextBelow = true;
        private Integer fontSize = 10;
        private Boolean includeCompanyName = true;
        private Boolean includeBarcodeText = true;
        private String customText;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GenerationSettings {
        private Integer copiesPerItem = 1;
        private Boolean batchGeneration = true;
        private Boolean autoPrint = false;
        private String printerName;
        private Boolean generateForAcceptedOnly = true;
    }
}