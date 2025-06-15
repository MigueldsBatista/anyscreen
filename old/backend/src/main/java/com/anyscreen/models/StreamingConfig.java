package com.anyscreen.models;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration class for streaming strategies.
 */
public class StreamingConfig {
    private final StreamingProtocol protocol;
    private final int width;
    private final int height;
    private final int frameRate;
    private final int quality;
    private final int port;
    private final String host;
    private final Map<String, Object> additionalParams;
    
    private StreamingConfig(Builder builder) {
        this.protocol = builder.protocol;
        this.width = builder.width;
        this.height = builder.height;
        this.frameRate = builder.frameRate;
        this.quality = builder.quality;
        this.port = builder.port;
        this.host = builder.host;
        this.additionalParams = new HashMap<>(builder.additionalParams);
    }
    
    // Getters
    public StreamingProtocol getProtocol() { return protocol; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public int getFrameRate() { return frameRate; }
    public int getQuality() { return quality; }
    public int getPort() { return port; }
    public String getHost() { return host; }
    public Map<String, Object> getAdditionalParams() { return new HashMap<>(additionalParams); }
    
    public static class Builder {
        private StreamingProtocol protocol = StreamingProtocol.MJPEG;
        private int width = 1920;
        private int height = 1080;
        private int frameRate = 30;
        private int quality = 80;
        private int port = 8080;
        private String host = "localhost";
        private Map<String, Object> additionalParams = new HashMap<>();
        
        public Builder protocol(StreamingProtocol protocol) {
            this.protocol = protocol;
            return this;
        }
        
        public Builder resolution(int width, int height) {
            this.width = width;
            this.height = height;
            return this;
        }
        
        public Builder frameRate(int frameRate) {
            this.frameRate = frameRate;
            return this;
        }
        
        public Builder quality(int quality) {
            this.quality = quality;
            return this;
        }
        
        public Builder serverAddress(String host, int port) {
            this.host = host;
            this.port = port;
            return this;
        }
        
        public Builder addParam(String key, Object value) {
            this.additionalParams.put(key, value);
            return this;
        }
        
        public StreamingConfig build() {
            return new StreamingConfig(this);
        }
    }
}
