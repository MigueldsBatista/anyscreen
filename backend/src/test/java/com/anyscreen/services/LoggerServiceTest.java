package com.anyscreen.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.anyscreen.interfaces.LoggerInterface;
import com.anyscreen.implementations.Log4jAdapter;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for LoggerService singleton.
 * Tests initialization, singleton behavior, and all logging methods.
 */
class LoggerServiceTest {

    @Mock
    private LoggerInterface mockLogger;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // Reset the singleton for each test
        LoggerService.reset();
    }

    @AfterEach
    void tearDown() {
        // Clean up after tests
        LoggerService.reset();
    }

    @Nested
    @DisplayName("Initialization Tests")
    class InitializationTests {

        @Test
        @DisplayName("Should initialize logger service with custom logger")
        void shouldInitializeLoggerServiceWithCustomLogger() {
            // When
            LoggerService.initialize(mockLogger);

            // Then
            LoggerService instance = LoggerService.getInstance();
            assertThat(instance).isNotNull();
            assertThat(LoggerService.getLogger()).isEqualTo(mockLogger);
        }

        @Test
        @DisplayName("Should initialize logger service with default logger")
        void shouldInitializeLoggerServiceWithDefaultLogger() {
            // When
            LoggerService.initializeDefault();

            // Then
            LoggerService instance = LoggerService.getInstance();
            assertThat(instance).isNotNull();
            assertThat(LoggerService.getLogger()).isInstanceOf(Log4jAdapter.class);
        }

        @Test
        @DisplayName("Should throw exception when initializing twice")
        void shouldThrowExceptionWhenInitializingTwice() {
            // Given
            LoggerService.initialize(mockLogger);

            // When & Then
            assertThatThrownBy(() -> LoggerService.initialize(mockLogger))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("LoggerService has already been initialized");
        }

        @Test
        @DisplayName("Should throw exception when accessing uninitialized instance")
        void shouldThrowExceptionWhenAccessingUninitializedInstance() {
            // When & Then
            assertThatThrownBy(() -> LoggerService.getInstance())
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("LoggerService has not been initialized");
        }

        @Test
        @DisplayName("Should throw exception when accessing uninitialized logger")
        void shouldThrowExceptionWhenAccessingUninitializedLogger() {
            // When & Then
            assertThatThrownBy(() -> LoggerService.getLogger())
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("LoggerService has not been initialized");
        }
    }

    @Nested
    @DisplayName("Singleton Behavior Tests")
    class SingletonBehaviorTests {

        @Test
        @DisplayName("Should return same instance on multiple calls")
        void shouldReturnSameInstanceOnMultipleCalls() {
            // Given
            LoggerService.initialize(mockLogger);

            // When
            LoggerService instance1 = LoggerService.getInstance();
            LoggerService instance2 = LoggerService.getInstance();

            // Then
            assertThat(instance1).isSameAs(instance2);
        }

        @Test
        @DisplayName("Should maintain state across multiple accesses")
        void shouldMaintainStateAcrossMultipleAccesses() {
            // Given
            LoggerService.initialize(mockLogger);

            // When
            LoggerInterface logger1 = LoggerService.getLogger();
            LoggerInterface logger2 = LoggerService.getLogger();

            // Then
            assertThat(logger1).isSameAs(logger2);
            assertThat(logger1).isEqualTo(mockLogger);
        }
    }

    @Nested
    @DisplayName("Logging Methods Tests")
    class LoggingMethodsTests {

        @BeforeEach
        void setUpLogger() {
            LoggerService.initialize(mockLogger);
        }

        @Test
        @DisplayName("Should delegate info messages to underlying logger")
        void shouldDelegateInfoMessagesToUnderlyingLogger() {
            // Given
            String message = "Test info message";

            // When
            LoggerService.info(message);

            // Then
            verify(mockLogger).info(message);
        }

        @Test
        @DisplayName("Should delegate debug messages to underlying logger")
        void shouldDelegateDebugMessagesToUnderlyingLogger() {
            // Given
            String message = "Test debug message";

            // When
            LoggerService.debug(message);

            // Then
            verify(mockLogger).debug(message);
        }

        @Test
        @DisplayName("Should delegate error messages to underlying logger")
        void shouldDelegateErrorMessagesToUnderlyingLogger() {
            // Given
            String message = "Test error message";

            // When
            LoggerService.error(message);

            // Then
            verify(mockLogger).error(message);
        }

        @Test
        @DisplayName("Should delegate warn messages to underlying logger")
        void shouldDelegateWarnMessagesToUnderlyingLogger() {
            // Given
            String message = "Test warn message";

            // When
            LoggerService.warn(message);

            // Then
            verify(mockLogger).warn(message);
        }

        @Test
        @DisplayName("Should handle null messages gracefully")
        void shouldHandleNullMessagesGracefully() {
            // When
            LoggerService.info(null);
            LoggerService.debug(null);
            LoggerService.error(null);
            LoggerService.warn(null);

            // Then
            verify(mockLogger).info(null);
            verify(mockLogger).debug(null);
            verify(mockLogger).error(null);
            verify(mockLogger).warn(null);
        }

        @Test
        @DisplayName("Should handle empty messages")
        void shouldHandleEmptyMessages() {
            // Given
            String emptyMessage = "";

            // When
            LoggerService.info(emptyMessage);
            LoggerService.debug(emptyMessage);
            LoggerService.error(emptyMessage);
            LoggerService.warn(emptyMessage);

            // Then
            verify(mockLogger).info(emptyMessage);
            verify(mockLogger).debug(emptyMessage);
            verify(mockLogger).error(emptyMessage);
            verify(mockLogger).warn(emptyMessage);
        }

        @Test
        @DisplayName("Should handle very long messages")
        void shouldHandleVeryLongMessages() {
            // Given
            String longMessage = "a".repeat(10000);

            // When
            LoggerService.info(longMessage);

            // Then
            verify(mockLogger).info(longMessage);
        }

        @Test
        @DisplayName("Should handle messages with special characters")
        void shouldHandleMessagesWithSpecialCharacters() {
            // Given
            String specialMessage = "Test message with special chars: äöü ñ ü €";

            // When
            LoggerService.info(specialMessage);

            // Then
            verify(mockLogger).info(specialMessage);
        }
    }

    @Nested
    @DisplayName("Thread Safety Tests")
    class ThreadSafetyTests {

        @Test
        @DisplayName("Should handle concurrent initialization attempts safely")
        void shouldHandleConcurrentInitializationAttemptsSafely() throws InterruptedException {
            // Given
            int threadCount = 10;
            Thread[] threads = new Thread[threadCount];
            boolean[] successFlags = new boolean[threadCount];

            // When
            for (int i = 0; i < threadCount; i++) {
                final int index = i;
                threads[i] = new Thread(() -> {
                    try {
                        LoggerService.initialize(mockLogger);
                        successFlags[index] = true;
                    } catch (IllegalStateException e) {
                        // Expected for all but one thread
                        successFlags[index] = false;
                    }
                });
                threads[i].start();
            }

            // Wait for all threads to complete
            for (Thread thread : threads) {
                thread.join();
            }

            // Then
            int successCount = 0;
            for (boolean success : successFlags) {
                if (success) successCount++;
            }
            assertThat(successCount).isEqualTo(1); // Only one thread should succeed

            // Service should be properly initialized
            assertThat(LoggerService.getInstance()).isNotNull();
        }

        @Test
        @DisplayName("Should handle concurrent logging calls safely")
        void shouldHandleConcurrentLoggingCallsSafely() throws InterruptedException {
            // Given
            LoggerService.initialize(mockLogger);
            int threadCount = 10;
            int messagesPerThread = 100;
            Thread[] threads = new Thread[threadCount];

            // When
            for (int i = 0; i < threadCount; i++) {
                final int threadIndex = i;
                threads[i] = new Thread(() -> {
                    for (int j = 0; j < messagesPerThread; j++) {
                        LoggerService.info("Thread " + threadIndex + " Message " + j);
                    }
                });
                threads[i].start();
            }

            // Wait for all threads to complete
            for (Thread thread : threads) {
                thread.join();
            }

            // Then
            verify(mockLogger, times(threadCount * messagesPerThread)).info(anyString());
        }
    }

    @Nested
    @DisplayName("Reset Functionality Tests")
    class ResetFunctionalityTests {

        @Test
        @DisplayName("Should reset singleton instance")
        void shouldResetSingletonInstance() {
            // Given
            LoggerService.initialize(mockLogger);
            assertThat(LoggerService.getInstance()).isNotNull();

            // When
            LoggerService.reset();

            // Then
            assertThatThrownBy(() -> LoggerService.getInstance())
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("Should allow reinitialization after reset")
        void shouldAllowReinitializationAfterReset() {
            // Given
            LoggerService.initialize(mockLogger);
            LoggerService.reset();

            // When
            LoggerService.initialize(mockLogger);

            // Then
            assertThat(LoggerService.getInstance()).isNotNull();
        }
    }
}
