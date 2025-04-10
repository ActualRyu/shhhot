# Shhhot

<div align="center">
  <img src="app/src/main/res/mipmap-xxxhdpi/ic_launcher.png" width="100" alt="App Icon">
</div>

Shhhot is a privacy-focused Android application that lets you censor text in images using multiple methods. Perfect for quickly redacting sensitive information before sharing screenshots, documents, or photos.

[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

## Features

- **Powerful Text Detection**: Utilizes ML Kit to accurately identify text within images
- **Multiple Censoring Methods**: 
  - **Block Mode**: Cover text with solid black rectangles
  - **Hide Mode**: Intelligently samples background colors to blend censored areas
- **Intuitive UI**: Modern, clean interface built with Jetpack Compose
- **Privacy-First**: Works completely offline with no data collection
- **Universal Compatibility**: Functions on all Android devices, even without Google services
- **Export Functionality**: Save censored images directly to your gallery

## Screenshots

<div align="center">
  <div style="display:flex; gap: 10px; flex-wrap: wrap; justify-content: center;">
    <img src="screenshots/home_screen.jpg" width="200" alt="Home Screen">
    <img src="screenshots/editor_screen.jpg" width="200" alt="Editor Screen">
    <img src="screenshots/censored_image.jpg" width="200" alt="Censored Image">
  </div>
</div>

## Installation

### From GitHub Releases
1. Navigate to the [Releases](https://github.com/yourusername/shhhot/releases) page
2. Download the latest APK file
3. Enable installation from unknown sources in your device settings if needed
4. Open the downloaded APK to install

### From Source
1. Clone the repository: `git clone https://github.com/yourusername/shhhot.git`
2. Open the project in Android Studio
3. Connect your device or start an emulator
4. Click 'Run' to build and install the app

## How to Use

1. **Launch the app** and grant necessary permissions when prompted
2. **Select an image** by tapping the button on the home screen
3. **View detected text** which will appear highlighted on your image
4. **Tap on text** to mark it for censoring
5. **Choose a censoring method** from the bottom panel
    - Block: Covers text with solid black rectangles
    - Hide: Samples background colors to blend censored areas
6. **Export** the censored image to save it to your gallery

## How It Works

Shhhot uses on-device text recognition via ML Kit to detect and process text without sending data to external servers:

1. **Text Detection**: When you select an image, ML Kit scans it to identify text regions
2. **Interactive Editing**: Detected text is displayed as interactive blocks that can be toggled for censoring
3. **Smart Censoring**: Based on your selected mode, the app applies different censoring techniques:
   - Block mode applies a simple opaque overlay
   - Hide mode samples surrounding colors to create a camouflage effect
4. **Secure Export**: The final image is processed entirely on your device and saved to your gallery

## Technical Details

### Built With

- **[Kotlin](https://kotlinlang.org/)** - Primary programming language
- **[Jetpack Compose](https://developer.android.com/jetpack/compose)** - Modern UI toolkit
- **[ML Kit](https://developers.google.com/ml-kit)** - On-device text recognition
- **[Coil](https://coil-kt.github.io/coil/)** - Image loading and caching
- **[MVVM Architecture](https://developer.android.com/topic/architecture)** - Clean separation of concerns

### System Requirements

- Android 7.0 (API Level 24) or higher
- ~20MB of disk space
- Works with or without Google Play Services

## Privacy

Shhhot is designed with privacy in mind:

- All processing happens directly on your device
- No data is collected or transmitted
- No internet permission is required
- No third-party analytics or tracking

## Contributing

Contributions are welcome! If you'd like to improve Shhhot:

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/amazing-feature`
3. Commit your changes: `git commit -m 'Add amazing feature'`
4. Push to the branch: `git push origin feature/amazing-feature`
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- [Google ML Kit](https://developers.google.com/ml-kit) for text recognition capabilities
- Icon design inspired by privacy and security themes
- All contributors who help improve this app
