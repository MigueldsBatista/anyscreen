package com.anyscreen.implementations;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import javax.imageio.ImageIO;

import com.anyscreen.interfaces.ScreenCaptureSaverInterface;
import com.anyscreen.services.LoggerService;

/**
 * Default implementation of ScreenCaptureSaver using Java ImageIO.
 */
public class ImageIOScreenCaptureSaver implements ScreenCaptureSaverInterface {
    
    public boolean saveToFile(BufferedImage image, String filePath, String format) throws Exception{
        try {
            if (image == null || filePath == null || format == null) {
            return false;
        }
        
        File outputFile = new File(filePath);
            // Create parent directories if they don't exist
        File parentDir = outputFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
            
        return ImageIO.write(image, format.toLowerCase(), outputFile);
        } catch (IOException e) {
            return false;
        } 
    }
    
    public boolean saveToStream(BufferedImage image, OutputStream outputStream, String format) {
        if (image == null || outputStream == null || format == null) {
            return false;
        }
        
        try {
            return ImageIO.write(image, format.toLowerCase(), outputStream);
        } catch (IOException e) {
           LoggerService.info("Failed to save image to stream - " + e.getMessage());
            return false;
        }
    }
    
    public byte[] toByteArray(BufferedImage image, String format) throws Exception{
        if (image == null || format == null) {
            return null;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        if (ImageIO.write(image, format.toLowerCase(), baos)) {
            return baos.toByteArray();
        }
        return null;
    }
    
    public String[] getSupportedFormats() {
        return ImageIO.getWriterFormatNames();
    }
}
