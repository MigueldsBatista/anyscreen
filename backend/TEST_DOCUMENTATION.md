# AnyScreen Backend Test Documentation

This document provides a comprehensive overview of all test cases implemented for the AnyScreen backend Java application. The test suite covers models, services, implementations, abstracts, interfaces, exceptions, and the main application class.

## Test Suite Overview

- **Total Test Files**: 18
- **Testing Framework**: JUnit 5 (Jupiter)
- **Mocking Framework**: Mockito 5.11.0
- **Assertion Library**: AssertJ 3.25.3
- **Test Architecture**: Nested test classes with descriptive naming

## Test Coverage by Package

### 1. Main Application Tests

#### AppTest.java
**Purpose**: Tests the main application entry point, CLI interactions, and overall workflow.

| Test Class | Test Method | Description | Expected Behavior |
|------------|-------------|-------------|-------------------|
| MainMethodTests | shouldStartApplicationSuccessfully | Tests main method execution | Application starts without errors |
| MainMethodTests | shouldHandleEmptyArgs | Tests main with no arguments | Displays help and exits gracefully |
| MainMethodTests | shouldHandleInvalidArgs | Tests main with invalid arguments | Shows error message and usage |
| ScreenSelectionTests | shouldDisplayAvailableScreens | Tests screen enumeration | Lists all available screens with details |
| ScreenSelectionTests | shouldHandleScreenSelection | Tests user screen selection | Correctly selects chosen screen |
| ScreenSelectionTests | shouldHandleInvalidSelection | Tests invalid screen choice | Shows error and re-prompts |
| RecordingWorkflowTests | shouldStartRecordingProcess | Tests recording initiation | Recording starts successfully |
| RecordingWorkflowTests | shouldStopRecordingOnInput | Tests recording termination | Recording stops on user input |
| RecordingWorkflowTests | shouldHandleRecordingErrors | Tests error scenarios | Graceful error handling |
| WaitForRecordingTests | shouldWaitForUserInput | Tests user input waiting | Blocks until input received |
| WaitForRecordingTests | shouldHandleInterruption | Tests interruption handling | Proper cleanup on interruption |

### 2. Model Tests

#### ScreenInfoTest.java
**Purpose**: Tests the ScreenInfo model class for screen information representation.

| Test Class | Test Method | Description | Expected Behavior |
|------------|-------------|-------------|-------------------|
| Basic Tests | shouldCreateScreenInfoWithAllParameters | Tests constructor with all params | Object created with correct values |
| Basic Tests | shouldCreatePrimaryScreenInfo | Tests primary screen creation | Primary flag set correctly |
| Basic Tests | shouldCreateSecondaryScreenInfo | Tests secondary screen creation | Secondary screen properties correct |
| Parameterized Tests | shouldHandleDifferentScreenIndices | Tests various screen indices | All indices handled correctly |
| Parameterized Tests | shouldHandleDifferentScreenBounds | Tests various screen dimensions | All bounds handled correctly |
| String Tests | shouldHaveProperToStringRepresentation | Tests toString method | Readable string representation |
| Edge Cases | shouldHandleNegativeIndices | Tests negative indices | Graceful handling of invalid indices |
| Edge Cases | shouldHandleNullBounds | Tests null bounds | Proper null handling |

#### RecordingInfoTest.java
**Purpose**: Tests the RecordingInfo model and its Builder pattern.

| Test Class | Test Method | Description | Expected Behavior |
|------------|-------------|-------------|-------------------|
| ConstructorTests | shouldCreateRecordingInfoWithRequiredFields | Tests basic constructor | Object created with defaults |
| ConstructorTests | shouldCreateRecordingInfoWithAllFields | Tests full constructor | All fields set correctly |
| BuilderTests | shouldBuildWithDefaults | Tests builder defaults | Default values applied |
| BuilderTests | shouldBuildWithCustomValues | Tests builder customization | Custom values set correctly |
| BuilderTests | shouldChainBuilderMethods | Tests method chaining | Fluent interface works |
| ValidationTests | shouldValidateFrameRate | Tests frame rate validation | Valid ranges enforced |
| ValidationTests | shouldValidateResolution | Tests resolution validation | Valid dimensions enforced |
| UtilityTests | shouldProvideTestDataProviders | Tests data providers | Test data generated correctly |

### 3. Service Tests

#### LoggerServiceTest.java
**Purpose**: Tests the singleton LoggerService for application logging.

| Test Class | Test Method | Description | Expected Behavior |
|------------|-------------|-------------|-------------------|
| SingletonTests | shouldReturnSameInstance | Tests singleton pattern | Same instance returned |
| SingletonTests | shouldInitializeOnFirstCall | Tests lazy initialization | Service initialized correctly |
| ThreadSafetyTests | shouldBeThreadSafe | Tests concurrent access | Thread-safe singleton behavior |
| LoggingTests | shouldDelegateInfoLogging | Tests info logging | Logs delegated to adapter |
| LoggingTests | shouldDelegateErrorLogging | Tests error logging | Error logs handled correctly |
| LoggingTests | shouldDelegateDebugLogging | Tests debug logging | Debug logs processed |
| StateTests | shouldResetInstanceCorrectly | Tests reset functionality | Instance reset for testing |

#### ScreenCaptureServiceTest.java
**Purpose**: Tests the ScreenCaptureService for screen capture operations.

| Test Class | Test Method | Description | Expected Behavior |
|------------|-------------|-------------|-------------------|
| InitializationTests | shouldInitializeWithDefaults | Tests default initialization | Service initialized with defaults |
| InitializationTests | shouldInitializeWithCustomCapture | Tests custom capture provider | Custom provider used |
| CaptureTests | shouldCaptureScreen | Tests screen capture | Screen captured successfully |
| CaptureTests | shouldCaptureRegion | Tests region capture | Specific region captured |
| CaptureTests | shouldHandleCaptureFailure | Tests capture errors | Errors handled gracefully |
| ScreenInfoTests | shouldGetAvailableScreens | Tests screen enumeration | Available screens listed |
| ScreenInfoTests | shouldGetPrimaryScreen | Tests primary screen detection | Primary screen identified |
| DelegationTests | shouldDelegateToProvider | Tests method delegation | Calls delegated to provider |

#### RecordingServiceTest.java
**Purpose**: Tests the RecordingService for recording lifecycle management.

| Test Class | Test Method | Description | Expected Behavior |
|------------|-------------|-------------|-------------------|
| RecordingLifecycleTests | shouldStartRecording | Tests recording start | Recording starts successfully |
| RecordingLifecycleTests | shouldStopRecording | Tests recording stop | Recording stops cleanly |
| RecordingLifecycleTests | shouldPauseRecording | Tests recording pause | Recording paused correctly |
| RecordingLifecycleTests | shouldResumeRecording | Tests recording resume | Recording resumed properly |
| FrameCaptureTests | shouldCaptureFrames | Tests frame capture | Frames captured at intervals |
| FrameCaptureTests | shouldHandleFrameErrors | Tests frame capture errors | Errors handled in capture loop |
| SchedulerTests | shouldManageScheduler | Tests scheduler lifecycle | Scheduler managed correctly |
| ErrorHandlingTests | shouldHandleServiceErrors | Tests service errors | Service errors handled gracefully |

### 4. Implementation Tests

#### ImageIOScreenCaptureSaverTest.java
**Purpose**: Tests the ImageIOScreenCaptureSaver for image saving operations.

| Test Class | Test Method | Description | Expected Behavior |
|------------|-------------|-------------|-------------------|
| SaveToFileTests | shouldSaveImageToFileWithDifferentFormats | Tests various image formats | All formats saved correctly |
| SaveToFileTests | shouldCreateParentDirectoriesWhenTheyDontExist | Tests directory creation | Parent directories created |
| SaveToFileTests | shouldHandleCaseInsensitiveFormats | Tests format case handling | Case insensitive format support |
| SaveToFileTests | shouldReturnFalseForNullImage | Tests null image handling | False returned for null input |
| SaveToFileTests | shouldHandleDifferentImageSizes | Tests various image sizes | All sizes handled correctly |
| SaveToFileTests | shouldOverwriteExistingFiles | Tests file overwriting | Existing files overwritten |
| SaveToStreamTests | shouldSaveImageToOutputStream | Tests stream saving | Image saved to stream |
| SaveToStreamTests | shouldSaveToStreamWithDifferentFormats | Tests stream format support | All formats work with streams |
| SaveToStreamTests | shouldReturnFalseForNullImageInStream | Tests null handling in streams | Null inputs handled in streams |
| ToByteArrayTests | shouldConvertImageToByteArray | Tests byte array conversion | Image converted to bytes |
| ToByteArrayTests | shouldProduceDifferentByteArraysForDifferentFormats | Tests format differences | Different formats produce different bytes |
| SupportedFormatsTests | shouldReturnNonEmptySupportedFormatsArray | Tests format listing | Supported formats listed |
| SupportedFormatsTests | shouldIncludeCommonImageFormats | Tests common format support | Common formats included |
| ErrorHandlingTests | shouldHandleInvalidFilePathGracefully | Tests invalid path handling | Invalid paths handled gracefully |
| ErrorHandlingTests | shouldHandleUnsupportedFormatGracefully | Tests unsupported formats | Unsupported formats handled |
| IntegrationTests | shouldCompleteFullSaveAndVerifyCycle | Tests complete workflow | Full save cycle works |

#### RobotScreenCaptureTest.java
**Purpose**: Tests the RobotScreenCapture implementation using Java Robot API.

| Test Class | Test Method | Description | Expected Behavior |
|------------|-------------|-------------|-------------------|
| ConstructorTests | shouldCreateRobotScreenCaptureSuccessfully | Tests successful creation | Object created successfully |
| ConstructorTests | shouldHandleAWTExceptionDuringRobotCreation | Tests Robot creation failure | AWTException handled gracefully |
| ScreenCaptureTests | shouldCapturePrimaryScreenSuccessfully | Tests primary screen capture | Primary screen captured |
| ScreenCaptureTests | shouldCaptureSpecificScreenByIndex | Tests indexed screen capture | Specific screen captured by index |
| ScreenCaptureTests | shouldHandleInvalidScreenIndex | Tests invalid index handling | Invalid indices handled |
| ScreenCaptureTests | shouldCaptureSpecificRegionSuccessfully | Tests region capture | Specific regions captured |
| ScreenCaptureTests | shouldHandleNullRegion | Tests null region handling | Null regions handled |
| ScreenCaptureTests | shouldHandleZeroSizedRegion | Tests zero-size regions | Zero-size regions handled |
| ScreenCaptureTests | shouldHandleNegativeRegionCoordinates | Tests negative coordinates | Negative coordinates handled |
| ScreenInformationTests | shouldGetAvailableScreensInformation | Tests screen info retrieval | Screen info retrieved correctly |
| ScreenInformationTests | shouldGetPrimaryScreenBounds | Tests primary screen bounds | Primary bounds retrieved |
| ScreenInformationTests | shouldReportSupportStatusCorrectly | Tests support status | Support status reported correctly |
| ErrorHandlingTests | shouldProvideMeaningfulErrorMessages | Tests error messaging | Meaningful errors provided |
| ErrorHandlingTests | shouldHandleExtremeScreenIndicesGracefully | Tests extreme indices | Extreme values handled |

#### JavaCVFrameEncoderAdapterTest.java
**Purpose**: Tests the JavaCV frame encoder adapter for video encoding.

| Test Class | Test Method | Description | Expected Behavior |
|------------|-------------|-------------|-------------------|
| InitializationTests | shouldInitializeEncoderSuccessfully | Tests encoder initialization | Encoder initialized correctly |
| InitializationTests | shouldHandleInvalidVideoFormat | Tests invalid format handling | Invalid formats handled |
| InitializationTests | shouldSetupEncoderWithCustomSettings | Tests custom settings | Custom settings applied |
| EncodingTests | shouldEncodeFrameSuccessfully | Tests frame encoding | Frames encoded successfully |
| EncodingTests | shouldEncodeMultipleFrames | Tests multiple frame encoding | Multiple frames encoded |
| EncodingTests | shouldHandleNullFrame | Tests null frame handling | Null frames handled |
| EncodingTests | shouldHandleInvalidFrameFormat | Tests invalid frame formats | Invalid formats handled |
| VideoOperationsTests | shouldStartVideoRecording | Tests video recording start | Recording starts correctly |
| VideoOperationsTests | shouldStopVideoRecording | Tests video recording stop | Recording stops cleanly |
| VideoOperationsTests | shouldFinalizeVideoFile | Tests video finalization | Video file finalized |
| ConfigurationTests | shouldConfigureVideoSettings | Tests video configuration | Settings configured correctly |
| ConfigurationTests | shouldValidateEncoderSettings | Tests setting validation | Settings validated |
| ErrorHandlingTests | shouldHandleEncodingErrors | Tests encoding error handling | Encoding errors handled |

#### JavaCVScreenRecorderTest.java
**Purpose**: Tests the JavaCV screen recorder implementation.

| Test Class | Test Method | Description | Expected Behavior |
|------------|-------------|-------------|-------------------|
| RecorderLifecycleTests | shouldInitializeRecorderSuccessfully | Tests recorder initialization | Recorder initialized correctly |
| RecorderLifecycleTests | shouldStartRecordingSuccessfully | Tests recording start | Recording starts successfully |
| RecorderLifecycleTests | shouldStopRecordingSuccessfully | Tests recording stop | Recording stops cleanly |
| RecorderLifecycleTests | shouldPauseAndResumeRecording | Tests pause/resume | Pause/resume works correctly |
| ConfigurationTests | shouldConfigureWithDefaultSettings | Tests default configuration | Default settings applied |
| ConfigurationTests | shouldConfigureWithCustomSettings | Tests custom configuration | Custom settings applied |
| ConfigurationTests | shouldValidateRecordingSettings | Tests setting validation | Settings validated correctly |
| FrameProcessingTests | shouldProcessFramesCorrectly | Tests frame processing | Frames processed correctly |
| FrameProcessingTests | shouldHandleFrameDrops | Tests frame drop handling | Frame drops handled |
| FrameProcessingTests | shouldMaintainFrameRate | Tests frame rate maintenance | Frame rate maintained |
| ErrorHandlingTests | shouldHandleRecordingErrors | Tests recording error handling | Recording errors handled |
| ErrorHandlingTests | shouldRecoverFromErrors | Tests error recovery | Recovery from errors works |
| ResourceManagementTests | shouldCleanupResourcesCorrectly | Tests resource cleanup | Resources cleaned up properly |

#### Log4jAdapterTest.java
**Purpose**: Tests the Log4j logging adapter implementation.

| Test Class | Test Method | Description | Expected Behavior |
|------------|-------------|-------------|-------------------|
| InitializationTests | shouldInitializeWithDefaultLogger | Tests default initialization | Default logger initialized |
| InitializationTests | shouldInitializeWithCustomLogger | Tests custom logger | Custom logger used |
| LoggingTests | shouldLogInfoMessages | Tests info logging | Info messages logged |
| LoggingTests | shouldLogErrorMessages | Tests error logging | Error messages logged |
| LoggingTests | shouldLogDebugMessages | Tests debug logging | Debug messages logged |
| LoggingTests | shouldLogWarningMessages | Tests warning logging | Warning messages logged |
| FormatTests | shouldFormatMessagesCorrectly | Tests message formatting | Messages formatted correctly |
| FormatTests | shouldHandleNullMessages | Tests null message handling | Null messages handled |
| FormatTests | shouldHandleSpecialCharacters | Tests special characters | Special characters handled |
| LevelTests | shouldRespectLoggingLevels | Tests logging levels | Logging levels respected |
| LevelTests | shouldFilterMessagesByLevel | Tests level filtering | Messages filtered by level |
| ExceptionTests | shouldLogExceptionsWithStackTrace | Tests exception logging | Exceptions logged with stack trace |
| ExceptionTests | shouldHandleNullExceptions | Tests null exception handling | Null exceptions handled |

### 5. Abstract Class Tests

#### AbstractScreenRecorderTest.java
**Purpose**: Tests the abstract screen recorder base functionality.

| Test Class | Test Method | Description | Expected Behavior |
|------------|-------------|-------------|-------------------|
| ConfigurationTests | shouldSetDefaultConfiguration | Tests default configuration | Default config set correctly |
| ConfigurationTests | shouldValidateConfiguration | Tests configuration validation | Invalid configs rejected |
| ConfigurationTests | shouldUpdateConfiguration | Tests configuration updates | Config updates applied |
| StateManagementTests | shouldTrackRecorderState | Tests state tracking | Recorder state tracked correctly |
| StateManagementTests | shouldHandleStateTransitions | Tests state transitions | Valid transitions allowed |
| StateManagementTests | shouldPreventInvalidTransitions | Tests invalid transitions | Invalid transitions prevented |
| TemplateMethodTests | shouldImplementTemplatePattern | Tests template method pattern | Template pattern implemented |
| TemplateMethodTests | shouldCallHookMethods | Tests hook method calls | Hook methods called correctly |
| HookMethodTests | shouldCallPreStartHook | Tests pre-start hook | Pre-start hook called |
| HookMethodTests | shouldCallPostStopHook | Tests post-stop hook | Post-stop hook called |
| HookMethodTests | shouldCallErrorHook | Tests error hook | Error hook called on errors |
| UtilityTests | shouldProvideUtilityMethods | Tests utility methods | Utility methods work correctly |

### 6. Interface Tests

#### ScreenCaptureInterfaceTest.java
**Purpose**: Tests the screen capture interface contract and mock implementations.

| Test Class | Test Method | Description | Expected Behavior |
|------------|-------------|-------------|-------------------|
| ContractTests | shouldDefineScreenCaptureContract | Tests interface contract | Contract properly defined |
| ContractTests | shouldRequireImplementationMethods | Tests required methods | All methods must be implemented |
| MockImplementationTests | shouldCreateMockImplementation | Tests mock creation | Mock implementation created |
| MockImplementationTests | shouldVerifyMethodCalls | Tests method call verification | Method calls verified |
| MockImplementationTests | shouldConfigureMockBehavior | Tests mock behavior | Mock behavior configured |
| IntegrationTests | shouldWorkWithRealImplementations | Tests real implementations | Real implementations work |
| IntegrationTests | shouldSupportPolymorphism | Tests polymorphic usage | Polymorphism supported |

#### FrameEncoderInterfaceTest.java
**Purpose**: Tests the frame encoder interface for video encoding contracts.

| Test Class | Test Method | Description | Expected Behavior |
|------------|-------------|-------------|-------------------|
| ContractTests | shouldDefineEncodingContract | Tests encoding contract | Encoding contract defined |
| ContractTests | shouldSpecifyEncodingMethods | Tests required encoding methods | Encoding methods specified |
| MockImplementationTests | shouldCreateEncoderMock | Tests encoder mock creation | Encoder mock created |
| MockImplementationTests | shouldVerifyEncodingCalls | Tests encoding call verification | Encoding calls verified |
| BehaviorTests | shouldConfigureEncodingBehavior | Tests encoding behavior | Encoding behavior configured |
| IntegrationTests | shouldIntegrateWithImplementations | Tests implementation integration | Integration works correctly |

#### LoggerInterfaceTest.java
**Purpose**: Tests the logger interface contract for logging implementations.

| Test Class | Test Method | Description | Expected Behavior |
|------------|-------------|-------------|-------------------|
| ContractTests | shouldDefineLoggingContract | Tests logging contract | Logging contract defined |
| ContractTests | shouldSpecifyLoggingMethods | Tests required logging methods | Logging methods specified |
| MockImplementationTests | shouldCreateLoggerMock | Tests logger mock creation | Logger mock created |
| MockImplementationTests | shouldVerifyLoggingCalls | Tests logging call verification | Logging calls verified |
| BehaviorTests | shouldConfigureLoggingBehavior | Tests logging behavior | Logging behavior configured |
| LevelTests | shouldSupportLoggingLevels | Tests logging level support | Logging levels supported |

#### ScreenCaptureSaverInterfaceTest.java
**Purpose**: Tests the screen capture saver interface for image saving contracts.

| Test Class | Test Method | Description | Expected Behavior |
|------------|-------------|-------------|-------------------|
| ContractTests | shouldDefineSavingContract | Tests saving contract | Saving contract defined |
| ContractTests | shouldSpecifySavingMethods | Tests required saving methods | Saving methods specified |
| MockImplementationTests | shouldCreateSaverMock | Tests saver mock creation | Saver mock created |
| MockImplementationTests | shouldVerifySavingCalls | Tests saving call verification | Saving calls verified |
| BehaviorTests | shouldConfigureSavingBehavior | Tests saving behavior | Saving behavior configured |
| FormatTests | shouldSupportMultipleFormats | Tests format support | Multiple formats supported |

### 7. Exception Tests

#### ExceptionClassesTest.java
**Purpose**: Tests all custom exception classes in the exception hierarchy.

| Test Class | Test Method | Description | Expected Behavior |
|------------|-------------|-------------|-------------------|
| ScreenCaptureExceptionTests | shouldCreateScreenCaptureException | Tests basic exception creation | Exception created correctly |
| ScreenCaptureExceptionTests | shouldCreateWithMessage | Tests exception with message | Message set correctly |
| ScreenCaptureExceptionTests | shouldCreateWithCause | Tests exception with cause | Cause set correctly |
| ScreenCaptureExceptionTests | shouldCreateWithMessageAndCause | Tests exception with both | Both message and cause set |
| RecordingExceptionTests | shouldCreateRecordingException | Tests recording exception creation | Recording exception created |
| RecordingExceptionTests | shouldInheritFromScreenCaptureException | Tests inheritance | Proper inheritance hierarchy |
| RecordingExceptionTests | shouldHandleRecordingErrors | Tests recording error handling | Recording errors handled |
| EncodingExceptionTests | shouldCreateEncodingException | Tests encoding exception creation | Encoding exception created |
| EncodingExceptionTests | shouldHandleEncodingErrors | Tests encoding error handling | Encoding errors handled |
| ExceptionHierarchyTests | shouldMaintainProperHierarchy | Tests exception hierarchy | Proper hierarchy maintained |
| ExceptionHierarchyTests | shouldAllowPolymorphicHandling | Tests polymorphic exception handling | Polymorphic handling works |
| SerializationTests | shouldSerializeExceptions | Tests exception serialization | Exceptions serializable |
| MessageTests | shouldProvideDetailedMessages | Tests exception messages | Detailed messages provided |

### 8. Utility Tests

#### TestUtils.java
**Purpose**: Provides utility methods and test data factories for comprehensive testing.

| Utility Category | Methods | Description | Purpose |
|-----------------|---------|-------------|---------|
| Mock Image Creation | createMockImage, createSmallMockImage | Creates test images with various sizes and colors | Image testing support |
| Mock Data Factories | createMockScreenInfo, createMockRecordingInfo | Creates test objects with realistic data | Object testing support |
| Test File Management | createTestFilePath, cleanupTestFiles, isValidFile | Manages test files and directories | File operation testing |
| Test Data Providers | getTestResolutions, getTestFrameRates, getTestImageFormats | Provides parameterized test data | Data-driven testing |
| Multi-Screen Support | createMultiScreenSetup, createDefaultTestRectangle | Supports multi-monitor testing | Screen testing scenarios |
| Validation Helpers | validateTestResults, createTestDirectory | Validates test outcomes and setup | Test result verification |

## Test Execution

### Running Tests
```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=AppTest

# Run tests with coverage
mvn test jacoco:report
```

### Test Configuration
- **Test Resource Directory**: `src/test/resources/`
- **Test Utilities**: `com.anyscreen.utils.TestUtils`
- **Mock Data**: Generated programmatically
- **Test Cleanup**: Automated via @AfterEach methods

## Coverage Goals

The test suite aims for:
- **Line Coverage**: >90%
- **Branch Coverage**: >85%
- **Method Coverage**: >95%
- **Class Coverage**: 100%

## Test Patterns Used

1. **Arrange-Act-Assert (AAA)**: Standard test structure
2. **Builder Pattern Testing**: For complex object creation
3. **Mock Object Pattern**: For dependency isolation
4. **Parameterized Testing**: For data-driven tests
5. **Nested Test Classes**: For organized test grouping
6. **Test Fixtures**: For consistent test setup
7. **Integration Testing**: For end-to-end workflows

## Error Scenarios Covered

- Null input handling
- Invalid parameter validation
- Resource allocation failures
- Threading and concurrency issues
- File system operation failures
- Network and I/O exceptions
- Configuration validation errors
- State transition violations

This comprehensive test suite ensures robust application behavior across all scenarios and provides confidence in the codebase quality and reliability.
