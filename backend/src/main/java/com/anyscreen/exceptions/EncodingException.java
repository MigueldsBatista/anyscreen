package com.anyscreen.exceptions;

/**
 * Exception thrown when recording operations fail.
 * This includes issues with starting, stopping, or managing recordings.
 */
public class EncodingException extends Exception {
    
    public EncodingException(String message) {
        super(message);
    }
    
    public EncodingException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public EncodingException(Throwable cause) {
        super(cause);
    }
}
