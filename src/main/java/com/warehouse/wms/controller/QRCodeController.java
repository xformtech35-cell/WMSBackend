// ====== FILE: src/main/java/com/warehouse/wms/controller/QRCodeController.java ======
package com.warehouse.wms.controller;

import com.warehouse.wms.dto.request.QRCodeGenerateRequest;
import com.warehouse.wms.dto.request.QRCodePrintRequest;
import com.warehouse.wms.dto.response.QRCodeResponse;
import com.warehouse.wms.service.QRCodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/qr-codes")
@RequiredArgsConstructor
@Tag(name = "QR Code Management", description = "APIs for QR Code and Barcode generation")
public class QRCodeController {

    private final QRCodeService qrCodeService;

    @PostMapping("/generate")
    @Operation(summary = "Generate QR Code")
    public ResponseEntity<QRCodeResponse> generateQRCode(@Valid @RequestBody QRCodeGenerateRequest request) {
        log.info("Received request to generate QR Code");
        QRCodeResponse response = qrCodeService.generateQRCode(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/generate/batch")
    @Operation(summary = "Generate multiple QR Codes in batch")
    public ResponseEntity<List<QRCodeResponse>> generateBatchQRCodes(@Valid @RequestBody List<QRCodeGenerateRequest> requests) {
        log.info("Received request to generate {} QR Codes", requests.size());
        List<QRCodeResponse> responses = qrCodeService.generateBatchQRCodes(requests);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get QR Code by ID")
    public ResponseEntity<QRCodeResponse> getQRCodeById(@PathVariable Long id) {
        QRCodeResponse response = qrCodeService.getQRCodeById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/code/{qrCode}")
    @Operation(summary = "Get QR Code by QR Code value")
    public ResponseEntity<QRCodeResponse> getQRCodeByCode(@PathVariable String qrCode) {
        QRCodeResponse response = qrCodeService.getQRCodeByCode(qrCode);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/barcode/{barcode}")
    @Operation(summary = "Get QR Code by Barcode value")
    public ResponseEntity<QRCodeResponse> getQRCodeByBarcode(@PathVariable String barcode) {
        QRCodeResponse response = qrCodeService.getQRCodeByBarcode(barcode);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/task/{taskId}")
    @Operation(summary = "Get QR Codes by Putaway Task ID")
    public ResponseEntity<List<QRCodeResponse>> getQRCodesByTaskId(@PathVariable Long taskId) {
        List<QRCodeResponse> responses = qrCodeService.getQRCodesByTaskId(taskId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/grn/{grnNumber}")
    @Operation(summary = "Get QR Codes by GRN Number")
    public ResponseEntity<List<QRCodeResponse>> getQRCodesByGrnNumber(@PathVariable String grnNumber) {
        List<QRCodeResponse> responses = qrCodeService.getQRCodesByGrnNumber(grnNumber);
        return ResponseEntity.ok(responses);
    }

    @GetMapping
    @Operation(summary = "Get all QR Codes with pagination")
    public ResponseEntity<Page<QRCodeResponse>> getAllQRCodes(@PageableDefault(size = 20) Pageable pageable) {
        Page<QRCodeResponse> responses = qrCodeService.getAllQRCodes(pageable);
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/print")
    @Operation(summary = "Print QR Codes")
    public ResponseEntity<QRCodeResponse> printQRCode(@Valid @RequestBody QRCodePrintRequest request) {
        log.info("Received request to print QR Codes");
        QRCodeResponse response = qrCodeService.printQRCode(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/scan")
    @Operation(summary = "Scan QR Code")
    public ResponseEntity<QRCodeResponse> scanQRCode(@RequestParam String qrCode, @RequestParam String scannedBy) {
        log.info("Received request to scan QR Code: {}", qrCode);
        QRCodeResponse response = qrCodeService.scanQRCode(qrCode, scannedBy);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/generate/image/qr")
    @Operation(summary = "Generate QR Code image")
    public ResponseEntity<byte[]> generateQRCodeImage(@RequestParam String data) {
        byte[] image = qrCodeService.generateQRCodeImage(data);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=qr-code.png")
                .contentType(MediaType.IMAGE_PNG)
                .body(image);
    }

    @GetMapping("/generate/image/barcode")
    @Operation(summary = "Generate Barcode image")
    public ResponseEntity<byte[]> generateBarcodeImage(@RequestParam String data) {
        byte[] image = qrCodeService.generateBarcodeImage(data);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=barcode.png")
                .contentType(MediaType.IMAGE_PNG)
                .body(image);
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Update QR Code status")
    public ResponseEntity<Void> updateQRCodeStatus(@PathVariable Long id, @RequestParam String status) {
        qrCodeService.updateQRCodeStatus(id, status);
        return ResponseEntity.ok().build();
    }
}