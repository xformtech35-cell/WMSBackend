package com.warehouse.wms.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EditReservationRequest {

    @NotBlank(message = "Reservation number is required")
    private String reservationNumber;

    private Integer quantity;
    private String batchNumber;
    private String binId;
    private String remarks;
    private String updatedBy;

    // For bulk edit
    private List<EditReservationItem> items;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class EditReservationItem {
    private String reservationNumber;
    private Integer quantity;
    private String batchNumber;
    private String binId;
    private String remarks;
}