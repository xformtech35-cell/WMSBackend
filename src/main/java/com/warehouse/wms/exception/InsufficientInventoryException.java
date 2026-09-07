// ====== FILE: src/main/java/com/warehouse/wms/exception/InsufficientInventoryException.java ======
package com.warehouse.wms.exception;

public class InsufficientInventoryException extends RuntimeException {
    
    public InsufficientInventoryException(String message) {
        super(message);
    }
    
    public InsufficientInventoryException(String message, Throwable cause) {
        super(message, cause);
    }
}