package com.warehouse.wms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "wms_inspection_image")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InspectionImage {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "inbound_line_id", nullable = false)
    private Long inboundLineId;
    
    @Column(name = "inbound_id", nullable = false)
    private Long inboundId;
    
    @Column(name = "file_name", nullable = false)
    private String fileName;
    
    @Column(name = "file_path", nullable = false)
    private String filePath;
    
    @Column(name = "file_size")
    private Long fileSize;
    
    @Column(name = "file_type")
    private String fileType;
    
    @Column(name = "file_extension")
    private String fileExtension;
    
    @Column(name = "uploaded_by")
    private Long uploadedBy;
    
    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;
    
    @CreationTimestamp
    @Column(name = "uploaded_at", updatable = false)
    private LocalDateTime uploadedAt;
    
    @Column(name = "is_deleted")
    private Boolean isDeleted = false;
}