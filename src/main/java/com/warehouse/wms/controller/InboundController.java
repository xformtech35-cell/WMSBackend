package com.warehouse.wms.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.warehouse.wms.dto.ApiResponse;
import com.warehouse.wms.dto.CreateInboundDTO;
import com.warehouse.wms.dto.GRNStatusUpdateRequest;
import com.warehouse.wms.dto.GateEntryDTO;
import com.warehouse.wms.dto.GoodsReceivingDTO;
import com.warehouse.wms.dto.InboundDTO;
import com.warehouse.wms.dto.InboundFilterDTO;
import com.warehouse.wms.dto.InboundFilterRequestDTO;
import com.warehouse.wms.dto.InboundImageDTO;
import com.warehouse.wms.dto.QualityInspectionApprovalDTO;
import com.warehouse.wms.dto.QualityInspectionDTO;
import com.warehouse.wms.dto.QualityInspectionItemDTO;
import com.warehouse.wms.dto.UnloadingDTO;
import com.warehouse.wms.entity.InboundStatus;
import com.warehouse.wms.entity.InspectionImage;
import com.warehouse.wms.service.ImageService;
import com.warehouse.wms.service.InboundService;
import com.warehouse.wms.util.SecurityUtils;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/inbound")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class InboundController {

    private final InboundService inboundService;
    private final ObjectMapper objectMapper;
    
    private final ImageService imageService;

    
    

    // ============ 1. CREATE INBOUND FROM PO ============
    @PostMapping
    public ResponseEntity<ApiResponse<InboundDTO>> createInbound(
            @Valid @RequestBody CreateInboundDTO requestDTO) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            InboundDTO created = inboundService.createInbound(requestDTO, userId);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Inbound created successfully", created));
        } catch (Exception e) {
            log.error("Error creating inbound", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Error creating inbound: " + e.getMessage()));
        }
    }

    // ============ 2. GATE ENTRY ============
    @PostMapping("/{inboundId}/gate-entry")
    public ResponseEntity<ApiResponse<InboundDTO>> gateEntry(
            @PathVariable Long inboundId,
            @Valid @RequestBody GateEntryDTO gateEntryDTO) {
        try {
            InboundDTO updated = inboundService.gateEntry(inboundId, gateEntryDTO);
            return ResponseEntity.ok(ApiResponse.success("Gate entry completed successfully", updated));
        } catch (Exception e) {
            log.error("Error in gate entry", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Error in gate entry: " + e.getMessage()));
        }
    }

    // ============ 3. TRUCK UNLOADING ============
    @PostMapping("/{inboundId}/unloading")
    public ResponseEntity<ApiResponse<InboundDTO>> unloading(
            @PathVariable Long inboundId,
            @Valid @RequestBody UnloadingDTO unloadingDTO) {
        try {
            InboundDTO updated = inboundService.unloading(inboundId, unloadingDTO);
            return ResponseEntity.ok(ApiResponse.success("Truck unloading completed successfully", updated));
        } catch (Exception e) {
            log.error("Error in unloading", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Error in unloading: " + e.getMessage()));
        }
    }

    // ============ 4. GOODS RECEIVING ============
    @PostMapping("/{inboundId}/receive")
    public ResponseEntity<ApiResponse<InboundDTO>> goodsReceiving(
            @PathVariable Long inboundId,
            @Valid @RequestBody GoodsReceivingDTO receivingDTO) {
        try {
            InboundDTO updated = inboundService.goodsReceiving(inboundId, receivingDTO);
            return ResponseEntity.ok(ApiResponse.success("Goods receiving completed successfully", updated));
        } catch (Exception e) {
            log.error("Error in goods receiving", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Error in goods receiving: " + e.getMessage()));
        }
    }

    // ============ 5. QUALITY INSPECTION ============
//    @PostMapping("/{inboundId}/quality-inspection")
//    public ResponseEntity<ApiResponse<InboundDTO>> qualityInspection(
//            @PathVariable Long inboundId,
//            @Valid @RequestBody QualityInspectionDTO inspectionDTO) {
//        try {
//            InboundDTO updated = inboundService.qualityInspection(inboundId, inspectionDTO);
//            return ResponseEntity.ok(ApiResponse.success("Quality inspection completed successfully", updated));
//        } catch (Exception e) {
//            log.error("Error in quality inspection", e);
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                .body(ApiResponse.error("Error in quality inspection: " + e.getMessage()));
//        }
//    }
    
    
    
    
    
    
    
//    @PostMapping(value = "/{inboundId}/quality-inspection", 
//            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//public ResponseEntity<ApiResponse<InboundDTO>> qualityInspection(
//        @PathVariable Long inboundId,
//        @RequestPart("inspectionData") String inspectionDataJson,
//        @RequestPart(value = "images", required = false) List<MultipartFile> images) {
//    try {
//        // Parse JSON data
//        ObjectMapper objectMapper = new ObjectMapper();
//        QualityInspectionDTO inspectionDTO = objectMapper.readValue(inspectionDataJson, QualityInspectionDTO.class);
//        
//        // Assign images to items
//        if (images != null && !images.isEmpty()) {
//            int imageIndex = 0;
//            for (QualityInspectionItemDTO item : inspectionDTO.getItems()) {
//                List<MultipartFile> itemImages = new ArrayList<>();
//                // Each item gets images based on some logic
//                // For example, if you have 3 items and 6 images, each gets 2 images
//                int imagesPerItem = images.size() / inspectionDTO.getItems().size();
//                for (int i = 0; i < imagesPerItem && imageIndex < images.size(); i++) {
//                    itemImages.add(images.get(imageIndex++));
//                }
//                item.setImageFiles(itemImages);
//            }
//        }
//        
//        InboundDTO updated = inboundService.qualityInspection(inboundId, inspectionDTO);
//        return ResponseEntity.ok(ApiResponse.success("Quality inspection completed successfully", updated));
//    } catch (Exception e) {
//        log.error("Error in quality inspection", e);
//        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//            .body(ApiResponse.error("Error in quality inspection: " + e.getMessage()));
//    }
//}
//
//@GetMapping("/image/{imageId}")
//public ResponseEntity<byte[]> getImage(@PathVariable Long imageId) {
//    try {
//        byte[] imageData = imageService.getImage(imageId);
//        return ResponseEntity.ok()
//            .contentType(MediaType.IMAGE_JPEG)
//            .body(imageData);
//    } catch (Exception e) {
//        log.error("Error getting image: {}", imageId, e);
//        return ResponseEntity.notFound().build();
//    }
//}
//
//@GetMapping("/line/{lineId}/images")
//public ResponseEntity<ApiResponse<Object>> getImagesByLineId(@PathVariable Long lineId) {
//    try {
//        var images = imageService.getImagesByLineId(lineId);
//        return ResponseEntity.ok(ApiResponse.success("Images retrieved successfully", images));
//    } catch (Exception e) {
//        log.error("Error getting images for line: {}", lineId, e);
//        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//            .body(ApiResponse.error("Failed to get images: " + e.getMessage()));
//    }
//}
    
    
    
    
//    @PostMapping(value = "/{inboundId}/quality-inspection", 
//            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//public ResponseEntity<ApiResponse<InboundDTO>> qualityInspectionAdvanced(
//        @PathVariable Long inboundId,
//        @RequestPart("inspectionData") String inspectionDataJson,
//        @RequestParam(value = "images", required = false) List<MultipartFile> allImages) {
//    
//    try {
//        log.info("Received advanced quality inspection for inbound: {}", inboundId);
//        
//        ObjectMapper objectMapper = new ObjectMapper();
//        QualityInspectionDTO inspectionDTO = objectMapper.readValue(inspectionDataJson, QualityInspectionDTO.class);
//        
//        // If we have images, distribute them to items
//        if (allImages != null && !allImages.isEmpty()) {
//            int imageIndex = 0;
//            for (QualityInspectionItemDTO item : inspectionDTO.getItems()) {
//                List<MultipartFile> itemImages = new ArrayList<>();
//                
//                // Calculate images per item (you can customize this logic)
//                // Option 1: Equal distribution
//                int imagesPerItem = allImages.size() / inspectionDTO.getItems().size();
//                
//                // Option 2: All images to all items (if you want each item to have all images)
//                // int imagesPerItem = allImages.size();
//                
//                for (int i = 0; i < imagesPerItem && imageIndex < allImages.size(); i++) {
//                    itemImages.add(allImages.get(imageIndex++));
//                }
//                item.setImageFiles(itemImages);
//            }
//        }
//        
//        InboundDTO updated = inboundService.qualityInspection(inboundId, inspectionDTO);
//        return ResponseEntity.ok(ApiResponse.success("Quality inspection completed successfully", updated));
//        
//    } catch (Exception e) {
//        log.error("Error in advanced quality inspection", e);
//        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//            .body(ApiResponse.error("Error: " + e.getMessage()));
//    }
//}

/**
 * Quality Inspection with Explicit Per-Item Images
 * This method expects images for each item in separate parameters
 */
@PostMapping(value = "/{inboundId}/quality-inspection", 
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
public ResponseEntity<ApiResponse<InboundDTO>> qualityInspectionExplicit(
        @PathVariable Long inboundId,
        @RequestPart("inspectionData") String inspectionDataJson,
        @RequestParam(value = "images_item_1", required = false) List<MultipartFile> imagesItem1,
        @RequestParam(value = "images_item_2", required = false) List<MultipartFile> imagesItem2,
        @RequestParam(value = "images_item_3", required = false) List<MultipartFile> imagesItem3) {
    
    try {
        log.info("Received explicit quality inspection for inbound: {}", inboundId);
        
        ObjectMapper objectMapper = new ObjectMapper();
        QualityInspectionDTO inspectionDTO = objectMapper.readValue(inspectionDataJson, QualityInspectionDTO.class);
        
        // Map images to specific items
        List<List<MultipartFile>> imageGroups = List.of(
            imagesItem1 != null ? imagesItem1 : new ArrayList<>(),
            imagesItem2 != null ? imagesItem2 : new ArrayList<>(),
            imagesItem3 != null ? imagesItem3 : new ArrayList<>()
        );
        
        for (int i = 0; i < inspectionDTO.getItems().size() && i < imageGroups.size(); i++) {
            QualityInspectionItemDTO item = inspectionDTO.getItems().get(i);
            List<MultipartFile> itemImages = imageGroups.get(i);
            if (itemImages != null && !itemImages.isEmpty()) {
                item.setImageFiles(itemImages);
                log.info("Item {} has {} images", i+1, itemImages.size());
            }
        }
        
        InboundDTO updated = inboundService.qualityInspection(inboundId, inspectionDTO);
        return ResponseEntity.ok(ApiResponse.success("Quality inspection completed successfully", updated));
        
    } catch (Exception e) {
        log.error("Error in explicit quality inspection", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error("Error: " + e.getMessage()));
    }
}

/**
 * Quality Inspection with Dynamic Per-Item Images using Map
 * This method handles any number of items dynamically
 */
@PostMapping(value = "/{inboundId}/quality-inspection-dynamic", 
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
public ResponseEntity<ApiResponse<InboundDTO>> qualityInspectionDynamic(
        @PathVariable Long inboundId,
        @RequestPart("inspectionData") String inspectionDataJson,
        @RequestParam MultiValueMap<String, MultipartFile> allParams) {
    
    try {
        log.info("Received dynamic quality inspection for inbound: {}", inboundId);
        
        ObjectMapper objectMapper = new ObjectMapper();
        QualityInspectionDTO inspectionDTO = objectMapper.readValue(inspectionDataJson, QualityInspectionDTO.class);
        
        // Extract images for each item from the params
        for (int i = 0; i < inspectionDTO.getItems().size(); i++) {
            QualityInspectionItemDTO item = inspectionDTO.getItems().get(i);
            String paramName = "images_item_" + (i + 1);
            List<MultipartFile> itemImages = allParams.get(paramName);
            if (itemImages != null && !itemImages.isEmpty()) {
                item.setImageFiles(itemImages);
                log.info("Item {} has {} images", i+1, itemImages.size());
            }
        }
        
        InboundDTO updated = inboundService.qualityInspection(inboundId, inspectionDTO);
        return ResponseEntity.ok(ApiResponse.success("Quality inspection completed successfully", updated));
        
    } catch (Exception e) {
        log.error("Error in dynamic quality inspection", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error("Error: " + e.getMessage()));
    }
}

// ========================================
// IMAGE VIEWING ENDPOINTS
// ========================================

/**
 * Get all images for an inbound with item details
 */
@GetMapping("/{inboundId}/images")
public ResponseEntity<ApiResponse<InboundImageDTO>> getAllImagesByInbound(
        @PathVariable Long inboundId) {
    try {
        InboundImageDTO result = inboundService.getInboundWithImages(inboundId);
        return ResponseEntity.ok(ApiResponse.success("Images retrieved successfully", result));
    } catch (Exception e) {
        log.error("Error getting images for inbound: {}", inboundId, e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error("Failed to get images: " + e.getMessage()));
    }
}

/**
 * Get images for a specific item
 */
@GetMapping("/{inboundId}/line/{lineId}/images")
public ResponseEntity<ApiResponse<List<InspectionImage>>> getImagesByInboundAndLine(
        @PathVariable Long inboundId,
        @PathVariable Long lineId) {
    try {
        List<InspectionImage> images = imageService.getImagesByLineId(lineId);
        return ResponseEntity.ok(ApiResponse.success("Images retrieved successfully", images));
    } catch (Exception e) {
        log.error("Error getting images for inbound: {} line: {}", inboundId, lineId, e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error("Failed to get images: " + e.getMessage()));
    }
}

/**
 * View image in browser
 */
@GetMapping("/{inboundId}/image/{imageId}/view")
public ResponseEntity<byte[]> viewImage(
        @PathVariable Long inboundId,
        @PathVariable Long imageId) {
    try {
        byte[] imageData = imageService.getImage(imageId);
        InspectionImage image = imageService.getImageMetadata(imageId);
        
        String contentType = image.getFileType() != null ? image.getFileType() : "image/jpeg";
        
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(contentType))
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + image.getFileName() + "\"")
            .body(imageData);
    } catch (Exception e) {
        log.error("Error viewing image: {}", imageId, e);
        return ResponseEntity.notFound().build();
    }
}

/**
 * Get thumbnail for an image
 */
@GetMapping("/{inboundId}/image/{imageId}/thumbnail")
public ResponseEntity<byte[]> getThumbnail(
        @PathVariable Long inboundId,
        @PathVariable Long imageId) {
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
 * Download image
 */
@GetMapping("/{inboundId}/image/{imageId}/download")
public ResponseEntity<byte[]> downloadImage(
        @PathVariable Long inboundId,
        @PathVariable Long imageId) {
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
    
    











@PostMapping("/{inboundId}/quality-inspection/approve-reject")
public ResponseEntity<ApiResponse<InboundDTO>> approveOrRejectQualityInspection(
        @PathVariable Long inboundId,
        @Valid @RequestBody QualityInspectionApprovalDTO approvalDTO) {
    try {
        log.info("Processing quality inspection approval/rejection for inbound: {}", inboundId);
        
        // Validate approval status
        String status = approvalDTO.getApprovalStatus();
        if (!"APPROVED".equals(status) && !"REJECTED".equals(status)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Approval status must be APPROVED or REJECTED"));
        }
        
        // If REJECTED, validate rejection reason
        if ("REJECTED".equals(status)) {
            if (approvalDTO.getRejectionReason() == null || approvalDTO.getRejectionReason().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Rejection reason is required when rejecting"));
            }
        }
        
        InboundDTO updated = inboundService.approveOrRejectQualityInspection(inboundId, approvalDTO);
        
        String message = "APPROVED".equals(status) ? 
            "Quality inspection approved successfully" : 
            "Quality inspection rejected successfully";
        
        return ResponseEntity.ok(ApiResponse.success(message, updated));
        
    } catch (IllegalStateException e) {
        log.warn("Invalid state for approval: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(e.getMessage()));
    } catch (IllegalArgumentException e) {
        log.warn("Invalid argument: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(e.getMessage()));
    } catch (Exception e) {
        log.error("Error processing quality inspection approval/rejection", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error("Error processing approval: " + e.getMessage()));
    }
}


    // ============ 6. GENERATE GRN ============
    @PostMapping("/{inboundId}/generate-grn")
    public ResponseEntity<ApiResponse<InboundDTO>> generateGRN(@PathVariable Long inboundId) {
        try {
            InboundDTO updated = inboundService.generateGRN(inboundId);
            return ResponseEntity.ok(ApiResponse.success("GRN generated successfully", updated));
        } catch (Exception e) {
            log.error("Error generating GRN", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Error generating GRN: " + e.getMessage()));
        }
    }

    
    @PutMapping("/{inboundId}/grn-status")
    public ResponseEntity<InboundDTO> updateGRNStatus(
            @PathVariable Long inboundId,
            @RequestBody GRNStatusUpdateRequest request) {
        
        InboundDTO updatedInbound = inboundService.updateGRNStatus(inboundId, request.getGrnStatus());
        return ResponseEntity.ok(updatedInbound);
    }
    
    
    
    
    // ============ GET INBOUND BY ID ============
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InboundDTO>> getInboundById(@PathVariable Long id) {
        try {
            InboundDTO inbound = inboundService.getInboundById(id);
            return ResponseEntity.ok(ApiResponse.success(inbound));
        } catch (Exception e) {
            log.error("Error getting inbound", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("Inbound not found: " + e.getMessage()));
        }
    }

    // ============ GET INBOUND BY NUMBER ============
    @GetMapping("/number/{inboundNumber}")
    public ResponseEntity<ApiResponse<InboundDTO>> getInboundByNumber(@PathVariable String inboundNumber) {
        try {
            InboundDTO inbound = inboundService.getInboundByNumber(inboundNumber);
            return ResponseEntity.ok(ApiResponse.success(inbound));
        } catch (Exception e) {
            log.error("Error getting inbound by number", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("Inbound not found: " + e.getMessage()));
        }
    }

    // ============ GET ALL INBOUNDS ============
    @GetMapping
    public ResponseEntity<ApiResponse<Page<InboundDTO>>> getAllInbounds(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        try {
            Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
            Page<InboundDTO> inbounds = inboundService.getAllInbounds(pageable);
            return ResponseEntity.ok(ApiResponse.success(inbounds));
        } catch (Exception e) {
            log.error("Error getting all inbounds", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Error getting inbounds: " + e.getMessage()));
        }
    }

    // ============ FILTER INBOUNDS ============
    @PostMapping("/filter")
    public ResponseEntity<ApiResponse<Page<InboundDTO>>> filterInbounds(
            @RequestBody(required = false) InboundFilterRequestDTO filterRequest) {
        try {
            if (filterRequest == null) {
                filterRequest = InboundFilterRequestDTO.builder()
                    .filters(InboundFilterDTO.builder().build())
                    .build();
            }
            
            if (filterRequest.getFilters() == null) {
                filterRequest.setFilters(InboundFilterDTO.builder().build());
            }
            
            Sort.Direction direction = filterRequest.getSortDir().equalsIgnoreCase("desc") ? 
                Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(
                filterRequest.getPage(), 
                filterRequest.getSize(), 
                Sort.by(direction, filterRequest.getSortBy())
            );
            
            Page<InboundDTO> result = inboundService.filterInbounds(filterRequest.getFilters(), pageable);
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            log.error("Error filtering inbounds", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Error filtering inbounds: " + e.getMessage()));
        }
    }

    // ============ GET INBOUNDS BY STATUS ============
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<InboundDTO>>> getInboundsByStatus(@PathVariable InboundStatus status) {
        try {
            List<InboundDTO> inbounds = inboundService.getInboundsByStatus(status);
            return ResponseEntity.ok(ApiResponse.success(inbounds));
        } catch (Exception e) {
            log.error("Error getting inbounds by status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Error getting inbounds: " + e.getMessage()));
        }
    }
    
    
 // ====== FILE: src/main/java/com/warehouse/wms/controller/InboundController.java ======
 // Add this endpoint

 /**
  * Get all inbounds with GRN status APPROVED with search and pagination
  */
 @GetMapping("/grn-status/APPROVED")
 public ResponseEntity<ApiResponse<Page<InboundDTO>>> getInboundsByGrnStatusApproved(
         @RequestParam(required = false) String search,
         @RequestParam(required = false) Boolean barcodeGenerate,
         @RequestParam(required = false) Boolean taskAssigned,
         @RequestParam(defaultValue = "0") int page,
         @RequestParam(defaultValue = "10") int size,
         @RequestParam(defaultValue = "createdAt") String sortBy,
         @RequestParam(defaultValue = "desc") String sortDir) {
     try {
         log.info("📦 Fetching inbounds with GRN status APPROVED - search: {}, page: {}, size: {}", 
                  search, page, size);
         
         Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? 
             Sort.Direction.DESC : Sort.Direction.ASC;
         Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
         
         Page<InboundDTO> inbounds = inboundService.getInboundsByGrnStatusApproved(search,barcodeGenerate, taskAssigned, pageable);
         
         return ResponseEntity.ok(ApiResponse.success("GRN APPROVED inbounds fetched successfully", inbounds));
     } catch (Exception e) {
         log.error("Error fetching GRN APPROVED inbounds", e);
         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
             .body(ApiResponse.error("Error fetching GRN APPROVED inbounds: " + e.getMessage()));
     }
 }
}