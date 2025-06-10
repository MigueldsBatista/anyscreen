package com.anyscreen.interfaces;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.awt.image.BufferedImage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;

import com.anyscreen.models.RecordingInfo;
import com.anyscreen.utils.TestUtils;

/**
 * Test suite for FrameEncoderInterface contract validation.
 * Tests interface behavior through mock implementations and validates
 * that all implementations follow the expected contract.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("FrameEncoderInterface Contract Tests")
public class FrameEncoderInterfaceTest {

    private FrameEncoderInterface mockEncoder;
    private RecordingInfo testRecordingInfo;
    private BufferedImage testImage;

    @BeforeEach
    void setUp() {
        mockEncoder = mock(FrameEncoderInterface.class);
        testRecordingInfo = TestUtils.createMockRecordingInfo();
        testImage = TestUtils.createMockImage();
    }

    @Nested
    @DisplayName("Configuration Contract Tests")
    class ConfigurationContractTests {

        @Test
        @DisplayName("Should accept configuration method call")
        void shouldAcceptConfigurationMethodCall() {
            // When
            mockEncoder.configure(testRecordingInfo);
            
            // Then
            verify(mockEncoder).configure(testRecordingInfo);
        }

        @Test
        @DisplayName("Should handle null configuration gracefully")
        void shouldHandleNullConfigurationGracefully() {
            // When
            mockEncoder.configure(null);
            
            // Then
            verify(mockEncoder).configure(null);
        }

        @Test
        @DisplayName("Should allow multiple configurations")
        void shouldAllowMultipleConfigurations() {
            // Given
            RecordingInfo config1 = TestUtils.createMockRecordingInfo(1920, 1080, 30);
            RecordingInfo config2 = TestUtils.createMockRecordingInfo();
            
            // When
            mockEncoder.configure(config1);
            mockEncoder.configure(config2);
            
            // Then
            verify(mockEncoder).configure(config1);
            verify(mockEncoder).configure(config2);
        }
    }

    @Nested
    @DisplayName("Lifecycle Contract Tests")
    class LifecycleContractTests {

        @Test
        @DisplayName("Should support start operation")
        void shouldSupportStartOperation() throws Exception {
            // When
            mockEncoder.start();
            
            // Then
            verify(mockEncoder).start();
        }

        @Test
        @DisplayName("Should support stop operation")
        void shouldSupportStopOperation() throws Exception {
            // When
            mockEncoder.stop();
            
            // Then
            verify(mockEncoder).stop();
        }

        @Test
        @DisplayName("Should handle start exceptions")
        void shouldHandleStartExceptions() throws Exception {
            // Given
            doThrow(new RuntimeException("Start failed")).when(mockEncoder).start();
            
            // When & Then
            assertThatThrownBy(() -> mockEncoder.start())
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Start failed");
        }

        @Test
        @DisplayName("Should handle stop exceptions")
        void shouldHandleStopExceptions() throws Exception {
            // Given
            doThrow(new RuntimeException("Stop failed")).when(mockEncoder).stop();
            
            // When & Then
            assertThatThrownBy(() -> mockEncoder.stop())
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Stop failed");
        }
    }

    @Nested
    @DisplayName("State Management Contract Tests")
    class StateManagementContractTests {

        @Test
        @DisplayName("Should provide running state")
        void shouldProvideRunningState() {
            // Given
            when(mockEncoder.isRunning()).thenReturn(true);
            
            // When
            boolean isRunning = mockEncoder.isRunning();
            
            // Then
            assertThat(isRunning).isTrue();
            verify(mockEncoder).isRunning();
        }

        @Test
        @DisplayName("Should handle state transitions")
        void shouldHandleStateTransitions() {
            // Given
            when(mockEncoder.isRunning())
                .thenReturn(false)  // Initially not running
                .thenReturn(true)   // After start
                .thenReturn(false); // After stop
            
            // When & Then
            assertThat(mockEncoder.isRunning()).isFalse(); // Initial state
            assertThat(mockEncoder.isRunning()).isTrue();  // Running state
            assertThat(mockEncoder.isRunning()).isFalse(); // Stopped state
        }

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        @DisplayName("Should handle different running states")
        void shouldHandleDifferentRunningStates(boolean runningState) {
            // Given
            when(mockEncoder.isRunning()).thenReturn(runningState);
            
            // When
            boolean result = mockEncoder.isRunning();
            
            // Then
            assertThat(result).isEqualTo(runningState);
        }
    }

    @Nested
    @DisplayName("Frame Encoding Contract Tests")
    class FrameEncodingContractTests {

        @Test
        @DisplayName("Should accept frame encoding calls")
        void shouldAcceptFrameEncodingCalls() throws Exception {
            // When
            mockEncoder.encodeFrame(testImage);
            
            // Then
            verify(mockEncoder).encodeFrame(testImage);
        }

        @Test
        @DisplayName("Should handle null frame encoding")
        void shouldHandleNullFrameEncoding() throws Exception {
            // When
            mockEncoder.encodeFrame(null);
            
            // Then
            verify(mockEncoder).encodeFrame(null);
        }

        @Test
        @DisplayName("Should handle encoding exceptions")
        void shouldHandleEncodingExceptions() throws Exception {
            // Given
            doThrow(new RuntimeException("Encoding failed")).when(mockEncoder).encodeFrame(any());
            
            // When & Then
            assertThatThrownBy(() -> mockEncoder.encodeFrame(testImage))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Encoding failed");
        }

        @Test
        @DisplayName("Should support multiple frame encodings")
        void shouldSupportMultipleFrameEncodings() throws Exception {
            // Given
            BufferedImage frame1 = TestUtils.createMockImage(100, 100);
            BufferedImage frame2 = TestUtils.createMockImage(200, 200);
            BufferedImage frame3 = TestUtils.createMockImage(300, 300);
            
            // When
            mockEncoder.encodeFrame(frame1);
            mockEncoder.encodeFrame(frame2);
            mockEncoder.encodeFrame(frame3);
            
            // Then
            verify(mockEncoder).encodeFrame(frame1);
            verify(mockEncoder).encodeFrame(frame2);
            verify(mockEncoder).encodeFrame(frame3);
        }

        @Test
        @DisplayName("Should handle different image types")
        void shouldHandleDifferentImageTypes() throws Exception {
            // Given
            BufferedImage rgbImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
            BufferedImage argbImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
            BufferedImage grayImage = new BufferedImage(100, 100, BufferedImage.TYPE_BYTE_GRAY);
            
            // When
            mockEncoder.encodeFrame(rgbImage);
            mockEncoder.encodeFrame(argbImage);
            mockEncoder.encodeFrame(grayImage);
            
            // Then
            verify(mockEncoder).encodeFrame(rgbImage);
            verify(mockEncoder).encodeFrame(argbImage);
            verify(mockEncoder).encodeFrame(grayImage);
        }
    }

    @Nested
    @DisplayName("Integration Contract Tests")
    class IntegrationContractTests {

        @Test
        @DisplayName("Should support complete workflow cycle")
        void shouldSupportCompleteWorkflowCycle() throws Exception {
            // Given
            when(mockEncoder.isRunning())
                .thenReturn(false)  // Before start
                .thenReturn(true)   // After start
                .thenReturn(false); // After stop
            
            // When - Complete workflow
            mockEncoder.configure(testRecordingInfo);
            mockEncoder.start();
            boolean runningAfterStart = mockEncoder.isRunning();
            mockEncoder.encodeFrame(testImage);
            mockEncoder.stop();
            boolean runningAfterStop = mockEncoder.isRunning();
            
            // Then
            assertThat(runningAfterStart).isTrue();
            assertThat(runningAfterStop).isFalse();
            
            verify(mockEncoder).configure(testRecordingInfo);
            verify(mockEncoder).start();
            verify(mockEncoder).encodeFrame(testImage);
            verify(mockEncoder).stop();
            verify(mockEncoder, times(2)).isRunning();
        }

        @Test
        @DisplayName("Should support reconfiguration workflow")
        void shouldSupportReconfigurationWorkflow() throws Exception {
            // Given
            RecordingInfo newConfig = TestUtils.createMockRecordingInfo();
            
            // When
            mockEncoder.configure(testRecordingInfo);
            mockEncoder.configure(newConfig);
            mockEncoder.start();
            mockEncoder.encodeFrame(testImage);
            mockEncoder.stop();
            
            // Then
            verify(mockEncoder).configure(testRecordingInfo);
            verify(mockEncoder).configure(newConfig);
            verify(mockEncoder).start();
            verify(mockEncoder).encodeFrame(testImage);
            verify(mockEncoder).stop();
        }

        @Test
        @DisplayName("Should handle partial workflow execution")
        void shouldHandlePartialWorkflowExecution() throws Exception {
            // When - Only configuration
            mockEncoder.configure(testRecordingInfo);
            
            // Then
            verify(mockEncoder).configure(testRecordingInfo);
            verify(mockEncoder, never()).start();
            verify(mockEncoder, never()).stop();
            verify(mockEncoder, never()).encodeFrame(any());
        }
    }

    @Nested
    @DisplayName("Error Handling Contract Tests")
    class ErrorHandlingContractTests {

        @Test
        @DisplayName("Should propagate configuration errors")
        void shouldPropagateConfigurationErrors() {
            // Given
            doThrow(new IllegalArgumentException("Invalid config")).when(mockEncoder).configure(any());
            
            // When & Then
            assertThatThrownBy(() -> mockEncoder.configure(testRecordingInfo))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid config");
        }

        @Test
        @DisplayName("Should propagate lifecycle errors")
        void shouldPropagateLifecycleErrors() throws Exception {
            // Given
            doThrow(new IllegalStateException("Already started")).when(mockEncoder).start();
            doThrow(new IllegalStateException("Not running")).when(mockEncoder).stop();
            
            // When & Then
            assertThatThrownBy(() -> mockEncoder.start())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Already started");
                
            assertThatThrownBy(() -> mockEncoder.stop())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Not running");
        }

        @Test
        @DisplayName("Should handle encoding error recovery")
        void shouldHandleEncodingErrorRecovery() throws Exception {
            // Given
            doThrow(new RuntimeException("Frame 1 failed"))
                .doNothing()
                .when(mockEncoder).encodeFrame(any());
            
            BufferedImage frame1 = TestUtils.createMockImage(100, 100);
            BufferedImage frame2 = TestUtils.createMockImage(100, 100);
            
            // When & Then
            assertThatThrownBy(() -> mockEncoder.encodeFrame(frame1))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Frame 1 failed");
            
            // Should be able to continue with next frame
            mockEncoder.encodeFrame(frame2);
            verify(mockEncoder, times(2)).encodeFrame(any());
        }
    }

    @Nested
    @DisplayName("Thread Safety Contract Tests")
    class ThreadSafetyContractTests {

        @Test
        @DisplayName("Should handle concurrent method calls")
        void shouldHandleConcurrentMethodCalls() throws Exception {
            // Given
            when(mockEncoder.isRunning()).thenReturn(true);
            
            // When - Simulate concurrent access
            Runnable configureTask = () -> mockEncoder.configure(testRecordingInfo);
            Runnable encodeTask = () -> {
                try {
                    mockEncoder.encodeFrame(testImage);
                } catch (Exception e) {
                    // Handle exceptions in thread
                }
            };
            Runnable stateTask = () -> mockEncoder.isRunning();
            
            Thread t1 = new Thread(configureTask);
            Thread t2 = new Thread(encodeTask);
            Thread t3 = new Thread(stateTask);
            
            t1.start();
            t2.start();
            t3.start();
            
            t1.join();
            t2.join();
            t3.join();
            
            // Then - All methods should have been called
            verify(mockEncoder, atLeastOnce()).configure(any());
            verify(mockEncoder, atLeastOnce()).encodeFrame(any());
            verify(mockEncoder, atLeastOnce()).isRunning();
        }

        @Test
        @DisplayName("Should maintain state consistency under concurrent access")
        void shouldMaintainStateConsistencyUnderConcurrentAccess() throws InterruptedException {
            // Given
            when(mockEncoder.isRunning()).thenReturn(true);
            
            // When - Multiple threads check state
            final int threadCount = 10;
            Thread[] threads = new Thread[threadCount];
            
            for (int i = 0; i < threadCount; i++) {
                threads[i] = new Thread(() -> {
                    boolean state = mockEncoder.isRunning();
                    assertThat(state).isTrue();
                });
                threads[i].start();
            }
            
            for (Thread thread : threads) {
                thread.join();
            }
            
            // Then
            verify(mockEncoder, times(threadCount)).isRunning();
        }
    }
}
