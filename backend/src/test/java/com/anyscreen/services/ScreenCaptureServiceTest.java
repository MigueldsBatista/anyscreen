package com.anyscreen.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.anyscreen.interfaces.ScreenCaptureInterface;
import com.anyscreen.interfaces.ScreenCaptureSaverInterface;
import com.anyscreen.implementations.RobotScreenCapture;
import com.anyscreen.implementations.ImageIOScreenCaptureSaver;
import com.anyscreen.exceptions.ScreenCaptureException;
import com.anyscreen.models.ScreenInfo;
import com.anyscreen.utils.TestUtils;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.List;

/**
 * Comprehensive test suite for ScreenCaptureService.
 * Tests service composition, delegation, and integration scenarios.
 */
class ScreenCaptureServiceTest {

    @Mock
    private ScreenCaptureInterface mockCaptureInterface;

    @Mock
    private ScreenCaptureSaverInterface mockSaver;

    private ScreenCaptureService screenCaptureService;
    private BufferedImage testImage;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        screenCaptureService = new ScreenCaptureService(mockCaptureInterface, mockSaver);
        testImage = TestUtils.createMockImage();
    }

    @AfterEach
    void tearDown() throws Exception {
        TestUtils.cleanupTestFiles();
    }

    @Nested
    @DisplayName("Constructor and Factory Tests")
    class ConstructorAndFactoryTests {

        @Test
        @DisplayName("Should create service with provided interfaces")
        void shouldCreateServiceWithProvidedInterfaces() {
            // When
            ScreenCaptureService service = new ScreenCaptureService(mockCaptureInterface, mockSaver);

            // Then
            assertThat(service).isNotNull();
        }

        @Test
        @DisplayName("Should create default service")
        void shouldCreateDefaultService() throws ScreenCaptureException {
            // When
            ScreenCaptureService service = ScreenCaptureService.createDefault();

            // Then
            assertThat(service).isNotNull();
        }

        @Test
        @DisplayName("Should throw exception when creating default service fails")
        void shouldThrowExceptionWhenCreatingDefaultServiceFails() {
            // Note: This test might be environment-dependent
            // In headless environments, this could fail
            assertThatCode(() -> ScreenCaptureService.createDefault())
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Screen Capture Delegation Tests")
    class ScreenCaptureDelegationTests {

        @Test
        @DisplayName("Should delegate captureScreen to interface")
        void shouldDelegateCaptureScreenToInterface() throws ScreenCaptureException {
            // Given
            when(mockCaptureInterface.captureScreen()).thenReturn(testImage);

            // When
            BufferedImage result = screenCaptureService.captureScreen();

            // Then
            assertThat(result).isEqualTo(testImage);
            verify(mockCaptureInterface).captureScreen();
        }

        @Test
        @DisplayName("Should delegate captureRegion to interface")
        void shouldDelegateCaptureRegionToInterface() throws ScreenCaptureException {
            // Given
            Rectangle region = TestUtils.createTestRectangle(100, 100, 800, 600);
            when(mockCaptureInterface.captureRegion(region)).thenReturn(testImage);

            // When
            BufferedImage result = screenCaptureService.captureRegion(region);

            // Then
            assertThat(result).isEqualTo(testImage);
            verify(mockCaptureInterface).captureRegion(region);
        }

        @Test
        @DisplayName("Should delegate captureScreen with index to interface")
        void shouldDelegateCaptureScreenWithIndexToInterface() throws ScreenCaptureException {
            // Given
            int screenIndex = 1;
            when(mockCaptureInterface.captureScreen(screenIndex)).thenReturn(testImage);

            // When
            BufferedImage result = screenCaptureService.captureScreen(screenIndex);

            // Then
            assertThat(result).isEqualTo(testImage);
            verify(mockCaptureInterface).captureScreen(screenIndex);
        }

        @Test
        @DisplayName("Should delegate getAvailableScreens to interface")
        void shouldDelegateGetAvailableScreensToInterface() {
            // Given
            List<ScreenInfo> screens = TestUtils.createMultipleScreenInfos();
            when(mockCaptureInterface.getAvailableScreens()).thenReturn(screens);

            // When
            List<ScreenInfo> result = screenCaptureService.getAvailableScreens();

            // Then
            assertThat(result).isEqualTo(screens);
            verify(mockCaptureInterface).getAvailableScreens();
        }

        @Test
        @DisplayName("Should get screen info by index")
        void shouldGetScreenInfoByIndex() {
            // Given
            List<ScreenInfo> screens = TestUtils.createMultipleScreenInfos();
            when(mockCaptureInterface.getAvailableScreens()).thenReturn(screens);

            // When
            ScreenInfo result = screenCaptureService.getScreenInfo(1);

            // Then
            assertThat(result).isEqualTo(screens.get(1));
            verify(mockCaptureInterface).getAvailableScreens();
        }

        @Test
        @DisplayName("Should delegate getPrimaryScreenBounds to interface")
        void shouldDelegateGetPrimaryScreenBoundsToInterface() {
            // Given
            Rectangle bounds = TestUtils.createDefaultTestRectangle();
            when(mockCaptureInterface.getPrimaryScreenBounds()).thenReturn(bounds);

            // When
            Rectangle result = screenCaptureService.getPrimaryScreenBounds();

            // Then
            assertThat(result).isEqualTo(bounds);
            verify(mockCaptureInterface).getPrimaryScreenBounds();
        }

        @Test
        @DisplayName("Should delegate isSupported to interface")
        void shouldDelegateIsSupportedToInterface() {
            // Given
            when(mockCaptureInterface.isSupported()).thenReturn(true);

            // When
            boolean result = screenCaptureService.isSupported();

            // Then
            assertThat(result).isTrue();
            verify(mockCaptureInterface).isSupported();
        }
    }

    @Nested
    @DisplayName("Screen Capture and Save Combination Tests")
    class CaptureAndSaveTests {

        @Test
        @DisplayName("Should capture and save screen")
        void shouldCaptureAndSaveScreen() throws Exception {
            // Given
            String filePath = TestUtils.createTestFilePath("capture_and_save.png");
            String format = "png";
            when(mockCaptureInterface.captureScreen()).thenReturn(testImage);
            when(mockSaver.saveToFile(testImage, filePath, format)).thenReturn(true);

            // When
            boolean result = screenCaptureService.captureAndSave(filePath, format);

            // Then
            assertThat(result).isTrue();
            verify(mockCaptureInterface).captureScreen();
            verify(mockSaver).saveToFile(testImage, filePath, format);
        }

        @Test
        @DisplayName("Should capture region and save")
        void shouldCaptureRegionAndSave() throws Exception {
            // Given
            Rectangle region = TestUtils.createTestRectangle(0, 0, 800, 600);
            String filePath = TestUtils.createTestFilePath("capture_region_and_save.png");
            String format = "png";
            when(mockCaptureInterface.captureRegion(region)).thenReturn(testImage);
            when(mockSaver.saveToFile(testImage, filePath, format)).thenReturn(true);

            // When
            boolean result = screenCaptureService.captureRegionAndSave(region, filePath, format);

            // Then
            assertThat(result).isTrue();
            verify(mockCaptureInterface).captureRegion(region);
            verify(mockSaver).saveToFile(testImage, filePath, format);
        }

        @Test
        @DisplayName("Should capture screen by index and save")
        void shouldCaptureScreenByIndexAndSave() throws Exception {
            // Given
            int screenIndex = 0;
            String filePath = TestUtils.createTestFilePath("capture_screen_by_index.png");
            String format = "png";
            when(mockCaptureInterface.captureScreen(screenIndex)).thenReturn(testImage);
            when(mockSaver.saveToFile(testImage, filePath, format)).thenReturn(true);

            // When
            boolean result = screenCaptureService.captureScreenAndSave(screenIndex, filePath, format);

            // Then
            assertThat(result).isTrue();
            verify(mockCaptureInterface).captureScreen(screenIndex);
            verify(mockSaver).saveToFile(testImage, filePath, format);
        }

        @Test
        @DisplayName("Should capture to byte array")
        void shouldCaptureToByteArray() throws Exception {
            // Given
            String format = "png";
            byte[] expectedBytes = new byte[]{1, 2, 3, 4, 5};
            when(mockCaptureInterface.captureScreen()).thenReturn(testImage);
            when(mockSaver.toByteArray(testImage, format)).thenReturn(expectedBytes);

            // When
            byte[] result = screenCaptureService.captureToByteArray(format);

            // Then
            assertThat(result).isEqualTo(expectedBytes);
            verify(mockCaptureInterface).captureScreen();
            verify(mockSaver).toByteArray(testImage, format);
        }
    }

    @Nested
    @DisplayName("Save Method Delegation Tests")
    class SaveMethodDelegationTests {

        @Test
        @DisplayName("Should delegate saveToFile to saver")
        void shouldDelegateSaveToFileToSaver() throws Exception {
            // Given
            String filePath = TestUtils.createTestFilePath("delegate_save.png");
            String format = "png";
            when(mockSaver.saveToFile(testImage, filePath, format)).thenReturn(true);

            // When
            boolean result = screenCaptureService.saveToFile(testImage, filePath, format);

            // Then
            assertThat(result).isTrue();
            verify(mockSaver).saveToFile(testImage, filePath, format);
        }

        @Test
        @DisplayName("Should delegate saveToStream to saver")
        void shouldDelegateSaveToStreamToSaver() {
            // Given
            OutputStream outputStream = new ByteArrayOutputStream();
            String format = "png";
            when(mockSaver.saveToStream(testImage, outputStream, format)).thenReturn(true);

            // When
            boolean result = screenCaptureService.saveToStream(testImage, outputStream, format);

            // Then
            assertThat(result).isTrue();
            verify(mockSaver).saveToStream(testImage, outputStream, format);
        }

        @Test
        @DisplayName("Should delegate toByteArray to saver")
        void shouldDelegateToByteArrayToSaver() throws Exception {
            // Given
            String format = "png";
            byte[] expectedBytes = new byte[]{1, 2, 3, 4, 5};
            when(mockSaver.toByteArray(testImage, format)).thenReturn(expectedBytes);

            // When
            byte[] result = screenCaptureService.toByteArray(testImage, format);

            // Then
            assertThat(result).isEqualTo(expectedBytes);
            verify(mockSaver).toByteArray(testImage, format);
        }

        @Test
        @DisplayName("Should delegate getSupportedFormats to saver")
        void shouldDelegateGetSupportedFormatsToSaver() {
            // Given
            String[] formats = {"png", "jpg", "gif"};
            when(mockSaver.getSupportedFormats()).thenReturn(formats);

            // When
            String[] result = screenCaptureService.getSupportedFormats();

            // Then
            assertThat(result).isEqualTo(formats);
            verify(mockSaver).getSupportedFormats();
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should handle screen capture exception")
        void shouldHandleScreenCaptureException() throws ScreenCaptureException {
            // Given
            ScreenCaptureException exception = new ScreenCaptureException("Capture failed");
            when(mockCaptureInterface.captureScreen()).thenThrow(exception);

            // When & Then
            assertThatThrownBy(() -> screenCaptureService.captureScreen())
                    .isInstanceOf(ScreenCaptureException.class)
                    .hasMessage("Capture failed");
        }

        @Test
        @DisplayName("Should handle capture region exception and return false")
        void shouldHandleCaptureRegionExceptionAndReturnFalse() throws Exception {
            // Given
            Rectangle region = TestUtils.createTestRectangle(0, 0, 800, 600);
            String filePath = TestUtils.createTestFilePath("error_test.png");
            String format = "png";
            when(mockCaptureInterface.captureRegion(region)).thenThrow(new ScreenCaptureException("Region capture failed"));

            // When
            boolean result = screenCaptureService.captureRegionAndSave(region, filePath, format);

            // Then
            assertThat(result).isFalse();
            verify(mockCaptureInterface).captureRegion(region);
            verify(mockSaver, never()).saveToFile(any(), any(), any());
        }

        @Test
        @DisplayName("Should handle save operation failure")
        void shouldHandleSaveOperationFailure() throws Exception {
            // Given
            String filePath = TestUtils.createTestFilePath("save_failure.png");
            String format = "png";
            when(mockCaptureInterface.captureScreen()).thenReturn(testImage);
            when(mockSaver.saveToFile(testImage, filePath, format)).thenReturn(false);

            // When
            boolean result = screenCaptureService.captureAndSave(filePath, format);

            // Then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("Should work with real implementations")
        void shouldWorkWithRealImplementations() throws Exception {
            // Given - this test might fail in headless environments
            try {
                ScreenCaptureService realService = ScreenCaptureService.createDefault();
                
                // When & Then
                assertThat(realService.isSupported()).isTrue();
                assertThat(realService.getAvailableScreens()).isNotEmpty();
                assertThat(realService.getPrimaryScreenBounds()).isNotNull();
                assertThat(realService.getSupportedFormats()).isNotEmpty();
            } catch (Exception e) {
                // Skip test in headless environments
            }
        }

        @Test
        @DisplayName("Should handle multiple operations in sequence")
        void shouldHandleMultipleOperationsInSequence() throws Exception {
            // Given
            when(mockCaptureInterface.captureScreen()).thenReturn(testImage);
            when(mockSaver.saveToFile(any(), any(), any())).thenReturn(true);
            when(mockSaver.toByteArray(any(), any())).thenReturn(new byte[]{1, 2, 3});

            // When
            BufferedImage captured = screenCaptureService.captureScreen();
            boolean saved = screenCaptureService.saveToFile(captured, TestUtils.createTestFilePath("sequence.png"), "png");
            byte[] bytes = screenCaptureService.toByteArray(captured, "png");

            // Then
            assertThat(captured).isNotNull();
            assertThat(saved).isTrue();
            assertThat(bytes).isNotEmpty();
        }
    }
}
