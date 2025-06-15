package com.anyscreen.models;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.CsvSource;

import com.anyscreen.utils.TestUtils;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive test suite for RecordingInfo model and its Builder pattern.
 * Tests all constructors, builder methods, and edge cases.
 */
class RecordingInfoTest {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create RecordingInfo with default constructor")
        void shouldCreateRecordingInfoWithDefaultConstructor() {
            // When
            RecordingInfo recordingInfo = new RecordingInfo();

            // Then
            assertThat(recordingInfo.getIsRecording()).isFalse();
            assertThat(recordingInfo.getFrameRate()).isEqualTo(30);
            assertThat(recordingInfo.getHeight()).isEqualTo(720);
            assertThat(recordingInfo.getWidth()).isEqualTo(1280);
            assertThat(recordingInfo.getVideoCodec()).isZero();
            assertThat(recordingInfo.getFormat()).isEqualTo("mp4");
            assertThat(recordingInfo.getOutputFileName()).isEqualTo("recording.mp4");
            assertThat(recordingInfo.getBitRate()).isEqualTo(2000000);
        }

        @Test
        @DisplayName("Should create RecordingInfo with full constructor")
        void shouldCreateRecordingInfoWithFullConstructor() {
            // Given
            Boolean isRecording = true;
            Integer frameRate = 60;
            Integer height = 1080;
            Integer videoCodec = 1;
            Integer width = 1920;
            String format = "avi";
            String outputFileName = "test.avi";
            Integer screenIndex = 1;

            // When
            RecordingInfo recordingInfo = new RecordingInfo(isRecording, frameRate, height, videoCodec, width, format, outputFileName, screenIndex);

            // Then
            assertThat(recordingInfo.getIsRecording()).isEqualTo(isRecording);
            assertThat(recordingInfo.getFrameRate()).isEqualTo(frameRate);
            assertThat(recordingInfo.getHeight()).isEqualTo(height);
            assertThat(recordingInfo.getVideoCodec()).isEqualTo(videoCodec);
            assertThat(recordingInfo.getWidth()).isEqualTo(width);
            assertThat(recordingInfo.getFormat()).isEqualTo(format);
            assertThat(recordingInfo.getOutputFileName()).isEqualTo(outputFileName);
            assertThat(recordingInfo.getScreenIndex()).isEqualTo(screenIndex);
            assertThat(recordingInfo.getBitRate()).isEqualTo(2000000); // Default bit rate
        }

        @Test
        @DisplayName("Should create RecordingInfo with constructor without screen index")
        void shouldCreateRecordingInfoWithoutScreenIndex() {
            // Given
            Boolean isRecording = false;
            Integer frameRate = 24;
            Integer height = 480;
            Integer videoCodec = 2;
            Integer width = 640;
            String format = "mov";
            String outputFileName = "small.mov";

            // When
            RecordingInfo recordingInfo = new RecordingInfo(isRecording, frameRate, height, videoCodec, width, format, outputFileName);

            // Then
            assertThat(recordingInfo.getScreenIndex()).isZero(); // Default screen index
            assertThat(recordingInfo.getFrameRate()).isEqualTo(frameRate);
            assertThat(recordingInfo.getHeight()).isEqualTo(height);
            assertThat(recordingInfo.getWidth()).isEqualTo(width);
        }
    }

    @Nested
    @DisplayName("Builder Pattern Tests")
    class BuilderTests {

        @Test
        @DisplayName("Should create RecordingInfo using builder with default values")
        void shouldCreateRecordingInfoUsingBuilderWithDefaults() {
            // When
            RecordingInfo recordingInfo = new RecordingInfo.Builder().build();

            // Then
            assertThat(recordingInfo.getFrameRate()).isEqualTo(30);
            assertThat(recordingInfo.getHeight()).isEqualTo(720);
            assertThat(recordingInfo.getWidth()).isEqualTo(1280);
            assertThat(recordingInfo.getVideoCodec()).isZero();
            assertThat(recordingInfo.getBitRate()).isEqualTo(2000000);
            assertThat(recordingInfo.getFormat()).isEqualTo("mp4");
            assertThat(recordingInfo.getOutputFileName()).isEqualTo("recording.mp4");
            assertThat(recordingInfo.getScreenIndex()).isZero();
        }

        @Test
        @DisplayName("Should create RecordingInfo using builder with custom values")
        void shouldCreateRecordingInfoUsingBuilderWithCustomValues() {
            // When
            RecordingInfo recordingInfo = new RecordingInfo.Builder()
                    .frameRate(60)
                    .resolution(1920, 1080)
                    .videoCodec(1)
                    .bitRate(5000000)
                    .format("avi")
                    .outputFile("custom.avi")
                    .screenIndex(2)
                    .build();

            // Then
            assertThat(recordingInfo.getFrameRate()).isEqualTo(60);
            assertThat(recordingInfo.getHeight()).isEqualTo(1080);
            assertThat(recordingInfo.getWidth()).isEqualTo(1920);
            assertThat(recordingInfo.getVideoCodec()).isEqualTo(1);
            assertThat(recordingInfo.getBitRate()).isEqualTo(5000000);
            assertThat(recordingInfo.getFormat()).isEqualTo("avi");
            assertThat(recordingInfo.getOutputFileName()).isEqualTo("custom.avi");
            assertThat(recordingInfo.getScreenIndex()).isEqualTo(2);
        }

        @Test
        @DisplayName("Should handle resolution with double values")
        void shouldHandleResolutionWithDoubleValues() {
            // When
            RecordingInfo recordingInfo = new RecordingInfo.Builder()
                    .resolution(1920.5, 1080.7)
                    .build();

            // Then
            assertThat(recordingInfo.getWidth()).isEqualTo(1921); // Rounded
            assertThat(recordingInfo.getHeight()).isEqualTo(1081); // Rounded
        }

        @Test
        @DisplayName("Should chain builder methods fluently")
        void shouldChainBuilderMethodsFluently() {
            // When
            RecordingInfo recordingInfo = new RecordingInfo.Builder()
                    .frameRate(30)
                    .resolution(1280, 720)
                    .format("mp4")
                    .outputFile("chained.mp4")
                    .screenIndex(1)
                    .bitRate(3000000)
                    .videoCodec(0)
                    .build();

            // Then
            assertThat(recordingInfo.getFrameRate()).isEqualTo(30);
            assertThat(recordingInfo.getWidth()).isEqualTo(1280);
            assertThat(recordingInfo.getHeight()).isEqualTo(720);
            assertThat(recordingInfo.getFormat()).isEqualTo("mp4");
            assertThat(recordingInfo.getOutputFileName()).isEqualTo("chained.mp4");
        }
    }

    @Nested
    @DisplayName("Parameterized Tests")
    class ParameterizedTests {

        @ParameterizedTest
        @ValueSource(ints = {15, 24, 30, 60, 120})
        @DisplayName("Should handle different frame rates")
        void shouldHandleDifferentFrameRates(int frameRate) {
            // When
            RecordingInfo recordingInfo = new RecordingInfo.Builder()
                    .frameRate(frameRate)
                    .build();

            // Then
            assertThat(recordingInfo.getFrameRate()).isEqualTo(frameRate);
        }

        @ParameterizedTest
        @CsvSource({
            "640, 480",
            "1280, 720",
            "1920, 1080",
            "2560, 1440",
            "3840, 2160"
        })
        @DisplayName("Should handle different resolutions")
        void shouldHandleDifferentResolutions(int width, int height) {
            // When
            RecordingInfo recordingInfo = new RecordingInfo.Builder()
                    .resolution(width, height)
                    .build();

            // Then
            assertThat(recordingInfo.getWidth()).isEqualTo(width);
            assertThat(recordingInfo.getHeight()).isEqualTo(height);
        }

        @ParameterizedTest
        @ValueSource(strings = {"mp4", "avi", "mov", "mkv", "wmv"})
        @DisplayName("Should handle different video formats")
        void shouldHandleDifferentVideoFormats(String format) {
            // When
            RecordingInfo recordingInfo = new RecordingInfo.Builder()
                    .format(format)
                    .build();

            // Then
            assertThat(recordingInfo.getFormat()).isEqualTo(format);
        }

        @ParameterizedTest
        @ValueSource(ints = {0, 1, 2, 3, 5})
        @DisplayName("Should handle different screen indices")
        void shouldHandleDifferentScreenIndices(int screenIndex) {
            // When
            RecordingInfo recordingInfo = new RecordingInfo.Builder()
                    .screenIndex(screenIndex)
                    .build();

            // Then
            assertThat(recordingInfo.getScreenIndex()).isEqualTo(screenIndex);
        }
    }

    @Nested
    @DisplayName("Utility and Edge Case Tests")
    class UtilityTests {

        @Test
        @DisplayName("Should have proper toString representation")
        void shouldHaveProperToStringRepresentation() {
            // Given
            RecordingInfo recordingInfo = TestUtils.createMockRecordingInfo();

            // When
            String toString = recordingInfo.toString();

            // Then
            assertThat(toString).contains("RecordingInfo{");
            assertThat(toString).contains("frameRate=");
            assertThat(toString).contains("height=");
            assertThat(toString).contains("width=");
            assertThat(toString).contains("format=");
            assertThat(toString).contains("outputFileName=");
        }

        @Test
        @DisplayName("Should work with TestUtils helper methods")
        void shouldWorkWithTestUtilsHelperMethods() {
            // Given
            RecordingInfo defaultInfo = TestUtils.createMockRecordingInfo();
            RecordingInfo customFileInfo = TestUtils.createMockRecordingInfo("custom.mp4");
            RecordingInfo customResolutionInfo = TestUtils.createMockRecordingInfo(640, 480);

            // Then
            assertThat(defaultInfo.getOutputFileName()).isEqualTo(TestUtils.DEFAULT_OUTPUT_FILE);
            assertThat(customFileInfo.getOutputFileName()).isEqualTo("custom.mp4");
            assertThat(customResolutionInfo.getWidth()).isEqualTo(640);
            assertThat(customResolutionInfo.getHeight()).isEqualTo(480);
        }

        @Test
        @DisplayName("Should handle null values gracefully")
        void shouldHandleNullValuesGracefully() {
            // When
            RecordingInfo recordingInfo = new RecordingInfo.Builder()
                    .format(null)
                    .outputFile(null)
                    .build();

            // Then
            assertThat(recordingInfo.getFormat()).isNull();
            assertThat(recordingInfo.getOutputFileName()).isNull();
        }

        @Test
        @DisplayName("Should handle negative values")
        void shouldHandleNegativeValues() {
            // When
            RecordingInfo recordingInfo = new RecordingInfo.Builder()
                    .frameRate(-30)
                    .resolution(-1920, -1080)
                    .bitRate(-1000000)
                    .screenIndex(-1)
                    .build();

            // Then
            assertThat(recordingInfo.getFrameRate()).isEqualTo(-30);
            assertThat(recordingInfo.getWidth()).isEqualTo(-1920);
            assertThat(recordingInfo.getHeight()).isEqualTo(-1080);
            assertThat(recordingInfo.getBitRate()).isEqualTo(-1000000);
            assertThat(recordingInfo.getScreenIndex()).isEqualTo(-1);
        }

        @Test
        @DisplayName("Should handle zero values")
        void shouldHandleZeroValues() {
            // When
            RecordingInfo recordingInfo = new RecordingInfo.Builder()
                    .frameRate(0)
                    .resolution(0, 0)
                    .bitRate(0)
                    .screenIndex(0)
                    .videoCodec(0)
                    .build();

            // Then
            assertThat(recordingInfo.getFrameRate()).isZero();
            assertThat(recordingInfo.getWidth()).isZero();
            assertThat(recordingInfo.getHeight()).isZero();
            assertThat(recordingInfo.getBitRate()).isZero();
            assertThat(recordingInfo.getScreenIndex()).isZero();
            assertThat(recordingInfo.getVideoCodec()).isZero();
        }

        @Test
        @DisplayName("Should handle large values")
        void shouldHandleLargeValues() {
            // When
            RecordingInfo recordingInfo = new RecordingInfo.Builder()
                    .frameRate(1000)
                    .resolution(7680, 4320) // 8K resolution
                    .bitRate(100000000) // 100 Mbps
                    .screenIndex(100)
                    .build();

            // Then
            assertThat(recordingInfo.getFrameRate()).isEqualTo(1000);
            assertThat(recordingInfo.getWidth()).isEqualTo(7680);
            assertThat(recordingInfo.getHeight()).isEqualTo(4320);
            assertThat(recordingInfo.getBitRate()).isEqualTo(100000000);
            assertThat(recordingInfo.getScreenIndex()).isEqualTo(100);
        }
    }
}
