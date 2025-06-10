package com.anyscreen.implementations;

import java.awt.image.BufferedImage;

import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;

import com.anyscreen.interfaces.FrameEncoderInterface;
import com.anyscreen.models.RecordingInfo;
import com.anyscreen.services.LoggerService;

/**
 * JavaCV-based frame encoder implementation using FFmpeg.
 * Handles video encoding to various formats (mp4, avi, mkv, etc.).
 */
public class JavaCVFrameEncoderAdapter implements FrameEncoderInterface {
    private FFmpegFrameRecorder recorder;
    private Java2DFrameConverter converter;
    private boolean running = false;
    private RecordingInfo config;
    
    public JavaCVFrameEncoderAdapter() {
        this.converter = new Java2DFrameConverter();
    }
    
    
    public void configure(RecordingInfo recordingInfo) {
        this.config = recordingInfo;
        
        this.recorder = new FFmpegFrameRecorder(
            recordingInfo.getOutputFileName(),
            recordingInfo.getWidth(),
            recordingInfo.getHeight()
        );
        
        // Configure video settings
        recorder.setFrameRate(recordingInfo.getFrameRate());
        recorder.setFormat(recordingInfo.getFormat());
        recorder.setVideoCodec(recordingInfo.getVideoCodec());
        recorder.setVideoBitrate(recordingInfo.getBitRate());
        // Additional quality settings
        recorder.setVideoQuality(0); // Best quality
        recorder.setGopSize(30);     // GOP size for better compression
        
        LoggerService.info("JavaCV encoder configured: " + recordingInfo.getOutputFileName() + 
                          " (" + recordingInfo.getWidth() + "x" + recordingInfo.getHeight() + 
                          "@" + recordingInfo.getFrameRate() + "fps)");
    }
    
    public void start() throws Exception{
        if(!isConfigured()){
            throw new IllegalStateException("configure() must be called before recording");
        }
        recorder.start();

        running = true;
        LoggerService.info("JavaCV encoder started successfully");
    }
    
    public void encodeFrame(BufferedImage image) throws Exception{
        if (!running) {
            throw new IllegalStateException("Encoder not running. Call start() first.");
        }
        
        if (image == null) {
            throw new IllegalArgumentException("Frame cannot be null");
        }
        Frame frame = converter.convert(image);
        recorder.record(frame);
        
    }
    
    public void stop() throws Exception{
        running = false;
        
        if (recorder != null) {
            recorder.stop();
            LoggerService.info("JavaCV encoder stopped");
        }
    }
    
    public boolean isRunning() {
        return running;
    }
    
    /**
     * Gets the current recording configuration.
     * @return RecordingInfo or null if not configured
     */
    public RecordingInfo getConfig() {
        return config;
    }
    
    /**
     * Checks if the encoder is properly configured.
     * @return true if configured, false otherwise
     */
    public boolean isConfigured() {
        return recorder != null && config != null;
    }
    
    // Add proper cleanup method
    public void cleanup() throws Exception {
        stop();
        if (recorder != null) {
            recorder.release();
            recorder = null;
        }
    }
}
