// ====== FILE: src/main/java/com/warehouse/wms/service/QRCodeGeneratorService.java ======

package com.warehouse.wms.service;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.springframework.stereotype.Service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.Writer;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.datamatrix.DataMatrixWriter;
import com.google.zxing.oned.Code128Writer;
import com.google.zxing.oned.Code39Writer;
import com.google.zxing.oned.EAN13Writer;
import com.google.zxing.oned.EAN8Writer;
import com.google.zxing.oned.ITFWriter;
import com.google.zxing.oned.UPCAWriter;
import com.google.zxing.oned.UPCEWriter;
import com.google.zxing.pdf417.PDF417Writer;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.warehouse.wms.dto.QRGenerationRequest;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class QRCodeGeneratorService {

    // ====== MAIN GENERATE METHOD ======
    public String generateQRCode(String data, Map<String, Object> options, String type) {
        try {
            // Extract options with defaults
            int width = options.containsKey("size") ? 
                parseSize(options.get("size").toString()) : 200;
            int height = width;
            String color = options.containsKey("color") ? 
                options.get("color").toString() : "#000000";
            String bgColor = options.containsKey("backgroundColor") ? 
                options.get("backgroundColor").toString() : "#FFFFFF";
            String errorCorrection = options.containsKey("errorCorrection") ? 
                options.get("errorCorrection").toString() : "H";
            boolean showText = options.containsKey("showTextBelow") && 
                (Boolean) options.get("showTextBelow");

            // Parse error correction level
            ErrorCorrectionLevel ecLevel = parseErrorCorrection(errorCorrection);

            // Get barcode format
            BarcodeFormat format = getBarcodeFormat(type);

            // Generate barcode
            Writer writer = getWriter(type);
            BitMatrix bitMatrix;

            if (type.equalsIgnoreCase("QR_CODE")) {
                // For QR Code with error correction
                Map<EncodeHintType, Object> hints = new HashMap<>();
                hints.put(EncodeHintType.ERROR_CORRECTION, ecLevel);
                hints.put(EncodeHintType.MARGIN, 2);
                bitMatrix = writer.encode(data, format, width, height, hints);
            } else {
                bitMatrix = writer.encode(data, format, width, height);
            }

            // Convert to image
            BufferedImage image = MatrixToImageWriter.toBufferedImage(bitMatrix);

            // Add text if needed
            if (showText) {
                image = addTextToImage(image, data);
            }

            // Convert to base64
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "PNG", baos);
            byte[] bytes = baos.toByteArray();

            return Base64.getEncoder().encodeToString(bytes);

        } catch (Exception e) {
            log.error("Error generating QR code: {}", e.getMessage(), e);
            return null;
        }
    }

    // ====== OVERLOADED METHOD FOR DESIGN OPTIONS ======
    public String generateQRCode(String data, QRGenerationRequest.DesignOptions design, String type) {
        Map<String, Object> options = new HashMap<>();
        options.put("size", design.getSize());
        options.put("color", design.getColor());
        options.put("backgroundColor", design.getBackgroundColor());
        options.put("errorCorrection", design.getErrorCorrection());
        options.put("includeLogo", design.getIncludeLogo());
        options.put("logoUrl", design.getLogoUrl());
        options.put("showTextBelow", true);
        return generateQRCode(data, options, type);
    }

    // ====== HELPER: Parse Size ======
    private int parseSize(String size) {
        if (size == null) return 200;
        
        try {
            // Handle "Medium (3cm)" format
            if (size.contains("(") && size.contains("cm")) {
                String num = size.substring(size.indexOf("(") + 1, size.indexOf("cm")).trim();
                return Integer.parseInt(num) * 50; // Convert cm to pixels
            }
            
            // Handle "Small", "Medium", "Large"
            String lower = size.toLowerCase();
            if (lower.contains("small")) return 150;
            if (lower.contains("medium")) return 200;
            if (lower.contains("large")) return 300;
            
            // Handle direct number
            String numOnly = size.replaceAll("[^0-9]", "");
            if (!numOnly.isEmpty()) {
                int val = Integer.parseInt(numOnly);
                return val > 0 ? val : 200;
            }
            
            return 200;
        } catch (Exception e) {
            log.warn("Could not parse size: {}, using default 200", size);
            return 200;
        }
    }

    // ====== HELPER: Parse Error Correction ======
    private ErrorCorrectionLevel parseErrorCorrection(String ec) {
        if (ec == null) return ErrorCorrectionLevel.H;
        
        String upper = ec.toUpperCase();
        if (upper.contains("L")) return ErrorCorrectionLevel.L;
        if (upper.contains("M")) return ErrorCorrectionLevel.M;
        if (upper.contains("Q")) return ErrorCorrectionLevel.Q;
        if (upper.contains("H")) return ErrorCorrectionLevel.H;
        
        // Check for percentage values
        if (upper.contains("7")) return ErrorCorrectionLevel.L;
        if (upper.contains("15")) return ErrorCorrectionLevel.M;
        if (upper.contains("25")) return ErrorCorrectionLevel.Q;
        if (upper.contains("30")) return ErrorCorrectionLevel.H;
        
        return ErrorCorrectionLevel.H;
    }

    // ====== HELPER: Get Barcode Format ======
    private BarcodeFormat getBarcodeFormat(String type) {
        if (type == null) return BarcodeFormat.QR_CODE;

        switch (type.toUpperCase()) {
            case "QR_CODE":
                return BarcodeFormat.QR_CODE;
            case "CODE128":
                return BarcodeFormat.CODE_128;
            case "CODE39":
                return BarcodeFormat.CODE_39;
            case "EAN13":
                return BarcodeFormat.EAN_13;
            case "EAN8":
                return BarcodeFormat.EAN_8;
            case "UPC_A":
                return BarcodeFormat.UPC_A;
            case "UPC_E":
                return BarcodeFormat.UPC_E;
            case "DATAMATRIX":
                return BarcodeFormat.DATA_MATRIX;
            case "PDF417":
                return BarcodeFormat.PDF_417;
            case "ITF14":
                return BarcodeFormat.ITF;
            case "AZTEC":
                return BarcodeFormat.AZTEC;
            default:
                return BarcodeFormat.QR_CODE;
        }
    }

    // ====== HELPER: Get Writer ======
    private Writer getWriter(String type) {
        if (type == null) return new QRCodeWriter();

        switch (type.toUpperCase()) {
            case "QR_CODE":
                return new QRCodeWriter();
            case "CODE128":
                return new Code128Writer();
            case "CODE39":
                return new Code39Writer();
            case "EAN13":
                return new EAN13Writer();
            case "EAN8":
                return new EAN8Writer();
            case "UPC_A":
                return new UPCAWriter();
            case "UPC_E":
                return new UPCEWriter();
            case "DATAMATRIX":
                return new DataMatrixWriter();
            case "PDF417":
                return new PDF417Writer();
            case "ITF14":
                return new ITFWriter();
            case "AZTEC":
                return new com.google.zxing.aztec.AztecWriter();
            default:
                return new QRCodeWriter();
        }
    }

    // ====== HELPER: Add Text to Image ======
    private BufferedImage addTextToImage(BufferedImage image, String text) {
        int width = image.getWidth();
        int height = image.getHeight();
        int textHeight = 30;

        BufferedImage newImage = new BufferedImage(width, height + textHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = newImage.createGraphics();

        // Fill background
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height + textHeight);

        // Draw barcode
        g.drawImage(image, 0, 0, null);

        // Draw text
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.PLAIN, 12));
        FontMetrics fm = g.getFontMetrics();

        // Split text for better display
        String displayText = text.length() > 50 ? text.substring(0, 47) + "..." : text;
        int textWidth = fm.stringWidth(displayText);
        int x = (width - textWidth) / 2;
        int y = height + 20;
        g.drawString(displayText, x, y);

        g.dispose();
        return newImage;
    }

    // ====== GENERATE WITH LOGO ======
    public String generateQRCodeWithLogo(String data, Map<String, Object> options, String type, String logoBase64) {
        String qrImage = generateQRCode(data, options, type);

        if (logoBase64 != null && !logoBase64.isEmpty()) {
            try {
                // Decode logo
                byte[] logoBytes = Base64.getDecoder().decode(logoBase64);
                BufferedImage logo = ImageIO.read(new java.io.ByteArrayInputStream(logoBytes));

                // Decode QR
                byte[] qrBytes = Base64.getDecoder().decode(qrImage);
                BufferedImage qr = ImageIO.read(new java.io.ByteArrayInputStream(qrBytes));

                // Overlay logo
                BufferedImage result = overlayLogo(qr, logo);

                // Convert back to base64
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(result, "PNG", baos);
                return Base64.getEncoder().encodeToString(baos.toByteArray());

            } catch (Exception e) {
                log.error("Error adding logo to QR: {}", e.getMessage());
                return qrImage;
            }
        }

        return qrImage;
    }

    // ====== HELPER: Overlay Logo ======
    private BufferedImage overlayLogo(BufferedImage qr, BufferedImage logo) {
        int qrSize = qr.getWidth();
        int logoSize = qrSize / 4;

        BufferedImage result = new BufferedImage(qrSize, qrSize, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = result.createGraphics();

        // Draw QR
        g.drawImage(qr, 0, 0, null);

        // Draw logo in center with white background
        int x = (qrSize - logoSize) / 2;
        int y = (qrSize - logoSize) / 2;

        g.setColor(Color.WHITE);
        g.fillRect(x - 5, y - 5, logoSize + 10, logoSize + 10);

        g.drawImage(logo, x, y, logoSize, logoSize, null);
        g.dispose();

        return result;
    }

    // ====== HELPER: Generate ZPL Format (For Zebra Printers) ======
    public String generateZPL(String data, String type, int width, int height) {
        StringBuilder zpl = new StringBuilder();
        zpl.append("^XA");
        zpl.append("^FO50,50^BXN,10,200^FD").append(data).append("^FS");
        zpl.append("^FO50,250^A0N,20,20^FD").append(data).append("^FS");
        zpl.append("^XZ");
        return zpl.toString();
    }
}