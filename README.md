project-root/
├── backend/
│   ├── src/
│   │   ├── main/java/com/yourapp/
│   │   │   ├── capture/             # Screen capture logic
│   │   │   ├── encoding/            # Frame encoding/compression logic
│   │   │   ├── signaling/           # WebSocket signaling server
│   │   │   ├── webrtc/              # Integration with WebRTC/GStreamer
│   │   │   ├── api/                 # REST controllers
│   │   │   ├── service/             # Application logic
│   │   │   ├── model/               # Domain models/entities
│   │   │   ├── config/              # Configuration (logging, WebSocket, etc)
│   │   │   └── App.java             # Main entry point
│   │   └── resources/
│   │       ├── application.properties
│   │       └── logback.xml
│   └── pom.xml                      # Maven config
│
├── frontend/
│   ├── public/
│   ├── src/
│   │   ├── components/
│   │   ├── views/
│   │   ├── App.vue
│   │   └── main.js
│   ├── package.json
│   └── vite.config.js
│
└── docs/                            # Specs, diagrams, flowcharts

## 🧱 Entities and Responsibilities

| Module | Description |
|--------|-------------|
| ScreenCaptureService | Captures screen frames with Robot, sends to encoder. |
| FrameEncoder | Encodes to MJPEG or H.264 (via external tool). |
| WebSocketSignalingServer | Accepts signaling from frontend (offer/answer/ICE). |
| WebRTCStreamer | Sends media stream to peer (via GStreamer, FFmpeg or RTP). |
| StreamSession | Tracks connected clients, stream state. |
| CaptureController | REST/WebSocket controller to start/stop streams. |

## 🧠 Design Patterns to Apply

### 1. Strategy Pattern
Use for interchangeable encoding strategies:

```java
public interface FrameEncoder {
    byte[] encode(BufferedImage frame);
}
public class MJPEGEncoder implements FrameEncoder { ... }
public class H264Encoder implements FrameEncoder { ... }
```

### 2. Observer Pattern
Used to notify listeners when a new frame is captured:

```java
interface FrameListener {
    void onFrame(byte[] encodedFrame);
}
```

### 3. Command Pattern
Used for handling WebSocket commands (start, stop, etc.)

```java
interface Command {
    void execute();
}
class StartStreamCommand implements Command { ... }
class StopStreamCommand implements Command { ... }
```

### 4. Factory Pattern
To instantiate different WebRTC or encoder configurations.

### 5. Builder Pattern
For building complex SDP/WebRTC session configs or GStreamer pipelines.

## 🔁 Integration Points

| Component | How it talks | Notes |
|-----------|-------------|-------|
| Java ↔ Frontend | WebSocket | For signaling |
| Java ↔ Media Server | RTP / GStreamer / FFmpeg | Encode/send frames |
| Frontend ↔ Java | WebSocket + REST | Control and feedback |
| Frontend (Vue) | WebRTC API | Receives media |

## 📌 Example Class Mapping

- **ScreenCaptureService**: ⬅️ Singleton (or managed by DI)
- **WebSocketHandler**: ⬅️ Observer + Command
- **StreamSession**: ⬅️ Aggregate root for user stream state
- **StreamController**: ⬅️ REST interface
- **GStreamerPipelineBuilder**: ⬅️ Builder

## ✅ Optional Enhancements

- Use SLF4J + Logback for structured logs
- Use Jackson for JSON parsing
- Add unit tests for Command, Service, and Encoder modules
- **Future**: replace GStreamer with native WebRTC media track via JNI if needed

If you want, I can generate some boilerplate for this structure (ex: the Command dispatcher, WebSocket handler skeleton, etc). Want that next?