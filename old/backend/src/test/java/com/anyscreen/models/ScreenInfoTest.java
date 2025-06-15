package com.anyscreen.models;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.anyscreen.utils.TestUtils;

import static org.assertj.core.api.Assertions.*;

import java.awt.Rectangle;
import java.util.List;

/**
 * Comprehensive test suite for ScreenInfo model.
 * Tests all constructors, getters, and edge cases.
 */
class ScreenInfoTest {

    @Test
    @DisplayName("Should create ScreenInfo with all parameters")
    void shouldCreateScreenInfoWithAllParameters() {
        // Given
        int index = 1;
        Rectangle bounds = new Rectangle(0, 0, 1920, 1080);
        boolean isPrimary = true;
        String deviceId = "test-device-1";

        // When
        ScreenInfo screenInfo = new ScreenInfo(index, bounds, isPrimary, deviceId);

        // Then
        assertThat(screenInfo.getIndex()).isEqualTo(index);
        assertThat(screenInfo.getBounds()).isEqualTo(bounds);
        assertThat(screenInfo.isPrimary()).isEqualTo(isPrimary);
        assertThat(screenInfo.getDeviceId()).isEqualTo(deviceId);
    }

    @Test
    @DisplayName("Should create primary screen info")
    void shouldCreatePrimaryScreenInfo() {
        // Given
        ScreenInfo screenInfo = TestUtils.createMockScreenInfo(0, TestUtils.createDefaultTestRectangle(), true, "primary");

        // When & Then
        assertThat(screenInfo.isPrimary()).isTrue();
        assertThat(screenInfo.getIndex()).isZero();
        assertThat(screenInfo.getDeviceId()).isEqualTo("primary");
    }

    @Test
    @DisplayName("Should create secondary screen info")
    void shouldCreateSecondaryScreenInfo() {
        // Given
        Rectangle secondaryBounds = new Rectangle(1920, 0, 1366, 768);
        ScreenInfo screenInfo = TestUtils.createMockScreenInfo(1, secondaryBounds, false, "secondary");

        // When & Then
        assertThat(screenInfo.isPrimary()).isFalse();
        assertThat(screenInfo.getIndex()).isEqualTo(1);
        assertThat(screenInfo.getBounds()).isEqualTo(secondaryBounds);
        assertThat(screenInfo.getDeviceId()).isEqualTo("secondary");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 5, 10})
    @DisplayName("Should handle different screen indices")
    void shouldHandleDifferentScreenIndices(int index) {
        // Given
        ScreenInfo screenInfo = TestUtils.createMockScreenInfo(index, TestUtils.createDefaultTestRectangle(), false, "device-" + index);

        // When & Then
        assertThat(screenInfo.getIndex()).isEqualTo(index);
        assertThat(screenInfo.getDeviceId()).isEqualTo("device-" + index);
    }

    @ParameterizedTest
    @CsvSource({
        "0, 0, 1920, 1080",
        "1920, 0, 1366, 768",
        "0, 1080, 1280, 720",
        "-1920, 0, 1920, 1080"
    })
    @DisplayName("Should handle different screen bounds")
    void shouldHandleDifferentScreenBounds(int x, int y, int width, int height) {
        // Given
        Rectangle bounds = new Rectangle(x, y, width, height);
        ScreenInfo screenInfo = TestUtils.createMockScreenInfo(0, bounds, true, "test");

        // When & Then
        assertThat(screenInfo.getBounds()).isEqualTo(bounds);
        assertThat(screenInfo.getBounds().x).isEqualTo(x);
        assertThat(screenInfo.getBounds().y).isEqualTo(y);
        assertThat(screenInfo.getBounds().width).isEqualTo(width);
        assertThat(screenInfo.getBounds().height).isEqualTo(height);
    }

    @Test
    @DisplayName("Should have proper toString representation")
    void shouldHaveProperToStringRepresentation() {
        // Given
        Rectangle bounds = new Rectangle(0, 0, 1920, 1080);
        ScreenInfo screenInfo = new ScreenInfo(0, bounds, true, "primary-device");

        // When
        String toString = screenInfo.toString();

        // Then
        assertThat(toString).contains("ScreenInfo{");
        assertThat(toString).contains("index=0");
        assertThat(toString).contains("bounds=");
        assertThat(toString).contains("isPrimary=true");
        assertThat(toString).contains("deviceId='primary-device'");
    }

    @Test
    @DisplayName("Should handle null device ID")
    void shouldHandleNullDeviceId() {
        // Given
        ScreenInfo screenInfo = TestUtils.createMockScreenInfo(0, TestUtils.createDefaultTestRectangle(), true, null);

        // When & Then
        assertThat(screenInfo.getDeviceId()).isNull();
        assertThat(screenInfo.toString()).contains("deviceId='null'");
    }

    @Test
    @DisplayName("Should handle empty device ID")
    void shouldHandleEmptyDeviceId() {
        // Given
        ScreenInfo screenInfo = TestUtils.createMockScreenInfo(0, TestUtils.createDefaultTestRectangle(), true, "");

        // When & Then
        assertThat(screenInfo.getDeviceId()).isEmpty();
    }

    @Test
    @DisplayName("Should handle negative screen index")
    void shouldHandleNegativeScreenIndex() {
        // Given
        ScreenInfo screenInfo = TestUtils.createMockScreenInfo(-1, TestUtils.createDefaultTestRectangle(), false, "negative");

        // When & Then
        assertThat(screenInfo.getIndex()).isEqualTo(-1);
    }

    @Test
    @DisplayName("Should work with multiple screens setup")
    void shouldWorkWithMultipleScreensSetup() {
        // Given
        List<ScreenInfo> screens = TestUtils.createMultipleScreenInfos();

        // When & Then
        assertThat(screens).hasSize(3);
        assertThat(screens.get(0).isPrimary()).isTrue();
        assertThat(screens.get(1).isPrimary()).isFalse();
        assertThat(screens.get(2).isPrimary()).isFalse();
        
        assertThat(screens.get(0).getIndex()).isZero();
        assertThat(screens.get(1).getIndex()).isEqualTo(1);
        assertThat(screens.get(2).getIndex()).isEqualTo(2);
    }
}
