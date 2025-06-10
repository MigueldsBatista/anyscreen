package com.anyscreen.implementations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.awt.image.BufferedImage;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.anyscreen.models.RecordingInfo;
import com.anyscreen.services.LoggerService;
import com.anyscreen.utils.TestUtils;

/**
 * Comprehensive test suite for JavaCVFrameEncoderAdapter wrapper class.
 * Focuses on testing our wrapper logic, state management, and error handling
 * without mocking external JavaCV library classes.
 * 
 * This test class validates:
 * - State management (isRunning, isConfigured, getConfig)
 * - Configuration validation and persistence
 * - Error handling for invalid states and inputs
 * - Lifecycle management across multiple start/stop cycles
 * 
 * Testing Philosophy:
 * We test our wrapper's behavior and state management, not the JavaCV library.
 * External library functionality is assumed to work correctly.
 */
@DisplayName("JavaCVFrameEncoderAdapter Tests")
class JavaCVFrameEncoderAdapterTest {

    private JavaCVFrameEncoderAdapter encoder;
    private RecordingInfo recordingInfo;

    @BeforeEach
    void setUp() {
        LoggerService.reset();
        recordingInfo = TestUtils.createMockRecordingInfo();
        encoder = new JavaCVFrameEncoderAdapter();
    }

    @AfterEach
    void tearDown() {
        // Safely stop encoder if it's running
        try {
            if (encoder != null && encoder.isRunning()) {
                encoder.stop();
            }
        } catch (Exception e) {
            // Ignore cleanup errors
        }
        TestUtils.cleanupTestFiles();
        LoggerService.reset();
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create encoder with default state")
        void shouldCreateEncoderWithDefaultState() {
            // When
            JavaCVFrameEncoderAdapter newEncoder = new JavaCVFrameEncoderAdapter();

            // Then
            assertThat(newEncoder.isRunning()).isFalse();
            assertThat(newEncoder.getConfig()).isNull();
            assertThat(newEncoder.isConfigured()).isFalse();
        }

        @Test
        @DisplayName("Should create multiple independent instances")
        void shouldCreateMultipleIndependentInstances() {
            // When
            JavaCVFrameEncoderAdapter encoder1 = new JavaCVFrameEncoderAdapter();
            JavaCVFrameEncoderAdapter encoder2 = new JavaCVFrameEncoderAdapter();

            // Then
            assertThat(encoder1).isNotSameAs(encoder2);
            assertThat(encoder1.isRunning()).isFalse();
            assertThat(encoder2.isRunning()).isFalse();
            assertThat(encoder1.isConfigured()).isFalse();
            assertThat(encoder2.isConfigured()).isFalse();
        }
    }

    @Nested
    @DisplayName("Configuration Tests")
    class ConfigurationTests {

        @Test
        @DisplayName("Should configure encoder with recording info")
        void shouldConfigureEncoderWithRecordingInfo() {
            // When
            encoder.configure(recordingInfo);

            // Then
            assertThat(encoder.getConfig()).isEqualTo(recordingInfo);
            assertThat(encoder.isConfigured()).isTrue();
            assertThat(encoder.isRunning()).isFalse();
        }

        @Test
        @DisplayName("Should handle null recording info during configuration")
        void shouldHandleNullRecordingInfoDuringConfiguration() {
            // When & Then
            assertThatThrownBy(() -> encoder.configure(null))
                .isInstanceOf(NullPointerException.class);
            
            // State should remain unchanged
            assertThat(encoder.isConfigured()).isFalse();
            assertThat(encoder.getConfig()).isNull();
        }

        @Test
        @DisplayName("Should allow reconfiguration")
        void shouldAllowReconfiguration() {
            // Given
            RecordingInfo newRecordingInfo = new RecordingInfo.Builder()
                .outputFile("new_recording.mp4")
                .resolution(2560, 1440)
                .frameRate(60)
                .build();

            // When
            encoder.configure(recordingInfo);
            encoder.configure(newRecordingInfo);

            // Then
            assertThat(encoder.getConfig()).isEqualTo(newRecordingInfo);
            assertThat(encoder.isConfigured()).isTrue();
        }

        @Test
        @DisplayName("Should configure with different video formats")
        void shouldConfigureWithDifferentVideoFormats() {
            // Given
            RecordingInfo aviRecording = new RecordingInfo.Builder()
                .outputFile("test.avi")
                .format("avi")
                .build();

            // When
            encoder.configure(aviRecording);

            // Then
            assertThat(encoder.getConfig().getFormat()).isEqualTo("avi");
            assertThat(encoder.isConfigured()).isTrue();
        }

        @Test
        @DisplayName("Should configure with different resolutions")
        void shouldConfigureWithDifferentResolutions() {
            // Given
            RecordingInfo hdRecording = new RecordingInfo.Builder()
                .resolution(1920, 1080)
                .build();

            // When
            encoder.configure(hdRecording);

            // Then
            assertThat(encoder.getConfig().getWidth()).isEqualTo(1920);
            assertThat(encoder.getConfig().getHeight()).isEqualTo(1080);
            assertThat(encoder.isConfigured()).isTrue();
        }

        @ParameterizedTest
        @ValueSource(ints = {24, 30, 60, 120})
        @DisplayName("Should configure with different frame rates")
        void shouldConfigureWithDifferentFrameRates(Integer frameRate) {
            // Given
            RecordingInfo customFrameRateRecording = new RecordingInfo.Builder()
                .frameRate(frameRate)
                .build();

            // When
            encoder.configure(customFrameRateRecording);

            // Then
            assertThat(encoder.getConfig().getFrameRate()).isEqualTo(frameRate);
            assertThat(encoder.isConfigured()).isTrue();
        }
    }

    @Nested
    @DisplayName("Lifecycle Management Tests")
    class LifecycleManagementTests {

        @Test
        @DisplayName("Should start encoder successfully after configuration")
        void shouldStartEncoderSuccessfullyAfterConfiguration() throws Exception {
            // Given
            encoder.configure(recordingInfo);

            // When
            encoder.start();

            // Then
            assertThat(encoder.isRunning()).isTrue();
            assertThat(encoder.isConfigured()).isTrue();
        }

        @Test
        @DisplayName("Should throw exception when starting without configuration")
        void shouldThrowExceptionWhenStartingWithoutConfiguration() {
            // When & Then
            assertThatThrownBy(() -> encoder.start())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Encoder not configured. Call configure() first.");
            
            assertThat(encoder.isRunning()).isFalse();
        }

        @Test
        @DisplayName("Should stop encoder successfully")
        void shouldStopEncoderSuccessfully() throws Exception {
            // Given
            encoder.configure(recordingInfo);
            encoder.start();

            // When
            encoder.stop();

            // Then
            assertThat(encoder.isRunning()).isFalse();
            assertThat(encoder.isConfigured()).isTrue(); // Config should persist
        }

        @Test
        @DisplayName("Should handle stop when not running")
        void shouldHandleStopWhenNotRunning() throws Exception {
            // Given
            encoder.configure(recordingInfo);

            // When & Then - Should not throw exception
            assertThatCode(() -> encoder.stop()).doesNotThrowAnyException();
            assertThat(encoder.isRunning()).isFalse();
        }

        @Test
        @DisplayName("Should handle multiple start/stop cycles")
        void shouldHandleMultipleStartStopCycles() throws Exception {
            // Given - Configure once
            
            // When & Then - Multiple cycles with same configuration
            for (int i = 0; i < 3; i++) {
                encoder.configure(recordingInfo);
                encoder.start();
                assertThat(encoder.isConfigured()).isTrue();
                assertThat(encoder.isRunning()).isTrue();
                
                encoder.stop();
                assertThat(encoder.isRunning()).isFalse();
                assertThat(encoder.isConfigured()).isTrue(); // Should remain configured
            }
        }

        @Test
        @DisplayName("Should handle stop without configuration")
        void shouldHandleStopWithoutConfiguration() throws Exception {
            // When & Then - Should not throw exception
            assertThatCode(() -> encoder.stop()).doesNotThrowAnyException();
            assertThat(encoder.isRunning()).isFalse();
        }
    }

    @Nested
    @DisplayName("Frame Encoding Tests")
    class FrameEncodingTests {

        @Test
        @DisplayName("Should encode frame successfully when running")
        void shouldEncodeFrameSuccessfullyWhenRunning() throws Exception {
            // Given
            encoder.configure(recordingInfo);
            encoder.start();
            BufferedImage testImage = TestUtils.createMockImage();

            // When & Then - Should not throw exception
            assertThatCode(() -> encoder.encodeFrame(testImage)).doesNotThrowAnyException();
            assertThat(encoder.isRunning()).isTrue();
        }

        @Test
        @DisplayName("Should throw exception when encoding frame without running")
        void shouldThrowExceptionWhenEncodingFrameWithoutRunning() throws Exception {
            // Given
            encoder.configure(recordingInfo);
            BufferedImage testImage = TestUtils.createMockImage();

            // When & Then
            assertThatThrownBy(() -> encoder.encodeFrame(testImage))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Encoder not running. Call start() first.");
        }

        @Test
        @DisplayName("Should throw exception when encoding null frame")
        void shouldThrowExceptionWhenEncodingNullFrame() throws Exception {
            // Given
            encoder.configure(recordingInfo);
            encoder.start();

            // When & Then
            assertThatThrownBy(() -> encoder.encodeFrame(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Frame cannot be null");
        }

        @Test
        @DisplayName("Should handle multiple frame encodings")
        void shouldHandleMultipleFrameEncodings() throws Exception {
            // Given
            encoder.configure(recordingInfo);
            encoder.start();
            BufferedImage testImage = TestUtils.createMockImage();

            // When & Then - Encode multiple frames
            for (int i = 0; i < 5; i++) {
                assertThatCode(() -> encoder.encodeFrame(testImage)).doesNotThrowAnyException();
            }
            assertThat(encoder.isRunning()).isTrue();
        }

        @Test
        @DisplayName("Should throw exception when encoding without configuration")
        void shouldThrowExceptionWhenEncodingWithoutConfiguration() {
            // Given
            BufferedImage testImage = TestUtils.createMockImage();

            // When & Then
            assertThatThrownBy(() -> encoder.encodeFrame(testImage))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Encoder not running. Call start() first.");
        }
    }

    @Nested
    @DisplayName("State Management Tests")
    class StateManagementTests {

        @Test
        @DisplayName("Should maintain consistent state during configuration")
        void shouldMaintainConsistentStateDuringConfiguration() {
            // Initially
            assertThat(encoder.isRunning()).isFalse();
            assertThat(encoder.isConfigured()).isFalse();
            assertThat(encoder.getConfig()).isNull();

            // After configuration
            encoder.configure(recordingInfo);
            assertThat(encoder.isRunning()).isFalse();
            assertThat(encoder.isConfigured()).isTrue();
            assertThat(encoder.getConfig()).isEqualTo(recordingInfo);
        }

        @Test
        @DisplayName("Should maintain consistent state during start/stop")
        void shouldMaintainConsistentStateDuringStartStop() throws Exception {
            // Configure first
            encoder.configure(recordingInfo);
            assertThat(encoder.isRunning()).isFalse();
            assertThat(encoder.isConfigured()).isTrue();

            // After start
            encoder.start();
            assertThat(encoder.isRunning()).isTrue();
            assertThat(encoder.isConfigured()).isTrue();

            // After stop
            encoder.stop();
            assertThat(encoder.isRunning()).isFalse();
            assertThat(encoder.isConfigured()).isTrue(); // Config persists
        }

        @Test
        @DisplayName("Should handle state transitions correctly")
        void shouldHandleStateTransitionsCorrectly() throws Exception {
            // State 1: Initial
            assertThat(encoder.isRunning()).isFalse();
            assertThat(encoder.isConfigured()).isFalse();

            // State 2: Configured
            encoder.configure(recordingInfo);
            assertThat(encoder.isRunning()).isFalse();
            assertThat(encoder.isConfigured()).isTrue();

            // State 3: Running
            encoder.start();
            assertThat(encoder.isRunning()).isTrue();
            assertThat(encoder.isConfigured()).isTrue();

            // State 4: Stopped (but still configured)
            encoder.stop();
            assertThat(encoder.isRunning()).isFalse();
            assertThat(encoder.isConfigured()).isTrue();

            // State 5: Reconfigured
            RecordingInfo newConfig = new RecordingInfo.Builder()
                .outputFile("new.mp4")
                .build();
            encoder.configure(newConfig);
            assertThat(encoder.isRunning()).isFalse();
            assertThat(encoder.isConfigured()).isTrue();
            assertThat(encoder.getConfig()).isEqualTo(newConfig);
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should handle configuration errors gracefully")
        void shouldHandleConfigurationErrorsGracefully() {
            // Test null configuration
            assertThatThrownBy(() -> encoder.configure(null))
                .isInstanceOf(NullPointerException.class);

            // State should remain unchanged
            assertThat(encoder.isConfigured()).isFalse();
            assertThat(encoder.getConfig()).isNull();
            assertThat(encoder.isRunning()).isFalse();
        }

        @Test
        @DisplayName("Should handle startup errors gracefully")
        void shouldHandleStartupErrorsGracefully() {
            // Test start without configuration
            assertThatThrownBy(() -> encoder.start())
                .isInstanceOf(IllegalStateException.class);

            // State should remain unchanged
            assertThat(encoder.isRunning()).isFalse();
            assertThat(encoder.isConfigured()).isFalse();
        }

        @Test
        @DisplayName("Should handle encoding errors gracefully")
        void shouldHandleEncodingErrorsGracefully() throws Exception {
            // Test encode without start
            BufferedImage testImage = TestUtils.createMockImage();
            encoder.configure(recordingInfo);

            assertThatThrownBy(() -> encoder.encodeFrame(testImage))
                .isInstanceOf(IllegalStateException.class);

            // Test encode null frame
            encoder.start();
            assertThatThrownBy(() -> encoder.encodeFrame(null))
                .isInstanceOf(IllegalArgumentException.class);

            // Encoder should still be running after error
            assertThat(encoder.isRunning()).isTrue();
        }
    }

    @Nested
    @DisplayName("Multi-Cycle Operation Tests")
    class MultiCycleOperationTests {

        @Test
        @DisplayName("Should handle complete recording cycles")
        void shouldHandleCompleteRecordingCycles() throws Exception {
            BufferedImage testImage = TestUtils.createMockImage();

            // Cycle 1
            encoder.configure(recordingInfo);
            encoder.start();
            encoder.encodeFrame(testImage);
            encoder.stop();

            // Cycle 2 with different config
            RecordingInfo newConfig = new RecordingInfo.Builder()
                .outputFile("cycle2.mp4")
                .resolution(1280, 720)
                .build();

            encoder.configure(newConfig);
            encoder.start();
            encoder.encodeFrame(testImage);
            encoder.stop();

            // Final state verification
            assertThat(encoder.isRunning()).isFalse();
            assertThat(encoder.isConfigured()).isTrue();
            assertThat(encoder.getConfig()).isEqualTo(newConfig);
        }

        @Test
        @DisplayName("Should handle configuration changes during stopped state")
        void shouldHandleConfigurationChangesDuringStoppedState() throws Exception {
            // Initial cycle
            encoder.configure(recordingInfo);
            encoder.start();
            encoder.stop();

            // Change configuration while stopped
            RecordingInfo newConfig = new RecordingInfo.Builder()
                .outputFile("changed.mp4")
                .frameRate(60)
                .build();

            encoder.configure(newConfig);

            // Verify new configuration is applied
            assertThat(encoder.getConfig()).isEqualTo(newConfig);
            assertThat(encoder.isConfigured()).isTrue();
            assertThat(encoder.isRunning()).isFalse();

            // Should be able to start with new config
            assertThatCode(() -> encoder.start()).doesNotThrowAnyException();
            assertThat(encoder.isRunning()).isTrue();
        }

        @Test
        @DisplayName("Should handle rapid start/stop cycles")
        void shouldHandleRapidStartStopCycles() throws Exception {
            // Given
            
            // When & Then - Rapid cycles
            for (int i = 0; i < 10; i++) {
                encoder.configure(recordingInfo);
                encoder.start();
                assertThat(encoder.isRunning()).isTrue();
                
                encoder.stop();
                assertThat(encoder.isRunning()).isFalse();
                assertThat(encoder.isConfigured()).isTrue();
            }
        }
    }

    @Nested
    @DisplayName("Configuration Persistence Tests")
    class ConfigurationPersistenceTests {

        @Test
        @DisplayName("Should preserve configuration across start/stop cycles")
        void shouldPreserveConfigurationAcrossStartStopCycles() throws Exception {
            // Given
            encoder.configure(recordingInfo);
            RecordingInfo originalConfig = encoder.getConfig();
            
            // When & Then - Multiple cycles
            for (int i = 0; i < 3; i++) {
                encoder.configure(recordingInfo);
                encoder.start();
                assertThat(encoder.getConfig()).isEqualTo(originalConfig);
                
                encoder.stop();
                assertThat(encoder.getConfig()).isEqualTo(originalConfig);
            }
        }

        @Test
        @DisplayName("Should update configuration when reconfigured")
        void shouldUpdateConfigurationWhenReconfigured() {
            // Given
            encoder.configure(recordingInfo);
            RecordingInfo originalConfig = encoder.getConfig();

            // When
            RecordingInfo newConfig = new RecordingInfo.Builder()
                .outputFile("updated.mp4")
                .resolution(3840, 2160)
                .frameRate(30)
                .build();
            encoder.configure(newConfig);

            // Then
            assertThat(encoder.getConfig()).isEqualTo(newConfig);
            assertThat(encoder.getConfig()).isNotEqualTo(originalConfig);
        }

        @Test
        @DisplayName("Should reuse same recorder across start/stop cycles")
        void shouldReuseSameRecorderAcrossStartStopCycles() throws Exception {
            // Given
            encoder.configure(recordingInfo);
            RecordingInfo originalConfig = encoder.getConfig();

            // When & Then - Verify config persistence across cycles
            for (int i = 0; i < 3; i++) {
                // Before start
                encoder.configure(recordingInfo);
                
                assertThat(encoder.getConfig()).isSameAs(originalConfig);
                assertThat(encoder.isConfigured()).isTrue();
                
                encoder.start();
                assertThat(encoder.getConfig()).isSameAs(originalConfig);
                assertThat(encoder.isRunning()).isTrue();
                
                encoder.stop();
                assertThat(encoder.getConfig()).isSameAs(originalConfig);
                assertThat(encoder.isConfigured()).isTrue();
                assertThat(encoder.isRunning()).isFalse();
            }
        }
    }
}
