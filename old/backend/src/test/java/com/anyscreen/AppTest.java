package com.anyscreen;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.awt.Rectangle;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import com.anyscreen.models.RecordingInfo;
import com.anyscreen.models.ScreenInfo;
import com.anyscreen.services.LoggerService;
import com.anyscreen.services.RecordingService;
import com.anyscreen.services.ScreenCaptureService;
import com.anyscreen.utils.TestUtils;

/**
 * Comprehensive test suite for the App main class.
 * Tests CLI interactions, screen selection, recording functionality,
 * and error handling scenarios.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("App Main Class Tests")
public class AppTest {
    
    private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;
    
    @BeforeEach
    void setUp() {
        // Capture system output for testing
        System.setOut(new PrintStream(outputStream));
        System.setErr(new PrintStream(outputStream));
        
        // Reset logger service for clean state
        LoggerService.reset();
    }
    
    @AfterEach
    void tearDown() {
        // Restore original streams
        System.setOut(originalOut);
        System.setErr(originalErr);
        
        // Clean up test resources
        TestUtils.cleanupTestFiles();
        LoggerService.reset();
    }
    
    @Nested
    @DisplayName("Main Method Tests")
    class MainMethodTests {
        
        @Test
        @DisplayName("Should initialize logger and complete recording workflow successfully")
        void shouldCompleteSuccessfulWorkflow() throws Exception {
            // Given
            String input = "0\n"; // Select first screen
            System.setIn(new ByteArrayInputStream(input.getBytes()));
            
            ScreenInfo mockScreen = TestUtils.createMockScreenInfo();
            List<ScreenInfo> screens = Arrays.asList(mockScreen);
            
            ScreenCaptureService mockCaptureService = mock(ScreenCaptureService.class);
            RecordingService mockRecordingService = mock(RecordingService.class);
            
            when(mockCaptureService.getAvailableScreens()).thenReturn(screens);
            when(mockCaptureService.getScreenInfo(0)).thenReturn(mockScreen);
            
            try (MockedStatic<ScreenCaptureService> captureServiceStatic = mockStatic(ScreenCaptureService.class);
                 MockedStatic<RecordingService> recordingServiceStatic = mockStatic(RecordingService.class)) {
                
                captureServiceStatic.when(ScreenCaptureService::createDefault)
                    .thenReturn(mockCaptureService);
                recordingServiceStatic.when(() -> RecordingService.createDefault(any(RecordingInfo.class)))
                    .thenReturn(mockRecordingService);
                
                // When
                App.main(new String[]{});
                
                // Then
                verify(mockCaptureService).getAvailableScreens();
                verify(mockCaptureService).getScreenInfo(0);
                verify(mockRecordingService).startRecording();
                verify(mockRecordingService).stopRecording();
            }
        }
        
        @Test
        @DisplayName("Should handle exception during screen selection")
        void shouldHandleScreenSelectionException() throws Exception {
            // Given
            ScreenCaptureService mockCaptureService = mock(ScreenCaptureService.class);
            when(mockCaptureService.getAvailableScreens())
                .thenThrow(new RuntimeException("Screen capture failed"));
            
            try (MockedStatic<ScreenCaptureService> captureServiceStatic = mockStatic(ScreenCaptureService.class)) {
                captureServiceStatic.when(ScreenCaptureService::createDefault)
                    .thenReturn(mockCaptureService);
                
                // When
                App.main(new String[]{});
                
                // Then - Should complete without throwing exception
                assertThat(outputStream.toString()).contains("Application finished");
            }
        }
        
        @Test
        @DisplayName("Should handle exception during recording")
        void shouldHandleRecordingException() throws Exception {
            // Given
            String input = "0\n";
            System.setIn(new ByteArrayInputStream(input.getBytes()));
            
            ScreenInfo mockScreen = TestUtils.createMockScreenInfo();
            List<ScreenInfo> screens = Arrays.asList(mockScreen);
            
            ScreenCaptureService mockCaptureService = mock(ScreenCaptureService.class);
            RecordingService mockRecordingService = mock(RecordingService.class);
            
            when(mockCaptureService.getAvailableScreens()).thenReturn(screens);
            when(mockCaptureService.getScreenInfo(0)).thenReturn(mockScreen);
            doThrow(new RuntimeException("Recording failed")).when(mockRecordingService).startRecording();
            
            try (MockedStatic<ScreenCaptureService> captureServiceStatic = mockStatic(ScreenCaptureService.class);
                 MockedStatic<RecordingService> recordingServiceStatic = mockStatic(RecordingService.class)) {
                
                captureServiceStatic.when(ScreenCaptureService::createDefault)
                    .thenReturn(mockCaptureService);
                recordingServiceStatic.when(() -> RecordingService.createDefault(any(RecordingInfo.class)))
                    .thenReturn(mockRecordingService);
                
                // When
                App.main(new String[]{});
                
                // Then - Should handle exception gracefully
                assertThat(outputStream.toString()).contains("Application finished");
            }
        }
    }
    
    @Nested
    @DisplayName("Screen Selection Tests")
    class ScreenSelectionTests {
        
        @Test
        @DisplayName("Should display available screens and accept valid selection")
        void shouldSelectValidScreen() throws Exception {
            // Given
            String input = "1\n";
            setSystemInput(input);
            
            ScreenInfo screen1 = TestUtils.createMockScreenInfo();
            ScreenInfo screen2 = TestUtils.createMockScreenInfo();;
            List<ScreenInfo> screens = Arrays.asList(screen1, screen2);
            
            ScreenCaptureService mockCaptureService = mock(ScreenCaptureService.class);
            when(mockCaptureService.getAvailableScreens()).thenReturn(screens);
            when(mockCaptureService.getScreenInfo(1)).thenReturn(screen2);
            
            try (MockedStatic<ScreenCaptureService> captureServiceStatic = mockStatic(ScreenCaptureService.class)) {
                captureServiceStatic.when(ScreenCaptureService::createDefault)
                    .thenReturn(mockCaptureService);
                
                // When
                ScreenInfo result = invokeSelectScreen();
                
                // Then
                assertThat(result).isEqualTo(screen2);
                verify(mockCaptureService).getAvailableScreens();
                verify(mockCaptureService).getScreenInfo(1);
            }
        }
        
        @Test
        @DisplayName("Should handle multiple screens display correctly")
        void shouldDisplayMultipleScreens() throws Exception {
            // Given
            String input = "0\n";
            setSystemInput(input);
            
            List<ScreenInfo> screens = TestUtils.createMultipleScreenInfos();
            ScreenCaptureService mockCaptureService = mock(ScreenCaptureService.class);
            when(mockCaptureService.getAvailableScreens()).thenReturn(screens);
            when(mockCaptureService.getScreenInfo(0)).thenReturn(screens.get(0));
            
            try (MockedStatic<ScreenCaptureService> captureServiceStatic = mockStatic(ScreenCaptureService.class)) {
                captureServiceStatic.when(ScreenCaptureService::createDefault)
                    .thenReturn(mockCaptureService);
                
                // When
                invokeSelectScreen();
                
                // Then
                verify(mockCaptureService).getAvailableScreens();
                verify(mockCaptureService).getScreenInfo(0);
            }
        }
    }
    
    @Nested
    @DisplayName("Wait For Recording Tests")
    class WaitForRecordingTests {
        
        @Test
        @DisplayName("Should wait for specified duration")
        void shouldWaitForSpecifiedDuration() throws Exception {
            // Given
            long startTime = System.currentTimeMillis();
            long waitTime = 100; // Short wait for test
            
            // When
            invokeWaitForRecording(waitTime);
            
            // Then
            long elapsed = System.currentTimeMillis() - startTime;
            assertThat(elapsed).isGreaterThanOrEqualTo(waitTime);
        }
        
        @Test
        @DisplayName("Should handle thread interruption gracefully")
        void shouldHandleThreadInterruption() throws Exception {
            // Given
            Thread testThread = new Thread(() -> {
                try {
                    invokeWaitForRecording(5000); // Long wait
                } catch (Exception e) {
                    // Expected for reflection call
                }
            });
            
            // When
            testThread.start();
            Thread.sleep(50); // Let it start waiting
            testThread.interrupt();
            testThread.join(1000); // Wait for completion
            
            // Then
            assertThat(testThread.isInterrupted()).isTrue();
        }
    }
    
    // Helper methods for reflection-based testing of private methods
    
    private void setSystemInput(String input) {
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        
        try {
            Field scannerField = App.class.getDeclaredField("scanner");
            scannerField.setAccessible(true);
            scannerField.set(null, new Scanner(System.in));
        } catch (Exception e) {
            throw new RuntimeException("Failed to set system input", e);
        }
    }
    
    private ScreenInfo invokeSelectScreen() throws Exception {
        Method selectScreenMethod = App.class.getDeclaredMethod("selectScreen");
        selectScreenMethod.setAccessible(true);
        return (ScreenInfo) selectScreenMethod.invoke(null);
    }
    
    private void invokeWaitForRecording(long milliseconds) throws Exception {
        Method waitForRecordingMethod = App.class.getDeclaredMethod("waitForRecording", long.class);
        waitForRecordingMethod.setAccessible(true);
        waitForRecordingMethod.invoke(null, milliseconds);
    }
}
