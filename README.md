# WiFi QR Scanner

A modern Android app that allows you to scan WiFi QR codes, view connection details, and connect to WiFi networks.

## Features

- 📷 **QR Code Scanning**: Scan WiFi QR codes using your device camera
- 🔍 **Connection Details**: View SSID, password, security type, and hidden network status
- 🔐 **Secure Connection**: Choose whether to connect after reviewing details
- 🎨 **Modern UI**: Built with Jetpack Compose and Material Design 3
- 📱 **Android 7.0+**: Supports Android API 24 and above

## Technology Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Design**: Material Design 3
- **Camera**: CameraX
- **QR Scanning**: ML Kit Barcode Scanning
- **Permissions**: Accompanist Permissions

## Requirements

- Android Studio Hedgehog (2023.1.1) or newer
- Android SDK 34
- Minimum Android version: 7.0 (API 24)
- Gradle 8.2+

## Setup Instructions

### Prerequisites

1. Install [Android Studio](https://developer.android.com/studio)
2. Install Android SDK 34 through Android Studio SDK Manager
3. Ensure you have a physical Android device or emulator set up

### Building the Project

1. Open the project in Android Studio
2. Wait for Gradle sync to complete
3. Connect your Android device or start an emulator
4. Click the "Run" button or press Shift+F10

### Using Gradle Command Line

```bash
# Build the project
./gradlew build

# Install on connected device
./gradlew installDebug

# Run tests
./gradlew test
```

## Permissions

The app requires the following permissions:
- **Camera**: To scan QR codes
- **Location**: Required by Android for WiFi operations
- **WiFi State**: To access and modify WiFi connections

## WiFi QR Code Format

The app supports standard WiFi QR codes in the format:
```
WIFI:T:WPA;S:NetworkName;P:Password;H:false;;
```

Where:
- `T`: Security type (WPA, WEP, nopass)
- `S`: SSID (network name)
- `P`: Password (omit for open networks)
- `H`: Hidden network (true/false)

## Project Structure

```
app/
├── src/main/
│   ├── AndroidManifest.xml
│   ├── java/com/wifiscanner/app/
│   │   ├── MainActivity.kt          # Entry point
│   │   ├── WiFiScannerApp.kt        # Main app composable
│   │   ├── QRScannerScreen.kt       # Camera and scanning logic
│   │   ├── WiFiDetailsScreen.kt     # Display scanned details
│   │   ├── WiFiData.kt              # Data model and parser
│   │   ├── WiFiConnector.kt         # WiFi connection logic
│   │   └── ui/theme/                # Material Design 3 theme
│   └── res/                         # Resources and assets
└── build.gradle.kts                 # App dependencies
```

## How It Works

1. **Scan**: Tap "Scan QR Code" to open the camera
2. **Point**: Aim your camera at a WiFi QR code
3. **Review**: View the network details (SSID, password, security)
4. **Connect**: Choose to connect or cancel

## Compatibility Notes

- **Android 10+**: Uses `WifiNetworkSpecifier` for modern connection handling
- **Android 7-9**: Uses legacy `WifiConfiguration` API
- The app automatically handles both methods based on Android version

## License

This project is open source and available for educational purposes.

## Troubleshooting

**Camera not working**: Ensure camera permissions are granted in app settings

**Can't connect to WiFi**: 
- Check location permission is granted
- Ensure WiFi is enabled on your device
- Verify the QR code format is correct

**Build errors**: 
- Sync Gradle files
- Clean and rebuild project
- Ensure Android SDK 34 is installed

## Contributing

Feel free to submit issues and enhancement requests!
