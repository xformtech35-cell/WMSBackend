package com.warehouse.wms.dto;

public class ScanRequest {
    private String barCode;
    private String scannedBy;

    // Getters and Setters
    public String getBarCode() { return barCode; }
    public void setBarCode(String barCode) { this.barCode = barCode; }
    public String getScannedBy() { return scannedBy; }
    public void setScannedBy(String scannedBy) { this.scannedBy = scannedBy; }
}