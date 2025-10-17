# How to Build and Install on Your Phone

## 🎯 Quick Guide: 3 Methods

### Method 1: Android Studio (Easiest - Recommended)

**Why this is best**: Android Studio handles everything automatically - SDK, dependencies, building, and installation.

#### Steps:
1. **Download Android Studio**: https://developer.android.com/studio
2. **Install Android Studio** (includes Android SDK and build tools)
3. **Open this project**: 
   - Launch Android Studio
   - File → Open → Select `androidWifiScanner` folder
   - Wait for Gradle sync (first time: 5-10 minutes for downloads)
4. **Enable USB Debugging on your phone**:
   - Go to Settings → About Phone
   - Tap "Build Number" 7 times (enables Developer Options)
   - Go to Settings → Developer Options
   - Enable "USB Debugging"
5. **Connect phone via USB**
6. **Click the green "Run" button** in Android Studio
7. **Done!** App installs and launches on your phone

---

### Method 2: Build APK with Android Studio

**Why use this**: Get an APK file you can share or install later.

#### Steps:
1. Open project in Android Studio (see Method 1)
2. Go to: **Build → Build Bundle(s) / APK(s) → Build APK(s)**
3. Wait for build to complete
4. Click "locate" in the notification, or find APK at:
   ```
   app\build\outputs\apk\debug\app-debug.apk
   ```
5. **Transfer to phone**:
   - Copy APK to your phone via USB, email, or cloud storage
   - On phone: Open the APK file
   - Allow "Install from unknown sources" if prompted
   - Tap "Install"

---

### Method 3: Command Line (Advanced - Requires Setup)

**Requirements**:
- Java JDK 11 or higher installed
- Android SDK installed
- Environment variables configured

#### Check if you have requirements:
```powershell
java -version   # Should show Java 11+
```

#### If you have Android Studio installed:
```powershell
# Navigate to project
cd "f:\Code\Fun\androidWifiScanner"

# Build APK
.\gradlew.bat assembleDebug

# APK will be at: app\build\outputs\apk\debug\app-debug.apk
```

Then transfer APK to phone and install.

---

## 📱 Installing APK on Your Phone

Once you have the APK file:

1. **Transfer the APK** to your phone:
   - USB cable and copy to Downloads folder
   - Email it to yourself
   - Use Google Drive, Dropbox, etc.

2. **Enable installation from unknown sources**:
   - Android 8+: Settings → Apps → Special Access → Install unknown apps → Select your file browser → Allow
   - Android 7: Settings → Security → Unknown Sources → Enable

3. **Install**:
   - Open your file browser on phone
   - Navigate to the APK file
   - Tap to install
   - Grant permissions when prompted

---

## ✅ Recommended Approach

**For first-time Android development**: Use Method 1 (Android Studio)

**Why?**
- ✅ Everything is included (SDK, tools, emulator)
- ✅ Automatic dependency management
- ✅ Easy debugging and testing
- ✅ One-click installation to phone
- ✅ Can make changes and rebuild easily

**Installation Time**: 
- Android Studio download: ~1GB
- First build: ~10 minutes (downloading dependencies)
- Subsequent builds: ~1-2 minutes

---

## 🔧 Troubleshooting

**"USB Debugging not authorized"**
→ Unlock phone and check for authorization popup

**"App not installed"**
→ Uninstall any previous version of the app first

**"Installation blocked"**
→ Enable "Install from unknown sources" in phone settings

**Build fails in Android Studio**
→ File → Invalidate Caches → Restart

---

## 📦 APK Size
- Debug APK: ~15-20 MB
- Release APK (optimized): ~10-15 MB

---

## 🚀 After Installation

1. Open "WiFi QR Scanner" app
2. Grant Camera permission
3. Grant Location permission (required by Android for WiFi)
4. Tap "Scan QR Code"
5. Point camera at a WiFi QR code
6. Review details and tap "Connect"

Enjoy your WiFi QR Scanner! 📱✨
