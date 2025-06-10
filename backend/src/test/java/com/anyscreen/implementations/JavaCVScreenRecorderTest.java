package com.anyscreen.implementations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

import java.awt.image.BufferedImage;

import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;

import com.anyscreen.interfaces.FrameEncoderInterface;
import com.anyscreen.models.RecordingInfo;
import com.anyscreen.services.LoggerService;
import com.anyscreen.utils.TestUtils;

/**
 * Comprehensive test suite for JavaCVScreenRecorder implementation.
 * Tests recording functionality, configuration, lifecycle management, and error handling.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JavaCVScreenRecorder Tests")
public class JavaCVScreenRecorderTest {

    @Mock
    private FrameEncoderInterface mockEncoder;
    
    private JavaCVScreenRecorder recorder;
    private RecordingInfo testRecordingInfo;
    private BufferedImage testImage;

    @BeforeEach
    void setUp() {
        testRecordingInfo = TestUtils.createMockRecordingInfo();
        testImage = TestUtils.createMockImage();
        LoggerService.initialize(mock(Log4jAdapter.class));
    }

    @AfterEach
    void tearDown() throws Exception {
        if (recorder != null && recorder.isRecording()) {
            recorder.stop();
        }
        TestUtils.cleanupTestFiles();
        LoggerService.reset();
    }

    @Nested
    @DisplayName("Constructor and Initialization Tests")
    class ConstructorAndInitializationTests {

        @Test
        @DisplayName("Should initialize recorder with valid parameters")
        void shouldInitializeRecorderWithValidParameters() {
            try (MockedConstruction<FFmpegFrameRecorder> recorderConstruction = 
                 mockConstruction(FFmpegFrameRecorder.class)) {
                
                // When
                recorder = new JavaCVScreenRecorder(testRecordingInfo, mockEncoder);
                
                // Then
                assertThat(recorder).isNotNull();
                assertThat(recorder.getRecordingInfo()).isEqualTo(testRecordingInfo);
                assertThat(recorder.getEncoder()).isEqualTo(mockEncoder);
                assertThat(recorder.isRecording()).isFalse();
                
                // Verify FFmpegFrameRecorder was constructed
                assertThat(recorderConstruction.constructed()).hasSize(1);
                FFmpegFrameRecorder constructedRecorder = recorderConstruction.constructed().get(0);
                
                verify(constructedRecorder).setFrameRate(testRecordingInfo.getFrameRate());
                verify(constructedRecorder).setFormat(testRecordingInfo.getFormat());
                verify(constructedRecorder).setVideoCodec(testRecordingInfo.getVideoCodec());
                verify(constructedRecorder).setVideoBitrate(testRecordingInfo.getBitRate());
            }
        }

        @Test
        @DisplayName("Should handle null encoder")
        void shouldHandleNullEncoder() {
            try (MockedConstruction<FFmpegFrameRecorder> recorderConstruction = 
                 mockConstruction(FFmpegFrameRecorder.class)) {
                
                // When
                recorder = new JavaCVScreenRecorder(testRecordingInfo, null);
                
                // Then
                assertThat(recorder).isNotNull();
                assertThat(recorder.getRecordingInfo()).isEqualTo(testRecordingInfo);
                assertThat(recorder.getEncoder()).isNull();
                
                // Verify FFmpegFrameRecorder was still constructed
                assertThat(recorderConstruction.constructed()).hasSize(1);
            }
        }

        @Test
        @DisplayName("Should handle null recording info")
        void shouldHandleNullRecordingInfo() {
            try (MockedConstruction<FFmpegFrameRecorder> recorderConstruction = 
                 mockConstruction(FFmpegFrameRecorder.class)) {
                
                // When
                recorder = new JavaCVScreenRecorder(null, mockEncoder);
                
                // Then
                assertThat(recorder).isNotNull();
                assertThat(recorder.getRecordingInfo()).isNull();
                assertThat(recorder.getEncoder()).isEqualTo(mockEncoder);
            }
        }

        @Test
        @DisplayName("Should configure recorder with different recording parameters")
        void shouldConfigureRecorderWithDifferentRecordingParameters() {
            try (MockedConstruction<FFmpegFrameRecorder> recorderConstruction = 
                 mockConstruction(FFmpegFrameRecorder.class)) {
                
                // Given
                RecordingInfo customInfo = TestUtils.createMockRecordingInfo();
                
                // When
                recorder = new JavaCVScreenRecorder(customInfo, mockEncoder);
                
                // Then
                FFmpegFrameRecorder constructedRecorder = recorderConstruction.constructed().get(0);
                verify(constructedRecorder).setFrameRate(60);
                verify(constructedRecorder).setFormat(customInfo.getFormat());
                verify(constructedRecorder).setVideoCodec(customInfo.getVideoCodec());
                verify(constructedRecorder).setVideoBitrate(customInfo.getBitRate());
            }
        }

        @ParameterizedTest
        @ValueSource(ints = {30, 60, 120})
        @DisplayName("Should handle different frame rates")
        void shouldHandleDifferentFrameRates(int frameRate) {
            try (MockedConstruction<FFmpegFrameRecorder> recorderConstruction = 
                 mockConstruction(FFmpegFrameRecorder.class)) {
                
                // Given
                RecordingInfo customInfo = TestUtils.createMockRecordingInfo(1920, 1080, frameRate);
                
                // When
                recorder = new JavaCVScreenRecorder(customInfo, mockEncoder);
                
                // Then
                FFmpegFrameRecorder constructedRecorder = recorderConstruction.constructed().get(0);
                verify(constructedRecorder).setFrameRate(frameRate);
            }
        }
    }

    @Nested
    @DisplayName("Lifecycle Management Tests")
    class LifecycleManagementTests {

        @BeforeEach
        void setUpRecorder() {
            try (MockedConstruction<FFmpegFrameRecorder> ignored = 
                 mockConstruction(FFmpegFrameRecorder.class)) {
                recorder = new JavaCVScreenRecorder(testRecordingInfo, mockEncoder);
            }
        }

        @Test
        @DisplayName("Should start recording successfully")
        void shouldStartRecordingSuccessfully() throws Exception {
            // When
            recorder.start();
            
            // Then
            assertThat(recorder.isRecording()).isTrue();
            verify(mockEncoder).configure(testRecordingInfo);
            verify(mockEncoder).start();
        }

        @Test
        @DisplayName("Should stop recording successfully")
        void shouldStopRecordingSuccessfully() throws Exception {
            // Given
            when(mockEncoder.isRunning()).thenReturn(true);
            recorder.start();
            
            // When
            recorder.stop();
            
            // Then
            assertThat(recorder.isRecording()).isFalse();
            verify(mockEncoder).stop();
        }

        @Test
        @DisplayName("Should handle start with null encoder")
        void shouldHandleStartWithNullEncoder() {
            // Given
            recorder = new JavaCVScreenRecorder(testRecordingInfo, null);
            
            // When & Then
            assertThatThrownBy(() -> recorder.start())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Encoder not set");
        }

        @Test
        @DisplayName("Should handle start with null recording info")
        void shouldHandleStartWithNullRecordingInfo() {
            // Given
            recorder = new JavaCVScreenRecorder(null, mockEncoder);
            
            // When & Then
            assertThatThrownBy(() -> recorder.start())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("RecordingInfo not set");
        }

        @Test
        @DisplayName("Should handle encoder start failure")
        void shouldHandleEncoderStartFailure() throws Exception {
            // Given
            doThrow(new RuntimeException("Encoder start failed")).when(mockEncoder).start();
            
            // When & Then
            assertThatThrownBy(() -> recorder.start())
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Encoder start failed");
        }

        @Test
        @DisplayName("Should handle stop when not recording")
        void shouldHandleStopWhenNotRecording() throws Exception {
            // When - Stop without starting
            recorder.stop();
            
            // Then
            assertThat(recorder.isRecording()).isFalse();
            verify(mockEncoder, never()).stop();
        }

        @Test
        @DisplayName("Should handle stop with encoder not running")
        void shouldHandleStopWithEncoderNotRunning() throws Exception {
            // Given
            when(mockEncoder.isRunning()).thenReturn(false);
            recorder.start();
            
            // When
            recorder.stop();
            
            // Then
            assertThat(recorder.isRecording()).isFalse();
            verify(mockEncoder, never()).stop();
        }

        @Test
        @DisplayName("Should handle encoder stop failure")
        void shouldHandleEncoderStopFailure() throws Exception {
            // Given
            when(mockEncoder.isRunning()).thenReturn(true);
            doThrow(new RuntimeException("Encoder stop failed")).when(mockEncoder).stop();
            recorder.start();
            
            // When & Then
            assertThatThrownBy(() -> recorder.stop())
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Encoder stop failed");
        }
    }

    @Nested
    @DisplayName("Frame Recording Tests")
    class FrameRecordingTests {

        @BeforeEach
        void setUpRecorder() throws Exception {
            try (MockedConstruction<FFmpegFrameRecorder> ignored = 
                 mockConstruction(FFmpegFrameRecorder.class)) {
                recorder = new JavaCVScreenRecorder(testRecordingInfo, mockEncoder);
            }
        }

        @Test
        @DisplayName("Should record frame when recording is active")
        void shouldRecordFrameWhenRecordingIsActive() throws Exception {
            // Given
            when(mockEncoder.isRunning()).thenReturn(true);
            recorder.start();
            
            // When
            recorder.recordFrame(testImage);
            
            // Then
            verify(mockEncoder).encodeFrame(testImage);
        }

        @Test
        @DisplayName("Should not record frame when recording is inactive")
        void shouldNotRecordFrameWhenRecordingIsInactive() throws Exception {
            // When
            recorder.recordFrame(testImage);
            
            // Then
            verify(mockEncoder, never()).encodeFrame(any(BufferedImage.class));
        }

        @Test
        @DisplayName("Should not record frame when encoder is not running")
        void shouldNotRecordFrameWhenEncoderNotRunning() throws Exception {
            // Given
            when(mockEncoder.isRunning()).thenReturn(false);
            recorder.start();
            
            // When
            recorder.recordFrame(testImage);
            
            // Then
            verify(mockEncoder, never()).encodeFrame(any(BufferedImage.class));
        }

        @Test
        @DisplayName("Should handle null frame")
        void shouldHandleNullFrame() throws Exception {
            // Given
            when(mockEncoder.isRunning()).thenReturn(true);
            recorder.start();
            
            // When
            recorder.recordFrame(null);
            
            // Then
            verify(mockEncoder).encodeFrame(null);
        }

        @Test
        @DisplayName("Should handle multiple frame recordings")
        void shouldHandleMultipleFrameRecordings() throws Exception {
            // Given
            when(mockEncoder.isRunning()).thenReturn(true);
            recorder.start();
            
            BufferedImage frame1 = TestUtils.createMockImage(100, 100);
            BufferedImage frame2 = TestUtils.createMockImage(200, 200);
            BufferedImage frame3 = TestUtils.createMockImage(300, 300);
            
            // When
            recorder.recordFrame(frame1);
            recorder.recordFrame(frame2);
            recorder.recordFrame(frame3);
            
            // Then
            verify(mockEncoder).encodeFrame(frame1);
            verify(mockEncoder).encodeFrame(frame2);
            verify(mockEncoder).encodeFrame(frame3);
        }

        @Test
        @DisplayName("Should handle frame encoding failure")
        void shouldHandleFrameEncodingFailure() throws Exception {
            // Given
            when(mockEncoder.isRunning()).thenReturn(true);
            doThrow(new RuntimeException("Encoding failed")).when(mockEncoder).encodeFrame(any());
            recorder.start();
            
            // When & Then
            assertThatThrownBy(() -> recorder.recordFrame(testImage))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Encoding failed");
        }

        @Test
        @DisplayName("Should handle null encoder during frame recording")
        void shouldHandleNullEncoderDuringFrameRecording() throws Exception {
            // Given
            recorder = new JavaCVScreenRecorder(testRecordingInfo, null);
            recorder.getRecording().set(true); // Manually set recording state for testing
            
            // When & Then - Should not throw exception
            recorder.recordFrame(testImage);
            assertEquals(false, recorder.isRecording());
        }
    }

    @Nested
    @DisplayName("Configuration Tests")
    class ConfigurationTests {

        @BeforeEach
        void setUpRecorder() {
            try (MockedConstruction<FFmpegFrameRecorder> ignored = 
                 mockConstruction(FFmpegFrameRecorder.class)) {
                recorder = new JavaCVScreenRecorder(testRecordingInfo, mockEncoder);
            }
        }

        @Test
        @DisplayName("Should configure encoder properly")
        void shouldConfigureEncoderProperly() {
            // When
            recorder.configure();
            
            // Then
            verify(mockEncoder).configure(testRecordingInfo);
        }

        @Test
        @DisplayName("Should handle configuration with null encoder")
        void shouldHandleConfigurationWithNullEncoder() {
            // Given
            recorder = new JavaCVScreenRecorder(testRecordingInfo, null);
            
            // When & Then - Should not throw exception
            recorder.configure();
        }

        @Test
        @DisplayName("Should reconfigure with new recording info")
        void shouldReconfigureWithNewRecordingInfo() {
            // Given
            RecordingInfo newInfo = TestUtils.createMockRecordingInfo();
            
            // When
            recorder.configure();
            // Simulate changing recording info
            recorder.setRecordingInfo(newInfo);
            recorder.configure();
            
            // Then
            verify(mockEncoder).configure(testRecordingInfo);
            verify(mockEncoder).configure(newInfo);
        }

        @Test
        @DisplayName("Should handle configuration errors")
        void shouldHandleConfigurationErrors() {
            // Given
            doThrow(new IllegalArgumentException("Invalid config")).when(mockEncoder).configure(any());
            
            // When & Then
            assertThatThrownBy(() -> recorder.configure())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid config");
        }
    }

    @Nested
    @DisplayName("State Management Tests")
    class StateManagementTests {

        @BeforeEach
        void setUpRecorder() {
            try (MockedConstruction<FFmpegFrameRecorder> ignored = 
                 mockConstruction(FFmpegFrameRecorder.class)) {
                recorder = new JavaCVScreenRecorder(testRecordingInfo, mockEncoder);
            }
        }

        @Test
        @DisplayName("Should track recording state correctly")
        void shouldTrackRecordingStateCorrectly() throws Exception {
            // Initially not recording
            assertThat(recorder.isRecording()).isFalse();
            
            // After start
            recorder.start();
            assertThat(recorder.isRecording()).isTrue();
            
            // After stop
            recorder.stop();
            assertThat(recorder.isRecording()).isFalse();
        }

        @Test
        @DisplayName("Should return correct recording info")
        void shouldReturnCorrectRecordingInfo() {
            // When
            RecordingInfo result = recorder.getRecordingInfo();
            
            // Then
            assertThat(result).isEqualTo(testRecordingInfo);
        }

        @Test
        @DisplayName("Should return correct encoder")
        void shouldReturnCorrectEncoder() {
            // When
            FrameEncoderInterface result = recorder.getEncoder();
            
            // Then
            assertThat(result).isEqualTo(mockEncoder);
        }

        @Test
        @DisplayName("Should handle state queries during transitions")
        void shouldHandleStateQueriesDuringTransitions() throws Exception {
            // Given
            recorder.start();
            
            // When - Query state while recording
            boolean duringRecording = recorder.isRecording();
            recorder.stop();
            boolean afterStop = recorder.isRecording();
            
            // Then
            assertThat(duringRecording).isTrue();
            assertThat(afterStop).isFalse();
        }
    }

    @Nested
    @DisplayName("JavaCV Integration Tests")
    class JavaCVIntegrationTests {

        @Test
        @DisplayName("Should properly integrate with FFmpegFrameRecorder")
        void shouldProperlyIntegrateWithFFmpegFrameRecorder() {
            try (MockedConstruction<FFmpegFrameRecorder> recorderConstruction = 
                 mockConstruction(FFmpegFrameRecorder.class)) {
                
                // When
                recorder = new JavaCVScreenRecorder(testRecordingInfo, mockEncoder);
                
                // Then
                assertThat(recorderConstruction.constructed()).hasSize(1);
                FFmpegFrameRecorder ffmpegRecorder = recorderConstruction.constructed().get(0);
                
                // Verify all configuration was applied
                verify(ffmpegRecorder).setFrameRate(testRecordingInfo.getFrameRate());
                verify(ffmpegRecorder).setFormat(testRecordingInfo.getFormat());
                verify(ffmpegRecorder).setVideoCodec(testRecordingInfo.getVideoCodec());
                verify(ffmpegRecorder).setVideoBitrate(testRecordingInfo.getBitRate());
                
                // Verify recorder is accessible
                assertThat(recorder.recorder).isEqualTo(ffmpegRecorder);
            }
        }

        @Test
        @DisplayName("Should handle FFmpegFrameRecorder construction failure")
        void shouldHandleFFmpegFrameRecorderConstructionFailure() {
            try (MockedConstruction<FFmpegFrameRecorder> recorderConstruction = 
                 mockConstruction(FFmpegFrameRecorder.class, (mock, context) -> {
                     throw new RuntimeException("FFmpeg initialization failed");
                 })) {
                
                // When & Then
                assertThatThrownBy(() -> new JavaCVScreenRecorder(testRecordingInfo, mockEncoder))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("FFmpeg initialization failed");
            }
        }

        @Test
        @DisplayName("Should properly configure FFmpeg with various formats")
        void shouldProperlyConfigureFFmpegWithVariousFormats() {
            String[] formats = {"mp4", "avi", "mkv"};
            
            for (String format : formats) {
                try (MockedConstruction<FFmpegFrameRecorder> recorderConstruction = 
                     mockConstruction(FFmpegFrameRecorder.class)) {
                    
                    // Given
                    RecordingInfo formatInfo = new RecordingInfo.Builder()
                        .format(format)
                        .resolution(1920, 1080)
                        .frameRate(30)
                        .build();
                    
                    // When
                    recorder = new JavaCVScreenRecorder(formatInfo, mockEncoder);
                    
                    // Then
                    FFmpegFrameRecorder ffmpegRecorder = recorderConstruction.constructed().get(0);
                    verify(ffmpegRecorder).setFormat(format);
                }
            }
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @BeforeEach
        void setUpRecorder() {
            try (MockedConstruction<FFmpegFrameRecorder> ignored = 
                 mockConstruction(FFmpegFrameRecorder.class)) {
                recorder = new JavaCVScreenRecorder(testRecordingInfo, mockEncoder);
            }
        }

        @Test
        @DisplayName("Should handle invalid recording configuration")
        void shouldHandleInvalidRecordingConfiguration() {
            // Given
            RecordingInfo invalidInfo = new RecordingInfo.Builder()
                .resolution(-1, -1)
                .frameRate(-30)
                .build();
            
            // When & Then - Should still create recorder but configuration might fail later
            var recorder = new JavaCVScreenRecorder(invalidInfo, mockEncoder);
            //TODO FIX this test case later and improve validation
        }

        @Test
        @DisplayName("Should propagate encoder exceptions")
        void shouldPropagateEncoderExceptions() throws Exception {
            // Given
            doThrow(new RuntimeException("Encoder failure")).when(mockEncoder).start();
            
            // When & Then
            assertThatThrownBy(() -> recorder.start())
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Encoder failure");
        }

        @Test
        @DisplayName("Should handle partial failures gracefully")
        void shouldHandlePartialFailuresGracefully() throws Exception {
            // Given
            when(mockEncoder.isRunning()).thenReturn(true);
            doThrow(new RuntimeException("Frame 1 failed"))
                .doNothing()
                .when(mockEncoder).encodeFrame(any());
            
            recorder.start();
            
            BufferedImage frame1 = TestUtils.createMockImage(100, 100);
            BufferedImage frame2 = TestUtils.createMockImage(100, 100);
            
            // When & Then
            assertThatThrownBy(() -> recorder.recordFrame(frame1))
                .isInstanceOf(RuntimeException.class);
            
            // Should be able to continue with next frame
            recorder.recordFrame(frame2);
            verify(mockEncoder, times(2)).encodeFrame(any());
        }
    }

    @Nested
    @DisplayName("Thread Safety Tests")
    class ThreadSafetyTests {

        @BeforeEach
        void setUpRecorder() {
            try (MockedConstruction<FFmpegFrameRecorder> ignored = 
                 mockConstruction(FFmpegFrameRecorder.class)) {
                recorder = new JavaCVScreenRecorder(testRecordingInfo, mockEncoder);
            }
        }

        @Test
        @DisplayName("Should handle concurrent state changes")
        void shouldHandleConcurrentStateChanges() throws Exception {
            // Given
            when(mockEncoder.isRunning()).thenReturn(true);
            
            // When - Concurrent start/stop operations
            Thread startThread = new Thread(() -> {
                try {
                    recorder.start();
                } catch (Exception e) {
                    // Handle in thread
                }
            });
            
            Thread stopThread = new Thread(() -> {
                try {
                    Thread.sleep(10); // Small delay
                    recorder.stop();
                } catch (Exception e) {
                    // Handle in thread
                }
            });
            
            startThread.start();
            stopThread.start();
            
            startThread.join();
            stopThread.join();
            
            // Then - Should end up in stopped state
            assertThat(recorder.isRecording()).isFalse();
        }

        @Test
        @DisplayName("Should handle concurrent frame recording")
        void shouldHandleConcurrentFrameRecording() throws Exception {
            // Given
            when(mockEncoder.isRunning()).thenReturn(true);
            recorder.start();
            
            final int threadCount = 5;
            final int framesPerThread = 10;
            Thread[] threads = new Thread[threadCount];
            
            // When
            for (int i = 0; i < threadCount; i++) {
                final int threadId = i;
                threads[i] = new Thread(() -> {
                    for (int j = 0; j < framesPerThread; j++) {
                        try {
                            BufferedImage frame = TestUtils.createMockImage(100 + threadId, 100 + threadId);
                            recorder.recordFrame(frame);
                        } catch (Exception e) {
                            // Handle in thread
                        }
                    }
                });
                threads[i].start();
            }
            
            for (Thread thread : threads) {
                thread.join();
            }
            
            // Then
            verify(mockEncoder, times(threadCount * framesPerThread)).encodeFrame(any());
        }
    }
}
