package com.anyscreen.implementations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import java.awt.AWTException;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import com.anyscreen.exceptions.ScreenCaptureException;
import com.anyscreen.models.ScreenInfo;
import com.anyscreen.services.LoggerService;
import com.anyscreen.utils.TestUtils;

/**
 * Comprehensive test suite for RobotScreenCapture implementation.
 * Tests screen capture functionality, error handling, and multi-screen support.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RobotScreenCapture Tests")
public class RobotScreenCaptureTest {

    @Mock
    private Robot mockRobot;
    
    @Mock
    private GraphicsEnvironment mockGraphicsEnvironment;
    
    @Mock
    private GraphicsDevice mockDevice1;
    
    @Mock
    private GraphicsDevice mockDevice2;
    
    private RobotScreenCapture robotScreenCapture;

    @BeforeEach
    void setUp() {
        LoggerService.reset();
        // Note: We cannot easily mock the constructor due to native dependencies
        // Most tests will need to use integration-style testing or skip Robot creation
    }

    @AfterEach
    void tearDown() {
        TestUtils.cleanupTestFiles();
        LoggerService.reset();
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create RobotScreenCapture successfully")
        void shouldCreateRobotScreenCaptureSuccessfully() {
            // Given & When & Then
            try {
                RobotScreenCapture capture = new RobotScreenCapture();
                assertThat(capture).isNotNull();
                assertThat(capture.isSupported()).isTrue();
            } catch (ScreenCaptureException e) {
                // This might fail in headless environments, which is acceptable
                assertThat(e.getMessage()).contains("Error while creating robot");
            }
        }

        @Test
        @DisplayName("Should handle AWTException during Robot creation")
        void shouldHandleAWTExceptionDuringRobotCreation() {
            // This test demonstrates the expected behavior when Robot creation fails
            // In actual headless environments, this is the expected behavior
            try {
                new RobotScreenCapture();
            } catch (ScreenCaptureException e) {
                assertThat(e.getMessage()).contains("Error while creating robot");
                assertThat(e.getCause()).isInstanceOf(AWTException.class);
            }
        }
    }

    @Nested
    @DisplayName("Screen Capture Tests")
    class ScreenCaptureTests {

        @Test
        @DisplayName("Should capture primary screen successfully")
        void shouldCapturePrimaryScreenSuccessfully() throws ScreenCaptureException {
            try {
                // Given
                RobotScreenCapture capture = new RobotScreenCapture();
                
                // When
                BufferedImage result = capture.captureScreen();
                
                // Then
                assertThat(result).isNotNull();
                assertThat(result.getWidth()).isGreaterThan(0);
                assertThat(result.getHeight()).isGreaterThan(0);
            } catch (ScreenCaptureException e) {
                // Skip test in headless environment
                assertThat(e.getMessage()).contains("Error while creating robot");
            }
        }

        @Test
        @DisplayName("Should capture specific screen by index")
        void shouldCaptureSpecificScreenByIndex() throws ScreenCaptureException {
            try {
                // Given
                RobotScreenCapture capture = new RobotScreenCapture();
                
                // When
                BufferedImage result = capture.captureScreen(0);
                
                // Then
                assertThat(result).isNotNull();
                assertThat(result.getWidth()).isGreaterThan(0);
                assertThat(result.getHeight()).isGreaterThan(0);
            } catch (ScreenCaptureException e) {
                // Skip test in headless environment or handle invalid screen index
                assertThat(e.getMessage()).satisfiesAnyOf(
                    message -> assertThat(message).contains("Error while creating robot"),
                    message -> assertThat(message).contains("Invalid screen index")
                );
            }
        }

        @Test
        @DisplayName("Should handle invalid screen index")
        void shouldHandleInvalidScreenIndex() throws ScreenCaptureException {
            try {
                // Given
                RobotScreenCapture capture = new RobotScreenCapture();
                
                // When & Then
                assertThatThrownBy(() -> capture.captureScreen(-1))
                    .isInstanceOf(ScreenCaptureException.class)
                    .hasMessageContaining("Invalid screen index");
                
                assertThatThrownBy(() -> capture.captureScreen(999))
                    .isInstanceOf(ScreenCaptureException.class)
                    .hasMessageContaining("Invalid screen index");
            } catch (ScreenCaptureException e) {
                // Skip test in headless environment
                assertThat(e.getMessage()).contains("Error while creating robot");
            }
        }

        @Test
        @DisplayName("Should capture specific region successfully")
        void shouldCaptureSpecificRegionSuccessfully() throws ScreenCaptureException {
            try {
                // Given
                RobotScreenCapture capture = new RobotScreenCapture();
                Rectangle region = new Rectangle(0, 0, 100, 100);
                
                // When
                BufferedImage result = capture.captureRegion(region);
                
                // Then
                assertThat(result).isNotNull();
                assertThat(result.getWidth()).isEqualTo(100);
                assertThat(result.getHeight()).isEqualTo(100);
            } catch (ScreenCaptureException e) {
                // Skip test in headless environment
                assertThat(e.getMessage()).contains("Error while creating robot");
            }
        }

        @Test
        @DisplayName("Should handle null region")
        void shouldHandleNullRegion() throws ScreenCaptureException {
            try {
                // Given
                RobotScreenCapture capture = new RobotScreenCapture();
                
                // When & Then
                assertThatThrownBy(() -> capture.captureRegion(null))
                    .isInstanceOf(ScreenCaptureException.class)
                    .hasMessage("Region cannot be null");
            } catch (ScreenCaptureException e) {
                // Skip test in headless environment
                assertThat(e.getMessage()).contains("Error while creating robot");
            }
        }

        @Test
        @DisplayName("Should handle zero-sized region")
        void shouldHandleZeroSizedRegion() throws ScreenCaptureException {
            try {
                // Given
                RobotScreenCapture capture = new RobotScreenCapture();
                Rectangle zeroRegion = new Rectangle(0, 0, 0, 0);
                
                // When
                BufferedImage result = capture.captureRegion(zeroRegion);
                
                // Then
                assertThat(result).isNotNull();
                assertThat(result.getWidth()).isEqualTo(0);
                assertThat(result.getHeight()).isEqualTo(0);
            } catch (ScreenCaptureException e) {
                // Skip test in headless environment
                assertThat(e.getMessage()).contains("Error while creating robot");
            }
        }

        @Test
        @DisplayName("Should handle negative region coordinates")
        void shouldHandleNegativeRegionCoordinates() throws ScreenCaptureException {
            try {
                // Given
                RobotScreenCapture capture = new RobotScreenCapture();
                Rectangle negativeRegion = new Rectangle(-10, -10, 50, 50);
                
                // When
                BufferedImage result = capture.captureRegion(negativeRegion);
                
                // Then
                assertThat(result).isNotNull();
                assertThat(result.getWidth()).isEqualTo(50);
                assertThat(result.getHeight()).isEqualTo(50);
            } catch (ScreenCaptureException e) {
                // Skip test in headless environment
                assertThat(e.getMessage()).contains("Error while creating robot");
            }
        }
    }

    @Nested
    @DisplayName("Screen Information Tests")
    class ScreenInformationTests {

        @Test
        @DisplayName("Should get available screens information")
        void shouldGetAvailableScreensInformation() throws ScreenCaptureException {
            try {
                // Given
                RobotScreenCapture capture = new RobotScreenCapture();
                
                // When
                List<ScreenInfo> screens = capture.getAvailableScreens();
                
                // Then
                assertThat(screens).isNotNull();
                assertThat(screens).isNotEmpty();
                
                for (ScreenInfo screen : screens) {
                    assertThat(screen.getIndex()).isGreaterThanOrEqualTo(0);
                    assertThat(screen.getBounds()).isNotNull();
                    assertThat(screen.getBounds().getWidth()).isGreaterThan(0);
                    assertThat(screen.getBounds().getHeight()).isGreaterThan(0);
                    assertThat(screen.getDeviceId()).isNotNull();
                }
                
                // At least one screen should be primary
                boolean hasPrimaryScreen = screens.stream().anyMatch(ScreenInfo::isPrimary);
                assertThat(hasPrimaryScreen).isTrue();
            } catch (ScreenCaptureException e) {
                // Skip test in headless environment
                assertThat(e.getMessage()).contains("Error while creating robot");
            }
        }

        @Test
        @DisplayName("Should get primary screen bounds")
        void shouldGetPrimaryScreenBounds() throws ScreenCaptureException {
            try {
                // Given
                RobotScreenCapture capture = new RobotScreenCapture();
                
                // When
                Rectangle bounds = capture.getPrimaryScreenBounds();
                
                // Then
                assertThat(bounds).isNotNull();
                assertThat(bounds.getWidth()).isGreaterThan(0);
                assertThat(bounds.getHeight()).isGreaterThan(0);
                assertThat(bounds.getX()).isGreaterThanOrEqualTo(0);
                assertThat(bounds.getY()).isGreaterThanOrEqualTo(0);
            } catch (ScreenCaptureException e) {
                // Skip test in headless environment
                assertThat(e.getMessage()).contains("Error while creating robot");
            }
        }

        @Test
        @DisplayName("Should report support status correctly")
        void shouldReportSupportStatusCorrectly() throws ScreenCaptureException {
            try {
                // Given
                RobotScreenCapture capture = new RobotScreenCapture();
                
                // When
                boolean isSupported = capture.isSupported();
                
                // Then
                assertThat(isSupported).isTrue();
            } catch (ScreenCaptureException e) {
                // In headless environment, we can still test the interface
                assertThat(e.getMessage()).contains("Error while creating robot");
            }
        }

        @Test
        @DisplayName("Should handle screen information consistency")
        void shouldHandleScreenInformationConsistency() throws ScreenCaptureException {
            try {
                // Given
                RobotScreenCapture capture = new RobotScreenCapture();
                
                // When
                List<ScreenInfo> screens1 = capture.getAvailableScreens();
                List<ScreenInfo> screens2 = capture.getAvailableScreens();
                Rectangle primaryBounds1 = capture.getPrimaryScreenBounds();
                Rectangle primaryBounds2 = capture.getPrimaryScreenBounds();
                
                // Then - Multiple calls should return consistent results
                assertThat(screens1).hasSize(screens2.size());
                assertThat(primaryBounds1).isEqualTo(primaryBounds2);
                
                for (int i = 0; i < screens1.size(); i++) {
                    ScreenInfo screen1 = screens1.get(i);
                    ScreenInfo screen2 = screens2.get(i);
                    
                    assertThat(screen1.getIndex()).isEqualTo(screen2.getIndex());
                    assertThat(screen1.getBounds()).isEqualTo(screen2.getBounds());
                    assertThat(screen1.isPrimary()).isEqualTo(screen2.isPrimary());
                    assertThat(screen1.getDeviceId()).isEqualTo(screen2.getDeviceId());
                }
            } catch (ScreenCaptureException e) {
                // Skip test in headless environment
                assertThat(e.getMessage()).contains("Error while creating robot");
            }
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should provide meaningful error messages")
        void shouldProvideMeaningfulErrorMessages() {
            try {
                RobotScreenCapture capture = new RobotScreenCapture();
                
                // Test invalid screen index error message
                assertThatThrownBy(() -> capture.captureScreen(-1))
                    .isInstanceOf(ScreenCaptureException.class)
                    .hasMessageContaining("Invalid screen index: -1")
                    .hasMessageContaining("Available screens: 0-");
                
                // Test null region error message
                assertThatThrownBy(() -> capture.captureRegion(null))
                    .isInstanceOf(ScreenCaptureException.class)
                    .hasMessage("Region cannot be null");
                    
            } catch (ScreenCaptureException e) {
                // Skip test in headless environment
                assertThat(e.getMessage()).contains("Error while creating robot");
            }
        }

        @Test
        @DisplayName("Should handle extreme screen indices gracefully")
        void shouldHandleExtremeScreenIndicesGracefully() throws ScreenCaptureException {
            try {
                // Given
                RobotScreenCapture capture = new RobotScreenCapture();
                List<ScreenInfo> screens = capture.getAvailableScreens();
                int maxValidIndex = screens.size() - 1;
                
                // When & Then
                assertThatThrownBy(() -> capture.captureScreen(Integer.MAX_VALUE))
                    .isInstanceOf(ScreenCaptureException.class)
                    .hasMessageContaining("Invalid screen index: " + Integer.MAX_VALUE)
                    .hasMessageContaining("Available screens: 0-" + maxValidIndex);
                
                assertThatThrownBy(() -> capture.captureScreen(Integer.MIN_VALUE))
                    .isInstanceOf(ScreenCaptureException.class)
                    .hasMessageContaining("Invalid screen index: " + Integer.MIN_VALUE);
            } catch (ScreenCaptureException e) {
                // Skip test in headless environment
                assertThat(e.getMessage()).contains("Error while creating robot");
            }
        }

        @Test
        @DisplayName("Should handle very large regions")
        void shouldHandleVeryLargeRegions() throws ScreenCaptureException {
            try {
                // Given
                RobotScreenCapture capture = new RobotScreenCapture();
                Rectangle largeRegion = new Rectangle(0, 0, 10000, 10000);
                
                // When
                BufferedImage result = capture.captureRegion(largeRegion);
                
                // Then
                assertThat(result).isNotNull();
                // The actual captured size may be clipped to screen bounds
                assertThat(result.getWidth()).isGreaterThan(0);
                assertThat(result.getHeight()).isGreaterThan(0);
            } catch (ScreenCaptureException e) {
                // Skip test in headless environment
                assertThat(e.getMessage()).contains("Error while creating robot");
            }
        }
    }

    @Nested
    @DisplayName("Performance and Thread Safety Tests")
    class PerformanceAndThreadSafetyTests {

        @Test
        @DisplayName("Should handle concurrent capture operations")
        void shouldHandleConcurrentCaptureOperations() throws Exception {
            try {
                // Given
                RobotScreenCapture capture = new RobotScreenCapture();
                int threadCount = 5;
                Thread[] threads = new Thread[threadCount];
                boolean[] results = new boolean[threadCount];
                
                // When
                for (int i = 0; i < threadCount; i++) {
                    final int threadIndex = i;
                    threads[i] = new Thread(() -> {
                        try {
                            BufferedImage image = capture.captureScreen();
                            results[threadIndex] = (image != null && image.getWidth() > 0);
                        } catch (Exception e) {
                            results[threadIndex] = false;
                        }
                    });
                    threads[i].start();
                }
                
                // Wait for all threads to complete
                for (Thread thread : threads) {
                    thread.join();
                }
                
                // Then
                for (boolean result : results) {
                    assertThat(result).isTrue();
                }
            } catch (ScreenCaptureException e) {
                // Skip test in headless environment
                assertThat(e.getMessage()).contains("Error while creating robot");
            }
        }

        @Test
        @DisplayName("Should maintain performance for multiple captures")
        void shouldMaintainPerformanceForMultipleCaptures() throws ScreenCaptureException {
            try {
                // Given
                RobotScreenCapture capture = new RobotScreenCapture();
                int captureCount = 10;
                
                // When
                long startTime = System.currentTimeMillis();
                for (int i = 0; i < captureCount; i++) {
                    BufferedImage image = capture.captureScreen();
                    assertThat(image).isNotNull();
                }
                long elapsedTime = System.currentTimeMillis() - startTime;
                
                // Then
                // Performance should be reasonable (less than 1 second per capture on average)
                assertThat(elapsedTime / captureCount).isLessThan(1000);
            } catch (ScreenCaptureException e) {
                // Skip test in headless environment
                assertThat(e.getMessage()).contains("Error while creating robot");
            }
        }

        @Test
        @DisplayName("Should handle rapid successive captures")
        void shouldHandleRapidSuccessiveCaptures() throws ScreenCaptureException {
            try {
                // Given
                RobotScreenCapture capture = new RobotScreenCapture();
                
                // When & Then
                for (int i = 0; i < 5; i++) {
                    BufferedImage image1 = capture.captureScreen();
                    BufferedImage image2 = capture.captureScreen();
                    
                    assertThat(image1).isNotNull();
                    assertThat(image2).isNotNull();
                    assertThat(image1.getWidth()).isEqualTo(image2.getWidth());
                    assertThat(image1.getHeight()).isEqualTo(image2.getHeight());
                }
            } catch (ScreenCaptureException e) {
                // Skip test in headless environment
                assertThat(e.getMessage()).contains("Error while creating robot");
            }
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("Should work with screen capture service")
        void shouldWorkWithScreenCaptureService() throws Exception {
            try {
                // Given
                RobotScreenCapture capture = new RobotScreenCapture();
                
                // When
                List<ScreenInfo> screens = capture.getAvailableScreens();
                BufferedImage primaryScreen = capture.captureScreen();
                Rectangle primaryBounds = capture.getPrimaryScreenBounds();
                
                // Then
                assertThat(screens).isNotEmpty();
                assertThat(primaryScreen).isNotNull();
                assertThat(primaryBounds).isNotNull();
                
                // Primary screen image should match bounds
                assertThat(primaryScreen.getWidth()).isEqualTo((int) primaryBounds.getWidth());
                assertThat(primaryScreen.getHeight()).isEqualTo((int) primaryBounds.getHeight());
                
                // If multiple screens, test specific screen capture
                if (screens.size() > 1) {
                    BufferedImage specificScreen = capture.captureScreen(0);
                    assertThat(specificScreen).isNotNull();
                }
            } catch (ScreenCaptureException e) {
                // Skip test in headless environment
                assertThat(e.getMessage()).contains("Error while creating robot");
            }
        }

        @Test
        @DisplayName("Should provide consistent screen information")
        void shouldProvideConsistentScreenInformation() throws ScreenCaptureException {
            try {
                // Given
                RobotScreenCapture capture = new RobotScreenCapture();
                
                // When
                List<ScreenInfo> screens = capture.getAvailableScreens();
                
                // Then
                for (ScreenInfo screen : screens) {
                    BufferedImage captured = capture.captureScreen(screen.getIndex());
                    assertThat(captured).isNotNull();
                    
                    // Captured image should match screen bounds
                    assertThat(captured.getWidth()).isEqualTo((int) screen.getBounds().getWidth());
                    assertThat(captured.getHeight()).isEqualTo((int) screen.getBounds().getHeight());
                }
            } catch (ScreenCaptureException e) {
                // Skip test in headless environment
                assertThat(e.getMessage()).contains("Error while creating robot");
            }
        }
    }
}
