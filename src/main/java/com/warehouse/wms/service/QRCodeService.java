// ====== FILE: src/main/java/com/warehouse/wms/service/QRCodeService.java ======
package com.warehouse.wms.service;

import com.warehouse.wms.dto.request.QRCodeGenerateRequest;
import com.warehouse.wms.dto.request.QRCodePrintRequest;
import com.warehouse.wms.dto.response.QRCodeResponse;
import com.warehouse.wms.entity.QRCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface QRCodeService {

    QRCodeResponse generateQRCode(QRCodeGenerateRequest request);

    List<QRCodeResponse> generateBatchQRCodes(List<QRCodeGenerateRequest> requests);

    QRCodeResponse getQRCodeById(Long id);

    QRCodeResponse getQRCodeByCode(String qrCode);

    QRCodeResponse getQRCodeByBarcode(String barcode);

    List<QRCodeResponse> getQRCodesByTaskId(Long taskId);

    List<QRCodeResponse> getQRCodesByGrnNumber(String grnNumber);

    QRCodeResponse printQRCode(QRCodePrintRequest request);

    QRCodeResponse scanQRCode(String qrCode, String scannedBy);
    QRCodeResponse scanBarCode(String barCode, String scannedBy);


    QRCodeResponse scanBarcode(String barcode, String scannedBy);

    void updateQRCodeStatus(Long id, String status);

    Page<QRCodeResponse> getAllQRCodes(Pageable pageable);

    byte[] generateQRCodeImage(String data);

    byte[] generateBarcodeImage(String data);

    String generateQRCodeBase64(String data);

    String generateBarcodeBase64(String data);
}