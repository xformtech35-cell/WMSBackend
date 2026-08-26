// LabelImageResponse.java
package com.warehouse.wms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LabelImageResponse {
    private String labelNumber;
    private String base64Image;
    private String imageFormat; // "PNG" or "JPEG"
    private String labelData;
}

// QrCodeResponse.java
