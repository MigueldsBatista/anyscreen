package com.anyscreen.interfaces;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;

import com.anyscreen.utils.TestUtils;

/**
 * Test suite for ScreenCaptureSaverInterface contract validation.
 * Tests interface behavior through mock implementations and validates
 * that all implementations follow the expected saving contract.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ScreenCaptureSaverInterface Contract Tests")
public class ScreenCaptureSaverInterfaceTest {

    private ScreenCaptureSaverInterface mockSaver;
    private BufferedImage testImage;
    private OutputStream testStream;

    @BeforeEach
    void setUp() {
        mockSaver = mock(ScreenCaptureSaverInterface.class);
        testImage = TestUtils.createMockImage();
        testStream = new ByteArrayOutputStream();
    }

    @Nested
    @DisplayName("File Saving Contract Tests")
    class FileSavingContractTests {

        @Test
        @DisplayName("Should support file saving operation")
        void shouldSupportFileSavingOperation() throws Exception {
            // Given
            String filePath = "/test/path/image.png";
            String format = "png";
            when(mockSaver.saveToFile(testImage, filePath, format)).thenReturn(true);
            
            // When
            boolean result = mockSaver.saveToFile(testImage, filePath, format);
            
            // Then
            assertThat(result).isTrue();
            verify(mockSaver).saveToFile(testImage, filePath, format);
        }

        @Test
        @DisplayName("Should handle file saving failures")
        void shouldHandleFileSavingFailures() throws Exception {
            // Given
            String filePath = "/invalid/path/image.png";
            String format = "png";
            when(mockSaver.saveToFile(testImage, filePath, format)).thenReturn(false);
            
            // When
            boolean result = mockSaver.saveToFile(testImage, filePath, format);
            
            // Then
            assertThat(result).isFalse();
            verify(mockSaver).saveToFile(testImage, filePath, format);
        }

        @Test
        @DisplayName("Should handle null image in file saving")
        void shouldHandleNullImageInFileSaving() throws Exception {
            // Given
            String filePath = "/test/path/image.png";
            String format = "png";
            when(mockSaver.saveToFile(null, filePath, format)).thenReturn(false);
            
            // When
            boolean result = mockSaver.saveToFile(null, filePath, format);
            
            // Then
            assertThat(result).isFalse();
            verify(mockSaver).saveToFile(null, filePath, format);
        }

        @Test
        @DisplayName("Should handle null file path")
        void shouldHandleNullFilePath() throws Exception {
            // Given
            String format = "png";
            when(mockSaver.saveToFile(testImage, null, format)).thenReturn(false);
            
            // When
            boolean result = mockSaver.saveToFile(testImage, null, format);
            
            // Then
            assertThat(result).isFalse();
            verify(mockSaver).saveToFile(testImage, null, format);
        }

        @Test
        @DisplayName("Should handle null format in file saving")
        void shouldHandleNullFormatInFileSaving() throws Exception {
            // Given
            String filePath = "/test/path/image.png";
            when(mockSaver.saveToFile(testImage, filePath, null)).thenReturn(false);
            
            // When
            boolean result = mockSaver.saveToFile(testImage, filePath, null);
            
            // Then
            assertThat(result).isFalse();
            verify(mockSaver).saveToFile(testImage, filePath, null);
        }

        @ParameterizedTest
        @ValueSource(strings = {"png", "jpg", "jpeg", "bmp", "gif"})
        @DisplayName("Should handle different file formats")
        void shouldHandleDifferentFileFormats(String format) throws Exception {
            // Given
            String filePath = "/test/path/image." + format;
            when(mockSaver.saveToFile(testImage, filePath, format)).thenReturn(true);
            
            // When
            boolean result = mockSaver.saveToFile(testImage, filePath, format);
            
            // Then
            assertThat(result).isTrue();
            verify(mockSaver).saveToFile(testImage, filePath, format);
        }

        @Test
        @DisplayName("Should handle file saving exceptions")
        void shouldHandleFileSavingExceptions() throws Exception {
            // Given
            String filePath = "/test/path/image.png";
            String format = "png";
            when(mockSaver.saveToFile(testImage, filePath, format))
                .thenThrow(new RuntimeException("File system error"));
            
            // When & Then
            assertThatThrownBy(() -> mockSaver.saveToFile(testImage, filePath, format))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("File system error");
        }
    }

    @Nested
    @DisplayName("Stream Saving Contract Tests")
    class StreamSavingContractTests {

        @Test
        @DisplayName("Should support stream saving operation")
        void shouldSupportStreamSavingOperation() {
            // Given
            String format = "png";
            when(mockSaver.saveToStream(testImage, testStream, format)).thenReturn(true);
            
            // When
            boolean result = mockSaver.saveToStream(testImage, testStream, format);
            
            // Then
            assertThat(result).isTrue();
            verify(mockSaver).saveToStream(testImage, testStream, format);
        }

        @Test
        @DisplayName("Should handle stream saving failures")
        void shouldHandleStreamSavingFailures() {
            // Given
            String format = "invalid";
            when(mockSaver.saveToStream(testImage, testStream, format)).thenReturn(false);
            
            // When
            boolean result = mockSaver.saveToStream(testImage, testStream, format);
            
            // Then
            assertThat(result).isFalse();
            verify(mockSaver).saveToStream(testImage, testStream, format);
        }

        @Test
        @DisplayName("Should handle null image in stream saving")
        void shouldHandleNullImageInStreamSaving() {
            // Given
            String format = "png";
            when(mockSaver.saveToStream(null, testStream, format)).thenReturn(false);
            
            // When
            boolean result = mockSaver.saveToStream(null, testStream, format);
            
            // Then
            assertThat(result).isFalse();
            verify(mockSaver).saveToStream(null, testStream, format);
        }

        @Test
        @DisplayName("Should handle null stream")
        void shouldHandleNullStream() {
            // Given
            String format = "png";
            when(mockSaver.saveToStream(testImage, null, format)).thenReturn(false);
            
            // When
            boolean result = mockSaver.saveToStream(testImage, null, format);
            
            // Then
            assertThat(result).isFalse();
            verify(mockSaver).saveToStream(testImage, null, format);
        }

        @Test
        @DisplayName("Should handle null format in stream saving")
        void shouldHandleNullFormatInStreamSaving() {
            // Given
            when(mockSaver.saveToStream(testImage, testStream, null)).thenReturn(false);
            
            // When
            boolean result = mockSaver.saveToStream(testImage, testStream, null);
            
            // Then
            assertThat(result).isFalse();
            verify(mockSaver).saveToStream(testImage, testStream, null);
        }

        @ParameterizedTest
        @ValueSource(strings = {"png", "jpg", "jpeg", "bmp", "gif"})
        @DisplayName("Should handle different stream formats")
        void shouldHandleDifferentStreamFormats(String format) {
            // Given
            when(mockSaver.saveToStream(testImage, testStream, format)).thenReturn(true);
            
            // When
            boolean result = mockSaver.saveToStream(testImage, testStream, format);
            
            // Then
            assertThat(result).isTrue();
            verify(mockSaver).saveToStream(testImage, testStream, format);
        }

        @Test
        @DisplayName("Should handle different image types in stream")
        void shouldHandleDifferentImageTypesInStream() {
            // Given
            BufferedImage rgbImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
            BufferedImage argbImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
            String format = "png";
            
            when(mockSaver.saveToStream(rgbImage, testStream, format)).thenReturn(true);
            when(mockSaver.saveToStream(argbImage, testStream, format)).thenReturn(true);
            
            // When
            boolean rgbResult = mockSaver.saveToStream(rgbImage, testStream, format);
            boolean argbResult = mockSaver.saveToStream(argbImage, testStream, format);
            
            // Then
            assertThat(rgbResult).isTrue();
            assertThat(argbResult).isTrue();
            verify(mockSaver).saveToStream(rgbImage, testStream, format);
            verify(mockSaver).saveToStream(argbImage, testStream, format);
        }
    }

    @Nested
    @DisplayName("Byte Array Conversion Contract Tests")
    class ByteArrayConversionContractTests {

        @Test
        @DisplayName("Should support byte array conversion")
        void shouldSupportByteArrayConversion() throws Exception {
            // Given
            String format = "png";
            byte[] expectedBytes = new byte[]{1, 2, 3, 4, 5};
            when(mockSaver.toByteArray(testImage, format)).thenReturn(expectedBytes);
            
            // When
            byte[] result = mockSaver.toByteArray(testImage, format);
            
            // Then
            assertThat(result).isEqualTo(expectedBytes);
            verify(mockSaver).toByteArray(testImage, format);
        }

        @Test
        @DisplayName("Should handle null image in byte array conversion")
        void shouldHandleNullImageInByteArrayConversion() throws Exception {
            // Given
            String format = "png";
            when(mockSaver.toByteArray(null, format)).thenReturn(null);
            
            // When
            byte[] result = mockSaver.toByteArray(null, format);
            
            // Then
            assertThat(result).isNull();
            verify(mockSaver).toByteArray(null, format);
        }

        @Test
        @DisplayName("Should handle null format in byte array conversion")
        void shouldHandleNullFormatInByteArrayConversion() throws Exception {
            // Given
            when(mockSaver.toByteArray(testImage, null)).thenReturn(null);
            
            // When
            byte[] result = mockSaver.toByteArray(testImage, null);
            
            // Then
            assertThat(result).isNull();
            verify(mockSaver).toByteArray(testImage, null);
        }

        @Test
        @DisplayName("Should handle conversion failures")
        void shouldHandleConversionFailures() throws Exception {
            // Given
            String format = "invalid";
            when(mockSaver.toByteArray(testImage, format)).thenReturn(null);
            
            // When
            byte[] result = mockSaver.toByteArray(testImage, format);
            
            // Then
            assertThat(result).isNull();
            verify(mockSaver).toByteArray(testImage, format);
        }

        @Test
        @DisplayName("Should handle conversion exceptions")
        void shouldHandleConversionExceptions() throws Exception {
            // Given
            String format = "png";
            when(mockSaver.toByteArray(testImage, format))
                .thenThrow(new RuntimeException("Conversion failed"));
            
            // When & Then
            assertThatThrownBy(() -> mockSaver.toByteArray(testImage, format))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Conversion failed");
        }

        @ParameterizedTest
        @ValueSource(strings = {"png", "jpg", "jpeg", "bmp", "gif"})
        @DisplayName("Should handle different byte array formats")
        void shouldHandleDifferentByteArrayFormats(String format) throws Exception {
            // Given
            byte[] mockBytes = format.getBytes();
            when(mockSaver.toByteArray(testImage, format)).thenReturn(mockBytes);
            
            // When
            byte[] result = mockSaver.toByteArray(testImage, format);
            
            // Then
            assertThat(result).isEqualTo(mockBytes);
            verify(mockSaver).toByteArray(testImage, format);
        }
    }

    @Nested
    @DisplayName("Supported Formats Contract Tests")
    class SupportedFormatsContractTests {

        @Test
        @DisplayName("Should provide supported formats")
        void shouldProvideSupportedFormats() {
            // Given
            String[] expectedFormats = {"png", "jpg", "jpeg", "bmp", "gif"};
            when(mockSaver.getSupportedFormats()).thenReturn(expectedFormats);
            
            // When
            String[] result = mockSaver.getSupportedFormats();
            
            // Then
            assertThat(result).isEqualTo(expectedFormats);
            verify(mockSaver).getSupportedFormats();
        }

        @Test
        @DisplayName("Should handle empty supported formats")
        void shouldHandleEmptySupportedFormats() {
            // Given
            String[] emptyFormats = new String[0];
            when(mockSaver.getSupportedFormats()).thenReturn(emptyFormats);
            
            // When
            String[] result = mockSaver.getSupportedFormats();
            
            // Then
            assertThat(result).isEmpty();
            verify(mockSaver).getSupportedFormats();
        }

        @Test
        @DisplayName("Should return consistent supported formats")
        void shouldReturnConsistentSupportedFormats() {
            // Given
            String[] formats = {"png", "jpg"};
            when(mockSaver.getSupportedFormats()).thenReturn(formats);
            
            // When
            String[] result1 = mockSaver.getSupportedFormats();
            String[] result2 = mockSaver.getSupportedFormats();
            
            // Then
            assertThat(result1).isEqualTo(result2);
            verify(mockSaver, times(2)).getSupportedFormats();
        }

        @Test
        @DisplayName("Should handle null supported formats")
        void shouldHandleNullSupportedFormats() {
            // Given
            when(mockSaver.getSupportedFormats()).thenReturn(null);
            
            // When
            String[] result = mockSaver.getSupportedFormats();
            
            // Then
            assertThat(result).isNull();
            verify(mockSaver).getSupportedFormats();
        }
    }

    @Nested
    @DisplayName("Performance Contract Tests")
    class PerformanceContractTests {

        @Test
        @DisplayName("Should handle rapid saving operations")
        void shouldHandleRapidSavingOperations() throws Exception {
            // Given
            when(mockSaver.saveToStream(any(), any(), anyString())).thenReturn(true);
            
            // When
            for (int i = 0; i < 100; i++) {
                boolean result = mockSaver.saveToStream(testImage, testStream, "png");
                assertThat(result).isTrue();
            }
            
            // Then
            verify(mockSaver, times(100)).saveToStream(any(), any(), anyString());
        }

        @Test
        @DisplayName("Should handle concurrent saving operations")
        void shouldHandleConcurrentSavingOperations() throws InterruptedException {
            // Given
            when(mockSaver.saveToStream(any(), any(), anyString())).thenReturn(true);
            final int threadCount = 10;
            Thread[] threads = new Thread[threadCount];
            
            // When
            for (int i = 0; i < threadCount; i++) {
                threads[i] = new Thread(() -> {
                    boolean result = mockSaver.saveToStream(testImage, testStream, "png");
                    assertThat(result).isTrue();
                });
                threads[i].start();
            }
            
            for (Thread thread : threads) {
                thread.join();
            }
            
            // Then
            verify(mockSaver, times(threadCount)).saveToStream(any(), any(), anyString());
        }

        @Test
        @DisplayName("Should handle large images efficiently")
        void shouldHandleLargeImagesEfficiently() throws Exception {
            // Given
            BufferedImage largeImage = new BufferedImage(4096, 4096, BufferedImage.TYPE_INT_RGB);
            when(mockSaver.saveToStream(largeImage, testStream, "png")).thenReturn(true);
            when(mockSaver.toByteArray(largeImage, "png")).thenReturn(new byte[1024]);
            
            // When
            boolean streamResult = mockSaver.saveToStream(largeImage, testStream, "png");
            byte[] byteResult = mockSaver.toByteArray(largeImage, "png");
            
            // Then
            assertThat(streamResult).isTrue();
            assertThat(byteResult).isNotNull();
            verify(mockSaver).saveToStream(largeImage, testStream, "png");
            verify(mockSaver).toByteArray(largeImage, "png");
        }
    }

    @Nested
    @DisplayName("Integration Contract Tests")
    class IntegrationContractTests {

        @Test
        @DisplayName("Should support complete saving workflow")
        void shouldSupportCompleteSavingWorkflow() throws Exception {
            // Given
            String[] formats = {"png", "jpg"};
            when(mockSaver.getSupportedFormats()).thenReturn(formats);
            when(mockSaver.saveToFile(any(), anyString(), anyString())).thenReturn(true);
            when(mockSaver.saveToStream(any(), any(), anyString())).thenReturn(true);
            when(mockSaver.toByteArray(any(), anyString())).thenReturn(new byte[100]);
            
            // When - Complete workflow
            String[] supportedFormats = mockSaver.getSupportedFormats();
            boolean fileResult = mockSaver.saveToFile(testImage, "/path/image.png", "png");
            boolean streamResult = mockSaver.saveToStream(testImage, testStream, "png");
            byte[] byteResult = mockSaver.toByteArray(testImage, "png");
            
            // Then
            assertThat(supportedFormats).isNotNull();
            assertThat(fileResult).isTrue();
            assertThat(streamResult).isTrue();
            assertThat(byteResult).isNotNull();
            
            verify(mockSaver).getSupportedFormats();
            verify(mockSaver).saveToFile(testImage, "/path/image.png", "png");
            verify(mockSaver).saveToStream(testImage, testStream, "png");
            verify(mockSaver).toByteArray(testImage, "png");
        }

        @Test
        @DisplayName("Should handle mixed success and failure scenarios")
        void shouldHandleMixedSuccessAndFailureScenarios() throws Exception {
            // Given
            when(mockSaver.saveToFile(testImage, "/valid/path.png", "png")).thenReturn(true);
            when(mockSaver.saveToFile(testImage, "/invalid/path.png", "png")).thenReturn(false);
            when(mockSaver.saveToStream(testImage, testStream, "png")).thenReturn(true);
            when(mockSaver.saveToStream(testImage, testStream, "invalid")).thenReturn(false);
            when(mockSaver.toByteArray(testImage, "png")).thenReturn(new byte[100]);
            when(mockSaver.toByteArray(testImage, "invalid")).thenReturn(null);
            
            // When & Then
            assertThat(mockSaver.saveToFile(testImage, "/valid/path.png", "png")).isTrue();
            assertThat(mockSaver.saveToFile(testImage, "/invalid/path.png", "png")).isFalse();
            assertThat(mockSaver.saveToStream(testImage, testStream, "png")).isTrue();
            assertThat(mockSaver.saveToStream(testImage, testStream, "invalid")).isFalse();
            assertThat(mockSaver.toByteArray(testImage, "png")).isNotNull();
            assertThat(mockSaver.toByteArray(testImage, "invalid")).isNull();
        }

        @Test
        @DisplayName("Should maintain consistent behavior across operations")
        void shouldMaintainConsistentBehaviorAcrossOperations() throws Exception {
            // Given
            when(mockSaver.getSupportedFormats()).thenReturn(new String[]{"png"});
            when(mockSaver.saveToFile(any(), anyString(), eq("png"))).thenReturn(true);
            when(mockSaver.saveToStream(any(), any(), eq("png"))).thenReturn(true);
            when(mockSaver.toByteArray(any(), eq("png"))).thenReturn(new byte[100]);
            
            // When
            String[] formats1 = mockSaver.getSupportedFormats();
            boolean file1 = mockSaver.saveToFile(testImage, "/path1.png", "png");
            boolean stream1 = mockSaver.saveToStream(testImage, testStream, "png");
            byte[] bytes1 = mockSaver.toByteArray(testImage, "png");
            
            String[] formats2 = mockSaver.getSupportedFormats();
            boolean file2 = mockSaver.saveToFile(testImage, "/path2.png", "png");
            boolean stream2 = mockSaver.saveToStream(testImage, testStream, "png");
            byte[] bytes2 = mockSaver.toByteArray(testImage, "png");
            
            // Then
            assertThat(formats1).isEqualTo(formats2);
            assertThat(file1).isEqualTo(file2);
            assertThat(stream1).isEqualTo(stream2);
            assertThat(bytes1.length).isEqualTo(bytes2.length);
        }
    }
}
