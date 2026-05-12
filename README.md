# Deeleap

![Build Status](https://img.shields.io/badge/build-passing-brightgreen)
![Platform](https://img.shields.io/badge/platform-Android-blue)
![Language](https://img.shields.io/badge/language-Kotlin%20%7C%20C%2B%2B-orange)
![License](https://img.shields.io/badge/license-MIT-green)

Deeleap is a high-performance, professional-grade video editing application for Android. Built with a focus on speed and precision, it leverages a hybrid processing engine combining hardware acceleration with advanced C++ rendering logic.

---

## Key Features

- **High-Performance Rendering Engine**: Custom C++/OpenGL ES pipeline for frame-accurate, real-time previews and high-speed exports.
- **Advanced Keyframe Animation**: Full control over layer properties including Position, Scale, Rotation, and Opacity with professional interpolation.
- **Hybrid Processing**: Utilizes `MediaCodec` for zero-copy hardware acceleration and `FFmpeg` for broad format support and complex processing.
- **Modern UI/UX**: Fully built with **Jetpack Compose** for a fluid, responsive, and state-of-the-art editing experience.
- **Pro Timeline**: Intuitive multi-layer timeline management with precise trimming and splitting capabilities.
- **Custom Media Gallery**: A built-in, permission-aware gallery picker with lifecycle-integrated media previews.

## Tech Stack

- **Core**: Kotlin, C++ (JNI)
- **UI Framework**: Jetpack Compose
- **Video Processing**: Media3, FFmpeg-kit, MediaCodec
- **Graphics**: OpenGL ES 3.0+
- **Architecture**: Clean Architecture + MVVM + MVI

## Assembly & Build Instructions

To build and assemble the project from the command line, follow these steps:

### Prerequisites
- **Android SDK**: API Level 34+
- **NDK**: Version 25.1.8937393 (or compatible)
- **CMake**: Version 3.22.1+

### Assemble Debug Build
Generate a debug APK for testing:
```bash
./gradlew assembleDebug
```

### Assemble Release Build
Generate a production-ready signed APK:
```bash
./gradlew assembleRelease
```

### Native Code Assembly
The C++ components are automatically assembled via CMake. To trigger a clean native build:
```bash
./gradlew cleanExternalNativeBuild
./gradlew assembleDebug
```

### Build Artifacts
Once assembled, the APKs can be found in:
- `app/build/outputs/apk/debug/app-debug.apk`
- `app/build/outputs/apk/release/app-release.apk`

---

## Project Structure

- `app/src/main/java`: Kotlin source code (UI, ViewModels, Business Logic).
- `app/src/main/cpp`: Native C++ code for high-performance GPU rendering and compositing.
- `app/src/main/res`: Android resources and UI assets.

## Contributing
Contributions are welcome! Please feel free to submit a Pull Request or open an issue for any bugs or feature requests.

## License
This project is licensed under the MIT License - see the LICENSE file for details.

---
Developed by [Aashu Mishra](https://github.com/mishra-aashu)
