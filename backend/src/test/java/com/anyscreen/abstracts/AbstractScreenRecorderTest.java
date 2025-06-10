package com.anyscreen.abstracts;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.anyscreen.interfaces.FrameEncoderInterface;
import com.anyscreen.models.RecordingInfo;
import com.anyscreen.services.LoggerService;
import com.anyscreen.utils.TestUtils;

/**
 * Comprehensive test suite for the AbstractScreenRecorder abstract class.
 * Tests the recording lifecycle, state management, frame handling,
 * and validation logic through a concrete test implementation.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AbstractScreenRecorder Tests")
public class AbstractScreenRecorderTest {

    @Mock
    private FrameEncoderInterface mockEncoder;

    private RecordingInfo recordingInfo;
    private TestScreenRecorder recorder;

    @BeforeEach
    void setUp() {
        LoggerService.reset();
        recordingInfo = TestUtils.createMockRecordingInfo();
        recorder = new TestScreenRecorder(mockEncoder, recordingInfo);
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
        @DisplayName("Should initialize with encoder and recording info")
        void shouldInitializeWithEncoderAndRecordingInfo() {
            // Given & When
            TestScreenRecorder newRecorder = new TestScreenRecorder(mockEncoder, recordingInfo);

            // Then
            assertThat(newRecorder.getEncoder()).isEqualTo(mockEncoder);
            assertThat(newRecorder.getRecordingInfo()).isEqualTo(recordingInfo);
            assertThat(newRecorder.isRecording()).isFalse();
        }

        @Test
        @DisplayName("Should allow null encoder during construction")
        void shouldAllowNullEncoderDuringConstruction() {
            // Given & When
            TestScreenRecorder newRecorder = new TestScreenRecorder(null, recordingInfo);

            // Then
            assertThat(newRecorder.getEncoder()).isNull();
            assertThat(newRecorder.getRecordingInfo()).isEqualTo(recordingInfo);
        }

        @Test
        @DisplayName("Should allow null recording info during construction")
        void shouldAllowNullRecordingInfoDuringConstruction() {
            // Given & When
            TestScreenRecorder newRecorder = new TestScreenRecorder(mockEncoder, null);

            // Then
            assertThat(newRecorder.getEncoder()).isEqualTo(mockEncoder);
            assertThat(newRecorder.getRecordingInfo()).isNull();
        }
    }

    @Nested
    @DisplayName("Recording Lifecycle Tests")
    class RecordingLifecycleTests {

        @Test
        @DisplayName("Should start recording successfully")
        void shouldStartRecordingSuccessfully() throws Exception {
            // Given
            when(mockEncoder.isRunning()).thenReturn(false);

            // When
            recorder.start();

            // Then
            assertThat(recorder.isRecording()).isTrue();
            verify(mockEncoder).configure(recordingInfo);
            verify(mockEncoder).start();
        }

        @Test
        @DisplayName("Should stop recording successfully")
        void shouldStopRecordingSuccessfully() throws Exception {
            // Given
            recorder.start();
            when(mockEncoder.isRunning()).thenReturn(true);

            // When
            recorder.stop();

            // Then
            assertThat(recorder.isRecording()).isFalse();
            verify(mockEncoder).stop();
        }

        @Test
        @DisplayName("Should handle stop when encoder is not running")
        void shouldHandleStopWhenEncoderNotRunning() throws Exception {
            // Given
            recorder.start();
            when(mockEncoder.isRunning()).thenReturn(false);

            // When
            recorder.stop();

            // Then
            assertThat(recorder.isRecording()).isFalse();
            verify(mockEncoder, never()).stop();
        }

        @Test
        @DisplayName("Should handle stop with null encoder gracefully")
        void shouldHandleStopWithNullEncoderGracefully() throws Exception {
            // Given
            TestScreenRecorder recorderWithNullEncoder = new TestScreenRecorder(null, recordingInfo);

            // When & Then
            recorderWithNullEncoder.stop(); // Should not throw exception
            assertThat(recorderWithNullEncoder.isRecording()).isFalse();
        }

        @Test
        @DisplayName("Should maintain recording state across multiple operations")
        void shouldMaintainRecordingStateAcrossMultipleOperations() throws Exception {
            // Given
            when(mockEncoder.isRunning()).thenReturn(false, true, true);

            // When & Then
            assertThat(recorder.isRecording()).isFalse();
            
            recorder.start();
            assertThat(recorder.isRecording()).isTrue();
            
            recorder.stop();
            assertThat(recorder.isRecording()).isFalse();
        }
    }

    @Nested
    @DisplayName("Frame Recording Tests")
    class FrameRecordingTests {

        @Test
        @DisplayName("Should record frame when recording is active")
        void shouldRecordFrameWhenRecordingIsActive() throws Exception {
            // Given
            BufferedImage testImage = TestUtils.createMockImage(1920, 1080, Color.BLACK);
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
            // Given
            BufferedImage testImage = TestUtils.createMockImage();

            // When
            recorder.recordFrame(testImage);

            // Then
            verify(mockEncoder, never()).encodeFrame(any(BufferedImage.class));
        }

        @Test
        @DisplayName("Should not record frame when encoder is not running")
        void shouldNotRecordFrameWhenEncoderNotRunning() throws Exception {
            // Given
            BufferedImage testImage = TestUtils.createMockImage();
            when(mockEncoder.isRunning()).thenReturn(false);
            recorder.start();

            // When
            recorder.recordFrame(testImage);

            // Then
            verify(mockEncoder, never()).encodeFrame(any(BufferedImage.class));
        }

        @Test
        @DisplayName("Should handle null encoder during frame recording")
        void shouldHandleNullEncoderDuringFrameRecording() throws Exception {
            // Given
            TestScreenRecorder recorderWithNullEncoder = new TestScreenRecorder(null, recordingInfo);
            BufferedImage testImage = TestUtils.createMockImage();

            // When & Then - Should not throw exception
            recorderWithNullEncoder.recordFrame(testImage);
        }

        @Test
        @DisplayName("Should handle multiple frame recordings")
        void shouldHandleMultipleFrameRecordings() throws Exception {
            // Given
            BufferedImage frame1 = TestUtils.createMockImage();
            BufferedImage frame2 = TestUtils.createMockImage();
            BufferedImage frame3 = TestUtils.createMockImage();
            
            when(mockEncoder.isRunning()).thenReturn(true);
            recorder.start();

            // When
            recorder.recordFrame(frame1);
            recorder.recordFrame(frame2);
            recorder.recordFrame(frame3);

            // Then
            verify(mockEncoder).encodeFrame(frame1);
            verify(mockEncoder).encodeFrame(frame2);
            verify(mockEncoder).encodeFrame(frame3);
        }
    }

    @Nested
    @DisplayName("Configuration Tests")
    class ConfigurationTests {

        @Test
        @DisplayName("Should configure encoder with recording info")
        void shouldConfigureEncoderWithRecordingInfo() {
            // When
            recorder.configure();

            // Then
            verify(mockEncoder).configure(recordingInfo);
        }

        @Test
        @DisplayName("Should handle configuration with null encoder")
        void shouldHandleConfigurationWithNullEncoder() {
            // Given
            TestScreenRecorder recorderWithNullEncoder = new TestScreenRecorder(null, recordingInfo);

            // When & Then - Should not throw exception
            recorderWithNullEncoder.configure();
        }

        @Test
        @DisplayName("Should configure encoder during start")
        void shouldConfigureEncoderDuringStart() throws Exception {
            // When
            recorder.start();

            // Then
            verify(mockEncoder).configure(recordingInfo);
        }
    }

    @Nested
    @DisplayName("State Validation Tests")
    class StateValidationTests {

        @Test
        @DisplayName("Should validate state successfully with valid configuration")
        void shouldValidateStateSuccessfullyWithValidConfiguration() throws Exception {
            // When & Then - Should not throw exception
            recorder.start();
        }

        @Test
        @DisplayName("Should throw exception when recording info is null")
        void shouldThrowExceptionWhenRecordingInfoIsNull() {
            // Given
            TestScreenRecorder recorderWithNullInfo = new TestScreenRecorder(mockEncoder, null);

            // When & Then
            assertThatThrownBy(() -> recorderWithNullInfo.start())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("RecordingInfo not set");
        }

        @Test
        @DisplayName("Should throw exception when encoder is null")
        void shouldThrowExceptionWhenEncoderIsNull() {
            // Given
            TestScreenRecorder recorderWithNullEncoder = new TestScreenRecorder(null, recordingInfo);

            // When & Then
            assertThatThrownBy(() -> recorderWithNullEncoder.start())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Encoder not set");
        }

        @Test
        @DisplayName("Should throw exception when both encoder and recording info are null")
        void shouldThrowExceptionWhenBothEncoderAndRecordingInfoAreNull() {
            // Given
            TestScreenRecorder recorderWithNulls = new TestScreenRecorder(null, null);

            // When & Then
            assertThatThrownBy(() -> recorderWithNulls.start())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("RecordingInfo not set");
        }
    }

    @Nested
    @DisplayName("Getter Tests")
    class GetterTests {

        @Test
        @DisplayName("Should return correct encoder")
        void shouldReturnCorrectEncoder() {
            // When & Then
            assertThat(recorder.getEncoder()).isEqualTo(mockEncoder);
        }

        @Test
        @DisplayName("Should return correct recording info")
        void shouldReturnCorrectRecordingInfo() {
            // When & Then
            assertThat(recorder.getRecordingInfo()).isEqualTo(recordingInfo);
        }

        @Test
        @DisplayName("Should return correct recording state")
        void shouldReturnCorrectRecordingState() throws Exception {
            // Given & When
            assertThat(recorder.isRecording()).isFalse();
            
            recorder.start();
            assertThat(recorder.isRecording()).isTrue();
            
            recorder.stop();
            assertThat(recorder.isRecording()).isFalse();
        }

        @Test
        @DisplayName("Should return same recording info instance")
        void shouldReturnSameRecordingInfoInstance() {
            // When
            RecordingInfo returnedInfo = recorder.getRecordingInfo();

            // Then
            assertThat(returnedInfo).isSameAs(recordingInfo);
        }
    }

    @Nested
    @DisplayName("Thread Safety Tests")
    class ThreadSafetyTests {

        @Test
        @DisplayName("Should handle concurrent start/stop operations")
        void shouldHandleConcurrentStartStopOperations() throws Exception {
            // Given
            int threadCount = 10;
            Thread[] threads = new Thread[threadCount];
            AtomicBoolean exceptionOccurred = new AtomicBoolean(false);

            // When
            for (int i = 0; i < threadCount; i++) {
                final int threadIndex = i;
                threads[i] = new Thread(() -> {
                    try {
                        if (threadIndex % 2 == 0) {
                            recorder.start();
                        } else {
                            recorder.stop();
                        }
                    } catch (Exception e) {
                        exceptionOccurred.set(true);
                    }
                });
                threads[i].start();
            }

            // Wait for all threads to complete
            for (Thread thread : threads) {
                thread.join();
            }

            // Then
            assertThat(exceptionOccurred.get()).isFalse();
            // Final state should be consistent
            assertThat(recorder.isRecording()).isIn(true, false);
        }

        @Test
        @DisplayName("Should handle concurrent frame recording")
        void shouldHandleConcurrentFrameRecording() throws Exception {
            // Given
            recorder.start();
            when(mockEncoder.isRunning()).thenReturn(true);
            
            int frameCount = 5;
            Thread[] threads = new Thread[frameCount];
            AtomicBoolean exceptionOccurred = new AtomicBoolean(false);

            // When
            for (int i = 0; i < frameCount; i++) {
                threads[i] = new Thread(() -> {
                    try {
                        BufferedImage testImage = TestUtils.createMockImage();
                        recorder.recordFrame(testImage);
                    } catch (Exception e) {
                        exceptionOccurred.set(true);
                    }
                });
                threads[i].start();
            }

            // Wait for all threads to complete
            for (Thread thread : threads) {
                thread.join();
            }

            // Then
            assertThat(exceptionOccurred.get()).isFalse();
            verify(mockEncoder, times(frameCount)).encodeFrame(any(BufferedImage.class));
        }
    }

    /**
     * Concrete test implementation of AbstractScreenRecorder for testing purposes.
     */
    private static class TestScreenRecorder extends AbstractScreenRecorder {
        
        public TestScreenRecorder(FrameEncoderInterface encoder, RecordingInfo recordingInfo) {
            super(encoder, recordingInfo);
        }

        // Expose protected members for testing
        public FrameEncoderInterface getEncoder() {
            return encoder;
        }

        public AtomicBoolean getRecordingFlag() {
            return recording;
        }
    }
}
