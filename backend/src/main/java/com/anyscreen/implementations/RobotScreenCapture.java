package com.anyscreen.implementations;

import java.awt.AWTException;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import com.anyscreen.exceptions.ScreenCaptureException;
import com.anyscreen.interfaces.ScreenCaptureInterface;
import com.anyscreen.models.ScreenInfo;

/**
 * Robot-based implementation of screen capture.
 * Focuses solely on capturing screen content using Java Robot API.
 */
public class RobotScreenCapture implements ScreenCaptureInterface {
    private final Robot robot;
    private final GraphicsEnvironment graphicsEnvironment;
    public RobotScreenCapture() throws ScreenCaptureException {
        try{
            this.robot = new Robot();
        }catch(AWTException e){
            throw new ScreenCaptureException("Error while creating robot: "+e.getMessage());
        }
        
        this.graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
    }
    
    public BufferedImage captureScreen() throws ScreenCaptureException {
        Rectangle screenBounds = getPrimaryScreenBounds();
        return robot.createScreenCapture(screenBounds);
    }
    
    public BufferedImage captureRegion(Rectangle region) throws ScreenCaptureException {
        if (region == null) {
            throw new ScreenCaptureException("Region cannot be null");
        }
        
        return robot.createScreenCapture(region);
    }
    
    public BufferedImage captureScreen(int screenIndex) throws ScreenCaptureException {
        GraphicsDevice[] screens = graphicsEnvironment.getScreenDevices();
        
        if (screenIndex < 0 || screenIndex >= screens.length) {
            throw new ScreenCaptureException(
                "Invalid screen index: " + screenIndex + ". Available screens: 0-" + (screens.length - 1)
            );
        }
        
        Rectangle bounds = screens[screenIndex].getDefaultConfiguration().getBounds();
        return robot.createScreenCapture(bounds);
    }
    
    public List<ScreenInfo> getAvailableScreens() {
        List<ScreenInfo> screens = new ArrayList<>();
        GraphicsDevice[] devices = graphicsEnvironment.getScreenDevices();
        GraphicsDevice defaultDevice = graphicsEnvironment.getDefaultScreenDevice();
        
        for (int i = 0; i < devices.length; i++) {
            GraphicsDevice device = devices[i];
            Rectangle bounds = device.getDefaultConfiguration().getBounds();
            boolean isPrimary = device.equals(defaultDevice);
            String deviceId = device.getIDstring();
            
            screens.add(new ScreenInfo(i, bounds, isPrimary, deviceId));
        }
        
        return screens;
    }
    
    public Rectangle getPrimaryScreenBounds() {
        return graphicsEnvironment.getDefaultScreenDevice()
                                 .getDefaultConfiguration()
                                 .getBounds();
    }
    
    public boolean isSupported() {
        return !GraphicsEnvironment.isHeadless() &&
                graphicsEnvironment.getScreenDevices().length > 0;
    }
}
