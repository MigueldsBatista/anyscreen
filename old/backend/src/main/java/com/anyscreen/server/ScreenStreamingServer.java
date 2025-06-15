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
import com.anyscreen.constants.HTMLConstants;
import com.anyscreen.models.ScreenInfo;
import com.anyscreen.models.StreamingConfig;
import com.anyscreen.models.StreamingProtocol;
import com.anyscreen.server.routing.Router;
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
    private final Router router;
    private final int port;
    
    public ScreenStreamingServer(int port) throws Exception {
        this.port = port;
        this.server = HttpServer.create(new InetSocketAddress(port), 0);
        this.screenCaptureService = ScreenCaptureService.createDefault();
        this.streamingService = new StreamingService();
        this.router = new Router(server, screenCaptureService, streamingService);
        setupServer();
    }
    
    public void setupServer(){
        this.router.configureRoutes();
        this.server.setExecutor(Executors.newFixedThreadPool(10));
    }

    public void start() {
        server.start();
        LoggerService.info("Screen streaming server started on port " + port);
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

}