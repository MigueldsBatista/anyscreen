package com.anyscreen.exceptions;

/**
 * Exception thrown when recording operations fail.
 * This includes issues with starting, stopping, or managing recordings.
 */
public class RecordingException extends Exception {
    
    public RecordingException(String message) {
        super(message);
    }
    
    public RecordingException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public RecordingException(Throwable cause) {
        super(cause);
    }
}
