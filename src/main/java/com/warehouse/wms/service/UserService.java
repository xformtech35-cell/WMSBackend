package com.warehouse.wms.service;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.warehouse.wms.dto.CreateUserRequest;
import com.warehouse.wms.dto.UpdateUserRequest;
import com.warehouse.wms.dto.UserResponse;
import com.warehouse.wms.entity.User;
import com.warehouse.wms.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleService roleService;
    private final FileUploadService fileUploadService;

    @Transactional(readOnly = true)
    public List<UserResponse> listAll() {
        return userRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserResponse getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Transactional
    public UserResponse create(CreateUserRequest req) {
        // Validate username uniqueness
        if (userRepository.findByUsername(req.getUsername()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
        }

        User user = new User();
        user.setUsername(req.getUsername());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setRole(roleService.findByNameOrThrow(req.getRole()));

        // Set profile fields
        setProfileFields(user, req.getFullName(), req.getMobileNumber(), 
                req.getDesignation(), req.getEmployeeId(), req.getEmail(),
                req.getDepartment(), req.getLocation(), req.getBio());

        user.setJoiningDate(LocalDateTime.now());
        user.setIsActive(true);

        User savedUser = userRepository.save(user);
        log.info("User created successfully with ID: {}", savedUser.getId());

        return toResponse(savedUser);
    }

    @Transactional
    public UserResponse update(Long id, UpdateUserRequest req) {
        User user = findOrThrow(id);

        // Update username if provided
        if (req.getUsername() != null && !req.getUsername().isBlank()) {
            if (!req.getUsername().equals(user.getUsername()) && 
                userRepository.findByUsername(req.getUsername()).isPresent()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
            }
            user.setUsername(req.getUsername());
        }

        // Update role if provided
        if (req.getRole() != null && !req.getRole().isBlank()) {
            user.setRole(roleService.findByNameOrThrow(req.getRole()));
        }

        // Update password if provided
        if (req.getPassword() != null && !req.getPassword().isBlank()) {
            if (req.getPassword().length() < 8) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password must be at least 8 characters");
            }
            user.setPassword(passwordEncoder.encode(req.getPassword()));
        }

        // Update profile fields
        setProfileFields(user, req.getFullName(), req.getMobileNumber(),
                req.getDesignation(), req.getEmployeeId(), req.getEmail(),
                req.getDepartment(), req.getLocation(), req.getBio());

        // Update active status
        if (req.getIsActive() != null) {
            user.setIsActive(req.getIsActive());
        }

        User updatedUser = userRepository.save(user);
        log.info("User updated successfully with ID: {}", updatedUser.getId());

        return toResponse(updatedUser);
    }

    @Transactional
    public void delete(Long id) {
        User user = findOrThrow(id);
        
        // Delete profile photo if exists
        if (user.getProfilePhotoPath() != null) {
            fileUploadService.deleteProfilePhoto(user.getId(), user.getProfilePhotoPath());
        }
        
        userRepository.deleteById(id);
        log.info("User deleted successfully with ID: {}", id);
    }

    // ========== PROFILE PHOTO OPERATIONS ==========

    @Transactional
    public UserResponse uploadProfilePhoto(Long id, MultipartFile file) {
        User user = findOrThrow(id);

        // Delete old photo if exists
        if (user.getProfilePhotoPath() != null) {
            fileUploadService.deleteProfilePhoto(user.getId(), user.getProfilePhotoPath());
        }

        try {
            var result = fileUploadService.uploadProfilePhoto(user.getId(), file);
            user.setProfilePhotoPath(result.filePath());
            user.setProfilePhotoUrl(result.fileUrl());
            User updatedUser = userRepository.save(user);
            log.info("Profile photo uploaded successfully for user: {}", id);
            return toResponse(updatedUser);
        } catch (IOException e) {
            log.error("Failed to upload profile photo for user {}: {}", id, e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload profile photo: " + e.getMessage());
        }
    }

    @Transactional
    public void deleteProfilePhoto(Long id) {
        User user = findOrThrow(id);
        if (user.getProfilePhotoPath() != null) {
            boolean deleted = fileUploadService.deleteProfilePhoto(user.getId(), user.getProfilePhotoPath());
            if (deleted) {
                user.setProfilePhotoPath(null);
                user.setProfilePhotoUrl(null);
                userRepository.save(user);
                log.info("Profile photo deleted successfully for user: {}", id);
            } else {
                log.warn("Profile photo not found for user: {}", id);
            }
        } else {
            log.warn("No profile photo to delete for user: {}", id);
        }
    }

    public String getProfilePhotoPath(Long id) {
        User user = findOrThrow(id);
        if (user.getProfilePhotoPath() == null) {
            return null;
        }
        
        Path path = fileUploadService.getProfilePhotoPath(user.getId(), user.getProfilePhotoPath());
        return path != null ? path.toString() : null;
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private User findOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with ID: " + id));
    }

    private void setProfileFields(User user, String fullName, String mobileNumber, 
                                   String designation, String employeeId, String email,
                                   String department, String location, String bio) {
        if (fullName != null) user.setFullName(fullName);
        if (mobileNumber != null) user.setMobileNumber(mobileNumber);
        if (designation != null) user.setDesignation(designation);
        if (employeeId != null) user.setEmployeeId(employeeId);
        if (email != null) user.setEmail(email);
        if (department != null) user.setDepartment(department);
        if (location != null) user.setLocation(location);
        if (bio != null) user.setBio(bio);
    }

    private UserResponse toResponse(User u) {
        var permissions = u.getRole().getPermissions().stream()
                .map(Enum::name)
                .collect(Collectors.toList());

        return new UserResponse(
                u.getId(),
                u.getUsername(),
                u.getRole().getName(),
                permissions,
                u.getFullName(),
                u.getMobileNumber(),
                u.getDesignation(),
                u.getEmployeeId(),
                u.getEmail(),
                u.getDepartment(),
                u.getLocation(),
                u.getBio(),
                u.getProfilePhotoUrl() != null ? u.getProfilePhotoUrl() : 
                        (u.getProfilePhotoPath() != null ? "/api/users/" + u.getId() + "/profile-photo" : null),
                u.getIsActive(),
                u.getJoiningDate(),
                u.getLastLoginAt(),
                u.getCreatedAt(),
                u.getUpdatedAt()
        );
    }
}