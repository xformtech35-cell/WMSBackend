// ====== FILE: src/main/java/com/warehouse/wms/util/QRCodeGenerator.java ======
package com.warehouse.wms.util;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.EnumMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.springframework.stereotype.Component;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class QRCodeGenerator {

    private static final int DEFAULT_WIDTH = 300;
    private static final int DEFAULT_HEIGHT = 300;
    private static final int DEFAULT_MARGIN = 4;
    private static final String DEFAULT_IMAGE_FORMAT = "png";

    /**
     * Generate QR Code as Base64 encoded string
     */
    public String generateQRCodeBase64(String data) {
        return generateQRCodeBase64(data, null, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    /**
     * Generate QR Code with label as Base64 encoded string
     */
    public String generateQRCodeBase64(String data, String label) {
        return generateQRCodeBase64(data, label, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    /**
     * Generate QR Code with custom size and label
     */
    public String generateQRCodeBase64(String data, String label, int width, int height) {
        try {
            BufferedImage qrImage = generateQRCodeImage(data, width, height);
            BufferedImage finalImage = label != null && !label.isEmpty() 
                ? addLabelToQRCode(qrImage, label) 
                : qrImage;
            return convertToBase64(finalImage, DEFAULT_IMAGE_FORMAT);
        } catch (Exception e) {
            log.error("Error generating QR Code: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Generate QR Code with custom colors
     */
    public String generateQRCodeBase64(String data, String label, int width, int height, 
                                        int foregroundColor, int backgroundColor) {
        try {
            BufferedImage qrImage = generateQRCodeImage(data, width, height, foregroundColor, backgroundColor);
            BufferedImage finalImage = label != null && !label.isEmpty() 
                ? addLabelToQRCode(qrImage, label) 
                : qrImage;
            return convertToBase64(finalImage, DEFAULT_IMAGE_FORMAT);
        } catch (Exception e) {
            log.error("Error generating QR Code with colors: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Generate QR Code with logo in center
     */
    public String generateQRCodeWithLogo(String data, String label, BufferedImage logo) {
        return generateQRCodeWithLogo(data, label, logo, 400, 400);
    }

    /**
     * Generate QR Code with logo in center with custom size
     */
    public String generateQRCodeWithLogo(String data, String label, BufferedImage logo, 
                                          int width, int height) {
        try {
            BufferedImage qrImage = generateQRCodeImage(data, width, height);
            
            // Add logo in center
            int logoSize = Math.min(width, height) / 5;
            BufferedImage finalImage = addLogoToQR(qrImage, logo, logoSize);
            
            // Add label
            if (label != null && !label.isEmpty()) {
                finalImage = addLabelToQRCode(finalImage, label);
            }
            
            return convertToBase64(finalImage, DEFAULT_IMAGE_FORMAT);
        } catch (Exception e) {
            log.error("Error generating QR Code with logo: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Generate QR Code as BufferedImage
     */
    public BufferedImage generateQRCodeImage(String data, int width, int height) 
            throws WriterException {
        return generateQRCodeImage(data, width, height, Color.BLACK.getRGB(), Color.WHITE.getRGB());
    }

    /**
     * Generate QR Code as BufferedImage with custom colors
     */
    public BufferedImage generateQRCodeImage(String data, int width, int height, 
                                              int foregroundColor, int backgroundColor) 
            throws WriterException {
        Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.MARGIN, DEFAULT_MARGIN);

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, width, height, hints);
        
        MatrixToImageConfig config = new MatrixToImageConfig(foregroundColor, backgroundColor);
        return MatrixToImageWriter.toBufferedImage(bitMatrix, config);
    }

    /**
     * Generate QR Code with custom error correction level
     */
    public BufferedImage generateQRCodeImage(String data, int width, int height, 
                                              ErrorCorrectionLevel errorCorrection) 
            throws WriterException {
        Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.ERROR_CORRECTION, errorCorrection);
        hints.put(EncodeHintType.MARGIN, DEFAULT_MARGIN);

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, width, height, hints);
        
        return MatrixToImageWriter.toBufferedImage(bitMatrix);
    }

    /**
     * Generate multiple QR Codes in one image (grid layout)
     */
    public String generateMultipleQRCodes(Map<String, String> qrDataMap, String title) {
        try {
            int count = qrDataMap.size();
            int qrSize = 200;
            int spacing = 20;
            int cols = Math.min(3, count);
            int rows = (int) Math.ceil((double) count / cols);
            int totalWidth = (qrSize + spacing) * cols + spacing;
            int totalHeight = (qrSize + spacing + 60) * rows + spacing + 80;

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

            for (Map.Entry<String, String> entry : qrDataMap.entrySet()) {
                int row = index / cols;
                int col = index % cols;
                int x = spacing + col * (qrSize + spacing);
                int y = yOffset + row * (qrSize + spacing + 60);

                // Generate QR Code
                BufferedImage qrImage = generateQRCodeImage(entry.getValue(), qrSize, qrSize);
                g2d.drawImage(qrImage, x, y + 30, null);

                // Draw label
                g2d.setColor(Color.BLACK);
                g2d.setFont(new Font("Arial", Font.PLAIN, 12));
                FontMetrics fm = g2d.getFontMetrics();
                int labelX = x + (qrSize - fm.stringWidth(entry.getKey())) / 2;
                g2d.drawString(entry.getKey(), labelX, y + 20);

                index++;
            }

            g2d.dispose();
            return convertToBase64(combined, DEFAULT_IMAGE_FORMAT);
        } catch (Exception e) {
            log.error("Error generating multiple QR Codes: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Add label to QR Code
     */
    public BufferedImage addLabelToQRCode(BufferedImage qrImage, String label) {
        if (label == null || label.isEmpty()) {
            return qrImage;
        }

        int labelHeight = 40;
        int padding = 10;
        int totalHeight = qrImage.getHeight() + labelHeight + padding * 2;
        int width = qrImage.getWidth();

        BufferedImage combined = new BufferedImage(width, totalHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = combined.createGraphics();
        
        // White background
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, width, totalHeight);
        
        // Draw QR Code
        g2d.drawImage(qrImage, 0, padding, null);
        
        // Draw label with background
        g2d.setColor(new Color(240, 240, 240));
        g2d.fillRect(0, qrImage.getHeight() + padding, width, labelHeight + padding);
        
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        FontMetrics fm = g2d.getFontMetrics();
        
        // Handle long labels - truncate with ellipsis
        String displayLabel = label;
        if (fm.stringWidth(label) > width - 20) {
            while (fm.stringWidth(displayLabel + "...") > width - 20 && displayLabel.length() > 3) {
                displayLabel = displayLabel.substring(0, displayLabel.length() - 1);
            }
            displayLabel += "...";
        }
        
        int labelX = (width - fm.stringWidth(displayLabel)) / 2;
        int labelY = qrImage.getHeight() + padding + labelHeight / 2 + fm.getAscent() / 2 - 2;
        g2d.drawString(displayLabel, labelX, labelY);
        
        g2d.dispose();
        return combined;
    }

    /**
     * Add logo to center of QR Code
     */
    public BufferedImage addLogoToQR(BufferedImage qrImage, BufferedImage logo, int logoSize) {
        if (logo == null) {
            return qrImage;
        }

        int qrWidth = qrImage.getWidth();
        int qrHeight = qrImage.getHeight();

        BufferedImage result = new BufferedImage(qrWidth, qrHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = result.createGraphics();

        // Draw QR
        g2d.drawImage(qrImage, 0, 0, null);

        // Resize logo
        Image scaledLogo = logo.getScaledInstance(logoSize, logoSize, Image.SCALE_SMOOTH);
        
        // Calculate center
        int centerX = (qrWidth - logoSize) / 2;
        int centerY = (qrHeight - logoSize) / 2;

        // Draw white background circle
        g2d.setColor(Color.WHITE);
        g2d.fill(new RoundRectangle2D.Double(centerX - 10, centerY - 10, 
                logoSize + 20, logoSize + 20, 20, 20));

        // Draw border
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2));
        g2d.draw(new RoundRectangle2D.Double(centerX - 10, centerY - 10, 
                logoSize + 20, logoSize + 20, 20, 20));

        // Draw logo
        g2d.drawImage(scaledLogo, centerX, centerY, null);
        g2d.dispose();

        return result;
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
     * Generate QR Code and return as byte array
     */
    public byte[] generateQRCodeBytes(String data) {
        try {
            BufferedImage qrImage = generateQRCodeImage(data, DEFAULT_WIDTH, DEFAULT_HEIGHT);
            return convertToByteArray(qrImage, DEFAULT_IMAGE_FORMAT);
        } catch (Exception e) {
            log.error("Error generating QR Code bytes: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Generate QR Code with label and return as byte array
     */
    public byte[] generateQRCodeBytes(String data, String label) {
        try {
            BufferedImage qrImage = generateQRCodeImage(data, DEFAULT_WIDTH, DEFAULT_HEIGHT);
            BufferedImage finalImage = label != null && !label.isEmpty() 
                ? addLabelToQRCode(qrImage, label) 
                : qrImage;
            return convertToByteArray(finalImage, DEFAULT_IMAGE_FORMAT);
        } catch (Exception e) {
            log.error("Error generating QR Code bytes: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Generate QR Code as Data URI (for HTML embedding)
     */
    public String generateQRCodeDataURI(String data, String label) {
        String base64 = generateQRCodeBase64(data, label);
        if (base64 != null) {
            return "data:image/png;base64," + base64;
        }
        return null;
    }

    /**
     * Validate QR Code data
     */
    public boolean isValidQRCodeData(String data) {
        if (data == null || data.isEmpty()) {
            return false;
        }
        // QR Code can encode up to 4296 alphanumeric characters
        // Check for invalid characters that might break encoding
        return data.length() <= 4296 && !data.matches(".*[\\x00-\\x1F].*");
    }

    /**
     * Generate QR Code with gradient colors
     */
    public String generateQRCodeWithGradient(String data, String label, Color startColor, Color endColor) {
        try {
            // Generate base QR Code
            BufferedImage qrImage = generateQRCodeImage(data, DEFAULT_WIDTH, DEFAULT_HEIGHT);
            
            // Apply gradient to QR Code
            BufferedImage gradientQR = applyGradientToQR(qrImage, startColor, endColor);
            
            // Add label
            BufferedImage finalImage = label != null && !label.isEmpty() 
                ? addLabelToQRCode(gradientQR, label) 
                : gradientQR;
            
            return convertToBase64(finalImage, DEFAULT_IMAGE_FORMAT);
        } catch (Exception e) {
            log.error("Error generating QR Code with gradient: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Apply gradient colors to QR Code
     */
    private BufferedImage applyGradientToQR(BufferedImage qrImage, Color startColor, Color endColor) {
        int width = qrImage.getWidth();
        int height = qrImage.getHeight();
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        
        Graphics2D g2d = result.createGraphics();
        g2d.drawImage(qrImage, 0, 0, null);
        
        // Create gradient overlay
        GradientPaint gradient = new GradientPaint(0, 0, startColor, width, height, endColor);
        g2d.setPaint(gradient);
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 0.3f));
        g2d.fillRect(0, 0, width, height);
        
        g2d.dispose();
        return result;
    }

    /**
     * Generate QR Code with rounded corners
     */
    public String generateQRCodeWithRoundedCorners(String data, String label, int cornerRadius) {
        try {
            BufferedImage qrImage = generateQRCodeImage(data, DEFAULT_WIDTH, DEFAULT_HEIGHT);
            BufferedImage roundedImage = makeRoundedCorner(qrImage, cornerRadius);
            BufferedImage finalImage = label != null && !label.isEmpty() 
                ? addLabelToQRCode(roundedImage, label) 
                : roundedImage;
            return convertToBase64(finalImage, DEFAULT_IMAGE_FORMAT);
        } catch (Exception e) {
            log.error("Error generating QR Code with rounded corners: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Make image corners rounded
     */
    private BufferedImage makeRoundedCorner(BufferedImage image, int cornerRadius) {
        int w = image.getWidth();
        int h = image.getHeight();
        BufferedImage rounded = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = rounded.createGraphics();

        g2.setComposite(AlphaComposite.Src);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Color.WHITE);
        g2.fill(new RoundRectangle2D.Float(0, 0, w, h, cornerRadius, cornerRadius));

        g2.setComposite(AlphaComposite.SrcAtop);
        g2.drawImage(image, 0, 0, null);
        g2.dispose();

        return rounded;
    }
}