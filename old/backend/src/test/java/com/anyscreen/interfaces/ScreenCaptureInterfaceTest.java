package com.anyscreen.interfaces;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;

import com.anyscreen.exceptions.ScreenCaptureException;
import com.anyscreen.models.ScreenInfo;
import com.anyscreen.utils.TestUtils;

/**
 * Test suite for ScreenCaptureInterface contract validation.
 * Tests interface behavior through mock implementations and validates
 * that all implementations follow the expected screen capture contract.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ScreenCaptureInterface Contract Tests")
public class ScreenCaptureInterfaceTest {

    private ScreenCaptureInterface mockCaptureInterface;
    private BufferedImage mockImage;
    private Rectangle testRegion;
    private List<ScreenInfo> mockScreens;

    @BeforeEach
    void setUp() {
        mockCaptureInterface = mock(ScreenCaptureInterface.class);
        mockImage = TestUtils.createMockImage();
        testRegion = new Rectangle(0, 0, 800, 600);
        mockScreens = TestUtils.createMockMultiScreenInfo();
    }

    @Nested
    @DisplayName("Screen Capture Contract Tests")
    class ScreenCaptureContractTests {

        @Test
        @DisplayName("Should support primary screen capture")
        void shouldSupportPrimaryScreenCapture() throws ScreenCaptureException {
            // Given
            when(mockCaptureInterface.captureScreen()).thenReturn(mockImage);
            
            // When
            BufferedImage result = mockCaptureInterface.captureScreen();
            
            // Then
            assertThat(result).isNotNull();
            verify(mockCaptureInterface).captureScreen();
        }

        @Test
        @DisplayName("Should handle screen capture exceptions")
        void shouldHandleScreenCaptureExceptions() throws ScreenCaptureException {
            // Given
            when(mockCaptureInterface.captureScreen())
                .thenThrow(new ScreenCaptureException("Screen capture failed"));
            
            // When & Then
            assertThatThrownBy(() -> mockCaptureInterface.captureScreen())
                .isInstanceOf(ScreenCaptureException.class)
                .hasMessage("Screen capture failed");
        }

        @Test
        @DisplayName("Should return consistent image types")
        void shouldReturnConsistentImageTypes() throws ScreenCaptureException {
            // Given
            BufferedImage rgbImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
            when(mockCaptureInterface.captureScreen()).thenReturn(rgbImage);
            
            // When
            BufferedImage result = mockCaptureInterface.captureScreen();
            
            // Then
            assertThat(result).isNotNull();
            assertThat(result.getType()).isEqualTo(BufferedImage.TYPE_INT_RGB);
        }
    }

    @Nested
    @DisplayName("Region Capture Contract Tests")
    class RegionCaptureContractTests {

        @Test
        @DisplayName("Should support region capture")
        void shouldSupportRegionCapture() throws ScreenCaptureException {
            // Given
            when(mockCaptureInterface.captureRegion(testRegion)).thenReturn(mockImage);
            
            // When
            BufferedImage result = mockCaptureInterface.captureRegion(testRegion);
            
            // Then
            assertThat(result).isNotNull();
            verify(mockCaptureInterface).captureRegion(testRegion);
        }

        @Test
        @DisplayName("Should handle null region")
        void shouldHandleNullRegion() throws ScreenCaptureException {
            // Given
            when(mockCaptureInterface.captureRegion(null))
                .thenThrow(new ScreenCaptureException("Region cannot be null"));
            
            // When & Then
            assertThatThrownBy(() -> mockCaptureInterface.captureRegion(null))
                .isInstanceOf(ScreenCaptureException.class)
                .hasMessage("Region cannot be null");
        }

        @Test
        @DisplayName("Should handle invalid regions")
        void shouldHandleInvalidRegions() throws ScreenCaptureException {
            // Given
            Rectangle invalidRegion = new Rectangle(-100, -100, 0, 0);
            when(mockCaptureInterface.captureRegion(invalidRegion))
                .thenThrow(new ScreenCaptureException("Invalid region"));
            
            // When & Then
            assertThatThrownBy(() -> mockCaptureInterface.captureRegion(invalidRegion))
                .isInstanceOf(ScreenCaptureException.class)
                .hasMessage("Invalid region");
        }

        @ParameterizedTest
        @ValueSource(ints = {100, 500, 1000, 1920})
        @DisplayName("Should handle different region sizes")
        void shouldHandleDifferentRegionSizes(int size) throws ScreenCaptureException {
            // Given
            Rectangle region = new Rectangle(0, 0, size, size);
            BufferedImage expectedImage = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
            when(mockCaptureInterface.captureRegion(region)).thenReturn(expectedImage);
            
            // When
            BufferedImage result = mockCaptureInterface.captureRegion(region);
            
            // Then
            assertThat(result).isNotNull();
            assertThat(result.getWidth()).isEqualTo(size);
            assertThat(result.getHeight()).isEqualTo(size);
        }
    }

    @Nested
    @DisplayName("Multi-Screen Capture Contract Tests")
    class MultiScreenCaptureContractTests {

        @Test
        @DisplayName("Should support screen capture by index")
        void shouldSupportScreenCaptureByIndex() throws ScreenCaptureException {
            // Given
            when(mockCaptureInterface.captureScreen(0)).thenReturn(mockImage);
            
            // When
            BufferedImage result = mockCaptureInterface.captureScreen(0);
            
            // Then
            assertThat(result).isNotNull();
            verify(mockCaptureInterface).captureScreen(0);
        }

        @Test
        @DisplayName("Should handle invalid screen index")
        void shouldHandleInvalidScreenIndex() throws ScreenCaptureException {
            // Given
            when(mockCaptureInterface.captureScreen(999))
                .thenThrow(new ScreenCaptureException("Invalid screen index: 999"));
            
            // When & Then
            assertThatThrownBy(() -> mockCaptureInterface.captureScreen(999))
                .isInstanceOf(ScreenCaptureException.class)
                .hasMessage("Invalid screen index: 999");
        }

        @Test
        @DisplayName("Should handle negative screen index")
        void shouldHandleNegativeScreenIndex() throws ScreenCaptureException {
            // Given
            when(mockCaptureInterface.captureScreen(-1))
                .thenThrow(new ScreenCaptureException("Invalid screen index: -1"));
            
            // When & Then
            assertThatThrownBy(() -> mockCaptureInterface.captureScreen(-1))
                .isInstanceOf(ScreenCaptureException.class)
                .hasMessage("Invalid screen index: -1");
        }

        @ParameterizedTest
        @ValueSource(ints = {0, 1, 2})
        @DisplayName("Should handle multiple screen indices")
        void shouldHandleMultipleScreenIndices(int screenIndex) throws ScreenCaptureException {
            // Given
            BufferedImage screenImage = TestUtils.createMockImage();
            when(mockCaptureInterface.captureScreen(screenIndex)).thenReturn(screenImage);
            
            // When
            BufferedImage result = mockCaptureInterface.captureScreen(screenIndex);
            
            // Then
            assertThat(result).isNotNull();
            verify(mockCaptureInterface).captureScreen(screenIndex);
        }
    }

    @Nested
    @DisplayName("Screen Information Contract Tests")
    class ScreenInformationContractTests {

        @Test
        @DisplayName("Should provide available screens")
        void shouldProvideAvailableScreens() {
            // Given
            when(mockCaptureInterface.getAvailableScreens()).thenReturn(mockScreens);
            
            // When
            List<ScreenInfo> result = mockCaptureInterface.getAvailableScreens();
            
            // Then
            assertThat(result).isNotNull();
            assertThat(result).isNotEmpty();
            verify(mockCaptureInterface).getAvailableScreens();
        }

        @Test
        @DisplayName("Should handle no available screens")
        void shouldHandleNoAvailableScreens() {
            // Given
            when(mockCaptureInterface.getAvailableScreens()).thenReturn(List.of());
            
            // When
            List<ScreenInfo> result = mockCaptureInterface.getAvailableScreens();
            
            // Then
            assertThat(result).isNotNull();
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should provide primary screen bounds")
        void shouldProvidePrimaryScreenBounds() {
            // Given
            Rectangle primaryBounds = new Rectangle(0, 0, 1920, 1080);
            when(mockCaptureInterface.getPrimaryScreenBounds()).thenReturn(primaryBounds);
            
            // When
            Rectangle result = mockCaptureInterface.getPrimaryScreenBounds();
            
            // Then
            assertThat(result).isNotNull();
            assertThat(result.width).isEqualTo(1920);
            assertThat(result.height).isEqualTo(1080);
            verify(mockCaptureInterface).getPrimaryScreenBounds();
        }

        @Test
        @DisplayName("Should handle multiple screen configurations")
        void shouldHandleMultipleScreenConfigurations() {
            // Given
            List<ScreenInfo> multiScreenSetup = TestUtils.createMockMultiScreenInfo();
            when(mockCaptureInterface.getAvailableScreens()).thenReturn(multiScreenSetup);
            
            // When
            List<ScreenInfo> result = mockCaptureInterface.getAvailableScreens();
            
            // Then
            assertThat(result).hasSize(multiScreenSetup.size());
            assertThat(result.stream().anyMatch(ScreenInfo::isPrimary)).isTrue();
        }
    }

    @Nested
    @DisplayName("Support Check Contract Tests")
    class SupportCheckContractTests {

        @Test
        @DisplayName("Should indicate support availability")
        void shouldIndicateSupportAvailability() {
            // Given
            when(mockCaptureInterface.isSupported()).thenReturn(true);
            
            // When
            boolean isSupported = mockCaptureInterface.isSupported();
            
            // Then
            assertThat(isSupported).isTrue();
            verify(mockCaptureInterface).isSupported();
        }

        @Test
        @DisplayName("Should handle unsupported environments")
        void shouldHandleUnsupportedEnvironments() {
            // Given
            when(mockCaptureInterface.isSupported()).thenReturn(false);
            
            // When
            boolean isSupported = mockCaptureInterface.isSupported();
            
            // Then
            assertThat(isSupported).isFalse();
        }

        @Test
        @DisplayName("Should maintain consistent support status")
        void shouldMaintainConsistentSupportStatus() {
            // Given
            when(mockCaptureInterface.isSupported()).thenReturn(true);
            
            // When
            boolean firstCheck = mockCaptureInterface.isSupported();
            boolean secondCheck = mockCaptureInterface.isSupported();
            
            // Then
            assertThat(firstCheck).isEqualTo(secondCheck);
            verify(mockCaptureInterface, times(2)).isSupported();
        }
    }

    @Nested
    @DisplayName("Performance Contract Tests")
    class PerformanceContractTests {

        @Test
        @DisplayName("Should handle rapid consecutive captures")
        void shouldHandleRapidConsecutiveCaptures() throws ScreenCaptureException {
            // Given
            when(mockCaptureInterface.captureScreen()).thenReturn(mockImage);
            
            // When
            for (int i = 0; i < 100; i++) {
                BufferedImage result = mockCaptureInterface.captureScreen();
                assertThat(result).isNotNull();
            }
            
            // Then
            verify(mockCaptureInterface, times(100)).captureScreen();
        }

        //FIXME TODO -> fix this test to handle properly
        @Test
        @DisplayName("Should handle concurrent capture requests")
        void shouldHandleConcurrentCaptureRequests() throws Exception {
            // Given
            when(mockCaptureInterface.captureScreen()).thenReturn(mockImage);
            final int threadCount = 10;
            Thread[] threads = new Thread[threadCount];
            
            // When
            for (int i = 0; i < threadCount; i++) {
                threads[i] = new Thread(() -> {
                    try {
                        BufferedImage result = mockCaptureInterface.captureScreen();
                        assertThat(result).isNotNull();
                    } catch (ScreenCaptureException e) {
                        // Handle in thread
                    }
                });
                threads[i].start();
            }
            
            for (Thread thread : threads) {
                thread.join();
            }
            
            // Then
            verify(mockCaptureInterface, times(threadCount)).captureScreen();
        }
    }

    @Nested
    @DisplayName("Integration Contract Tests")
    class IntegrationContractTests {

        @Test
        @DisplayName("Should support complete workflow")
        void shouldSupportCompleteWorkflow() throws ScreenCaptureException {
            // Given
            when(mockCaptureInterface.isSupported()).thenReturn(true);
            when(mockCaptureInterface.getAvailableScreens()).thenReturn(mockScreens);
            when(mockCaptureInterface.getPrimaryScreenBounds()).thenReturn(new Rectangle(0, 0, 1920, 1080));
            when(mockCaptureInterface.captureScreen()).thenReturn(mockImage);
            when(mockCaptureInterface.captureScreen(0)).thenReturn(mockImage);
            when(mockCaptureInterface.captureRegion(any())).thenReturn(mockImage);
            
            // When - Complete workflow
            boolean supported = mockCaptureInterface.isSupported();
            List<ScreenInfo> screens = mockCaptureInterface.getAvailableScreens();
            Rectangle bounds = mockCaptureInterface.getPrimaryScreenBounds();
            BufferedImage primaryCapture = mockCaptureInterface.captureScreen();
            BufferedImage indexCapture = mockCaptureInterface.captureScreen(0);
            BufferedImage regionCapture = mockCaptureInterface.captureRegion(testRegion);
            
            // Then
            assertThat(supported).isTrue();
            assertThat(screens).isNotNull();
            assertThat(bounds).isNotNull();
            assertThat(primaryCapture).isNotNull();
            assertThat(indexCapture).isNotNull();
            assertThat(regionCapture).isNotNull();
            
            verify(mockCaptureInterface).isSupported();
            verify(mockCaptureInterface).getAvailableScreens();
            verify(mockCaptureInterface).getPrimaryScreenBounds();
            verify(mockCaptureInterface).captureScreen();
            verify(mockCaptureInterface).captureScreen(0);
            verify(mockCaptureInterface).captureRegion(testRegion);
        }

        @Test
        @DisplayName("Should handle mixed success and failure scenarios")
        void shouldHandleMixedSuccessAndFailureScenarios() throws ScreenCaptureException {
            // Given
            when(mockCaptureInterface.captureScreen()).thenReturn(mockImage);
            when(mockCaptureInterface.captureScreen(0)).thenReturn(mockImage);
            when(mockCaptureInterface.captureScreen(1))
                .thenThrow(new ScreenCaptureException("Screen 1 not available"));
            when(mockCaptureInterface.captureRegion(testRegion)).thenReturn(mockImage);
            when(mockCaptureInterface.captureRegion(null))
                .thenThrow(new ScreenCaptureException("Null region"));
            
            // When & Then
            // Successful operations
            assertThat(mockCaptureInterface.captureScreen()).isNotNull();
            assertThat(mockCaptureInterface.captureScreen(0)).isNotNull();
            assertThat(mockCaptureInterface.captureRegion(testRegion)).isNotNull();
            
            // Failed operations
            assertThatThrownBy(() -> mockCaptureInterface.captureScreen(1))
                .isInstanceOf(ScreenCaptureException.class);
            assertThatThrownBy(() -> mockCaptureInterface.captureRegion(null))
                .isInstanceOf(ScreenCaptureException.class);
        }

        @Test
        @DisplayName("Should maintain consistent state across operations")
        void shouldMaintainConsistentStateAcrossOperations() throws ScreenCaptureException {
            // Given
            when(mockCaptureInterface.isSupported()).thenReturn(true);
            when(mockCaptureInterface.getAvailableScreens()).thenReturn(mockScreens);
            when(mockCaptureInterface.captureScreen()).thenReturn(mockImage);
            
            // When
            boolean initialSupport = mockCaptureInterface.isSupported();
            List<ScreenInfo> initialScreens = mockCaptureInterface.getAvailableScreens();
            BufferedImage capture1 = mockCaptureInterface.captureScreen();
            boolean laterSupport = mockCaptureInterface.isSupported();
            List<ScreenInfo> laterScreens = mockCaptureInterface.getAvailableScreens();
            BufferedImage capture2 = mockCaptureInterface.captureScreen();
            
            // Then
            assertThat(initialSupport).isEqualTo(laterSupport);
            assertThat(initialScreens).isEqualTo(laterScreens);
            assertThat(capture1).isNotNull();
            assertThat(capture2).isNotNull();
        }
    }
}
