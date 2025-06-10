package com.anyscreen;

import java.util.List;
import java.util.Scanner;

import com.anyscreen.implementations.Log4jAdapter;
import com.anyscreen.models.RecordingInfo;
import com.anyscreen.models.ScreenInfo;
import com.anyscreen.services.LoggerService;
import com.anyscreen.services.RecordingService;
import com.anyscreen.services.ScreenCaptureService;

public class App 
{    
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        // Initialize logger service first

        LoggerService.initialize(new Log4jAdapter());
        LoggerService.info("Application starting...");
        
        try {
            ScreenInfo screenInfo = selectScreen();

            demoRecording(screenInfo);
        } catch (Exception e) {
            LoggerService.info(e.getMessage());
        }
        
        LoggerService.info("Application finished");
    }
    
    private static ScreenInfo selectScreen() throws Exception{
        ScreenCaptureService captureService = ScreenCaptureService.createDefault();
        
        List<ScreenInfo> screenInfos = captureService.getAvailableScreens();
        for(ScreenInfo info : screenInfos){
            LoggerService.info("Screen: " + info.getIndex() + " - " + info.getBounds().getWidth() + " x " + info.getBounds().getHeight() + " IsPriamary: " + info.isPrimary());
        }
        LoggerService.info("Select the screen index to start recording: ");
        return captureService.getScreenInfo(scanner.nextInt());        
    }
    
    private static void demoRecording(ScreenInfo screenInfo) throws Exception { 
        RecordingInfo recordingInfo = new RecordingInfo.Builder()
            .outputFile("media/demo_recording.mp4")
            .resolution(screenInfo.getBounds().getWidth(), screenInfo.getBounds().getHeight())
            .frameRate(30)
            .screenIndex(screenInfo.getIndex())
            .build();
        
        RecordingService recordingService = RecordingService.createDefault(recordingInfo);
        
        LoggerService.info("Starting 5-second recording...");
        recordingService.startRecording();
        
        // Record for 5 seconds
        waitForRecording(5000);
        
        recordingService.stopRecording();
        LoggerService.info("Recording completed: demo_recording.mp4");
    }
    
    private static void waitForRecording(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LoggerService.warn("Recording interrupted");
        }
    }
}