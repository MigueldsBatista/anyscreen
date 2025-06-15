package com.anyscreen.models;

import java.awt.Rectangle;

public class ScreenInfo {
    private final int index;
    private final Rectangle bounds;
    private final boolean isPrimary;
    private final String deviceId;
    
    public ScreenInfo(int index, Rectangle bounds, boolean isPrimary, String deviceId) {
        this.index = index;
        this.bounds = bounds;
        this.isPrimary = isPrimary;
        this.deviceId = deviceId;
    }

    public int getIndex() {
        return index;
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public boolean isPrimary() {
        return isPrimary;
    }

    public String getDeviceId() {
        return deviceId;
    }

    @Override
    public String toString() {
        return String.format("ScreenInfo{index=%d, bounds=%s, isPrimary=%s, deviceId='%s'}", 
                           index, bounds, isPrimary, deviceId);
    }
}
