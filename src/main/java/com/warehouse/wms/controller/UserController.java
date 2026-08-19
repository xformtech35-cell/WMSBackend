package com.warehouse.wms.controller;

import com.warehouse.wms.dto.CreateUserRequest;
import com.warehouse.wms.dto.UpdateUserRequest;
import com.warehouse.wms.dto.UserResponse;
import com.warehouse.wms.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // ========== USER CRUD OPERATIONS (JSON Request Body) ==========

    @GetMapping
    public ResponseEntity<List<UserResponse>> listAll() {
        return ResponseEntity.ok(userService.listAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getById(id));
    }

    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody CreateUserRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.create(req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest req) {
        return ResponseEntity.ok(userService.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ========== PROFILE PHOTO OPERATIONS (Separate APIs) ==========

    /**
     * Upload profile photo for a user
     * Endpoint: POST /api/users/{id}/profile-photo
     * Request: multipart/form-data with key "file"
     */
    @PostMapping(value = "/{id}/profile-photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserResponse> uploadProfilePhoto(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        UserResponse updatedUser = userService.uploadProfilePhoto(id, file);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Get profile photo for a user
     * Endpoint: GET /api/users/{id}/profile-photo
     */
    @GetMapping("/{id}/profile-photo")
    public ResponseEntity<Resource> getProfilePhoto(@PathVariable Long id) {
        String photoPath = userService.getProfilePhotoPath(id);
        
        if (photoPath == null) {
            return ResponseEntity.notFound().build();
        }

        try {
            Path path = Paths.get(photoPath);
            if (!Files.exists(path) || !Files.isReadable(path)) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = new FileSystemResource(path.toFile());
            String contentType = Files.probeContentType(path);
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType != null ? contentType : "image/jpeg"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + path.getFileName().toString() + "\"")
                    .body(resource);
        } catch (Exception e) {
            log.error("Failed to serve profile photo for user {}: {}", id, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Delete profile photo for a user
     * Endpoint: DELETE /api/users/{id}/profile-photo
     */
    @DeleteMapping("/{id}/profile-photo")
    public ResponseEntity<Void> deleteProfilePhoto(@PathVariable Long id) {
        userService.deleteProfilePhoto(id);
        return ResponseEntity.noContent().build();
    }
}