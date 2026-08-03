// ====== FILE: src/main/java/com/warehouse/wms/util/BarcodeGenerator.java ======
package com.warehouse.wms.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.Code128Writer;
import com.google.zxing.oned.Code39Writer;
import com.google.zxing.oned.EAN13Writer;
import com.google.zxing.oned.UPCAWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.EnumMap;
import java.util.Map;

@Slf4j
@Component
public class BarcodeGenerator {

    private static final int DEFAULT_WIDTH = 300;
    private static final int DEFAULT_HEIGHT = 100;
    private static final int DEFAULT_MARGIN = 10;
    private static final String DEFAULT_IMAGE_FORMAT = "png";

    /**
     * Generate Barcode as Base64 encoded string
     */
    public String generateBarcodeBase64(String data) {
        return generateBarcodeBase64(data, null, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    /**
     * Generate Barcode with label as Base64 encoded string
     */
    public String generateBarcodeBase64(String data, String label) {
        return generateBarcodeBase64(data, label, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    /**
     * Generate Barcode with custom size and label
     */
    public String generateBarcodeBase64(String data, String label, int width, int height) {
        try {
            BufferedImage barcodeImage = generateBarcodeImage(data, width, height);
            BufferedImage finalImage = label != null && !label.isEmpty() 
                ? addLabelToBarcode(barcodeImage, label) 
                : barcodeImage;
            return convertToBase64(finalImage, DEFAULT_IMAGE_FORMAT);
        } catch (Exception e) {
            log.error("Error generating Barcode: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Generate Barcode with custom colors
     */
    public String generateBarcodeBase64(String data, String label, int width, int height, 
                                         int foregroundColor, int backgroundColor) {
        try {
            BufferedImage barcodeImage = generateBarcodeImage(data, width, height, foregroundColor, backgroundColor);
            BufferedImage finalImage = label != null && !label.isEmpty() 
                ? addLabelToBarcode(barcodeImage, label) 
                : barcodeImage;
            return convertToBase64(finalImage, DEFAULT_IMAGE_FORMAT);
        } catch (Exception e) {
            log.error("Error generating Barcode with colors: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Generate Barcode with specific barcode format
     */
    public String generateBarcodeBase64(String data, String label, BarcodeFormat format) {
        return generateBarcodeBase64(data, label, format, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    /**
     * Generate Barcode with specific barcode format and custom size
     */
    public String generateBarcodeBase64(String data, String label, BarcodeFormat format, int width, int height) {
        try {
            BufferedImage barcodeImage = generateBarcodeImage(data, format, width, height);
            BufferedImage finalImage = label != null && !label.isEmpty() 
                ? addLabelToBarcode(barcodeImage, label) 
                : barcodeImage;
            return convertToBase64(finalImage, DEFAULT_IMAGE_FORMAT);
        } catch (Exception e) {
            log.error("Error generating Barcode: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Generate Barcode as BufferedImage
     */
    public BufferedImage generateBarcodeImage(String data, int width, int height) 
            throws WriterException {
        return generateBarcodeImage(data, BarcodeFormat.CODE_128, width, height);
    }

    /**
     * Generate Barcode as BufferedImage with custom colors
     */
    public BufferedImage generateBarcodeImage(String data, int width, int height, 
                                               int foregroundColor, int backgroundColor) 
            throws WriterException {
        return generateBarcodeImage(data, BarcodeFormat.CODE_128, width, height, 
                                    foregroundColor, backgroundColor);
    }

    /**
     * Generate Barcode as BufferedImage with specific format
     */
    public BufferedImage generateBarcodeImage(String data, BarcodeFormat format, int width, int height) 
            throws WriterException {
        Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, DEFAULT_MARGIN);

        com.google.zxing.Writer writer = getWriter(format);
        BitMatrix bitMatrix = writer.encode(data, format, width, height, hints);
        
        BufferedImage barcodeImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
        
        // Add human-readable text below barcode
        return addHumanReadableText(barcodeImage, data);
    }

    /**
     * Generate Barcode as BufferedImage with specific format and colors
     */
    public BufferedImage generateBarcodeImage(String data, BarcodeFormat format, int width, int height,
                                               int foregroundColor, int backgroundColor) 
            throws WriterException {
        Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, DEFAULT_MARGIN);

        com.google.zxing.Writer writer = getWriter(format);
        BitMatrix bitMatrix = writer.encode(data, format, width, height, hints);
        
        MatrixToImageConfig config = new MatrixToImageConfig(foregroundColor, backgroundColor);
        BufferedImage barcodeImage = MatrixToImageWriter.toBufferedImage(bitMatrix, config);
        
        // Add human-readable text below barcode
        return addHumanReadableText(barcodeImage, data);
    }

    /**
     * Get the appropriate writer for the barcode format
     */
    private com.google.zxing.Writer getWriter(BarcodeFormat format) {
        switch (format) {
            case CODE_128:
                return new Code128Writer();
            case CODE_39:
                return new Code39Writer();
            case EAN_13:
                return new EAN13Writer();
            case UPC_A:
                return new UPCAWriter();
            default:
                return new Code128Writer();
        }
    }

    /**
     * Add human-readable text below barcode
     */
    private BufferedImage addHumanReadableText(BufferedImage barcodeImage, String text) {
        int textHeight = 30;
        int padding = 10;
        int totalHeight = barcodeImage.getHeight() + textHeight + padding;
        int width = barcodeImage.getWidth();

        BufferedImage combined = new BufferedImage(width, totalHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = combined.createGraphics();

        // White background
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, width, totalHeight);

        // Draw barcode
        g2d.drawImage(barcodeImage, 0, 0, null);

        // Draw text
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
        FontMetrics fm = g2d.getFontMetrics();
        
        // Handle long text - truncate with ellipsis
        String displayText = text;
        if (fm.stringWidth(text) > width - 20) {
            while (fm.stringWidth(displayText + "...") > width - 20 && displayText.length() > 3) {
                displayText = displayText.substring(0, displayText.length() - 1);
            }
            displayText += "...";
        }
        
        int textX = (width - fm.stringWidth(displayText)) / 2;
        int textY = barcodeImage.getHeight() + padding + textHeight / 2 + fm.getAscent() / 2 - 2;
        g2d.drawString(displayText, textX, textY);

        g2d.dispose();
        return combined;
    }

    /**
     * Add label to barcode (above the barcode)
     */
    public BufferedImage addLabelToBarcode(BufferedImage barcodeImage, String label) {
        if (label == null || label.isEmpty()) {
            return barcodeImage;
        }

        int labelHeight = 30;
        int padding = 5;
        int totalHeight = barcodeImage.getHeight() + labelHeight + padding * 2;
        int width = Math.max(barcodeImage.getWidth(), 200);

        BufferedImage combined = new BufferedImage(width, totalHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = combined.createGraphics();

        // White background
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, width, totalHeight);

        // Draw label with background
        g2d.setColor(new Color(240, 240, 240));
        g2d.fillRect(0, 0, width, labelHeight + padding);

        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        FontMetrics fm = g2d.getFontMetrics();
        
        // Handle long labels
        String displayLabel = label;
        if (fm.stringWidth(label) > width - 20) {
            while (fm.stringWidth(displayLabel + "...") > width - 20 && displayLabel.length() > 3) {
                displayLabel = displayLabel.substring(0, displayLabel.length() - 1);
            }
            displayLabel += "...";
        }
        
        int labelX = (width - fm.stringWidth(displayLabel)) / 2;
        int labelY = labelHeight / 2 + fm.getAscent() / 2 - 2;
        g2d.drawString(displayLabel, labelX, labelY);

        // Draw barcode below label
        g2d.drawImage(barcodeImage, (width - barcodeImage.getWidth()) / 2, labelHeight + padding, null);

        g2d.dispose();
        return combined;
    }

    /**
     * Generate multiple barcodes in one image (grid layout)
     */
    public String generateMultipleBarcodes(Map<String, String> barcodeData, String title) {
        try {
            int count = barcodeData.size();
            int barcodeWidth = 250;
            int barcodeHeight = 80;
            int spacing = 20;
            int cols = Math.min(2, count);
            int rows = (int) Math.ceil((double) count / cols);
            int totalWidth = (barcodeWidth + spacing) * cols + spacing;
            int totalHeight = (barcodeHeight + spacing + 50) * rows + spacing + 80;

            BufferedImage combined = new BufferedImage(totalWidth, totalHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = combined.createGraphics();
            
            // White background
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, totalWidth, totalHeight);

            // Draw title
            if (title != null && !title.isEmpty()) {
                g2d.setColor(Color.BLACK);
                g2d.setFont(new Font("Arial", Font.BOLD, 18));
                FontMetrics fm = g2d.getFontMetrics();
                int titleX = (totalWidth - fm.stringWidth(title)) / 2;
                g2d.drawString(title, titleX, 40);
            }

            int index = 0;
            int yOffset = title != null && !title.isEmpty() ? 60 : 20;

            for (Map.Entry<String, String> entry : barcodeData.entrySet()) {
                int row = index / cols;
                int col = index % cols;
                int x = spacing + col * (barcodeWidth + spacing);
                int y = yOffset + row * (barcodeHeight + spacing + 50);

                // Generate barcode
                BufferedImage barcode = generateBarcodeImage(entry.getValue(), barcodeWidth, barcodeHeight);
                BufferedImage labeledBarcode = addLabelToBarcode(barcode, entry.getKey());
                
                g2d.drawImage(labeledBarcode, x, y, null);

                index++;
            }

            g2d.dispose();
            return convertToBase64(combined, DEFAULT_IMAGE_FORMAT);
        } catch (Exception e) {
            log.error("Error generating multiple barcodes: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Convert BufferedImage to Base64
     */
    public String convertToBase64(BufferedImage image, String format) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, format, baos);
            byte[] imageBytes = baos.toByteArray();
            return Base64.getEncoder().encodeToString(imageBytes);
        } catch (IOException e) {
            log.error("Error converting image to Base64: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Convert BufferedImage to byte array
     */
    public byte[] convertToByteArray(BufferedImage image, String format) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, format, baos);
            return baos.toByteArray();
        } catch (IOException e) {
            log.error("Error converting image to byte array: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Generate Barcode and return as byte array
     */
    public byte[] generateBarcodeBytes(String data) {
        try {
            BufferedImage barcodeImage = generateBarcodeImage(data, DEFAULT_WIDTH, DEFAULT_HEIGHT);
            return convertToByteArray(barcodeImage, DEFAULT_IMAGE_FORMAT);
        } catch (Exception e) {
            log.error("Error generating barcode bytes: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Generate Barcode with label and return as byte array
     */
    public byte[] generateBarcodeBytes(String data, String label) {
        try {
            BufferedImage barcodeImage = generateBarcodeImage(data, DEFAULT_WIDTH, DEFAULT_HEIGHT);
            BufferedImage finalImage = label != null && !label.isEmpty() 
                ? addLabelToBarcode(barcodeImage, label) 
                : barcodeImage;
            return convertToByteArray(finalImage, DEFAULT_IMAGE_FORMAT);
        } catch (Exception e) {
            log.error("Error generating barcode bytes: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Generate Barcode as Data URI (for HTML embedding)
     */
    public String generateBarcodeDataURI(String data, String label) {
        String base64 = generateBarcodeBase64(data, label);
        if (base64 != null) {
            return "data:image/png;base64," + base64;
        }
        return null;
    }

    /**
     * Validate barcode data
     */
    public boolean isValidBarcodeData(String data) {
        if (data == null || data.isEmpty()) {
            return false;
        }
        // Check for invalid characters
        // CODE128 can encode ASCII 0-127
        // EAN-13 requires exactly 13 digits
        // UPC-A requires exactly 12 digits
        return data.length() <= 128 && !data.matches(".*[\\x00-\\x1F].*");
    }

    /**
     * Generate EAN-13 barcode (requires exactly 13 digits)
     */
    public String generateEAN13Barcode(String data, String label) {
        try {
            if (data.length() != 13 || !data.matches("\\d+")) {
                throw new IllegalArgumentException("EAN-13 requires exactly 13 digits");
            }
            BufferedImage barcodeImage = generateBarcodeImage(data, BarcodeFormat.EAN_13, DEFAULT_WIDTH, DEFAULT_HEIGHT);
            BufferedImage finalImage = label != null && !label.isEmpty() 
                ? addLabelToBarcode(barcodeImage, label) 
                : barcodeImage;
            return convertToBase64(finalImage, DEFAULT_IMAGE_FORMAT);
        } catch (Exception e) {
            log.error("Error generating EAN-13 barcode: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Generate UPC-A barcode (requires exactly 12 digits)
     */
    public String generateUPABarcode(String data, String label) {
        try {
            if (data.length() != 12 || !data.matches("\\d+")) {
                throw new IllegalArgumentException("UPC-A requires exactly 12 digits");
            }
            BufferedImage barcodeImage = generateBarcodeImage(data, BarcodeFormat.UPC_A, DEFAULT_WIDTH, DEFAULT_HEIGHT);
            BufferedImage finalImage = label != null && !label.isEmpty() 
                ? addLabelToBarcode(barcodeImage, label) 
                : barcodeImage;
            return convertToBase64(finalImage, DEFAULT_IMAGE_FORMAT);
        } catch (Exception e) {
            log.error("Error generating UPC-A barcode: {}", e.getMessage(), e);
            return null;
        }
    }
    /**
     * Generate Code 39 barcode
     */
    public String generateCode39Barcode(String data, String label) {
        try {
            BufferedImage barcodeImage = generateBarcodeImage(data, BarcodeFormat.CODE_39, DEFAULT_WIDTH, DEFAULT_HEIGHT);
            BufferedImage finalImage = label != null && !label.isEmpty() 
                ? addLabelToBarcode(barcodeImage, label) 
                : barcodeImage;
            return convertToBase64(finalImage, DEFAULT_IMAGE_FORMAT);
        } catch (Exception e) {
            log.error("Error generating Code 39 barcode: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Generate barcode with custom font for human-readable text
     */
    public BufferedImage generateBarcodeWithCustomFont(String data, String label, Font font) 
            throws WriterException {
        BufferedImage barcodeImage = generateBarcodeImage(data, DEFAULT_WIDTH, DEFAULT_HEIGHT);
        
        int textHeight = 30;
        int padding = 10;
        int totalHeight = barcodeImage.getHeight() + textHeight + padding;
        int width = barcodeImage.getWidth();

        BufferedImage combined = new BufferedImage(width, totalHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = combined.createGraphics();

        // White background
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, width, totalHeight);

        // Draw barcode
        g2d.drawImage(barcodeImage, 0, 0, null);

        // Draw text with custom font
        g2d.setColor(Color.BLACK);
        g2d.setFont(font != null ? font : new Font("Arial", Font.PLAIN, 12));
        FontMetrics fm = g2d.getFontMetrics();
        int textX = (width - fm.stringWidth(data)) / 2;
        int textY = barcodeImage.getHeight() + padding + textHeight / 2 + fm.getAscent() / 2 - 2;
        g2d.drawString(data, textX, textY);

        g2d.dispose();
        return combined;
    }

    /**
     * Generate barcode with DPI support
     * FIXED: Removed setProperty and properly handle DPI scaling
     */
    public String generateBarcodeWithDPI(String data, String label, int width, int height, int dpi) {
        try {
            // Scale dimensions based on DPI (72 DPI is standard screen resolution)
            double scaleFactor = dpi / 72.0;
            int scaledWidth = (int) (width * scaleFactor);
            int scaledHeight = (int) (height * scaleFactor);
            
            // Generate barcode with scaled dimensions
            BufferedImage barcodeImage = generateBarcodeImage(data, scaledWidth, scaledHeight);
            BufferedImage finalImage = label != null && !label.isEmpty() 
                ? addLabelToBarcode(barcodeImage, label) 
                : barcodeImage;
            
            // If we need to set DPI metadata, we need to use a different approach
            // For PNG, we can't set DPI easily, but we can return the image with proper scaling
            // The DPI is effectively set by the scaling factor
            
            log.info("Generated barcode with {} DPI (scaled from {}x{} to {}x{})", 
                     dpi, width, height, scaledWidth, scaledHeight);
            
            return convertToBase64(finalImage, DEFAULT_IMAGE_FORMAT);
        } catch (Exception e) {
            log.error("Error generating barcode with DPI: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Generate barcode with DPI support and return as byte array
     */
    public byte[] generateBarcodeWithDPIAsBytes(String data, String label, int width, int height, int dpi) {
        try {
            double scaleFactor = dpi / 72.0;
            int scaledWidth = (int) (width * scaleFactor);
            int scaledHeight = (int) (height * scaleFactor);
            
            BufferedImage barcodeImage = generateBarcodeImage(data, scaledWidth, scaledHeight);
            BufferedImage finalImage = label != null && !label.isEmpty() 
                ? addLabelToBarcode(barcodeImage, label) 
                : barcodeImage;
            
            return convertToByteArray(finalImage, DEFAULT_IMAGE_FORMAT);
        } catch (Exception e) {
            log.error("Error generating barcode with DPI as bytes: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Generate barcode with high resolution (300 DPI) for printing
     */
    public String generateHighResBarcode(String data, String label) {
        return generateBarcodeWithDPI(data, label, DEFAULT_WIDTH, DEFAULT_HEIGHT, 300);
    }

    /**
     * Generate barcode with very high resolution (600 DPI) for professional printing
     */
    public String generateProfessionalBarcode(String data, String label) {
        return generateBarcodeWithDPI(data, label, DEFAULT_WIDTH, DEFAULT_HEIGHT, 600);
    }
}