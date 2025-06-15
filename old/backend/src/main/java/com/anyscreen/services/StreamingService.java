package com.anyscreen.services;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import javax.imageio.ImageIO;

import com.anyscreen.models.StreamingConfig;

/**
 * Simple streaming service for MJPEG streaming.
 * This is a simplified version without the full Strategy pattern for initial implementation.
 */
public class StreamingService {
    
    private static final String BOUNDARY = "frame";
    
    private StreamingConfig config;
    private final AtomicBoolean streaming = new AtomicBoolean(false);
    private final Map<String, ClientConnection> clients = new ConcurrentHashMap<>();
    private final AtomicLong frameCount = new AtomicLong(0);
    private final AtomicLong bytesTransferred = new AtomicLong(0);
    private long startTime;
    private volatile long lastFrameTime = 0;
    private static final int MAX_CLIENTS = 10; // Limit concurrent clients
    private static final long FRAME_SKIP_THRESHOLD = 33; // ~30fps max
    // Add reusable objects
    private static final ThreadLocal<ByteArrayOutputStream> JPEG_BUFFER = 
        ThreadLocal.withInitial(() -> new ByteArrayOutputStream(64 * 1024)); // 64KB initial
        
    private static class ClientConnection {
        final String clientId;
        final OutputStream outputStream;
        volatile boolean active = true;
        long lastFrameTime = System.currentTimeMillis();
        
        ClientConnection(String clientId, OutputStream outputStream) {
            this.clientId = clientId;
            this.outputStream = outputStream;
        }
    }
    
    public void configure(StreamingConfig config) throws Exception {
        if (config == null) {
            throw new IllegalArgumentException("StreamingConfig cannot be null");
        }
        
        this.config = config;
        LoggerService.info("Streaming service configured: " + 
                          config.getWidth() + "x" + config.getHeight() + 
                          " @ " + config.getFrameRate() + "fps, quality=" + config.getQuality());
    }

    public StreamingConfig getConfig() {
        return config;
    }
    
    public void startStreaming() throws Exception {
        if (config == null) {
            throw new IllegalStateException("Service not configured. Call configure() first.");
        }
        
        if (streaming.compareAndSet(false, true)) {
            startTime = System.currentTimeMillis();
            frameCount.set(0);
            bytesTransferred.set(0);
            LoggerService.info("Streaming started");
        }
    }
    
    public void streamFrame(BufferedImage frame) throws Exception {
        if (!streaming.get()) {
            return; // Not streaming, ignore frame
        }
        
        if (frame == null) {
            LoggerService.warn("Received null frame, skipping");
            return;
        }
        
        long currentTime = System.currentTimeMillis();
        if(currentTime - lastFrameTime < FRAME_SKIP_THRESHOLD) return;

        lastFrameTime = currentTime;

        // Convert frame to JPEG
        byte[] jpegData = convertToJPEG(frame);
        
        // Stream to all connected clients
        String boundary = "\r\n--" + BOUNDARY + "\r\n";
        String headers = "Content-Type: image/jpeg\r\n" +
                        "Content-Length: " + jpegData.length + "\r\n\r\n";
        
        clients.entrySet().removeIf(entry -> {
            ClientConnection client = entry.getValue();
            if (!client.active) {
                return true;
            }
            
            try {
                // Use synchronized block only for writing
                synchronized (client.outputStream) {
                    client.outputStream.write(boundary.getBytes());
                    client.outputStream.write(headers.getBytes());
                    client.outputStream.write(jpegData);
                    client.outputStream.flush();
                }
                
                client.lastFrameTime = currentTime;
                return false;
                
            } catch (IOException e) {
                LoggerService.debug("Client " + client.clientId + " disconnected");
                client.active = false;
                return true;
            }
        });
        
        // Update statistics
        frameCount.incrementAndGet();
        bytesTransferred.addAndGet(jpegData.length);
    }
    
private byte[] convertToJPEG(BufferedImage image) throws IOException {
    ByteArrayOutputStream baos = JPEG_BUFFER.get();
    baos.reset(); // Reuse the buffer
    
    // Only convert if not already RGB
    if (image.getType() == BufferedImage.TYPE_INT_RGB) {
        ImageIO.write(image, "jpeg", baos);
    } else {
        // Fast conversion without creating new BufferedImage
        BufferedImage rgbImage = new BufferedImage(
            image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        rgbImage.createGraphics().drawImage(image, 0, 0, null);
        ImageIO.write(rgbImage, "jpeg", baos);
        rgbImage.flush(); // Free memory immediately
    }
    
    return baos.toByteArray();
}
    public void stopStreaming() throws Exception {
        if (streaming.compareAndSet(true, false)) {
            // Close all client connections
            for (ClientConnection client : clients.values()) {
                try {
                    client.active = false;
                    client.outputStream.close();
                } catch (IOException e) {
                    LoggerService.warn("Error closing client connection: " + e.getMessage());
                }
            }
            clients.clear();
            
            LoggerService.info("Streaming stopped. Frames streamed: " + frameCount.get());
        }
    }
    
    public boolean isStreaming() {
        return streaming.get();
    }
    
    public Map<String, Object> getStreamingStats() {
        Map<String, Object> stats = new ConcurrentHashMap<>();
        stats.put("protocol", "MJPEG");
        stats.put("isStreaming", streaming.get());
        stats.put("connectedClients", clients.size());
        stats.put("frameCount", frameCount.get());
        stats.put("bytesTransferred", bytesTransferred.get());
        
        if (startTime > 0) {
            long uptime = System.currentTimeMillis() - startTime;
            stats.put("uptimeMs", uptime);
            
            if (uptime > 0) {
                stats.put("avgFps", (frameCount.get() * 1000.0) / uptime);
                stats.put("avgBytesPerSecond", (bytesTransferred.get() * 1000.0) / uptime);
            }
        }
        
        return stats;
    }
    
    public void handleClientConnection(String clientId, OutputStream outputStream) throws Exception {
        // Add client to active connections
        
        ClientConnection client = new ClientConnection(clientId, outputStream);
        clients.put(clientId, client);
        
        LoggerService.info("Client connected: " + clientId + " (total: " + clients.size() + ")");
    }
    
    public void handleClientDisconnection(String clientId) {
        ClientConnection client = clients.remove(clientId);
        if (client != null) {
            client.active = false;
            try {
                client.outputStream.close();
            } catch (IOException e) {
                // Ignore close errors
            }
            LoggerService.info("Client disconnected: " + clientId + " (remaining: " + clients.size() + ")");
        }
    }
    
    public int getConnectedClientsCount() {
        return clients.size();
    }
}
