package com.warehouse.wms.util;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;

import javax.imageio.ImageIO;

import org.springframework.stereotype.Component;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.Code128Writer;
import com.google.zxing.oned.Code39Writer;
import com.google.zxing.oned.EAN13Writer;
import com.google.zxing.qrcode.QRCodeWriter;

@Component
public class BarcodeUtils {

    // ====== GENERATE BARCODE (CODE128) ======
    public byte[] generateCode128Barcode(String data, int width, int height) throws WriterException, IOException {
        Code128Writer writer = new Code128Writer();
        BitMatrix bitMatrix = writer.encode(data, BarcodeFormat.CODE_128, width, height);
        return matrixToBytes(bitMatrix);
    }

    // ====== GENERATE BARCODE (CODE39) ======
    public byte[] generateCode39Barcode(String data, int width, int height) throws WriterException, IOException {
        Code39Writer writer = new Code39Writer();
        BitMatrix bitMatrix = writer.encode(data, BarcodeFormat.CODE_39, width, height);
        return matrixToBytes(bitMatrix);
    }

    // ====== GENERATE BARCODE (EAN13) ======
    public byte[] generateEAN13Barcode(String data, int width, int height) throws WriterException, IOException {
        EAN13Writer writer = new EAN13Writer();
        BitMatrix bitMatrix = writer.encode(data, BarcodeFormat.EAN_13, width, height);
        return matrixToBytes(bitMatrix);
    }

    // ====== GENERATE QR CODE ======
    public byte[] generateQRCode(String data, int width, int height) throws WriterException, IOException {
        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix bitMatrix = writer.encode(data, BarcodeFormat.QR_CODE, width, height);
        return matrixToBytes(bitMatrix);
    }

    // ====== GENERATE SHIPPING LABEL WITH BARCODE ======
    public byte[] generateShippingLabelWithBarcode(
            String labelNumber,
            String packageNumber,
            String packageBarcode,
            String soNumber,
            String customerName,
            String customerAddress,
            String itemName,
            Integer quantity,
            Double weight,
            String trackingNumber,
            String status) throws IOException, WriterException {

        // Create label image
        int width = 800;
        int height = 500;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();

        // Background
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, width, height);

        // Border
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(3));
        g2d.drawRect(10, 10, width - 20, height - 20);

        // Header
        g2d.setColor(new Color(0, 51, 102));
        g2d.fillRect(10, 10, width - 20, 50);
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 24));
        g2d.drawString("SHIPPING LABEL", 300, 45);

        // Label Details
        int y = 80;
        int x = 50;
        int spacing = 28;

        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 14));

        // Label Number
        g2d.drawString("LABEL NUMBER:", x, y);
        g2d.setFont(new Font("Arial", Font.PLAIN, 14));
        g2d.drawString(labelNumber, x + 200, y);

        // Package Number
        y += spacing;
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        g2d.drawString("PACKAGE NUMBER:", x, y);
        g2d.setFont(new Font("Arial", Font.PLAIN, 14));
        g2d.drawString(packageNumber, x + 200, y);

        // SO Number
        y += spacing;
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        g2d.drawString("SO NUMBER:", x, y);
        g2d.setFont(new Font("Arial", Font.PLAIN, 14));
        g2d.drawString(soNumber, x + 200, y);

        // Customer
        y += spacing;
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        g2d.drawString("CUSTOMER:", x, y);
        g2d.setFont(new Font("Arial", Font.PLAIN, 14));
        g2d.drawString(customerName, x + 200, y);

        // Address
        y += spacing;
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        g2d.drawString("ADDRESS:", x, y);
        g2d.setFont(new Font("Arial", Font.PLAIN, 14));
        if (customerAddress.length() > 40) {
            g2d.drawString(customerAddress.substring(0, 40), x + 200, y);
            y += spacing;
            g2d.drawString(customerAddress.substring(40), x + 200, y);
        } else {
            g2d.drawString(customerAddress, x + 200, y);
        }

        // Item
        y += spacing;
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        g2d.drawString("ITEM:", x, y);
        g2d.setFont(new Font("Arial", Font.PLAIN, 14));
        g2d.drawString(itemName, x + 200, y);

        // Quantity
        y += spacing;
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        g2d.drawString("QUANTITY:", x, y);
        g2d.setFont(new Font("Arial", Font.PLAIN, 14));
        g2d.drawString(String.valueOf(quantity), x + 200, y);

        // Weight
        y += spacing;
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        g2d.drawString("WEIGHT:", x, y);
        g2d.setFont(new Font("Arial", Font.PLAIN, 14));
        g2d.drawString(weight + " kg", x + 200, y);

        // Tracking
        y += spacing;
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        g2d.drawString("TRACKING:", x, y);
        g2d.setFont(new Font("Arial", Font.PLAIN, 14));
        g2d.drawString(trackingNumber != null ? trackingNumber : "N/A", x + 200, y);

        // Status
        y += spacing;
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        g2d.drawString("STATUS:", x, y);
        g2d.setFont(new Font("Arial", Font.PLAIN, 14));
        g2d.setColor(getStatusColor(status));
        g2d.drawString(status != null ? status : "PRINTED", x + 200, y);

        // Generate and draw barcode
        g2d.setColor(Color.BLACK);
        try {
            byte[] barcodeBytes = generateCode128Barcode(packageBarcode, 300, 80);
            BufferedImage barcodeImage = ImageIO.read(new java.io.ByteArrayInputStream(barcodeBytes));
            g2d.drawImage(barcodeImage, 250, height - 120, null);
        } catch (Exception e) {
            // Draw placeholder if barcode generation fails
            g2d.setColor(Color.BLACK);
            g2d.fillRect(250, height - 110, 300, 60);
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.PLAIN, 14));
            String barcodeText = packageBarcode != null ? packageBarcode : "BARCODE";
            int textWidth = g2d.getFontMetrics().stringWidth(barcodeText);
            g2d.drawString(barcodeText, 250 + (300 - textWidth) / 2, height - 70);
        }

        // Footer
        g2d.setColor(Color.GRAY);
        g2d.setFont(new Font("Arial", Font.PLAIN, 10));
        g2d.drawString("Generated: " + LocalDateTime.now().toString(), 20, height - 15);
        g2d.drawString("© Warehouse Management System", width - 200, height - 15);

        g2d.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "PNG", baos);
        return baos.toByteArray();
    }

    // ====== GENERATE BARCODE LABEL (Simple) ======
    public byte[] generateBarcodeLabel(String barcodeData, String labelText) throws WriterException, IOException {
        int width = 400;
        int height = 200;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();

        // White background
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, width, height);

        // Border
        g2d.setColor(Color.BLACK);
        g2d.drawRect(5, 5, width - 10, height - 10);

        // Generate barcode
        byte[] barcodeBytes = generateCode128Barcode(barcodeData, 350, 80);
        BufferedImage barcodeImage = ImageIO.read(new java.io.ByteArrayInputStream(barcodeBytes));
        g2d.drawImage(barcodeImage, 25, 30, null);

        // Label text
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
        int textWidth = g2d.getFontMetrics().stringWidth(labelText);
        g2d.drawString(labelText, (width - textWidth) / 2, height - 15);

        g2d.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "PNG", baos);
        return baos.toByteArray();
    }

    // ====== GENERATE BARCODE ONLY ======
    public byte[] generateBarcodeOnly(String data, int width, int height) throws WriterException, IOException {
        Code128Writer writer = new Code128Writer();
        BitMatrix bitMatrix = writer.encode(data, BarcodeFormat.CODE_128, width, height);
        return matrixToBytes(bitMatrix);
    }

    // ====== GENERATE BARCODE WITH TEXT ======
    public byte[] generateBarcodeWithText(String data, int width, int height) throws WriterException, IOException {
        int totalHeight = height + 40;
        BufferedImage image = new BufferedImage(width, totalHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();

        // White background
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, width, totalHeight);

        // Generate barcode
        Code128Writer writer = new Code128Writer();
        BitMatrix bitMatrix = writer.encode(data, BarcodeFormat.CODE_128, width, height);
        BufferedImage barcodeImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
        g2d.drawImage(barcodeImage, 0, 0, null);

        // Draw text
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.PLAIN, 14));
        int textWidth = g2d.getFontMetrics().stringWidth(data);
        g2d.drawString(data, (width - textWidth) / 2, height + 30);

        g2d.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "PNG", baos);
        return baos.toByteArray();
    }

    // ====== HELPER METHODS ======

    private byte[] matrixToBytes(BitMatrix bitMatrix) throws IOException {
        BufferedImage image = MatrixToImageWriter.toBufferedImage(bitMatrix);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "PNG", baos);
        return baos.toByteArray();
    }

    private Color getStatusColor(String status) {
        if (status == null) return Color.BLACK;
        switch (status) {
            case "PRINTED": return new Color(0, 0, 255);
            case "SCANNED": return new Color(255, 165, 0);
            case "SHIPPED": return new Color(0, 128, 0);
            case "DELIVERED": return new Color(0, 128, 0);
            case "CANCELLED": return Color.RED;
            default: return Color.BLACK;
        }
    }

    // ====== VALIDATE BARCODE ======
    public boolean validateBarcode(String barcode) {
        if (barcode == null || barcode.isEmpty()) return false;
        // Check if barcode is valid (alphanumeric, min length)
        return barcode.matches("^[A-Z0-9]+$") && barcode.length() >= 6;
    }

    // ====== GENERATE BARCODE DATA ======
    public String generateBarcodeData(String prefix, String number) {
        return prefix + String.format("%08d", System.currentTimeMillis() % 100000000) + number;
    }
}