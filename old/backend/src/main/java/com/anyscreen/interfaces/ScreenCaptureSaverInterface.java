package com.anyscreen.interfaces;

import java.awt.image.BufferedImage;
import java.io.OutputStream;

/**
 * Interface for saving screen captures in various formats.
 * Separates the concern of persistence from screen capture.
 */
public interface ScreenCaptureSaverInterface {
    
    /**
     * Saves a BufferedImage to a file.
     * @param image The image to save
     * @param filePath The path where to save the file
     * @param format The image format (png, jpg, gif, etc.)
     * @return true if successful, false otherwise
     */
    boolean saveToFile(BufferedImage image, String filePath, String format) throws Exception;
    
    /**
     * Saves a BufferedImage to an OutputStream.
     * @param image The image to save
     * @param outputStream The stream to write to
     * @param format The image format (png, jpg, gif, etc.)
     * @return true if successful, false otherwise
     */
    boolean saveToStream(BufferedImage image, OutputStream outputStream, String format);
    
    /**
     * Converts a BufferedImage to byte array.
     * @param image The image to convert
     * @param format The image format (png, jpg, gif, etc.)
     * @return byte array representation of the image, or null if conversion fails
     */
    byte[] toByteArray(BufferedImage image, String format) throws Exception;
    
    /**
     * Gets the supported image formats.
     * @return Array of supported format names
     */
    String[] getSupportedFormats();
}
