package com.anyscreen.interfaces;

import java.awt.image.BufferedImage;

import com.anyscreen.models.RecordingInfo;

public interface FrameEncoderInterface {
    void configure(RecordingInfo info);
    void start() throws Exception;
    void stop() throws Exception;
    void encodeFrame(BufferedImage image) throws Exception;
    boolean isRunning();
}