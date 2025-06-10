package com.anyscreen.implementations;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.anyscreen.utils.TestUtils;

import static org.assertj.core.api.Assertions.*;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Comprehensive test suite for ImageIOScreenCaptureSaver implementation.
 * Tests all save operations, format support, and error handling.
 */
class ImageIOScreenCaptureSaverTest {

    private ImageIOScreenCaptureSaver saver;
    private BufferedImage testImage;

    @BeforeEach
    void setUp() {
        saver = new ImageIOScreenCaptureSaver();
        testImage = TestUtils.createMockImage();
    }

    @AfterEach
    void tearDown() throws Exception {
        TestUtils.cleanupTestFiles();
    }

    @Nested
    @DisplayName("Save to File Tests")
    class SaveToFileTests {

        @ParameterizedTest
        @ValueSource(strings = {"png", "jpg", "jpeg", "bmp", "gif"})
        @DisplayName("Should save image to file with different formats")
        void shouldSaveImageToFileWithDifferentFormats(String format) throws Exception {
            // Given
            String fileName = "test_image." + format;
            String filePath = TestUtils.createTestFilePath(fileName);

            // When
            boolean result = saver.saveToFile(testImage, filePath, format);

            // Then
            assertThat(result).isTrue();
            assertThat(TestUtils.isValidFile(filePath)).isTrue();
        }

        @Test
        @DisplayName("Should create parent directories when they don't exist")
        void shouldCreateParentDirectoriesWhenTheyDontExist() throws Exception {
            // Given
            String filePath = TestUtils.createTestFilePath("subdir/nested/image.png");

            // When
            boolean result = saver.saveToFile(testImage, filePath, "png");

            // Then
            assertThat(result).isTrue();
            assertThat(TestUtils.isValidFile(filePath)).isTrue();
            assertThat(Files.exists(Paths.get(filePath).getParent())).isTrue();
        }

        @Test
        @DisplayName("Should handle case insensitive formats")
        void shouldHandleCaseInsensitiveFormats() throws Exception {
            // Given
            String filePath = TestUtils.createTestFilePath("test_uppercase.PNG");

            // When
            boolean result = saver.saveToFile(testImage, filePath, "PNG");

            // Then
            assertThat(result).isTrue();
            assertThat(TestUtils.isValidFile(filePath)).isTrue();
        }

        @Test
        @DisplayName("Should return false for null image")
        void shouldReturnFalseForNullImage() throws Exception {
            // Given
            String filePath = TestUtils.createTestFilePath("null_image.png");

            // When
            boolean result = saver.saveToFile(null, filePath, "png");

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return false for null file path")
        void shouldReturnFalseForNullFilePath() throws Exception {
            // When
            boolean result = saver.saveToFile(testImage, null, "png");

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return false for null format")
        void shouldReturnFalseForNullFormat() throws Exception {
            // Given
            String filePath = TestUtils.createTestFilePath("null_format.png");

            // When
            boolean result = saver.saveToFile(testImage, filePath, null);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should handle different image sizes")
        void shouldHandleDifferentImageSizes() throws Exception {
            // Given
            int[][] resolutions = TestUtils.getTestResolutions();

            for (int[] resolution : resolutions) {
                int width = resolution[0];
                int height = resolution[1];
                BufferedImage image = TestUtils.createMockImage(width, height, java.awt.Color.BLUE);
                String filePath = TestUtils.createTestFilePath("test_" + width + "x" + height + ".png");

                // When
                boolean result = saver.saveToFile(image, filePath, "png");

                // Then
                assertThat(result).isTrue();
                assertThat(TestUtils.isValidFile(filePath)).isTrue();
            }
        }

        @Test
        @DisplayName("Should overwrite existing files")
        void shouldOverwriteExistingFiles() throws Exception {
            // Given
            String filePath = TestUtils.createTestFilePath("overwrite_test.png");
            saver.saveToFile(testImage, filePath, "png");
            long firstSize = new File(filePath).length();

            BufferedImage largerImage = TestUtils.createMockImage(3840, 2160, java.awt.Color.RED);

            // When
            boolean result = saver.saveToFile(largerImage, filePath, "png");

            // Then
            assertThat(result).isTrue();
            long secondSize = new File(filePath).length();
            assertThat(secondSize).isNotEqualTo(firstSize);
        }
    }

    @Nested
    @DisplayName("Save to Stream Tests")
    class SaveToStreamTests {

        @Test
        @DisplayName("Should save image to output stream")
        void shouldSaveImageToOutputStream() {
            // Given
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            // When
            boolean result = saver.saveToStream(testImage, outputStream, "png");

            // Then
            assertThat(result).isTrue();
            assertThat(outputStream.size()).isGreaterThan(0);
        }

        @ParameterizedTest
        @ValueSource(strings = {"png", "jpg", "jpeg", "bmp", "gif"})
        @DisplayName("Should save to stream with different formats")
        void shouldSaveToStreamWithDifferentFormats(String format) {
            // Given
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            // When
            boolean result = saver.saveToStream(testImage, outputStream, format);

            // Then
            assertThat(result).isTrue();
            assertThat(outputStream.size()).isGreaterThan(0);
        }

        @Test
        @DisplayName("Should return false for null image in stream")
        void shouldReturnFalseForNullImageInStream() {
            // Given
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            // When
            boolean result = saver.saveToStream(null, outputStream, "png");

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return false for null output stream")
        void shouldReturnFalseForNullOutputStream() {
            // When
            boolean result = saver.saveToStream(testImage, null, "png");

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return false for null format in stream")
        void shouldReturnFalseForNullFormatInStream() {
            // Given
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            // When
            boolean result = saver.saveToStream(testImage, outputStream, null);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should handle different image types in stream")
        void shouldHandleDifferentImageTypesInStream() {
            // Given
            BufferedImage[] images = {
                new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB),
                new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB),
                new BufferedImage(100, 100, BufferedImage.TYPE_BYTE_GRAY),
                new BufferedImage(100, 100, BufferedImage.TYPE_3BYTE_BGR)
            };

            for (BufferedImage image : images) {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

                // When
                boolean result = saver.saveToStream(image, outputStream, "png");

                // Then
                assertThat(result).isTrue();
                assertThat(outputStream.size()).isGreaterThan(0);
            }
        }
    }

    @Nested
    @DisplayName("To Byte Array Tests")
    class ToByteArrayTests {

        @Test
        @DisplayName("Should convert image to byte array")
        void shouldConvertImageToByteArray() throws Exception {
            // When
            byte[] result = saver.toByteArray(testImage, "png");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.length).isGreaterThan(0);
        }

        @ParameterizedTest
        @ValueSource(strings = {"png", "jpg", "jpeg", "bmp", "gif"})
        @DisplayName("Should convert to byte array with different formats")
        void shouldConvertToByteArrayWithDifferentFormats(String format) throws Exception {
            // When
            byte[] result = saver.toByteArray(testImage, format);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.length).isGreaterThan(0);
        }

        @Test
        @DisplayName("Should return null for null image in byte array")
        void shouldReturnNullForNullImageInByteArray() throws Exception {
            // When
            byte[] result = saver.toByteArray(null, "png");

            // Then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should return null for null format in byte array")
        void shouldReturnNullForNullFormatInByteArray() throws Exception {
            // When
            byte[] result = saver.toByteArray(testImage, null);

            // Then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should produce different byte arrays for different formats")
        void shouldProduceDifferentByteArraysForDifferentFormats() throws Exception {
            // When
            byte[] pngBytes = saver.toByteArray(testImage, "png");
            byte[] jpgBytes = saver.toByteArray(testImage, "jpg");

            // Then
            assertThat(pngBytes).isNotEqualTo(jpgBytes);
            assertThat(pngBytes.length).isNotEqualTo(jpgBytes.length);
        }

        @Test
        @DisplayName("Should handle different image sizes for byte array")
        void shouldHandleDifferentImageSizesForByteArray() throws Exception {
            // Given
            BufferedImage smallImage = TestUtils.createSmallMockImage();
            BufferedImage largeImage = TestUtils.createMockImage();

            // When
            byte[] smallBytes = saver.toByteArray(smallImage, "png");
            byte[] largeBytes = saver.toByteArray(largeImage, "png");

            // Then
            assertThat(smallBytes).isNotNull();
            assertThat(largeBytes).isNotNull();
            assertThat(largeBytes.length).isGreaterThan(smallBytes.length);
        }
    }

    @Nested
    @DisplayName("Supported Formats Tests")
    class SupportedFormatsTests {

        @Test
        @DisplayName("Should return non-empty supported formats array")
        void shouldReturnNonEmptySupportedFormatsArray() {
            // When
            String[] formats = saver.getSupportedFormats();

            // Then
            assertThat(formats).isNotNull();
            assertThat(formats.length).isGreaterThan(0);
        }

        @Test
        @DisplayName("Should include common image formats")
        void shouldIncludeCommonImageFormats() {
            // When
            String[] formats = saver.getSupportedFormats();

            // Then
            assertThat(formats).contains("png");
            assertThat(formats).contains("jpg");
            // Note: Other formats may vary by system
        }

        @Test
        @DisplayName("Should return consistent results on multiple calls")
        void shouldReturnConsistentResultsOnMultipleCalls() {
            // When
            String[] formats1 = saver.getSupportedFormats();
            String[] formats2 = saver.getSupportedFormats();

            // Then
            assertThat(formats1).containsExactly(formats2);
        }
    }

    @Nested
    @DisplayName("Error Handling and Edge Cases")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should handle invalid file path gracefully")
        void shouldHandleInvalidFilePathGracefully() throws Exception {
            // Given
            String invalidPath = "/invalid/path/that/does/not/exist/and/cannot/be/created/image.png";

            // When & Then
            assertThatCode(() -> saver.saveToFile(testImage, invalidPath, "png"))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should handle unsupported format gracefully")
        void shouldHandleUnsupportedFormatGracefully() throws Exception {
            // Given
            String filePath = TestUtils.createTestFilePath("unsupported.xyz");

            // When
            boolean result = saver.saveToFile(testImage, filePath, "xyz");

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should handle empty format string")
        void shouldHandleEmptyFormatString() throws Exception {
            // Given
            String filePath = TestUtils.createTestFilePath("empty_format.png");

            // When
            boolean result = saver.saveToFile(testImage, filePath, "");

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should handle very large images")
        void shouldHandleVeryLargeImages() throws Exception {
            // Given - Create a large image (but not too large to avoid memory issues)
            BufferedImage largeImage = TestUtils.createMockImage(4096, 4096, java.awt.Color.GREEN);
            String filePath = TestUtils.createTestFilePath("large_image.png");

            // When
            boolean result = saver.saveToFile(largeImage, filePath, "png");

            // Then
            assertThat(result).isTrue();
            assertThat(TestUtils.isValidFile(filePath)).isTrue();
        }

        @Test
        @DisplayName("Should handle zero-sized images")
        void shouldHandleZeroSizedImages() throws Exception {
            // Given
            BufferedImage zeroImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
            String filePath = TestUtils.createTestFilePath("zero_image.png");

            // When
            boolean result = saver.saveToFile(zeroImage, filePath, "png");

            // Then
            assertThat(result).isTrue();
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("Should complete full save and verify cycle")
        void shouldCompleteFullSaveAndVerifyCycle() throws Exception {
            // Given
            String[] formats = TestUtils.getTestImageFormats();

            for (String format : formats) {
                String filePath = TestUtils.createTestFilePath("integration_test." + format);

                // When
                boolean saveResult = saver.saveToFile(testImage, filePath, format);
                byte[] byteResult = saver.toByteArray(testImage, format);

                // Then
                if (java.util.Arrays.asList(saver.getSupportedFormats()).contains(format)) {
                    assertThat(saveResult).isTrue();
                    assertThat(byteResult).isNotNull();
                    assertThat(TestUtils.isValidFile(filePath)).isTrue();
                }
            }
        }

        @Test
        @DisplayName("Should work with all supported image operations")
        void shouldWorkWithAllSupportedImageOperations() throws Exception {
            // Given
            String filePath = TestUtils.createTestFilePath("all_operations.png");
            ByteArrayOutputStream stream = new ByteArrayOutputStream();

            // When
            boolean fileResult = saver.saveToFile(testImage, filePath, "png");
            boolean streamResult = saver.saveToStream(testImage, stream, "png");
            byte[] byteResult = saver.toByteArray(testImage, "png");
            String[] formats = saver.getSupportedFormats();

            // Then
            assertThat(fileResult).isTrue();
            assertThat(streamResult).isTrue();
            assertThat(byteResult).isNotNull();
            assertThat(formats).isNotEmpty();
            assertThat(TestUtils.isValidFile(filePath)).isTrue();
            assertThat(stream.size()).isGreaterThan(0);
        }
    }
}
