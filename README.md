# Deeleap

Deeleap is a high-performance, professional-grade video editing application for Android. Built with a focus on speed and precision, it leverages a hybrid processing engine combining hardware acceleration with advanced C++ rendering logic.

## 🚀 Key Features

- **High-Performance Rendering Engine**: Custom C++/OpenGL ES pipeline for frame-accurate, real-time previews and high-speed exports.
- **Advanced Keyframe Animation**: Full control over layer properties including Position, Scale, Rotation, and Opacity with professional interpolation.
- **Hybrid Processing**: Utilizes `MediaCodec` for zero-copy hardware acceleration and `FFmpeg` for broad format support and complex processing.
- **Modern UI/UX**: Fully built with **Jetpack Compose** for a fluid, responsive, and state-of-the-art editing experience.
- **Pro Timeline**: Intuitive multi-layer timeline management with precise trimming and splitting capabilities.
- **Custom Media Gallery**: A built-in, permission-aware gallery picker with lifecycle-integrated media previews.

## 🛠 Tech Stack

- **Language**: Kotlin, C++ (JNI)
- **UI Framework**: Jetpack Compose
- **Video Processing**: Media3, FFmpeg-kit, MediaCodec
- **Graphics**: OpenGL ES 3.0+
- **Architecture**: Clean Architecture + MVVM + MVI
- **Dependency Injection**: Hilt (planned/implemented)
- **Concurrency**: Kotlin Coroutines & Flow

## 🏗 Project Structure

- `app/src/main/java`: Kotlin source code (UI, ViewModels, Business Logic).
- `app/src/main/cpp`: Native C++ code for high-performance GPU rendering and compositing.
- `app/src/main/res`: Android resources and UI assets.

## 🚀 Getting Started

### Prerequisites
- Android Studio Ladybug or newer
- Android SDK 34+
- NDK (Side-by-side) configured

### Build Instructions
1. Clone the repository:
   ```bash
   git clone https://github.com/mishra-aashu/Deeleap.git
   ```
2. Open the project in Android Studio.
3. Sync Gradle and build the project.
4. Run on a physical device (recommended for GPU features).

## 🤝 Contributing
Contributions are welcome! Please feel free to submit a Pull Request or open an issue for any bugs or feature requests.

## 📄 License
This project is licensed under the MIT License - see the LICENSE file for details.

---
Developed with ❤️ by [Aashu Mishra](https://github.com/mishra-aashu)
