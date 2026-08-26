package com.warehouse.wms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QrCodeResponses {
    private String labelNumber;
    private String packageNumber;
    private String soNumber;
    private String trackingNumber;
    private String qrCodeBase64;
    private String qrCodeData;
}
