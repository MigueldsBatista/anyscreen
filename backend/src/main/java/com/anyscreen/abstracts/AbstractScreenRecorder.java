package com.anyscreen.abstracts;

import java.awt.image.BufferedImage;
import java.util.concurrent.atomic.AtomicBoolean;

import com.anyscreen.interfaces.FrameEncoderInterface;
import com.anyscreen.models.RecordingInfo;
import com.anyscreen.services.LoggerService;

import lombok.Getter;

@Getter
public abstract class AbstractScreenRecorder {
    protected RecordingInfo recordingInfo;
    protected FrameEncoderInterface encoder;
    protected final AtomicBoolean recording = new AtomicBoolean(false);

    public AbstractScreenRecorder(FrameEncoderInterface encoder, RecordingInfo recordingInfo) {
        this.encoder = encoder;
        this.recordingInfo = recordingInfo;
    }

    public void start() throws Exception{
        validateState();
        configure();
        encoder.start();
        recording.set(true);
        LoggerService.info("Screen recorder started");
    }
    
    public void stop() throws Exception{
        recording.set(false);
        if (encoder != null && encoder.isRunning()) {
            encoder.stop();
        }
        LoggerService.info("Screen recorder stopped");
    }
    
    public boolean isRecording() {
        return recording.get();
    }
    
    public RecordingInfo getRecordingInfo(){
        return recordingInfo;
    }

    public void setRecordingInfo(RecordingInfo recordingInfo) {
        this.recordingInfo = recordingInfo;
    }

    public void recordFrame(BufferedImage image) throws Exception{
        if (!recording.get()) return;
        
        if (encoder != null && encoder.isRunning()) {
            encoder.encodeFrame(image);
        }
    }

    public void configure() {
        encoder.configure(recordingInfo);
    }
    
    protected void validateState(){

        if (recordingInfo == null) {
            throw new IllegalStateException("RecordingInfo not set");
        }
        if (encoder == null) {
            throw new IllegalStateException("Encoder not set");
        }
    }
}