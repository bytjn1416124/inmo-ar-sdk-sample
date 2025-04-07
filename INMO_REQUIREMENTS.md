# INMO AR SDK Requirements

This file contains important information about requirements for building and running applications with the INMO AR SDK.

## Development Environment

- **JDK Version**: JDK 11 is required
  - Modify `gradle.properties` to point to your JDK 11 installation: `org.gradle.java.home=/path/to/jdk-11`

## Build Settings

### For Android Studio Build

- **Minimum API Level**: 28 (Android 9 Pie)
- **Target API Level**: 32 or higher recommended
- **Java/Kotlin Compatibility**: JavaVersion.VERSION_11

### For Unity Build (when exporting to Android)

- **Scripting Backend**: IL2CPP mode is recommended for better performance and scalability
- **Target Architecture**: ARMv7 only (uncheck ARM64 to reduce APK size)
- **Frame Rate Optimization**: Disable "Optimize Frame Rate" during packaging
- **Unity Version Compatibility**:
  - Unity 2020.3 LTS → NDK r19
  - Unity 2021.3 LTS → NDK r21d

## Hardware Requirements

- INMO Air2 glasses
- INMO RING2 for interaction

## INMO AR Service

- Requires INMO Glass firmware version 2.4 or above to run the AR service.
