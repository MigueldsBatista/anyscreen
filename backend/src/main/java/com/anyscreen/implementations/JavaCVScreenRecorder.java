package com.anyscreen.implementations;

import org.bytedeco.javacv.FFmpegFrameRecorder;
import com.anyscreen.abstracts.AbstractScreenRecorder;
import com.anyscreen.interfaces.FrameEncoderInterface;
import com.anyscreen.models.RecordingInfo;

public class JavaCVScreenRecorder extends AbstractScreenRecorder {
    protected FFmpegFrameRecorder recorder;

    public JavaCVScreenRecorder(RecordingInfo recordingInfo, FrameEncoderInterface encoder) {
        super(encoder, recordingInfo);
        initializeRecorder();
    }
    
    private void initializeRecorder() {
        this.recorder = new FFmpegFrameRecorder(
            recordingInfo.getOutputFileName(),
            recordingInfo.getWidth(),
            recordingInfo.getHeight()
        );
        
        recorder.setFrameRate(recordingInfo.getFrameRate());
        recorder.setFormat(recordingInfo.getFormat());
        recorder.setVideoCodec(recordingInfo.getVideoCodec());
        recorder.setVideoBitrate(recordingInfo.getBitRate());
    }

}