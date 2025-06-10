package com.anyscreen.services;

import java.awt.image.BufferedImage;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.anyscreen.abstracts.AbstractScreenRecorder;
import com.anyscreen.exceptions.RecordingException;
import com.anyscreen.implementations.JavaCVFrameEncoderAdapter;
import com.anyscreen.implementations.JavaCVScreenRecorder;
import com.anyscreen.interfaces.FrameEncoderInterface;
import com.anyscreen.models.RecordingInfo;

/**
 * Recording Service that accepts any AbstractScreenRecorder implementation.
 * This eliminates the need for a factory or generic recorder wrapper.
 */
public class RecordingService {
    private AbstractScreenRecorder recorder;
    private ScreenCaptureService captureService;
    private ScheduledExecutorService scheduler;
    
    /**
     * Constructor that accepts any AbstractScreenRecorder implementation.
     * 
     * @param captureService Service for capturing screen frames
     * @param recorder Any implementation of AbstractScreenRecorder
     */
    public RecordingService(ScreenCaptureService captureService, AbstractScreenRecorder recorder) {
        this.captureService = captureService;
        this.recorder = recorder;
    }
    
    /**
     * Creates a default recording service with JavaCV recorder.
     * This is a convenience method for the most common use case.
     */
    public static RecordingService createDefault(RecordingInfo recordingInfo) throws RecordingException {
        try {
            ScreenCaptureService capturer = ScreenCaptureService.createDefault();
            AbstractScreenRecorder recorder = new JavaCVScreenRecorder(
                recordingInfo,
                new JavaCVFrameEncoderAdapter()
            );
            
            return new RecordingService(capturer, recorder);
        } catch (Exception e) {
            LoggerService.error("Failed to create default recording service: " + e.getMessage());
            throw new RecordingException("Failed to create recording service", e);
        }
    }

    /**
     * Starts recording with the configured recorder.
     */
    public void startRecording() throws Exception {
        if (recorder.isRecording()) {
            throw new RecordingException("Recording already in progress");
        }
        
        recorder.start();
        
        // Start capturing frames at the specified frame rate
        long frameDelayMs = 1000 / recorder.getRecordingInfo().getFrameRate();
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(this::captureAndRecordFrame, 0, frameDelayMs, TimeUnit.MILLISECONDS);
        
        LoggerService.info("Recording started successfully");
        
    }

    /**
     * Stops the current recording.
     */
    public void stopRecording() throws Exception {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
        }
        
        if (recorder != null) {
            recorder.stop();
        }
        LoggerService.info("Recording stopped successfully");

    }

    /**
     * Checks if currently recording.
     */
    public boolean isRecording() {
        return recorder != null && recorder.isRecording();
    }

    /**
     * Gets the current recording information.
     */
    public RecordingInfo getRecordingInfo() {
        return recorder != null ? recorder.getRecordingInfo() : null;
    }

    /**
     * Gets the recorder instance.
     */
    public AbstractScreenRecorder getRecorder() {
        return recorder;
    }

    /**
     * Gets the capture service instance.
     */
    public ScreenCaptureService getCaptureService() {
        return captureService;
    }

    private void captureAndRecordFrame() {
        try {
            BufferedImage frame = captureService.captureScreen(recorder.getRecordingInfo().getScreenIndex());
            recorder.recordFrame(frame);
        } catch (Exception e) {
            LoggerService.error("Error capturing frame: " + e.getMessage());
        }
    }
}