package com.anyscreen.exceptions;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Comprehensive test suite for custom exception classes.
 * Tests exception creation, message handling, and cause propagation.
 */
@DisplayName("Exception Classes Tests")
public class ExceptionClassesTest {

    @Nested
    @DisplayName("ScreenCaptureException Tests")
    class ScreenCaptureExceptionTests {

        @Test
        @DisplayName("Should create exception with message")
        void shouldCreateExceptionWithMessage() {
            // Given
            String message = "Screen capture failed";

            // When
            ScreenCaptureException exception = new ScreenCaptureException(message);

            // Then
            assertThat(exception.getMessage()).isEqualTo(message);
            assertThat(exception.getCause()).isNull();
            assertThat(exception).isInstanceOf(Exception.class);
        }

        @Test
        @DisplayName("Should create exception with message and cause")
        void shouldCreateExceptionWithMessageAndCause() {
            // Given
            String message = "Screen capture failed";
            Throwable cause = new RuntimeException("Hardware error");

            // When
            ScreenCaptureException exception = new ScreenCaptureException(message, cause);

            // Then
            assertThat(exception.getMessage()).isEqualTo(message);
            assertThat(exception.getCause()).isEqualTo(cause);
            assertThat(exception.getCause().getMessage()).isEqualTo("Hardware error");
        }

        @Test
        @DisplayName("Should create exception with cause only")
        void shouldCreateExceptionWithCauseOnly() {
            // Given
            Throwable cause = new IllegalArgumentException("Invalid screen index");

            // When
            ScreenCaptureException exception = new ScreenCaptureException(cause);

            // Then
            assertThat(exception.getCause()).isEqualTo(cause);
            assertThat(exception.getMessage()).contains("IllegalArgumentException");
            assertThat(exception.getMessage()).contains("Invalid screen index");
        }

        @Test
        @DisplayName("Should handle null message")
        void shouldHandleNullMessage() {
            // When
            ScreenCaptureException exception = new ScreenCaptureException((String) null);

            // Then
            assertThat(exception.getMessage()).isNull();
            assertThat(exception.getCause()).isNull();
        }

        @Test
        @DisplayName("Should handle null cause")
        void shouldHandleNullCause() {
            // When
            ScreenCaptureException exception = new ScreenCaptureException((Throwable) null);

            // Then
            assertThat(exception.getCause()).isNull();
            assertThat(exception.getMessage()).isEqualTo("null");
        }

        @Test
        @DisplayName("Should chain multiple causes")
        void shouldChainMultipleCauses() {
            // Given
            RuntimeException rootCause = new RuntimeException("Hardware failure");
            IllegalStateException intermediateCause = new IllegalStateException("Robot not available", rootCause);
            String message = "Screen capture initialization failed";

            // When
            ScreenCaptureException exception = new ScreenCaptureException(message, intermediateCause);

            // Then
            assertThat(exception.getMessage()).isEqualTo(message);
            assertThat(exception.getCause()).isEqualTo(intermediateCause);
            assertThat(exception.getCause().getCause()).isEqualTo(rootCause);
        }
    }

    @Nested
    @DisplayName("RecordingException Tests")
    class RecordingExceptionTests {

        @Test
        @DisplayName("Should create exception with message")
        void shouldCreateExceptionWithMessage() {
            // Given
            String message = "Recording start failed";

            // When
            RecordingException exception = new RecordingException(message);

            // Then
            assertThat(exception.getMessage()).isEqualTo(message);
            assertThat(exception.getCause()).isNull();
            assertThat(exception).isInstanceOf(Exception.class);
        }

        @Test
        @DisplayName("Should create exception with message and cause")
        void shouldCreateExceptionWithMessageAndCause() {
            // Given
            String message = "Recording configuration failed";
            Throwable cause = new IllegalArgumentException("Invalid frame rate");

            // When
            RecordingException exception = new RecordingException(message, cause);

            // Then
            assertThat(exception.getMessage()).isEqualTo(message);
            assertThat(exception.getCause()).isEqualTo(cause);
            assertThat(exception.getCause().getMessage()).isEqualTo("Invalid frame rate");
        }

        @Test
        @DisplayName("Should create exception with cause only")
        void shouldCreateExceptionWithCauseOnly() {
            // Given
            Throwable cause = new UnsupportedOperationException("Codec not supported");

            // When
            RecordingException exception = new RecordingException(cause);

            // Then
            assertThat(exception.getCause()).isEqualTo(cause);
            assertThat(exception.getMessage()).contains("UnsupportedOperationException");
            assertThat(exception.getMessage()).contains("Codec not supported");
        }

        @Test
        @DisplayName("Should maintain exception hierarchy")
        void shouldMaintainExceptionHierarchy() {
            // Given
            RecordingException exception = new RecordingException("Recording failed");

            // Then
            assertThat(exception).isInstanceOf(Exception.class);
            assertThat(exception).isInstanceOf(Throwable.class);
        }

        @Test
        @DisplayName("Should support error chaining for recording lifecycle")
        void shouldSupportErrorChainingForRecordingLifecycle() {
            // Given - Simulate recording lifecycle error chain
            Exception fileSystemError = new java.io.IOException("Disk full");
            Exception encoderError = new RuntimeException("Encoder failed to initialize", fileSystemError);
            String message = "Failed to start recording session";

            // When
            RecordingException exception = new RecordingException(message, encoderError);

            // Then
            assertThat(exception.getMessage()).isEqualTo(message);
            assertThat(exception.getCause()).isEqualTo(encoderError);
            assertThat(exception.getCause().getCause()).isEqualTo(fileSystemError);
            assertThat(exception.getCause().getCause().getMessage()).isEqualTo("Disk full");
        }
    }

    @Nested
    @DisplayName("EncodingException Tests")
    class EncodingExceptionTests {

        @Test
        @DisplayName("Should create exception with message")
        void shouldCreateExceptionWithMessage() {
            // Given
            String message = "Video encoding failed";

            // When
            EncodingException exception = new EncodingException(message);

            // Then
            assertThat(exception.getMessage()).isEqualTo(message);
            assertThat(exception.getCause()).isNull();
            assertThat(exception).isInstanceOf(Exception.class);
        }

        @Test
        @DisplayName("Should create exception with message and cause")
        void shouldCreateExceptionWithMessageAndCause() {
            // Given
            String message = "Frame encoding error";
            Throwable cause = new OutOfMemoryError("Insufficient memory for encoding");

            // When
            EncodingException exception = new EncodingException(message, cause);

            // Then
            assertThat(exception.getMessage()).isEqualTo(message);
            assertThat(exception.getCause()).isEqualTo(cause);
            assertThat(exception.getCause().getMessage()).isEqualTo("Insufficient memory for encoding");
        }

        @Test
        @DisplayName("Should create exception with cause only")
        void shouldCreateExceptionWithCauseOnly() {
            // Given
            Throwable cause = new IllegalStateException("Encoder not initialized");

            // When
            EncodingException exception = new EncodingException(cause);

            // Then
            assertThat(exception.getCause()).isEqualTo(cause);
            assertThat(exception.getMessage()).contains("IllegalStateException");
            assertThat(exception.getMessage()).contains("Encoder not initialized");
        }

        @Test
        @DisplayName("Should handle codec-specific errors")
        void shouldHandleCodecSpecificErrors() {
            // Given - Simulate codec error chain
            Throwable nativeLibraryError = new UnsatisfiedLinkError("FFmpeg library not found");
            Exception codecError = new RuntimeException("H.264 codec initialization failed", nativeLibraryError);
            String message = "Video encoder configuration failed";

            // When
            EncodingException exception = new EncodingException(message, codecError);

            // Then
            assertThat(exception.getMessage()).isEqualTo(message);
            assertThat(exception.getCause()).isEqualTo(codecError);
            assertThat(exception.getCause().getCause()).isEqualTo(nativeLibraryError);
        }

        @Test
        @DisplayName("Should support serialization")
        void shouldSupportSerialization() {
            // Given
            String message = "Encoding failed during frame processing";
            Exception cause = new IllegalArgumentException("Invalid frame format");
            EncodingException exception = new EncodingException(message, cause);

            // When - Simulate serialization scenario
            String className = exception.getClass().getName();
            StackTraceElement[] stackTrace = exception.getStackTrace();

            // Then
            assertThat(className).isEqualTo("com.anyscreen.exceptions.EncodingException");
            assertThat(stackTrace).isNotEmpty();
            assertThat(exception.getMessage()).isEqualTo(message);
            assertThat(exception.getCause()).isEqualTo(cause);
        }
    }

    @Nested
    @DisplayName("Exception Integration Tests")
    class ExceptionIntegrationTests {

        @Test
        @DisplayName("Should demonstrate proper exception usage in application context")
        void shouldDemonstrateProperExceptionUsageInApplicationContext() {
            // Given - Simulate application error scenarios
            ScreenCaptureException captureException = new ScreenCaptureException("Screen not accessible");
            RecordingException recordingException = new RecordingException("Recording service unavailable", captureException);
            EncodingException encodingException = new EncodingException("Final encoding step failed", recordingException);

            // Then - Verify complete error chain
            assertThat(encodingException.getCause()).isEqualTo(recordingException);
            assertThat(encodingException.getCause().getCause()).isEqualTo(captureException);
            
            // Verify error messages are preserved
            assertThat(encodingException.getMessage()).contains("Final encoding step failed");
            assertThat(recordingException.getMessage()).contains("Recording service unavailable");
            assertThat(captureException.getMessage()).contains("Screen not accessible");
        }

        @Test
        @DisplayName("Should maintain type hierarchy for catch blocks")
        void shouldMaintainTypeHierarchyForCatchBlocks() {
            // Given
            ScreenCaptureException screenException = new ScreenCaptureException("Screen error");
            RecordingException recordingException = new RecordingException("Recording error");
            EncodingException encodingException = new EncodingException("Encoding error");

            // Then - All should be catchable as Exception
            assertThat(screenException).isInstanceOf(Exception.class);
            assertThat(recordingException).isInstanceOf(Exception.class);
            assertThat(encodingException).isInstanceOf(Exception.class);

            // And should maintain their specific types
            assertThat(screenException).isInstanceOf(ScreenCaptureException.class);
            assertThat(recordingException).isInstanceOf(RecordingException.class);
            assertThat(encodingException).isInstanceOf(EncodingException.class);
        }

        @Test
        @DisplayName("Should support exception conversion scenarios")
        void shouldSupportExceptionConversionScenarios() {
            // Given - Original low-level exception
            RuntimeException lowLevelError = new RuntimeException("Hardware access denied");
            
            // When - Convert to domain-specific exceptions
            ScreenCaptureException captureError = new ScreenCaptureException("Failed to access screen hardware", lowLevelError);
            RecordingException recordingError = new RecordingException("Cannot start recording due to capture failure", captureError);

            // Then - Verify proper error context preservation
            assertThat(recordingError.getCause()).isEqualTo(captureError);
            assertThat(captureError.getCause()).isEqualTo(lowLevelError);
            
            // Original error details should be accessible
            Throwable rootCause = recordingError.getCause().getCause();
            assertThat(rootCause.getMessage()).isEqualTo("Hardware access denied");
        }
    }

    @Nested
    @DisplayName("Exception Message and Documentation Tests")
    class ExceptionMessageAndDocumentationTests {

        @Test
        @DisplayName("Should verify exception class documentation")
        void shouldVerifyExceptionClassDocumentation() {
            // Given - Check that exception classes are properly documented
            Class<ScreenCaptureException> screenCaptureClass = ScreenCaptureException.class;
            Class<RecordingException> recordingClass = RecordingException.class;
            Class<EncodingException> encodingClass = EncodingException.class;

            // Then - Verify package structure
            assertThat(screenCaptureClass.getPackage().getName()).isEqualTo("com.anyscreen.exceptions");
            assertThat(recordingClass.getPackage().getName()).isEqualTo("com.anyscreen.exceptions");
            assertThat(encodingClass.getPackage().getName()).isEqualTo("com.anyscreen.exceptions");

            // Verify constructor availability
            assertThat(screenCaptureClass.getConstructors()).hasSize(3);
            assertThat(recordingClass.getConstructors()).hasSize(3);
            assertThat(encodingClass.getConstructors()).hasSize(3);
        }

        @Test
        @DisplayName("Should handle empty and special character messages")
        void shouldHandleEmptyAndSpecialCharacterMessages() {
            // Given
            String emptyMessage = "";
            String specialCharsMessage = "Error: <>&\"'测试";
            String unicodeMessage = "Erreur: Échec de l'encodage vidéo 🎥";

            // When
            ScreenCaptureException emptyException = new ScreenCaptureException(emptyMessage);
            RecordingException specialException = new RecordingException(specialCharsMessage);
            EncodingException unicodeException = new EncodingException(unicodeMessage);

            // Then
            assertThat(emptyException.getMessage()).isEqualTo("");
            assertThat(specialException.getMessage()).isEqualTo(specialCharsMessage);
            assertThat(unicodeException.getMessage()).isEqualTo(unicodeMessage);
        }

        @Test
        @DisplayName("Should handle very long error messages")
        void shouldHandleVeryLongErrorMessages() {
            // Given
            StringBuilder longMessage = new StringBuilder();
            for (int i = 0; i < 1000; i++) {
                longMessage.append("Very long error message part ").append(i).append(". ");
            }

            // When
            ScreenCaptureException exception = new ScreenCaptureException(longMessage.toString());

            // Then
            assertThat(exception.getMessage()).hasSize(longMessage.length());
            assertThat(exception.getMessage()).startsWith("Very long error message part 0.");
            assertThat(exception.getMessage()).endsWith("Very long error message part 999. ");
        }
    }
}
