package com.anyscreen.server.handlers;

import java.io.IOException;

import com.anyscreen.services.LoggerService;
import com.anyscreen.services.StreamingService;
import com.sun.net.httpserver.HttpExchange;

/**
 * Handler for GET /status
 * Returns server and streaming status
 */
public class StatusHandler extends AbstractHandler {

    private final StreamingService streamingService;

    public StatusHandler(StreamingService streamingService){
        this.streamingService = streamingService;
    }

    public void handle(HttpExchange exchange) throws IOException {
        if (!"GET".equals(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, "Method Not Allowed");
            return;
        }
        
        try {
            StringBuilder json = new StringBuilder();
            json.append("{\n");
            json.append("  \"server\": \"running\",\n");
            json.append("  \"port\": ").append(streamingService.getConfig().getPort()).append(",\n");
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
