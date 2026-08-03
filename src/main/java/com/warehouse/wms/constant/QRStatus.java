// ====== FILE: src/main/java/com/warehouse/wms/entity/enums/QRStatus.java ======
package com.warehouse.wms.constant;

public enum QRStatus {
    GENERATED,      // QR generated
    PRINTED,        // Label printed
    SCANNED,        // QR scanned during putaway
    USED,           // QR used/completed
    EXPIRED,        // QR expired
    CANCELLED       // QR cancelled
}