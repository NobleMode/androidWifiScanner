# Build Error Fix Guide

## Error: Missing Android SDK Platform 34

**Error Message**: `Cannot create mockable android.jar` - File not found: `F:\Tools\AndroidStudioSDK\platforms\android-34\android.jar`

## ✅ Solution: Install Android SDK Platform 34

### In Android Studio:

1. **Open SDK Manager**:
   - Click **Tools → SDK Manager** (or the SDK Manager icon in toolbar)
   - Or go to: **File → Settings → Appearance & Behavior → System Settings → Android SDK**

2. **Install Android 14.0 (API 34)**:
   - Go to the **SDK Platforms** tab
   - Check the box next to **"Android 14.0 (API 34)"** (or "Android UpsideDownCake")
   - Click **Apply** or **OK**
   - Wait for download and installation to complete

3. **Verify SDK Build Tools** (optional but recommended):
   - Click on **SDK Tools** tab
   - Ensure **Android SDK Build-Tools 34** is installed
   - If not, check it and click Apply

4. **Sync Project Again**:
   - Go back to your project
   - Click **File → Sync Project with Gradle Files**
   - Or click the "Sync Now" banner if it appears

---

## Alternative: Lower the Target SDK (Temporary Workaround)

If you can't install SDK 34 right now, you can temporarily lower the target to SDK 33:

### Edit `app/build.gradle.kts`:

Change these lines:
```kotlin
compileSdk = 34
targetSdk = 34
```

To:
```kotlin
compileSdk = 33
targetSdk = 33
```

Then sync the project again.

**Note**: This is a temporary workaround. SDK 34 (Android 14) is recommended for new apps.

---

## After Installing SDK 34:

1. **Invalidate Caches** (if needed):
   - **File → Invalidate Caches → Invalidate and Restart**

2. **Clean and Rebuild**:
   - **Build → Clean Project**
   - **Build → Rebuild Project**

3. **Build APK**:
   - **Build → Build Bundle(s) / APK(s) → Build APK(s)**

The project should now build successfully!
