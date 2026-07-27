package com.warehouse.wms.service;

import com.warehouse.wms.entity.InspectionImage;
import com.warehouse.wms.exception.ResourceNotFoundException;
import com.warehouse.wms.repository.InspectionImageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageService {

    private final InspectionImageRepository imageRepository;

    @Value("${upload.basepath.local}")
    private String localBasePath;

    @Value("${upload.basepath.server}")
    private String serverBasePath;

    private static final String INSPECTION_FOLDER = "inspection_images";

    private String getUploadPath() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            return localBasePath;
        } else {
            return serverBasePath;
        }
    }

    /**
     * Get image by ID
     */
    public byte[] getImage(Long imageId) {
        InspectionImage image = imageRepository.findById(imageId)
            .orElseThrow(() -> new ResourceNotFoundException("Image not found with id: " + imageId));

        try {
            String uploadPath = getUploadPath();
            Path filePath = Paths.get(uploadPath, image.getFilePath());
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            log.error("Error reading image: {}", imageId, e);
            throw new RuntimeException("Failed to read image: " + e.getMessage());
        }
    }

    /**
     * Get image by path
     */
    public byte[] getImageByPath(String path) {
        try {
            String uploadPath = getUploadPath();
            Path filePath = Paths.get(uploadPath, path);
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            log.error("Error reading image by path: {}", path, e);
            throw new RuntimeException("Failed to read image: " + e.getMessage());
        }
    }

    /**
     * Get image metadata
     */
    public InspectionImage getImageMetadata(Long imageId) {
        return imageRepository.findById(imageId)
            .orElseThrow(() -> new ResourceNotFoundException("Image not found with id: " + imageId));
    }

    /**
     * Get thumbnail of image
     */
    public byte[] getThumbnail(Long imageId) {
        try {
            byte[] imageData = getImage(imageId);
            BufferedImage originalImage = ImageIO.read(new java.io.ByteArrayInputStream(imageData));
            
            if (originalImage == null) {
                throw new RuntimeException("Could not read image");
            }

            // Create thumbnail (100x100)
            int thumbWidth = 100;
            int thumbHeight = 100;
            
            BufferedImage thumbnail = new BufferedImage(thumbWidth, thumbHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = thumbnail.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.drawImage(originalImage, 0, 0, thumbWidth, thumbHeight, null);
            g2d.dispose();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(thumbnail, "jpg", baos);
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Error creating thumbnail for image: {}", imageId, e);
            throw new RuntimeException("Failed to create thumbnail: " + e.getMessage());
        }
    }

    /**
     * Save inspection images
     */
    @Transactional
    public List<InspectionImage> saveInspectionImages(Long inboundId, Long lineId,
                                                       List<MultipartFile> imageFiles, Long uploadedBy) {
        List<InspectionImage> savedImages = new ArrayList<>();

        if (imageFiles == null || imageFiles.isEmpty()) {
            return savedImages;
        }

        String uploadPath = getUploadPath();
        String lineFolder = String.format("%s/inbound_%d/line_%d/", INSPECTION_FOLDER, inboundId, lineId);
        Path fullPath = Paths.get(uploadPath, lineFolder);

        try {
            Files.createDirectories(fullPath);

            for (MultipartFile file : imageFiles) {
                if (file == null || file.isEmpty()) {
                    continue;
                }

                String originalFileName = file.getOriginalFilename();
                String fileName = generateFileName(originalFileName);
                Path filePath = fullPath.resolve(fileName);

                // Save file to disk
                Files.write(filePath, file.getBytes());

                // Create image entity
                InspectionImage image = new InspectionImage();
                image.setInboundId(inboundId);
                image.setInboundLineId(lineId);
                image.setFileName(originalFileName != null ? originalFileName : fileName);
                image.setFilePath(lineFolder + fileName);
                image.setFileSize(file.getSize());
                image.setFileType(file.getContentType());
                image.setFileExtension(getFileExtension(fileName));
                image.setUploadedBy(uploadedBy);
                image.setIsDeleted(false);

                savedImages.add(imageRepository.save(image));
            }

            log.info("Saved {} images for line: {}", savedImages.size(), lineId);

        } catch (IOException e) {
            log.error("Error saving images", e);
            throw new RuntimeException("Failed to save images: " + e.getMessage());
        }

        return savedImages;
    }

    /**
     * Get images by line ID
     */
    public List<InspectionImage> getImagesByLineId(Long lineId) {
        return imageRepository.findByInboundLineIdAndIsDeletedFalse(lineId);
    }

    /**
     * Get images by inbound ID
     */
    public List<InspectionImage> getImagesByInboundId(Long inboundId) {
        return imageRepository.findByInboundIdAndIsDeletedFalse(inboundId);
    }

    /**
     * Delete image
     */
    @Transactional
    public void deleteImage(Long imageId) {
        InspectionImage image = imageRepository.findById(imageId)
            .orElseThrow(() -> new ResourceNotFoundException("Image not found with id: " + imageId));

        image.setIsDeleted(true);
        imageRepository.save(image);

        try {
            String uploadPath = getUploadPath();
            Path filePath = Paths.get(uploadPath, image.getFilePath());
            Files.deleteIfExists(filePath);
            log.info("Deleted physical file for image: {}", imageId);
        } catch (IOException e) {
            log.warn("Could not delete physical file for image: {}", imageId, e);
        }
    }

    /**
     * Delete images by line ID
     */
    @Transactional
    public void deleteImagesByLineId(Long lineId) {
        List<InspectionImage> images = imageRepository.findByInboundLineId(lineId);
        for (InspectionImage image : images) {
            deleteImage(image.getId());
        }
        log.info("Deleted all images for line: {}", lineId);
    }

    private String generateFileName(String originalFileName) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        String extension = getFileExtension(originalFileName);
        return String.format("img_%s_%s.%s", timestamp, uuid, extension);
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) return "jpg";
        int dotIndex = fileName.lastIndexOf('.');
        return fileName.substring(dotIndex + 1);
    }
}