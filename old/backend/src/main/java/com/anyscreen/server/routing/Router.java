package com.anyscreen.server.routing;

import com.sun.net.httpserver.HttpServer;
import com.anyscreen.server.handlers.*;
import com.anyscreen.services.ScreenCaptureService;
import com.anyscreen.services.StreamingService;

public class Router {
    private final HttpServer server;
    private final ScreenCaptureService screenCaptureService;
    private final StreamingService streamingService;
    
    public Router(HttpServer server, ScreenCaptureService screenCaptureService, StreamingService streamingService) {
        this.server = server;
        this.screenCaptureService = screenCaptureService;
        this.streamingService = streamingService;
    }
    
    public void configureRoutes() {
        server.createContext("/screens", new ScreenListHandler(screenCaptureService));
        server.createContext("/stream/", new ScreenStreamHandler(screenCaptureService, streamingService));
        server.createContext("/status", new StatusHandler(streamingService));
        server.createContext("/", new TestPageHandler());
    }
}