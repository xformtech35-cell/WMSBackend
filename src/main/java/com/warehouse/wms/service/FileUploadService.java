package com.warehouse.wms.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Service
public class FileUploadService {

    @Value("${upload.basepath.local}")
    private String localUploadPath;

    @Value("${upload.basepath.server}")
    private String serverUploadPath;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    private static final String PROFILE_PHOTO_DIR = "profile-photos";
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final String[] ALLOWED_EXTENSIONS = {"jpg", "jpeg", "png", "gif", "webp"};

    /**
     * Upload profile photo for a user
     */
    public ProfilePhotoUploadResult uploadProfilePhoto(Long userId, MultipartFile file) throws IOException {
        // Validate file
        validateFile(file);

        // Create directory structure
        String uploadDir = localUploadPath + PROFILE_PHOTO_DIR;
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String fileName = String.format("user_%d_%s_%s.%s", userId, timestamp, UUID.randomUUID().toString().substring(0, 8), extension);
        
        // Save file locally
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath);

        // Generate URL
        String fileUrl = baseUrl + "/api/users/" + userId + "/profile-photo";

        log.info("Profile photo uploaded for user {}: {}", userId, fileName);

        return new ProfilePhotoUploadResult(fileName, fileUrl, filePath.toString());
    }

    /**
     * Delete profile photo
     */
    public boolean deleteProfilePhoto(Long userId, String photoPath) {
        try {
            if (photoPath != null && !photoPath.isEmpty()) {
                Path path = Paths.get(photoPath);
                if (Files.exists(path)) {
                    Files.delete(path);
                    log.info("Profile photo deleted for user {}: {}", userId, photoPath);
                    return true;
                }
                
                // Try server path as fallback
                String serverDir = serverUploadPath + PROFILE_PHOTO_DIR;
                String fileName = getFileNameFromPath(photoPath);
                if (fileName != null) {
                    Path serverPath = Paths.get(serverDir, fileName);
                    if (Files.exists(serverPath)) {
                        Files.delete(serverPath);
                        log.info("Profile photo deleted from server for user {}: {}", userId, serverPath);
                        return true;
                    }
                }
            }
        } catch (IOException e) {
            log.error("Failed to delete profile photo for user {}: {}", userId, e.getMessage());
        }
        return false;
    }

    /**
     * Get profile photo path for user
     */
    public Path getProfilePhotoPath(Long userId, String photoPath) {
        if (photoPath == null || photoPath.isEmpty()) {
            return null;
        }
        
        Path path = Paths.get(photoPath);
        if (Files.exists(path) && Files.isReadable(path)) {
            return path;
        }
        
        // Check in server path if not found in local
        String serverDir = serverUploadPath + PROFILE_PHOTO_DIR;
        String fileName = getFileNameFromPath(photoPath);
        if (fileName != null) {
            Path serverPath = Paths.get(serverDir, fileName);
            if (Files.exists(serverPath) && Files.isReadable(serverPath)) {
                return serverPath;
            }
        }
        
        return null;
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds maximum allowed (5MB). Current size: " + file.getSize() + " bytes");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new IllegalArgumentException("Invalid file name");
        }

        String extension = getFileExtension(originalFilename);
        if (extension == null || !isAllowedExtension(extension)) {
            throw new IllegalArgumentException("File type not allowed. Allowed: " + String.join(", ", ALLOWED_EXTENSIONS));
        }

        // Validate content type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("File must be an image. Received: " + contentType);
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return null;
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    private boolean isAllowedExtension(String extension) {
        for (String allowed : ALLOWED_EXTENSIONS) {
            if (allowed.equalsIgnoreCase(extension)) {
                return true;
            }
        }
        return false;
    }

    private String getFileNameFromPath(String path) {
        if (path == null || path.isEmpty()) {
            return null;
        }
        return Paths.get(path).getFileName().toString();
    }

    public record ProfilePhotoUploadResult(String fileName, String fileUrl, String filePath) {}
}