// ====== FILE: src/main/java/com/warehouse/wms/dto/request/PutawayConfirmRequest.java ======
package com.warehouse.wms.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PutawayConfirmRequest {

    @NotBlank(message = "Task number is required")
    private String taskNumber;

    @NotBlank(message = "Confirmed by is required")
    private String confirmedBy;

    @NotNull(message = "Confirmed quantity is required")
    private Integer confirmedQuantity;

    private String binId;

    private String binBarcode;

    private Boolean isVerified = false;

    private String verifiedBy;

    private String remarks;

    @Valid
    @NotEmpty(message = "At least one confirmation line is required")
    private List<PutawayConfirmLineRequest> lines;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PutawayConfirmLineRequest {
        @NotNull(message = "Line ID is required")
        private Long lineId;

        private Integer confirmedQuantity;

        private String actualBin;

        private String actualBinBarcode;

        private String remarks;
    }
}