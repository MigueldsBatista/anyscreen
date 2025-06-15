package com.anyscreen.server;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.Executors;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import com.anyscreen.models.ScreenInfo;
import com.anyscreen.models.StreamingConfig;
import com.anyscreen.models.StreamingProtocol;
import com.anyscreen.services.ScreenCaptureService;
import com.anyscreen.services.StreamingService;
import com.anyscreen.services.LoggerService;

/**
 * Simple HTTP server for screen streaming without Spring overhead.
 * Uses Java's built-in HttpServer for lightweight operation.
 */
public class ScreenStreamingServer {
    
    private final HttpServer server;
    private final ScreenCaptureService screenCaptureService;
    private final StreamingService streamingService;
    private final int port;
    
    public ScreenStreamingServer(int port) throws Exception {
        this.port = port;
        this.server = HttpServer.create(new InetSocketAddress(port), 0);
        this.screenCaptureService = ScreenCaptureService.createDefault();
        this.streamingService = new StreamingService();
        
        setupRoutes();
        setupExecutor();
    }
    
    private void setupRoutes() {
        // GET /screens - List available screens
        server.createContext("/screens", new ScreenListHandler());
        
        // GET /stream/{screenIndex} - Stream specific screen
        server.createContext("/stream/", new ScreenStreamHandler());
        
        // GET /status - Get streaming status
        server.createContext("/status", new StatusHandler());
        
        // GET / - Serve test HTML page
        server.createContext("/", new TestPageHandler());
    }
    
    private void setupExecutor() {
        // Use thread pool for handling multiple concurrent requests
        server.setExecutor(Executors.newFixedThreadPool(10));
    }
    
    public void start() {
        server.start();
        LoggerService.info("Screen streaming server started on port " + port);
        LoggerService.info("Available endpoints:");
        LoggerService.info("  http://localhost:" + port + "/ - Test page");
        LoggerService.info("  http://localhost:" + port + "/screens - List screens");
        LoggerService.info("  http://localhost:" + port + "/stream/0 - Stream screen 0");
        LoggerService.info("  http://localhost:" + port + "/status - Server status");
    }
    
    public void stop() {
        server.stop(0);
        try {
            streamingService.stopStreaming();
        } catch (Exception e) {
            LoggerService.error("Error stopping streaming service: " + e.getMessage());
        }
        LoggerService.info("Screen streaming server stopped");
    }
    
    /**
     * Handler for GET /screens
     * Returns JSON list of available screens
     */
    private class ScreenListHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equals(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "Method Not Allowed");
                return;
            }
            
            try {
                List<ScreenInfo> screens = screenCaptureService.getAvailableScreens();
                StringBuilder json = new StringBuilder();
                json.append("[\n");
                
                for (int i = 0; i < screens.size(); i++) {
                    ScreenInfo screen = screens.get(i);
                    json.append("  {\n");
                    json.append("    \"index\": ").append(screen.getIndex()).append(",\n");
                    json.append("    \"width\": ").append(screen.getBounds().width).append(",\n");
                    json.append("    \"height\": ").append(screen.getBounds().height).append(",\n");
                    json.append("    \"isPrimary\": ").append(screen.isPrimary()).append("\n");
                    json.append("  }");
                    if (i < screens.size() - 1) json.append(",");
                    json.append("\n");
                }
                
                json.append("]");
                
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                sendResponse(exchange, 200, json.toString());
                
            } catch (Exception e) {
                LoggerService.error("Error getting screens: " + e.getMessage());
                sendResponse(exchange, 500, "Internal Server Error");
            }
        }
    }
    
    /**
     * Handler for GET /stream/{screenIndex}
     * Streams MJPEG video of specified screen
     */
    private class ScreenStreamHandler implements HttpHandler {
        @Override
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
    
    /**
     * Handler for GET /status
     * Returns server and streaming status
     */
    private class StatusHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equals(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "Method Not Allowed");
                return;
            }
            
            try {
                StringBuilder json = new StringBuilder();
                json.append("{\n");
                json.append("  \"server\": \"running\",\n");
                json.append("  \"port\": ").append(port).append(",\n");
                json.append("  \"streaming\": ").append(streamingService.isStreaming()).append(",\n");
                json.append("  \"clients\": ").append(streamingService.getConnectedClientsCount()).append("\n");
                json.append("}");
                
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                sendResponse(exchange, 200, json.toString());
                
            } catch (Exception e) {
                LoggerService.error("Error getting status: " + e.getMessage());
                sendResponse(exchange, 500, "Internal Server Error");
            }
        }
    }
    
    /**
     * Handler for GET /
     * Serves simple HTML test page
     */
    private class TestPageHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equals(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "Method Not Allowed");
                return;
            }
            
            String html = generateTestPage();
            exchange.getResponseHeaders().set("Content-Type", "text/html");
            sendResponse(exchange, 200, html);
        }
    }
    
    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        byte[] responseBytes = response.getBytes();
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }
    
    private String generateTestPage() {
        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>AnyScreen - Screen Stream Test</title>
                <style>
                    body {
                        font-family: Arial, sans-serif;
                        margin: 20px;
                        background-color: #f5f5f5;
                    }
                    .container {
                        max-width: 1200px;
                        margin: 0 auto;
                        background: white;
                        padding: 20px;
                        border-radius: 8px;
                        box-shadow: 0 2px 10px rgba(0,0,0,0.1);
                    }
                    .controls {
                        margin-bottom: 20px;
                        padding: 15px;
                        background: #f8f9fa;
                        border-radius: 5px;
                    }
                    .stream-container {
                        text-align: center;
                        border: 2px solid #ddd;
                        border-radius: 5px;
                        overflow: hidden;
                        background: #000;
                    }
                    .stream-img {
                        max-width: 100%;
                        height: auto;
                        display: block;
                    }
                    button {
                        padding: 10px 20px;
                        margin: 5px;
                        background: #007bff;
                        color: white;
                        border: none;
                        border-radius: 5px;
                        cursor: pointer;
                    }
                    button:hover {
                        background: #0056b3;
                    }
                    button:disabled {
                        background: #6c757d;
                        cursor: not-allowed;
                    }
                    .status {
                        margin-top: 15px;
                        padding: 10px;
                        background: #e9ecef;
                        border-radius: 5px;
                        font-family: monospace;
                        font-size: 12px;
                    }
                    .error {
                        color: red;
                        background: #ffe6e6;
                    }
                    .success {
                        color: green;
                        background: #e6ffe6;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <h1>AnyScreen - Screen Stream Test</h1>
                    
                    <div class="controls">
                        <h3>Stream Controls</h3>
                        <button onclick="loadScreens()">Load Available Screens</button>
                        <button onclick="startStream(0)" id="stream0">Stream Screen 0</button>
                        <button onclick="startStream(1)" id="stream1">Stream Screen 1</button>
                        <button onclick="stopStream()">Stop Stream</button>
                        <button onclick="getStatus()">Get Status</button>
                    </div>
                    
                    <div class="stream-container">
                        <img id="streamImage" class="stream-img" style="display: none;" />
                        <div id="noStream" style="padding: 50px; color: #666;">
                            No stream active. Click "Stream Screen X" to start.
                        </div>
                    </div>
                    
                    <div id="status" class="status">
                        Ready - Click "Load Available Screens" to start
                    </div>
                </div>
                
                <script>
                    let currentStream = null;
                    
                    function updateStatus(message, isError = false) {
                        const statusDiv = document.getElementById('status');
                        statusDiv.textContent = new Date().toLocaleTimeString() + ' - ' + message;
                        statusDiv.className = 'status ' + (isError ? 'error' : 'success');
                    }
                    
                    async function loadScreens() {
                        try {
                            const response = await fetch('/screens');
                            const screens = await response.json();
                            updateStatus(`Found ${screens.length} screens: ` + 
                                screens.map(s => `Screen ${s.index} (${s.width}x${s.height}${s.isPrimary ? ', Primary' : ''})`).join(', '));
                        } catch (error) {
                            updateStatus('Error loading screens: ' + error.message, true);
                        }
                    }
                    
                    function startStream(screenIndex) {
                        stopStream(); // Stop any existing stream
                        
                        const img = document.getElementById('streamImage');
                        const noStream = document.getElementById('noStream');
                        
                        img.src = `/stream/${screenIndex}?t=${Date.now()}`;
                        img.style.display = 'block';
                        noStream.style.display = 'none';
                        
                        currentStream = screenIndex;
                        updateStatus(`Streaming screen ${screenIndex}...`);
                        
                        img.onerror = function() {
                            updateStatus(`Failed to load stream for screen ${screenIndex}`, true);
                            stopStream();
                        };
                        
                        img.onload = function() {
                            updateStatus(`Stream active for screen ${screenIndex}`);
                        };
                    }
                    
                    function stopStream() {
                        const img = document.getElementById('streamImage');
                        const noStream = document.getElementById('noStream');
                        
                        img.src = '';
                        img.style.display = 'none';
                        noStream.style.display = 'block';
                        
                        currentStream = null;
                        updateStatus('Stream stopped');
                    }
                    
                    async function getStatus() {
                        try {
                            const response = await fetch('/status');
                            const status = await response.json();
                            updateStatus(`Server: ${status.server}, Streaming: ${status.streaming}, Clients: ${status.clients}`);
                        } catch (error) {
                            updateStatus('Error getting status: ' + error.message, true);
                        }
                    }
                    
                    // Auto-load screens on page load
                    window.onload = function() {
                        loadScreens();
                    };
                </script>
            </body>
            </html>
            """;
    }
}