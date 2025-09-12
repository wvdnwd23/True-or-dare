# Truth or Dare Deluxe - APK Build Instructions

This document provides step-by-step instructions for building the Truth or Dare Deluxe app into an APK file.

## Prerequisites

1. **Android Studio**
   - Android Studio Hedgehog (2023.1.1) or newer
   - Download from: [https://developer.android.com/studio](https://developer.android.com/studio)

2. **JDK 17**
   - Required for building the project
   - Can be installed via Android Studio or separately

3. **Android SDK 34**
   - Install via Android Studio's SDK Manager
   - Required components:
     - Android SDK Platform 34
     - Android SDK Build-Tools 34.0.0 or newer

## Building the APK

### Method 1: Using Android Studio (Recommended)

1. **Open the Project**
   - Launch Android Studio
   - Select "Open an existing project"
   - Navigate to and select the "True-or-dare" directory

2. **Sync Project**
   - Wait for the initial indexing to complete
   - Click "Sync Project with Gradle Files" (elephant icon in the toolbar)
   - Wait for the sync to complete successfully

3. **Build Debug APK**
   - Select "Build" > "Build Bundle(s) / APK(s)" > "Build APK(s)"
   - Wait for the build to complete
   - Android Studio will show a notification with a link to the APK location

4. **Build Release APK (Signed)**
   - Select "Build" > "Generate Signed Bundle / APK"
   - Choose "APK"
   - Follow the wizard to create or select a keystore
   - Select "release" build variant
   - Click "Finish" to build the signed APK

### Method 2: Using Command Line

1. **Navigate to Project Directory**
   ```bash
   cd path/to/True-or-dare
   ```

2. **Build Debug APK**
   ```bash
   ./gradlew assembleDebug
   ```
   The APK will be generated at: `app/build/outputs/apk/debug/app-debug.apk`

3. **Build Release APK**
   ```bash
   ./gradlew assembleRelease
   ```
   The APK will be generated at: `app/build/outputs/apk/release/app-release.apk`

   Note: For a signed release APK, you need to configure the signing in `app/build.gradle.kts` or use Android Studio's signing wizard.

## Installing the APK

### On a Physical Device

1. Enable "Install from Unknown Sources" in your device settings
2. Transfer the APK to your device
3. Tap on the APK file to install it

### On an Emulator

1. Start your Android emulator
2. Drag and drop the APK onto the emulator window, or
3. Use ADB:
   ```bash
   adb install path/to/app.apk
   ```

## Troubleshooting

### Common Issues

1. **Build Fails with Gradle Sync Issues**
   - Ensure you have the correct JDK version (17)
   - Try "File" > "Invalidate Caches / Restart"
   - Check that all dependencies are available

2. **Missing SDK Components**
   - Open SDK Manager in Android Studio
   - Install any missing components

3. **Signing Issues**
   - Verify keystore path and credentials
   - The project includes a task to generate a test keystore if needed

4. **Resource Not Found Errors**
   - Ensure all resource files are properly placed
   - Check for any case-sensitivity issues in resource references

## Multi-Select Categories Feature

The multi-select categories feature allows users to select multiple categories and sub-options for their game. This feature is implemented in:

- `app/src/main/java/com/wes/truthdare/ui/screens/categories/MultiSelectCategoriesScreen.kt`
- `app/src/main/java/com/wes/truthdare/ui/screens/categories/MultiSelectCategoriesViewModel.kt`

Sample content for this feature is located in:
- `app/src/main/assets/categories/`

## Need Help?

If you encounter any issues building the APK, please:
1. Check the build output for specific error messages
2. Consult the Android Studio documentation
3. Contact the development team for assistance