// ====== FILE: src/main/java/com/warehouse/wms/exception/BusinessException.java ======
package com.warehouse.wms.exception;

public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
    
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}