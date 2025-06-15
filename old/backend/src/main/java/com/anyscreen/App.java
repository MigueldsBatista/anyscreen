package com.anyscreen;

import java.util.List;
import java.util.Scanner;

import com.anyscreen.implementations.Log4jAdapter;
import com.anyscreen.models.ScreenInfo;
import com.anyscreen.server.ScreenStreamingServer;
import com.anyscreen.services.LoggerService;
import com.anyscreen.services.ScreenCaptureService;

public class App {    
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        // Initialize logger service first
        LoggerService.initialize(new Log4jAdapter());
        LoggerService.info("AnyScreen application starting...");
        
        try {
            // Show available screens first
            showAvailableScreens();
            
            // Start HTTP server for streaming
            int port = 8080;
            ScreenStreamingServer server = new ScreenStreamingServer(port);
            server.start();
            
            // Keep server running
            LoggerService.info("Press Enter to stop the server...");
            scanner.nextLine();
            
            // Stop server
            server.stop();
            
        } catch (Exception e) {
            LoggerService.error("Application error: " + e.getMessage());
            e.printStackTrace();
        }
        
        LoggerService.info("AnyScreen application finished");
    }
    
    private static void showAvailableScreens() throws Exception {
        ScreenCaptureService captureService = ScreenCaptureService.createDefault();
        
        List<ScreenInfo> screenInfos = captureService.getAvailableScreens();
        LoggerService.info("Available screens for streaming:");
        
        for (ScreenInfo info : screenInfos) {
            LoggerService.info("  Screen " + info.getIndex() + ": " + 
                             info.getBounds().getWidth() + "x" + info.getBounds().getHeight() + 
                             (info.isPrimary() ? " (Primary)" : ""));
        }
        
        LoggerService.info("Screens can be accessed via HTTP endpoints:");
        for (ScreenInfo info : screenInfos) {
            LoggerService.info("  http://localhost:8080/stream/" + info.getIndex());
        }
    }
}