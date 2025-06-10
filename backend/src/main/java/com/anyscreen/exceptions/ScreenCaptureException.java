package com.anyscreen.exceptions;

/**
 * Exception thrown when screen capture operations fail.
 */
public class ScreenCaptureException extends Exception {
    
    public ScreenCaptureException(String message) {
        super(message);
    }
    
    public ScreenCaptureException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public ScreenCaptureException(Throwable cause) {
        super(cause);
    }
}
