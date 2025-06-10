package com.anyscreen.utils;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.List;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Exchanger;
import java.util.Arrays;
import com.anyscreen.models.RecordingInfo;
import com.anyscreen.models.ScreenInfo;

/**
 * Utility class for creating common test resources and helper methods.
 * Provides reusable components across all test classes to improve coverage and consistency.
 */
public class TestUtils {
    
    // Test constants
    public static final int DEFAULT_WIDTH = 1920;
    public static final int DEFAULT_HEIGHT = 1080;
    public static final int DEFAULT_FRAME_RATE = 30;
    public static final String DEFAULT_FORMAT = "mp4";
    public static final String DEFAULT_OUTPUT_FILE = "test_recording.mp4";
    public static final String TEST_MEDIA_DIR = "test-media";
    public static final Color DEFAULT_COLOR = Color.WHITE;
    /**
     * Creates a mock BufferedImage with specified dimensions and color.
     * Useful for testing image processing functionality.
     */
    public static BufferedImage createMockImage(int width, int height, Color color) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setColor(color);
        g2d.fillRect(0, 0, width, height);
        g2d.dispose();
        return image;
    }
    /**
     * Creates a mock BufferedImage with specified dimensions and default color.
     * Useful for testing image processing functionality.
     */
    public static BufferedImage createMockImage(int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setColor(DEFAULT_COLOR);
        g2d.fillRect(0, 0, width, height);
        g2d.dispose();
        return image;
    }
    
    /**
     * Creates a mock BufferedImage with default dimensions (1920x1080) and blue color.
     */
    public static BufferedImage createMockImage() {
        return createMockImage(DEFAULT_WIDTH, DEFAULT_HEIGHT, Color.RED);
    }
    
    /**
     * Creates a small mock image for performance testing.
     */
    public static BufferedImage createSmallMockImage() {
        return createMockImage(640, 480, Color.RED);
    }
    
    /**
     * Creates a test ScreenInfo with default values.
     */
    public static ScreenInfo createMockScreenInfo() {
        return createMockScreenInfo(0, new Rectangle(0, 0, DEFAULT_WIDTH, DEFAULT_HEIGHT), true, "test-device-0");
    }
    /**
     * Creates a test ScreenInfo with multiple screen configuration.
     */
    public static java.util.List<ScreenInfo> createMockMultiScreenInfo() {
        return Arrays.asList(
            createMockScreenInfo(0, new Rectangle(0, 0, DEFAULT_WIDTH, DEFAULT_HEIGHT), true, "test-device-0"),
            createMockScreenInfo(1, new Rectangle(DEFAULT_WIDTH, 0, 1366, 768), false, "test-device-1"),
            createMockScreenInfo(2, new Rectangle(0, DEFAULT_HEIGHT, 1280, 720), false, "test-device-2")
        );
    }
    
    /**
     * Creates a test ScreenInfo with specified number of screens.
     */
    public static java.util.List<ScreenInfo> createMockMultiScreenInfo(int numberOfScreens) {
        java.util.List<ScreenInfo> screens = new java.util.ArrayList<>();
        
        for (int i = 0; i < numberOfScreens; i++) {
            boolean isPrimary = (i == 0);
            Rectangle bounds;
            
            if (i == 0) {
                bounds = new Rectangle(0, 0, DEFAULT_WIDTH, DEFAULT_HEIGHT);
            } else if (i == 1) {
                bounds = new Rectangle(DEFAULT_WIDTH, 0, 1366, 768);
            } else if (i == 2) {
                bounds = new Rectangle(0, DEFAULT_HEIGHT, 1280, 720);
            } else {
                // For additional screens, place them in a grid pattern
                int row = (i - 3) / 2;
                int col = (i - 3) % 2;
                bounds = new Rectangle(col * 1024, DEFAULT_HEIGHT + (row + 1) * 768, 1024, 768);
            }
            
            screens.add(createMockScreenInfo(i, bounds, isPrimary, "test-device-" + i));
        }
        
        return screens;
    }
    /**
     * Creates a test ScreenInfo with custom values.
     */
    public static ScreenInfo createMockScreenInfo(int index, Rectangle bounds, boolean isPrimary, String deviceId) {
        return new ScreenInfo(index, bounds, isPrimary, deviceId);
    }
    
    /**
     * Creates a test RecordingInfo with default values using builder pattern.
     */
    public static RecordingInfo createMockRecordingInfo() {
        return new RecordingInfo.Builder()
                .outputFile(DEFAULT_OUTPUT_FILE)
                .resolution(DEFAULT_WIDTH, DEFAULT_HEIGHT)
                .frameRate(DEFAULT_FRAME_RATE)
                .format(DEFAULT_FORMAT)
                .screenIndex(0)
                .build();
    }
    
    /**
     * Creates a test RecordingInfo with custom output file.
     */
    public static RecordingInfo createMockRecordingInfo(String outputFile) {
        return new RecordingInfo.Builder()
                .outputFile(outputFile)
                .resolution(DEFAULT_WIDTH, DEFAULT_HEIGHT)
                .frameRate(DEFAULT_FRAME_RATE)
                .format(DEFAULT_FORMAT)
                .screenIndex(0)
                .build();
    }
    
    /**
     * Creates a test RecordingInfo with custom dimensions.
     */
    public static RecordingInfo createMockRecordingInfo(int width, int height) {
        return new RecordingInfo.Builder()
                .outputFile(DEFAULT_OUTPUT_FILE)
                .resolution(width, height)
                .frameRate(DEFAULT_FRAME_RATE)
                .format(DEFAULT_FORMAT)
                .screenIndex(0)
                .build();
    }
    
    /**
     * Creates a test RecordingInfo with custom dimensions.
     */
    public static RecordingInfo createMockRecordingInfo(int width, int height, int fps) {
        return new RecordingInfo.Builder()
                .outputFile(DEFAULT_OUTPUT_FILE)
                .resolution(width, height)
                .frameRate(fps)
                .format(DEFAULT_FORMAT)
                .screenIndex(0)
                .build();
    }
    
    /**
     * Creates a temporary test directory for output files.
     */
    public static Path createTestDirectory() throws IOException {
        Path testDir = Paths.get(TEST_MEDIA_DIR);
        if (!Files.exists(testDir)) {
            Files.createDirectories(testDir);
        }
        return testDir;
    }
    
    /**
     * Creates a temporary test file path.
     */
    public static String createTestFilePath(String filename) throws IOException {
        Path testDir = createTestDirectory();
        return testDir.resolve(filename).toString();
    }
    
    /**
     * Cleans up test files and directories.
     */
    public static void cleanupTestFiles(){
        Path testDir = Paths.get(TEST_MEDIA_DIR);
            try {
            if (Files.exists(testDir)) {
                Files.walk(testDir)
                    .sorted((a, b) -> b.compareTo(a)) // Delete files before directories
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            // Ignore cleanup errors
                        }
                    });
                }
            } catch (IOException e) {
                //ignore IO Exceptions
            }
        }
    
    
    /**
     * Checks if a file exists and has content.
     */
    public static boolean isValidFile(String filePath) {
        File file = new File(filePath);
        return file.exists() && file.length() > 0;
    }
    
    /**
     * Creates a test Rectangle with specified bounds.
     */
    public static Rectangle createTestRectangle(int x, int y, int width, int height) {
        return new Rectangle(x, y, width, height);
    }
    
    /**
     * Creates a default test Rectangle (0, 0, 1920, 1080).
     */
    public static Rectangle createDefaultTestRectangle() {
        return createTestRectangle(0, 0, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }
    
    /**
     * Waits for a specified amount of time (useful for integration tests).
     */
    public static void waitMillis(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Creates an array of mock ScreenInfo objects for multi-screen testing.
     */
    public static java.util.List<ScreenInfo> createMultipleScreenInfos() {
        return Arrays.asList(
            createMockScreenInfo(0, new Rectangle(0, 0, 1920, 1080), true, "primary-screen"),
            createMockScreenInfo(1, new Rectangle(1920, 0, 1366, 768), false, "secondary-screen"),
            createMockScreenInfo(2, new Rectangle(0, 1080, 1280, 720), false, "third-screen")
        );
    }
    
    /**
     * Creates test data for parameterized tests - different image formats.
     */
    public static String[] getTestImageFormats() {
        return new String[]{"png", "jpg", "jpeg", "bmp", "gif"};
    }
    
    /**
     * Creates test data for parameterized tests - different resolutions.
     */
    public static int[][] getTestResolutions() {
        return new int[][]{
            {640, 480},
            {1280, 720},
            {1920, 1080},
            {2560, 1440},
            {3840, 2160}
        };
    }
    
    /**
     * Creates test data for parameterized tests - different frame rates.
     */
    public static int[] getTestFrameRates() {
        return new int[]{15, 24, 30, 60, 120};
    }

}
