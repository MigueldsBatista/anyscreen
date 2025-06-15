package com.anyscreen.services;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.util.List;

import com.anyscreen.exceptions.ScreenCaptureException;
import com.anyscreen.implementations.ImageIOScreenCaptureSaver;
import com.anyscreen.implementations.RobotScreenCapture;
import com.anyscreen.interfaces.ScreenCaptureInterface;
import com.anyscreen.interfaces.ScreenCaptureSaverInterface;
import com.anyscreen.models.ScreenInfo;

/**
 * High-level screen capture service that combines capture and save operations.
 * Provides convenience methods while maintaining separation of concerns.
 */
public class ScreenCaptureService {
    private final ScreenCaptureInterface captureInterface;
    private final ScreenCaptureSaverInterface saver;
    
    public ScreenCaptureService(ScreenCaptureInterface captureInterface, ScreenCaptureSaverInterface saver) {
        this.captureInterface = captureInterface;
        this.saver = saver;
    }
    
    /**
     * Creates a service with default Robot-based capture and ImageIO-based saving.
     */
    public static ScreenCaptureService createDefault() throws ScreenCaptureException {
        return new ScreenCaptureService(
            new RobotScreenCapture(),
            new ImageIOScreenCaptureSaver()
        );
    }
    
    // Delegate capture methods
    public BufferedImage captureScreen() throws ScreenCaptureException {
        return captureInterface.captureScreen();
    }
    
    public BufferedImage captureRegion(Rectangle region) throws ScreenCaptureException {
        return captureInterface.captureRegion(region);
    }
    
    public BufferedImage captureScreen(int screenIndex) throws ScreenCaptureException {
        return captureInterface.captureScreen(screenIndex);
    }
    
    public List<ScreenInfo> getAvailableScreens() {
        return captureInterface.getAvailableScreens();
    }
    public ScreenInfo getScreenInfo(Integer screenIndex){
        return captureInterface.getAvailableScreens().get(screenIndex);
    }
    
    public Rectangle getPrimaryScreenBounds() {
        return captureInterface.getPrimaryScreenBounds();
    }
    
    public boolean isSupported() {
        return captureInterface.isSupported();
    }
    
    // Convenience methods that combine capture and save
    public boolean captureAndSave(String filePath, String format) throws Exception{
        BufferedImage image = captureScreen();
        return saver.saveToFile(image, filePath, format);
    }
    
    public boolean captureRegionAndSave(Rectangle region, String filePath, String format) throws Exception{
        try {
            BufferedImage image = captureRegion(region);
            return saver.saveToFile(image, filePath, format);
        } catch (ScreenCaptureException e) {
           LoggerService.info("Failed to capture region: " + e.getMessage());
            return false;
        }
    }
    
    public boolean captureScreenAndSave(int screenIndex, String filePath, String format) throws Exception{
        BufferedImage image = captureScreen(screenIndex);
        return saver.saveToFile(image, filePath, format);
    }
    
    public byte[] captureToByteArray(String format) throws Exception{
        BufferedImage image = captureScreen();
        return saver.toByteArray(image, format);
    }
    
    // Delegate save methods
    public String[] getSupportedFormats() {
        return saver.getSupportedFormats();
    }

    public boolean saveToFile(BufferedImage image, String filePath, String format) throws Exception{
        return saver.saveToFile(image, filePath, format);
    }
    public boolean saveToStream(BufferedImage image, OutputStream outputStream, String format){
        return saver.saveToStream(image, outputStream, format);
    }

    public byte[] toByteArray(BufferedImage image, String format) throws Exception{
        return saver.toByteArray(image, format);
    }
}
