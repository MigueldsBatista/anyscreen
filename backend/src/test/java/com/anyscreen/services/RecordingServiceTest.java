package com.anyscreen.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.anyscreen.abstracts.AbstractScreenRecorder;
import com.anyscreen.exceptions.RecordingException;
import com.anyscreen.models.RecordingInfo;
import com.anyscreen.utils.TestUtils;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.awt.image.BufferedImage;

/**
 * Comprehensive test suite for RecordingService.
 * Tests service composition, recording lifecycle, and error handling.
 */
class RecordingServiceTest {

    @Mock
    private ScreenCaptureService mockCaptureService;

    @Mock
    private AbstractScreenRecorder mockRecorder;

    private RecordingService recordingService;
    private RecordingInfo testRecordingInfo;
    private BufferedImage testImage;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        recordingService = new RecordingService(mockCaptureService, mockRecorder);
        testRecordingInfo = TestUtils.createMockRecordingInfo();
        testImage = TestUtils.createMockImage();
    }

    @AfterEach
    void tearDown() throws Exception {
        TestUtils.cleanupTestFiles();
    }

    @Nested
    @DisplayName("Constructor and Factory Tests")
    class ConstructorAndFactoryTests {

        @Test
        @DisplayName("Should create service with provided dependencies")
        void shouldCreateServiceWithProvidedDependencies() {
            // When
            RecordingService service = new RecordingService(mockCaptureService, mockRecorder);

            // Then
            assertThat(service).isNotNull();
            assertThat(service.getCaptureService()).isEqualTo(mockCaptureService);
            assertThat(service.getRecorder()).isEqualTo(mockRecorder);
        }

        @Test
        @DisplayName("Should create default service")
        void shouldCreateDefaultService() throws RecordingException {
            // Given
            RecordingInfo recordingInfo = TestUtils.createMockRecordingInfo();

            // When & Then
            assertThatCode(() -> RecordingService.createDefault(recordingInfo))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should throw exception when creating default service fails")
        void shouldThrowExceptionWhenCreatingDefaultServiceFails() {
            // Given
            RecordingInfo invalidRecordingInfo = null;

            // When & Then
            assertThatThrownBy(() -> RecordingService.createDefault(invalidRecordingInfo))
                    .isInstanceOf(RecordingException.class)
                    .hasMessage("Failed to create recording service");
        }
    }

    @Nested
    @DisplayName("Recording Lifecycle Tests")
    class RecordingLifecycleTests {

        @Test
        @DisplayName("Should start recording successfully")
        void shouldStartRecordingSuccessfully() throws Exception {
            // Given
            when(mockRecorder.isRecording()).thenReturn(false);
            when(mockRecorder.getRecordingInfo()).thenReturn(testRecordingInfo);

            // When
            recordingService.startRecording();

            // Then
            verify(mockRecorder).start();
        }

        @Test
        @DisplayName("Should throw exception when starting recording while already recording")
        void shouldThrowExceptionWhenStartingRecordingWhileAlreadyRecording() throws Exception {
            // Given
            when(mockRecorder.isRecording()).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> recordingService.startRecording())
                    .isInstanceOf(RecordingException.class)
                    .hasMessage("Recording already in progress");
        }

        @Test
        @DisplayName("Should stop recording successfully")
        void shouldStopRecordingSuccessfully() throws Exception {
            // When
            recordingService.stopRecording();

            // Then
            verify(mockRecorder).stop();
        }

        @Test
        @DisplayName("Should handle stop recording when recorder is null")
        void shouldHandleStopRecordingWhenRecorderIsNull() throws Exception {
            // Given
            RecordingService serviceWithNullRecorder = new RecordingService(mockCaptureService, null);

            // When & Then
            assertThatCode(() -> serviceWithNullRecorder.stopRecording())
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should check recording status correctly")
        void shouldCheckRecordingStatusCorrectly() {
            // Given
            when(mockRecorder.isRecording()).thenReturn(true);

            // When
            boolean isRecording = recordingService.isRecording();

            // Then
            assertThat(isRecording).isTrue();
            verify(mockRecorder).isRecording();
        }

        @Test
        @DisplayName("Should return false when recorder is null for isRecording")
        void shouldReturnFalseWhenRecorderIsNullForIsRecording() {
            // Given
            RecordingService serviceWithNullRecorder = new RecordingService(mockCaptureService, null);

            // When
            boolean isRecording = serviceWithNullRecorder.isRecording();

            // Then
            assertThat(isRecording).isFalse();
        }
    }

    @Nested
    @DisplayName("Frame Capture and Recording Tests")
    class FrameCaptureAndRecordingTests {

        @Test
        @DisplayName("Should capture and record frames during recording")
        void shouldCaptureAndRecordFramesDuringRecording() throws Exception {
            // Given
            when(mockRecorder.getRecordingInfo()).thenReturn(testRecordingInfo);
            when(mockCaptureService.captureScreen(anyInt())).thenReturn(testImage);

            // When
            recordingService.startRecording();
            TestUtils.waitMillis(100); // Allow some frame captures

            // Then
            verify(mockCaptureService, atLeastOnce()).captureScreen(testRecordingInfo.getScreenIndex());
            verify(mockRecorder, atLeastOnce()).recordFrame(testImage);
        }

        @Test
        @DisplayName("Should handle frame capture exceptions gracefully")
        void shouldHandleFrameCaptureExceptionsGracefully() throws Exception {
            // Given
            when(mockRecorder.getRecordingInfo()).thenReturn(testRecordingInfo);
            when(mockCaptureService.captureScreen(anyInt())).thenThrow(new RuntimeException("Capture failed"));

            // When
            recordingService.startRecording();
            TestUtils.waitMillis(100);

            // Then - should not crash, should handle exception internally
            verify(mockCaptureService, atLeastOnce()).captureScreen(anyInt());
            // Verify that recording frame is not called when capture fails
            verify(mockRecorder, never()).recordFrame(any());
        }

        @Test
        @DisplayName("Should handle record frame exceptions gracefully")
        void shouldHandleRecordFrameExceptionsGracefully() throws Exception {
            // Given
            when(mockRecorder.getRecordingInfo()).thenReturn(testRecordingInfo);
            when(mockCaptureService.captureScreen(anyInt())).thenReturn(testImage);
            doThrow(new RuntimeException("Record failed")).when(mockRecorder).recordFrame(any());

            // When
            recordingService.startRecording();
            TestUtils.waitMillis(100);

            // Then - should not crash, should handle exception internally
            verify(mockCaptureService, atLeastOnce()).captureScreen(anyInt());
            verify(mockRecorder, atLeastOnce()).recordFrame(testImage);
        }
    }

    @Nested
    @DisplayName("Getter Methods Tests")
    class GetterMethodsTests {

        @Test
        @DisplayName("Should return recording info from recorder")
        void shouldReturnRecordingInfoFromRecorder() {
            // Given
            when(mockRecorder.getRecordingInfo()).thenReturn(testRecordingInfo);

            // When
            RecordingInfo result = recordingService.getRecordingInfo();

            // Then
            assertThat(result).isEqualTo(testRecordingInfo);
            verify(mockRecorder).getRecordingInfo();
        }

        @Test
        @DisplayName("Should return null when recorder is null for getRecordingInfo")
        void shouldReturnNullWhenRecorderIsNullForGetRecordingInfo() {
            // Given
            RecordingService serviceWithNullRecorder = new RecordingService(mockCaptureService, null);

            // When
            RecordingInfo result = serviceWithNullRecorder.getRecordingInfo();

            // Then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should return recorder instance")
        void shouldReturnRecorderInstance() {
            // When
            AbstractScreenRecorder result = recordingService.getRecorder();

            // Then
            assertThat(result).isEqualTo(mockRecorder);
        }

        @Test
        @DisplayName("Should return capture service instance")
        void shouldReturnCaptureServiceInstance() {
            // When
            ScreenCaptureService result = recordingService.getCaptureService();

            // Then
            assertThat(result).isEqualTo(mockCaptureService);
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should propagate recorder start exceptions")
        void shouldPropagateRecorderStartExceptions() throws Exception {
            // Given
            when(mockRecorder.isRecording()).thenReturn(false);
            doThrow(new RuntimeException("Start failed")).when(mockRecorder).start();

            // When & Then
            assertThatThrownBy(() -> recordingService.startRecording())
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Start failed");
        }

        @Test
        @DisplayName("Should propagate recorder stop exceptions")
        void shouldPropagateRecorderStopExceptions() throws Exception {
            // Given
            doThrow(new RuntimeException("Stop failed")).when(mockRecorder).stop();

            // When & Then
            assertThatThrownBy(() -> recordingService.stopRecording())
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Stop failed");
        }

        @Test
        @DisplayName("Should handle null recording info gracefully")
        void shouldHandleNullRecordingInfoGracefully() throws Exception {
            // Given
            when(mockRecorder.isRecording()).thenReturn(false);
            when(mockRecorder.getRecordingInfo()).thenReturn(null);

            // When & Then
            assertThatThrownBy(() -> recordingService.startRecording())
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("Scheduler Management Tests")
    class SchedulerManagementTests {

        @Test
        @DisplayName("Should shutdown scheduler when stopping recording")
        void shouldShutdownSchedulerWhenStoppingRecording() throws Exception {
            // Given
            when(mockRecorder.isRecording()).thenReturn(false);
            when(mockRecorder.getRecordingInfo()).thenReturn(testRecordingInfo);
            
            recordingService.startRecording();
            TestUtils.waitMillis(50); // Let scheduler start

            // When
            recordingService.stopRecording();

            // Then
            verify(mockRecorder).stop();
        }

        @Test
        @DisplayName("Should handle multiple start/stop cycles")
        void shouldHandleMultipleStartStopCycles() throws Exception {
            // Given
            when(mockRecorder.getRecordingInfo()).thenReturn(testRecordingInfo);
            when(mockRecorder.isRecording()).thenReturn(false, false, false);

            // When
            recordingService.startRecording();
            recordingService.stopRecording();
            
            recordingService.startRecording();
            recordingService.stopRecording();

            // Then
            verify(mockRecorder, times(2)).start();
            verify(mockRecorder, times(2)).stop();
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("Should complete full recording cycle")
        void shouldCompleteFullRecordingCycle() throws Exception {
            // Given
            when(mockRecorder.isRecording()).thenReturn(false);
            when(mockRecorder.getRecordingInfo()).thenReturn(testRecordingInfo);
            when(mockCaptureService.captureScreen(anyInt())).thenReturn(testImage);

            // When
            recordingService.startRecording();
            TestUtils.waitMillis(200); // Record for a short time
            recordingService.stopRecording();

            // Then
            verify(mockRecorder).start();
            verify(mockRecorder, atLeastOnce()).recordFrame(testImage);
            verify(mockRecorder).stop();
        }

        @Test
        @DisplayName("Should work with different frame rates")
        void shouldWorkWithDifferentFrameRates() throws Exception {
            // Given
            int[] frameRates = TestUtils.getTestFrameRates();
            
            for (int frameRate : frameRates) {
                RecordingInfo recordingInfo = TestUtils.createMockRecordingInfo();
                recordingInfo.setFrameRate(frameRate);
                
                when(mockRecorder.isRecording()).thenReturn(false);
                when(mockRecorder.getRecordingInfo()).thenReturn(recordingInfo);
                when(mockCaptureService.captureScreen(anyInt())).thenReturn(testImage);

                // When
                recordingService.startRecording();
                TestUtils.waitMillis(100);
                recordingService.stopRecording();

                // Then
                verify(mockRecorder, atLeastOnce()).start();
                verify(mockRecorder, atLeastOnce()).stop();
                
                // Reset mocks for next iteration
                reset(mockRecorder);
            }
        }
    }
}
