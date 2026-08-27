package com.warehouse.wms.dto.reports;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecentActivityResponse {
    
    // ====== Activity Identification ======
    private String activityId;
    private String activityType;
    private String activityCategory; // INBOUND, OUTBOUND, INVENTORY, SYSTEM
    
    // ====== Description ======
    private String title;
    private String description;
    private String details;
    
    // ====== Reference Numbers ======
    private String referenceNumber; // SO Number, GRN Number, PO Number, etc.
    private String referenceType; // SO, GRN, PO, INV, PKG, etc.
    
    // ====== Status ======
    private String status;
    private String soNumber;
    private String user;
    private String color;


    private String statusColor; // GREEN, YELLOW, RED, BLUE, GRAY
    
    // ====== User Information ======
    private String userId;
    private String userName;
    private String userRole;
    private String userProfileImage;
    
    // ====== Timestamps ======
    private LocalDateTime timestamp;
    private String formattedTime;
    private String relativeTime; // "2 hours ago", "Just now", etc.
    
    // ====== Icons & Styling ======
    private String icon;
    private String iconColor;
    private String backgroundColor;
    private String badge;
    private String badgeColor;
    
    // ====== Actions ======
    private String primaryAction;
    private String primaryActionUrl;
    private String secondaryAction;
    private String secondaryActionUrl;
    
    // ====== Additional Data ======
    private String location;
    private String warehouseId;
    private String zone;
    private String binId;
    private Integer quantity;
    private String uom;
    private Double weight;
    private String priority;
    private Boolean isRead;
    private Boolean isUrgent;
    private List<String> tags;
    private Map<String, Object> metadata;
    
    // ====== Helper Methods ======
    
    public String getFormattedTime() {
        if (timestamp != null) {
            return timestamp.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
        }
        return formattedTime;
    }
    
    public String getRelativeTime() {
        if (timestamp == null) {
            return relativeTime;
        }
        return calculateRelativeTime(timestamp);
    }
    
    private String calculateRelativeTime(LocalDateTime time) {
        LocalDateTime now = LocalDateTime.now();
        long seconds = java.time.Duration.between(time, now).getSeconds();
        
        if (seconds < 60) {
            return "Just now";
        } else if (seconds < 3600) {
            long minutes = seconds / 60;
            return minutes + " minute" + (minutes > 1 ? "s" : "") + " ago";
        } else if (seconds < 86400) {
            long hours = seconds / 3600;
            return hours + " hour" + (hours > 1 ? "s" : "") + " ago";
        } else if (seconds < 604800) {
            long days = seconds / 86400;
            return days + " day" + (days > 1 ? "s" : "") + " ago";
        } else if (seconds < 2592000) {
            long weeks = seconds / 604800;
            return weeks + " week" + (weeks > 1 ? "s" : "") + " ago";
        } else {
            long months = seconds / 2592000;
            return months + " month" + (months > 1 ? "s" : "") + " ago";
        }
    }
}