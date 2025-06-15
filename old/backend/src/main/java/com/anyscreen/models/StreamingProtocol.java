package com.anyscreen.models;

/**
 * Enumeration of supported streaming protocols.
 */
public enum StreamingProtocol {
    MJPEG("Motion JPEG", "image/jpeg"),
    WEBRTC("Web Real-Time Communication", "video/h264"),
    HLS("HTTP Live Streaming", "application/vnd.apple.mpegurl"),
    DASH("Dynamic Adaptive Streaming", "application/dash+xml");
    
    private final String description;
    private final String mimeType;
    
    StreamingProtocol(String description, String mimeType) {
        this.description = description;
        this.mimeType = mimeType;
    }
    
    public String getDescription() { 
        return description; 
    }
    
    public String getMimeType() { 
        return mimeType; 
    }
}
