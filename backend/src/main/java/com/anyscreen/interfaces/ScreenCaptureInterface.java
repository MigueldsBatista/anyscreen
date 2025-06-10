package com.anyscreen.interfaces;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.List;

import com.anyscreen.exceptions.ScreenCaptureException;
import com.anyscreen.models.ScreenInfo;

/**
 * Interface for screen capture operations.
 * Focuses solely on capturing screen content, following single responsibility principle.
 */
public interface ScreenCaptureInterface {

    /**
     * Captures the entire primary screen.
     * @return BufferedImage of the captured screen
     * @throws ScreenCaptureException if capture fails
     */
    BufferedImage captureScreen() throws ScreenCaptureException;

    /**
     * Captures a specific region of the screen.
     * @param region The rectangular region to capture
     * @return BufferedImage of the captured region
     * @throws ScreenCaptureException if capture fails
     */
    BufferedImage captureRegion(Rectangle region) throws ScreenCaptureException;

    /**
     * Captures a specific screen/monitor by index.
     * @param screenIndex The index of the screen to capture (0-based)
     * @return BufferedImage of the captured screen
     * @throws ScreenCaptureException if capture fails or invalid screen index
     */
    BufferedImage captureScreen(int screenIndex) throws ScreenCaptureException;

    /**
     * Gets information about available screens/monitors.
     * @return List of screen information (dimensions, position, etc.)
     */
    List<ScreenInfo> getAvailableScreens();

    /**
     * Gets the bounds of the primary screen.
     * @return Rectangle representing the primary screen bounds
     */
    Rectangle getPrimaryScreenBounds();

    /**
     * Checks if screen capture is supported on this system.
     * @return true if screen capture is available, false otherwise
     */
    boolean isSupported();
}
