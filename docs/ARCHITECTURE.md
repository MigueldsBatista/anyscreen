# AnyScreen Architecture Documentation

## 🏗️ Overview

AnyScreen is a Java-based screen capture and recording application designed to support multi-monitor environments with video streaming capabilities. The project is currently in its foundational phase (Week 1-2 of the roadmap), implementing core screen capture functionality with plans for WebRTC integration.

## 📋 Project Goals

The application is being developed in phases toward a WebRTC-based screen sharing solution:

1. **Screen Capture** (Current) - Capture screenshots from multiple monitors
2. **Video Encoding** - Encode frames for video recording
3. **WebSocket Signaling** - Enable real-time communication
4. **WebRTC Streaming** - Live screen sharing to web clients
5. **REST API Control** - Start/stop recording and streaming
6. **Performance Optimization** - Multi-threading and resource management

## 🎯 Architecture Principles

The codebase follows several key architectural principles:

### Single Responsibility Principle (SRP)
Each interface focuses on a specific concern:
- `ScreenCaptureInterface` - Only captures screen content
- `ScreenCaptureSaverInterface` - Only handles image persistence
- `FrameEncoderInterface` - Only handles video encoding
- `LoggerInterface` - Only handles logging

### Dependency Injection
Services are injected through constructors, enabling easy testing and flexibility:
```java
public ScreenCaptureService(ScreenCaptureInterface captureInterface, ScreenCaptureSaverInterface saver)
```

### Interface Segregation
Small, focused interfaces that clients can implement partially without being forced to implement unused methods.

### Composition over Inheritance
Services compose functionality by combining multiple interfaces rather than relying on inheritance hierarchies.

## 📐 Design Patterns

### 1. **Strategy Pattern**
Used extensively for interchangeable implementations:

- **Screen Capture Strategy**: `RobotScreenCapture` (current), future implementations could include DirectX or native APIs
- **Encoding Strategy**: `JavaCVFrameEncoderAdapter` for video, `ImageIOScreenCaptureSaver` for images
- **Logging Strategy**: `Log4jAdapter` (current), easily swappable for other logging frameworks

```java
// Example: Different capture strategies
ScreenCaptureInterface robotCapture = new RobotScreenCapture();
ScreenCaptureInterface nativeCapture = new NativeScreenCapture(); // Future
```

### 2. **Adapter Pattern**
Used to integrate external libraries:

- **`Log4jAdapter`**: Adapts Log4j to the application's `LoggerInterface`
- **`JavaCVFrameEncoderAdapter`**: Adapts JavaCV/FFmpeg to the `FrameEncoderInterface`

### 3. **Template Method Pattern**
**`AbstractScreenRecorder`** defines the recording workflow:
```java
public void start() throws Exception {
    validateState();    // Template step
    configure();        // Template step  
    encoder.start();    // Template step
    recording.set(true);
}
```

### 4. **Builder Pattern**
**`RecordingInfo.Builder`** provides fluent configuration:
```java
RecordingInfo config = new RecordingInfo.Builder()
    .outputFile("video.mp4")
    .resolution(1920, 1080)
    .frameRate(30)
    .build();
```

### 5. **Singleton Pattern**
**`LoggerService`** provides application-wide logging with controlled initialization:
```java
LoggerService.initialize(new Log4jAdapter());
LoggerService.info("Application started");
```

### 6. **Facade Pattern**
**`ScreenCaptureService`** provides a simplified interface that combines capture and save operations:
```java
// Simple facade method
service.captureAndSave("screenshot.png", "png");

// Instead of manually combining:
BufferedImage image = captureInterface.captureScreen();
saver.saveToFile(image, "screenshot.png", "png");
```

### 7. **Composite Pattern** (Planned)
Future command handling for WebSocket operations:
```java
interface Command { void execute(); }
class StartStreamCommand implements Command { ... }
class StopStreamCommand implements Command { ... }
```

## 📦 Package Structure

```
com.anyscreen/
├── abstracts/              # Abstract base classes
│   └── AbstractScreenRecorder.java
├── exceptions/             # Custom exception hierarchy
│   ├── ScreenCaptureException.java
│   ├── RecordingException.java
│   └── EncodingException.java
├── implementations/        # Concrete implementations
│   ├── RobotScreenCapture.java      # Java Robot-based capture
│   ├── ImageIOScreenCaptureSaver.java # ImageIO-based saving
│   ├── JavaCVFrameEncoderAdapter.java # JavaCV video encoding
│   ├── JavaCVScreenRecorder.java     # JavaCV-based recorder
│   └── Log4jAdapter.java             # Log4j integration
├── interfaces/             # Contract definitions
│   ├── ScreenCaptureInterface.java
│   ├── ScreenCaptureSaverInterface.java
│   ├── FrameEncoderInterface.java
│   └── LoggerInterface.java
├── models/                 # Data models
│   ├── ScreenInfo.java     # Screen metadata
│   └── RecordingInfo.java  # Recording configuration
├── services/              # High-level business logic
│   ├── ScreenCaptureService.java    # Main capture facade
│   ├── RecordingService.java        # Recording orchestration
│   └── LoggerService.java           # Logging facade
└── App.java              # Application entry point
```

## 🔄 Data Flow

### Screen Capture Flow
```
User Request → ScreenCaptureService → ScreenCaptureInterface (Robot) → BufferedImage → ScreenCaptureSaverInterface (ImageIO) → File
```

### Recording Flow
```
RecordingService → ScheduledExecutorService → ScreenCaptureService → AbstractScreenRecorder → FrameEncoderInterface (JavaCV) → Video File
```

### Logging Flow
```
Any Component → LoggerService (Singleton) → LoggerInterface (Log4jAdapter) → Log4j → Console/File
```

## 🏛️ Layer Architecture

### **Presentation Layer**
- `App.java` - Main application entry point
- Demo methods for showcasing functionality

### **Service Layer** 
- `ScreenCaptureService` - High-level capture operations
- `RecordingService` - Recording orchestration and lifecycle
- `LoggerService` - Centralized logging

### **Domain Layer**
- `ScreenInfo` - Screen metadata model
- `RecordingInfo` - Recording configuration with Builder pattern
- Abstract classes defining core business workflows

### **Infrastructure Layer**
- `RobotScreenCapture` - OS-level screen capture via Java Robot
- `ImageIOScreenCaptureSaver` - File system persistence
- `JavaCVFrameEncoderAdapter` - Video encoding via FFmpeg
- `Log4jAdapter` - Logging framework integration

## 🔧 Technology Stack

### Core Technologies
- **Java 17** - Programming language
- **Maven** - Dependency management and build
- **Java AWT Robot** - Screen capture mechanism
- **JavaCV/FFmpeg** - Video encoding and processing
- **Log4j 2** - Logging framework
- **Lombok** - Code generation for getters/setters

### External Dependencies
```xml
<dependency>
    <groupId>org.bytedeco</groupId>
    <artifactId>javacv-platform</artifactId>
    <version>1.5.9</version>
</dependency>
<dependency>
    <groupId>org.apache.logging.log4j</groupId>
    <artifactId>log4j-core</artifactId>
    <version>2.6.1</version>
</dependency>
```

## 🎭 Error Handling Strategy

### Exception Hierarchy
- **`ScreenCaptureException`** - Screen capture failures (hardware, permissions)
- **`RecordingException`** - Recording lifecycle issues (start/stop failures)
- **`EncodingException`** - Video encoding problems (codec, format issues)

### Error Handling Patterns
- **Fail-Fast**: Invalid parameters cause immediate exceptions
- **Graceful Degradation**: Non-critical failures return false/null rather than throwing
- **Resource Cleanup**: Try-with-resources and explicit cleanup in finally blocks
- **Centralized Logging**: All errors logged through `LoggerService`

## 🚀 Future Architecture Extensions

### Planned Components (Weeks 3-8)

#### **WebSocket Signaling Module**
```java
// Planned structure
com.anyscreen.signaling/
├── WebSocketSignalingServer.java
├── SignalingMessage.java
└── ClientSession.java
```

#### **WebRTC Integration Module**
```java
com.anyscreen.webrtc/
├── WebRTCStreamer.java
├── MediaStreamTrack.java
└── PeerConnectionManager.java
```

#### **REST API Module**
```java
com.anyscreen.api/
├── ScreenCaptureController.java
├── RecordingController.java
└── StreamController.java
```

#### **Command Pattern for WebSocket**
```java
com.anyscreen.commands/
├── Command.java (interface)
├── StartStreamCommand.java
├── StopStreamCommand.java
└── CommandDispatcher.java
```

## 📊 Performance Considerations

### Current Performance Features
- **Atomic Operations**: Thread-safe recording state with `AtomicBoolean`
- **Scheduled Execution**: `ScheduledExecutorService` for frame capture timing
- **Resource Management**: Proper cleanup of encoders and file handles

### Planned Optimizations
- **Frame Buffer Pool**: Reuse BufferedImage objects to reduce GC pressure
- **Multi-threaded Capture**: Separate threads for capture, encoding, and streaming
- **Adaptive Quality**: Dynamic bitrate and frame rate based on system performance

## 🧪 Testing Strategy

### Current Testing
- Basic unit test structure in place (`AppTest.java`)
- Manual testing through demo methods in `App.java`

### Planned Testing
- **Unit Tests**: Interface implementations with mocked dependencies
- **Integration Tests**: End-to-end capture and recording workflows
- **Performance Tests**: Frame rate and memory usage benchmarks
- **Compatibility Tests**: Multi-platform screen capture validation

## 🔒 Security Considerations

### Current Security
- **Input Validation**: Null checks and parameter validation
- **Resource Bounds**: File path validation in `ImageIOScreenCaptureSaver`

### Future Security Measures
- **Permission Management**: Screen capture permissions handling
- **WebRTC Security**: DTLS encryption for media streams
- **API Authentication**: JWT tokens for REST API access
- **Rate Limiting**: Prevent abuse of capture/recording APIs

## 🎯 Architectural Quality Attributes

### **Maintainability**: ✅ High
- Clear separation of concerns
- Small, focused interfaces
- Comprehensive documentation

### **Testability**: ✅ High  
- Dependency injection
- Interface-based design
- Minimal static dependencies

### **Extensibility**: ✅ High
- Strategy pattern for swappable implementations
- Plugin-like architecture for new capture methods
- Abstract base classes for common functionality

### **Performance**: ⚠️ Moderate
- Single-threaded capture (current limitation)
- Synchronous encoding
- Room for optimization in frame processing

### **Scalability**: ⚠️ Planned
- Currently single-user
- WebRTC integration will enable multi-client streaming
- Concurrent session management planned

## 📋 Development Guidelines

### Adding New Components
1. **Define Interface First**: Create contract before implementation
2. **Follow Naming Conventions**: `*Interface` for contracts, `*Service` for facades
3. **Implement Strategy Pattern**: Make components swappable
4. **Add Comprehensive Tests**: Unit tests for all public methods
5. **Document Thoroughly**: Javadoc for all public APIs

### Code Quality Standards
- **SOLID Principles**: Single responsibility, open/closed, interface segregation
- **DRY Principle**: Avoid code duplication
- **Fail-Fast**: Validate inputs early
- **Resource Management**: Always clean up resources
- **Consistent Error Handling**: Use appropriate exception types

This architecture provides a solid foundation for the planned WebRTC screen sharing application while maintaining flexibility for future enhancements and modifications.
