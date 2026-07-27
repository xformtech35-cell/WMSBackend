package com.warehouse.wms.controller;

import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.warehouse.wms.dto.ApiResponse;
import com.warehouse.wms.dto.ImageBase64DTO;
import com.warehouse.wms.entity.InspectionImage;
import com.warehouse.wms.service.ImageService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
@Slf4j
public class ImageController {

    private final ImageService imageService;

    /**
     * Get image by ID
     * GET /api/images/{imageId}
     */
    @GetMapping("/{imageId}")
    public ResponseEntity<byte[]> getImageById(@PathVariable Long imageId) {
        try {
            byte[] imageData = imageService.getImage(imageId);
            InspectionImage image = imageService.getImageMetadata(imageId);
            
            String contentType = image.getFileType() != null ? image.getFileType() : "image/jpeg";
            
            return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + image.getFileName() + "\"")
                .body(imageData);
        } catch (Exception e) {
            log.error("Error getting image: {}", imageId, e);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get image by path (path parameter)
     * GET /api/images/path?path=inspection_images/inbound_1/line_1/img_20260727161520_b3c3a7e2.jpg
     */
    @GetMapping("/path")
    public ResponseEntity<byte[]> getImageByPath(@RequestParam String path) {
        try {
            byte[] imageData = imageService.getImageByPath(path);
            
            String fileName = path.substring(path.lastIndexOf('/') + 1);
            String extension = fileName.substring(fileName.lastIndexOf('.') + 1);
            String contentType = getContentType(extension);
            
            return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"")
                .body(imageData);
        } catch (Exception e) {
            log.error("Error getting image by path: {}", path, e);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get image by full path (path variable)
     * GET /api/images/path/{year}/{month}/{day}/{filename}
     * Example: /api/images/path/2026/07/27/img_20260727161520_b3c3a7e2.jpg
     */
    @GetMapping("/path/{year}/{month}/{day}/{filename}")
    public ResponseEntity<byte[]> getImageByFullPath(
            @PathVariable int year,
            @PathVariable int month,
            @PathVariable int day,
            @PathVariable String filename) {
        try {
            String path = String.format("inspection_images/inbound_%d/line_%d/%s", 1, 1, filename);
            byte[] imageData = imageService.getImageByPath(path);
            
            String extension = filename.substring(filename.lastIndexOf('.') + 1);
            String contentType = getContentType(extension);
            
            return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .body(imageData);
        } catch (Exception e) {
            log.error("Error getting image by path: {}/{}/{}/{}", year, month, day, filename, e);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get image as base64
     * GET /api/images/{imageId}/base64
     */
    @GetMapping("/{imageId}/base64")
    public ResponseEntity<ApiResponse<ImageBase64DTO>> getImageAsBase64(@PathVariable Long imageId) {
        try {
            byte[] imageData = imageService.getImage(imageId);
            InspectionImage image = imageService.getImageMetadata(imageId);
            
            String base64 = java.util.Base64.getEncoder().encodeToString(imageData);
            String dataUri = "data:" + image.getFileType() + ";base64," + base64;
            
            ImageBase64DTO dto = ImageBase64DTO.builder()
                .id(imageId)
                .fileName(image.getFileName())
                .base64Data(dataUri)
                .fileSize(image.getFileSize())
                .fileType(image.getFileType())
                .uploadedAt(image.getUploadedAt())
                .build();
            
            return ResponseEntity.ok(ApiResponse.success("Image retrieved successfully", dto));
        } catch (Exception e) {
            log.error("Error getting image as base64: {}", imageId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to get image: " + e.getMessage()));
        }
    }
    
    
    /**
     * Get image by line ID
     * GET /api/images/line/{lineId}
     */
    @GetMapping("/line/{lineId}")
    public ResponseEntity<ApiResponse<List<InspectionImage>>> getImagesByLineId(@PathVariable Long lineId) {
        try {
            List<InspectionImage> images = imageService.getImagesByLineId(lineId);
            return ResponseEntity.ok(ApiResponse.success("Images retrieved successfully", images));
        } catch (Exception e) {
            log.error("Error getting images for line: {}", lineId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to get images: " + e.getMessage()));
        }
    }

    /**
     * Get image thumbnail
     * GET /api/images/{imageId}/thumbnail
     */
    @GetMapping("/{imageId}/thumbnail")
    public ResponseEntity<byte[]> getThumbnail(@PathVariable Long imageId) {
        try {
            byte[] thumbnailData = imageService.getThumbnail(imageId);
            return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"thumbnail_" + imageId + ".jpg\"")
                .body(thumbnailData);
        } catch (Exception e) {
            log.error("Error getting thumbnail: {}", imageId, e);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Download image as attachment
     * GET /api/images/{imageId}/download
     */
    @GetMapping("/{imageId}/download")
    public ResponseEntity<byte[]> downloadImage(@PathVariable Long imageId) {
        try {
            byte[] imageData = imageService.getImage(imageId);
            InspectionImage image = imageService.getImageMetadata(imageId);
            
            String contentType = image.getFileType() != null ? image.getFileType() : "application/octet-stream";
            
            return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + image.getFileName() + "\"")
                .body(imageData);
        } catch (Exception e) {
            log.error("Error downloading image: {}", imageId, e);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get image by inbound ID
     * GET /api/images/inbound/{inboundId}
     */
    @GetMapping("/inbound/{inboundId}")
    public ResponseEntity<ApiResponse<List<InspectionImage>>> getImagesByInboundId(@PathVariable Long inboundId) {
        try {
            List<InspectionImage> images = imageService.getImagesByInboundId(inboundId);
            return ResponseEntity.ok(ApiResponse.success("Images retrieved successfully", images));
        } catch (Exception e) {
            log.error("Error getting images for inbound: {}", inboundId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to get images: " + e.getMessage()));
        }
    }

    /**
     * Delete image
     * DELETE /api/images/{imageId}
     */
    @DeleteMapping("/{imageId}")
    public ResponseEntity<ApiResponse<Void>> deleteImage(@PathVariable Long imageId) {
        try {
            imageService.deleteImage(imageId);
            return ResponseEntity.ok(ApiResponse.success("Image deleted successfully", null));
        } catch (Exception e) {
            log.error("Error deleting image: {}", imageId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to delete image: " + e.getMessage()));
        }
    }

    private String getContentType(String extension) {
        switch (extension.toLowerCase()) {
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "gif":
                return "image/gif";
            case "bmp":
                return "image/bmp";
            case "webp":
                return "image/webp";
            default:
                return "application/octet-stream";
        }
    }
}