package com.anyscreen.server.handlers;

import java.io.IOException;
import java.util.List;

import com.anyscreen.models.ScreenInfo;
import com.anyscreen.services.LoggerService;
import com.anyscreen.services.ScreenCaptureService;
import com.sun.net.httpserver.HttpExchange;

/**
     * Handler for GET /screens
     * Returns JSON list of available screens
     */
    public class ScreenListHandler extends AbstractHandler {

        private ScreenCaptureService screenCaptureService;

        public ScreenListHandler(ScreenCaptureService screenCaptureService){
            this.screenCaptureService = screenCaptureService;
        
        }

        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestMethod().equals("GET")) {
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
    