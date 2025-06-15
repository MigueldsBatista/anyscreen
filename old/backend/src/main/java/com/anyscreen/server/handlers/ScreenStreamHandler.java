package com.anyscreen.server.handlers;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;

import com.anyscreen.models.ScreenInfo;
import com.anyscreen.models.StreamingConfig;
import com.anyscreen.models.StreamingProtocol;
import com.anyscreen.services.LoggerService;
import com.anyscreen.services.ScreenCaptureService;
import com.anyscreen.services.StreamingService;
import com.sun.net.httpserver.HttpExchange;

/**
 * Handler for GET /stream/{screenIndex}
 * Streams MJPEG video of specified screen
 */
public class ScreenStreamHandler extends AbstractHandler {

    private ScreenCaptureService screenCaptureService;
    private StreamingService streamingService;

    public ScreenStreamHandler(ScreenCaptureService screenCaptureService, StreamingService streamingService){
            this.screenCaptureService = screenCaptureService;
            this.streamingService = streamingService;
        
    }
    
    public void handle(HttpExchange exchange) throws IOException {
        if (!"GET".equals(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, "Method Not Allowed");
            return;
        }
        
        try {
            // Parse screen index from URL path
            String path = exchange.getRequestURI().getPath();
            String[] pathParts = path.split("/");
            
            if (pathParts.length < 3) {
                sendResponse(exchange, 400, "Invalid URL. Use /stream/{screenIndex}");
                return;
            }
            
            int screenIndex;
            try {
                screenIndex = Integer.parseInt(pathParts[2]);
            } catch (NumberFormatException e) {
                sendResponse(exchange, 400, "Invalid screen index");
                return;
            }
            
            // Get screen info
            ScreenInfo screenInfo = screenCaptureService.getScreenInfo(screenIndex);
            
            // Set MJPEG headers
            exchange.getResponseHeaders().set("Content-Type", "multipart/x-mixed-replace; boundary=frame");
            exchange.getResponseHeaders().set("Cache-Control", "no-cache, no-store, must-revalidate");
            exchange.getResponseHeaders().set("Pragma", "no-cache");
            exchange.getResponseHeaders().set("Expires", "0");
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            
            // Start streaming response
            exchange.sendResponseHeaders(200, 0);
            OutputStream outputStream = exchange.getResponseBody();
            
            String clientId = exchange.getRemoteAddress().toString();
            LoggerService.info("Starting stream for client: " + clientId + ", screen: " + screenIndex);
            
            // Configure streaming
            StreamingConfig config = new StreamingConfig.Builder()
                .protocol(StreamingProtocol.MJPEG)
                .resolution(screenInfo.getBounds().width, screenInfo.getBounds().height)
                .frameRate(30)
                .quality(80)
                .build();
            
            streamingService.configure(config);
            streamingService.handleClientConnection(clientId, outputStream);
            streamingService.startStreaming();
            
            // Stream frames continuously
            long frameDelay = 1000 / 30; // 30 FPS
            
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    BufferedImage frame = screenCaptureService.captureScreen(screenIndex);
                    streamingService.streamFrame(frame);
                    
                    // Use a simple delay mechanism instead of Thread.sleep
                    long startTime = System.currentTimeMillis();
                    while (System.currentTimeMillis() - startTime < frameDelay) {
                        // Busy wait for frame delay
                        if (Thread.currentThread().isInterrupted()) {
                            break;
                        }
                    }
                }
            } catch (InterruptedException e) {
                LoggerService.info("Streaming interrupted for client: " + clientId);
            } catch (Exception e) {
                LoggerService.error("Streaming error: " + e.getMessage());
            } finally {
                streamingService.handleClientDisconnection(clientId);
                outputStream.close();
            }
            
        } catch (Exception e) {
            LoggerService.error("Error in stream handler: " + e.getMessage());
            try {
                sendResponse(exchange, 500, "Streaming error: " + e.getMessage());
            } catch (IOException ioException) {
                // Ignore close errors
            }
        }
    }
}
