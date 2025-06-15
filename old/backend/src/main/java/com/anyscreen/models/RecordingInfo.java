package com.anyscreen.models;

import lombok.Data;

@Data
public class RecordingInfo {
    private Boolean isRecording;
    private Integer bitRate;
    private Integer frameRate;
    private Integer height;
    private Integer videoCodec;
    private Integer width;
    private String format;
    private String outputFileName;
    private Integer screenIndex;

        public RecordingInfo() {
            this(false, 30, 720, 0, 1280, "mp4", "recording.mp4");
        }
        
        
        public RecordingInfo(Boolean isRecording, Integer frameRate, Integer height, Integer videoCodec, Integer width, String format, String outputFileName, Integer screenIndex) {
            this.isRecording = isRecording;
            this.frameRate = frameRate;
            this.height = height;
            this.bitRate = 2000000;
            this.videoCodec = videoCodec;
            this.width = width;
            this.format = format;
            this.outputFileName = outputFileName;
            this.screenIndex = screenIndex;
        }
        public RecordingInfo(Boolean isRecording, Integer frameRate, Integer height, Integer videoCodec, Integer width, String format, String outputFileName) {
            this.isRecording = isRecording;
            this.frameRate = frameRate;
            this.height = height;
            this.bitRate = 2000000;
            this.videoCodec = videoCodec;
            this.width = width;
            this.format = format;
            this.outputFileName = outputFileName;
            this.screenIndex = 0;
        }
        private RecordingInfo(Builder builder) {
            this(false, builder.frameRate, builder.height, builder.videoCodec, builder.width, builder.format, builder.outputFileName, builder.screenIndex);
            this.bitRate = builder.bitRate;
        }

    
    @Override
    public String toString() {
        return "RecordingInfo{" +
                "isRecording=" + isRecording +
                ", bitRate=" + bitRate +
                ", frameRate=" + frameRate +
                ", height=" + height +
                ", videoCodec=" + videoCodec +
                ", width=" + width +
                ", format='" + format + '\'' +
                ", outputFileName='" + outputFileName + '\'' +
                '}';
    }

    public static class Builder {
        private Integer frameRate = 30;
        private Integer height = 720;
        private Integer width = 1280;
        private Integer videoCodec = 0;
        private Integer bitRate = 2000000;
        private String format = "mp4";
        private String outputFileName = "recording.mp4";
        private Integer screenIndex = 0;

        public Builder frameRate(Integer frameRate){
            this.frameRate = frameRate;
            return this;
        }
        public Builder resolution(Integer width, Integer height) {
            this.width = width;
            this.height = height;
            return this;
        }
        public Builder resolution(Double width, Double height) {
            this.width = (int)Math.round(width);
            this.height = (int)Math.round(height);
            return this;
        }
        public Builder videoCodec(Integer videoCodec){
            this.videoCodec = videoCodec;
            return this;
        }
        public Builder bitRate(Integer bitRate){
            this.bitRate = bitRate;
            return this;
        }
        public Builder format(String format) {
            this.format = format;
            return this;
        }
        
        public Builder outputFile(String fileName) {
            this.outputFileName = fileName;
            return this;
        }
        
        public Builder screenIndex(Integer screenIndex){
            this.screenIndex = screenIndex;
            return this;
        }

        public RecordingInfo build() {
            return new RecordingInfo(this);
        }
    }
}
