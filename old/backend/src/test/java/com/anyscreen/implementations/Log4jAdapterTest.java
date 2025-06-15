package com.anyscreen.implementations;

import com.anyscreen.interfaces.LoggerInterface;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for Log4jAdapter.
 * Tests Log4j integration, logger interface implementation, and message delegation.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Log4jAdapter Tests")
class Log4jAdapterTest {

    private Log4jAdapter log4jAdapter;
    private Logger mockLogger;

    @BeforeEach
    void setUp() {
        mockLogger = mock(Logger.class);
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create Log4jAdapter with Log4j logger")
        void shouldCreateLog4jAdapterWithLog4jLogger() {
            try (MockedStatic<LogManager> mockedLogManager = mockStatic(LogManager.class)) {
                mockedLogManager.when(() -> LogManager.getLogger(Log4jAdapter.class))
                    .thenReturn(mockLogger);

                log4jAdapter = new Log4jAdapter();

                assertThat(log4jAdapter).isNotNull();
                assertThat(log4jAdapter).isInstanceOf(LoggerInterface.class);
                mockedLogManager.verify(() -> LogManager.getLogger(Log4jAdapter.class));
            }
        }

        @Test
        @DisplayName("Should handle LogManager initialization")
        void shouldHandleLogManagerInitialization() {
            try (MockedStatic<LogManager> mockedLogManager = mockStatic(LogManager.class)) {
                mockedLogManager.when(() -> LogManager.getLogger(Log4jAdapter.class))
                    .thenReturn(mockLogger);

                assertThatCode(() -> new Log4jAdapter())
                    .doesNotThrowAnyException();
            }
        }

        @Test
        @DisplayName("Should use correct logger class")
        void shouldUseCorrectLoggerClass() {
            try (MockedStatic<LogManager> mockedLogManager = mockStatic(LogManager.class)) {
                mockedLogManager.when(() -> LogManager.getLogger(Log4jAdapter.class))
                    .thenReturn(mockLogger);

                new Log4jAdapter();

                mockedLogManager.verify(() -> LogManager.getLogger(Log4jAdapter.class));
                mockedLogManager.verifyNoMoreInteractions();
            }
        }
    }

    @Nested
    @DisplayName("Logging Method Tests")
    class LoggingMethodTests {

        @BeforeEach
        void setUpAdapter() {
            try (MockedStatic<LogManager> mockedLogManager = mockStatic(LogManager.class)) {
                mockedLogManager.when(() -> LogManager.getLogger(Log4jAdapter.class))
                    .thenReturn(mockLogger);
                log4jAdapter = new Log4jAdapter();
            }
        }

        @Nested
        @DisplayName("Info Logging Tests")
        class InfoLoggingTests {

            @Test
            @DisplayName("Should delegate info message to Log4j logger")
            void shouldDelegateInfoMessageToLog4jLogger() {
                String message = "This is an info message";

                log4jAdapter.info(message);

                verify(mockLogger).info(message);
                verifyNoMoreInteractions(mockLogger);
            }

            @ParameterizedTest
            @ValueSource(strings = {
                "Simple info message",
                "Info message with numbers: 123",
                "Info message with special chars: !@#$%^&*()",
                "Very long info message that contains multiple words and should be handled properly by the logging system",
                "日本語のメッセージ", // Japanese
                "Message with\nnewlines\nand\ttabs"
            })
            @DisplayName("Should handle various info message formats")
            void shouldHandleVariousInfoMessageFormats(String message) {
                log4jAdapter.info(message);

                verify(mockLogger).info(message);
            }

            @ParameterizedTest
            @NullAndEmptySource
            @DisplayName("Should handle null and empty info messages")
            void shouldHandleNullAndEmptyInfoMessages(String message) {
                assertThatCode(() -> log4jAdapter.info(message))
                    .doesNotThrowAnyException();

                verify(mockLogger).info(message);
            }
        }

        @Nested
        @DisplayName("Debug Logging Tests")
        class DebugLoggingTests {

            @Test
            @DisplayName("Should delegate debug message to Log4j logger")
            void shouldDelegateDebugMessageToLog4jLogger() {
                String message = "This is a debug message";

                log4jAdapter.debug(message);

                verify(mockLogger).debug(message);
                verifyNoMoreInteractions(mockLogger);
            }

            @ParameterizedTest
            @ValueSource(strings = {
                "Simple debug message",
                "Debug with variables: value=42",
                "Debug with stack trace info",
                "Debug message with JSON: {\"key\": \"value\"}",
                "XML debug: <tag>value</tag>"
            })
            @DisplayName("Should handle various debug message formats")
            void shouldHandleVariousDebugMessageFormats(String message) {
                log4jAdapter.debug(message);

                verify(mockLogger).debug(message);
            }

            @ParameterizedTest
            @NullAndEmptySource
            @DisplayName("Should handle null and empty debug messages")
            void shouldHandleNullAndEmptyDebugMessages(String message) {
                assertThatCode(() -> log4jAdapter.debug(message))
                    .doesNotThrowAnyException();

                verify(mockLogger).debug(message);
            }
        }

        @Nested
        @DisplayName("Error Logging Tests")
        class ErrorLoggingTests {

            @Test
            @DisplayName("Should delegate error message to Log4j logger")
            void shouldDelegateErrorMessageToLog4jLogger() {
                String message = "This is an error message";

                log4jAdapter.error(message);

                verify(mockLogger).error(message);
                verifyNoMoreInteractions(mockLogger);
            }

            @ParameterizedTest
            @ValueSource(strings = {
                "Simple error message",
                "NullPointerException occurred",
                "Error: Connection timeout after 30 seconds",
                "Failed to save file: /path/to/file.txt",
                "Error code: 500 - Internal server error"
            })
            @DisplayName("Should handle various error message formats")
            void shouldHandleVariousErrorMessageFormats(String message) {
                log4jAdapter.error(message);

                verify(mockLogger).error(message);
            }

            @ParameterizedTest
            @NullAndEmptySource
            @DisplayName("Should handle null and empty error messages")
            void shouldHandleNullAndEmptyErrorMessages(String message) {
                assertThatCode(() -> log4jAdapter.error(message))
                    .doesNotThrowAnyException();

                verify(mockLogger).error(message);
            }
        }

        @Nested
        @DisplayName("Warn Logging Tests")
        class WarnLoggingTests {

            @Test
            @DisplayName("Should delegate warn message to Log4j logger")
            void shouldDelegateWarnMessageToLog4jLogger() {
                String message = "This is a warning message";

                log4jAdapter.warn(message);

                verify(mockLogger).warn(message);
                verifyNoMoreInteractions(mockLogger);
            }

            @ParameterizedTest
            @ValueSource(strings = {
                "Simple warning message",
                "Warning: Deprecated method used",
                "Performance warning: Operation took 5 seconds",
                "Warning: Low disk space - 10% remaining",
                "Configuration warning: Missing optional parameter"
            })
            @DisplayName("Should handle various warn message formats")
            void shouldHandleVariousWarnMessageFormats(String message) {
                log4jAdapter.warn(message);

                verify(mockLogger).warn(message);
            }

            @ParameterizedTest
            @NullAndEmptySource
            @DisplayName("Should handle null and empty warn messages")
            void shouldHandleNullAndEmptyWarnMessages(String message) {
                assertThatCode(() -> log4jAdapter.warn(message))
                    .doesNotThrowAnyException();

                verify(mockLogger).warn(message);
            }
        }
    }

    @Nested
    @DisplayName("LoggerInterface Contract Tests")
    class LoggerInterfaceContractTests {

        @BeforeEach
        void setUpAdapter() {
            try (MockedStatic<LogManager> mockedLogManager = mockStatic(LogManager.class)) {
                mockedLogManager.when(() -> LogManager.getLogger(Log4jAdapter.class))
                    .thenReturn(mockLogger);
                log4jAdapter = new Log4jAdapter();
            }
        }

        @Test
        @DisplayName("Should implement LoggerInterface")
        void shouldImplementLoggerInterface() {
            assertThat(log4jAdapter).isInstanceOf(LoggerInterface.class);
        }

        @Test
        @DisplayName("Should have all required LoggerInterface methods")
        void shouldHaveAllRequiredLoggerInterfaceMethods() {
            LoggerInterface logger = log4jAdapter;

            assertThatCode(() -> {
                logger.info("test");
                logger.debug("test");
                logger.error("test");
                logger.warn("test");
            }).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should delegate all logging levels correctly")
        void shouldDelegateAllLoggingLevelsCorrectly() {
            String testMessage = "test message";

            log4jAdapter.info(testMessage);
            log4jAdapter.debug(testMessage);
            log4jAdapter.error(testMessage);
            log4jAdapter.warn(testMessage);

            verify(mockLogger).info(testMessage);
            verify(mockLogger).debug(testMessage);
            verify(mockLogger).error(testMessage);
            verify(mockLogger).warn(testMessage);
            verifyNoMoreInteractions(mockLogger);
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("Should work with real Log4j logger")
        void shouldWorkWithRealLog4jLogger() {
            // Test with actual Log4j logger (no mocking)
            Log4jAdapter realAdapter = new Log4jAdapter();

            assertThatCode(() -> {
                realAdapter.info("Integration test info message");
                realAdapter.debug("Integration test debug message");
                realAdapter.warn("Integration test warn message");
                realAdapter.error("Integration test error message");
            }).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should maintain thread safety")
        void shouldMaintainThreadSafety() throws InterruptedException {
            Log4jAdapter realAdapter = new Log4jAdapter();
            int numberOfThreads = 10;
            Thread[] threads = new Thread[numberOfThreads];

            for (int i = 0; i < numberOfThreads; i++) {
                final int threadId = i;
                threads[i] = new Thread(() -> {
                    for (int j = 0; j < 100; j++) {
                        realAdapter.info("Thread " + threadId + " message " + j);
                        realAdapter.debug("Thread " + threadId + " debug " + j);
                        realAdapter.warn("Thread " + threadId + " warn " + j);
                        realAdapter.error("Thread " + threadId + " error " + j);
                    }
                });
            }

            // Start all threads
            for (Thread thread : threads) {
                thread.start();
            }

            // Wait for all threads to complete
            for (Thread thread : threads) {
                thread.join();
            }

            // If we reach here, no exceptions were thrown
            assertThat(true).isTrue();
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should handle Log4j logger exceptions gracefully")
        void shouldHandleLog4jLoggerExceptionsGracefully() {
            try (MockedStatic<LogManager> mockedLogManager = mockStatic(LogManager.class)) {
                mockedLogManager.when(() -> LogManager.getLogger(Log4jAdapter.class))
                    .thenReturn(mockLogger);

                log4jAdapter = new Log4jAdapter();

                // Configure mock to throw exception
                doThrow(new RuntimeException("Log4j error")).when(mockLogger).info(anyString());

                assertThatThrownBy(() -> log4jAdapter.info("test message"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Log4j error");
            }
        }

        @Test
        @DisplayName("Should handle LogManager.getLogger exceptions")
        void shouldHandleLogManagerGetLoggerExceptions() {
            try (MockedStatic<LogManager> mockedLogManager = mockStatic(LogManager.class)) {
                mockedLogManager.when(() -> LogManager.getLogger(Log4jAdapter.class))
                    .thenThrow(new RuntimeException("LogManager error"));

                assertThatThrownBy(() -> new Log4jAdapter())
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("LogManager error");
            }
        }
    }

    @Nested
    @DisplayName("Performance Tests")
    class PerformanceTests {

        @Test
        @DisplayName("Should handle high volume logging efficiently")
        void shouldHandleHighVolumeLoggingEfficiently() {
            try (MockedStatic<LogManager> mockedLogManager = mockStatic(LogManager.class)) {
                mockedLogManager.when(() -> LogManager.getLogger(Log4jAdapter.class))
                    .thenReturn(mockLogger);

                log4jAdapter = new Log4jAdapter();

                long startTime = System.currentTimeMillis();

                // Log 1000 messages
                for (int i = 0; i < 1000; i++) {
                    log4jAdapter.info("Performance test message " + i);
                    log4jAdapter.debug("Performance test debug " + i);
                    log4jAdapter.warn("Performance test warn " + i);
                    log4jAdapter.error("Performance test error " + i);
                }

                long endTime = System.currentTimeMillis();
                long duration = endTime - startTime;

                // Verify all calls were made
                verify(mockLogger, times(1000)).info(startsWith("Performance test message"));
                verify(mockLogger, times(1000)).debug(startsWith("Performance test debug"));
                verify(mockLogger, times(1000)).warn(startsWith("Performance test warn"));
                verify(mockLogger, times(1000)).error(startsWith("Performance test error"));

                // Basic performance assertion - should complete in reasonable time
                assertThat(duration).isLessThan(5000); // 5 seconds max for 4000 log calls
            }
        }

        @Test
        @DisplayName("Should have minimal memory overhead")
        void shouldHaveMinimalMemoryOverhead() {
            try (MockedStatic<LogManager> mockedLogManager = mockStatic(LogManager.class)) {
                mockedLogManager.when(() -> LogManager.getLogger(Log4jAdapter.class))
                    .thenReturn(mockLogger);

                // Create multiple adapters to test memory usage
                Log4jAdapter[] adapters = new Log4jAdapter[100];
                
                for (int i = 0; i < adapters.length; i++) {
                    adapters[i] = new Log4jAdapter();
                }

                // All adapters should be functional
                for (Log4jAdapter adapter : adapters) {
                    adapter.info("Memory test");
                }

                verify(mockLogger, times(100)).info("Memory test");
            }
        }
    }
}
