# Calendar - Android Build & APK Instructions

This project is a native Android application built using Kotlin, Jetpack Compose, Material Design 3, Room local database persistence, and Android-compatible notification APIs.

---

## 1. How to Build the APK

You can build the installable APKs directly using the Gradle wrapper or the installed Gradle command.

### Building Debug APK (Recommended for Testing & Development)
From the root directory of the project, run:
```bash
./gradlew assembleDebug
```
*(or `gradle :app:assembleDebug`)*

### Building Release APK
From the root directory of the project, run:
```bash
./gradlew assembleRelease
```
*(or `gradle :app:assembleRelease`)*

---

## 2. Where the Generated APK Files Are Located

Once the build command completes successfully, the installable APK files are saved at:

- **Debug APK:**
  ```
  app/build/outputs/apk/debug/app-debug.apk
  ```

- **Release APK:**
  ```
  app/build/outputs/apk/release/app-release.apk
  ```

---

## 3. How to Install the APK on Your Android Device

### Option A: Using ADB (Android Debug Bridge)
Connect your Android device with USB debugging enabled, or launch an Android emulator, then run:
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```
or for the release build:
```bash
adb install -r app/build/outputs/apk/release/app-release.apk
```

### Option B: Direct File Transfer
1. Copy `app-debug.apk` or `app-release.apk` to your phone via USB, Google Drive, email, or local sharing.
2. Open your device's **Files** app or downloads folder.
3. Tap on the `.apk` file and select **Install** (allow "Install Unknown Apps" from your file manager if prompted).

---

## 4. Release Keystore Configuration (Optional)

By default, the release build automatically uses the embedded development keystore for seamless testing and local installation.

To sign with your custom production upload keystore for Google Play distribution, set the following environment variables prior to running `assembleRelease`:

```bash
export KEYSTORE_PATH="/path/to/your/upload-keystore.jks"
export STORE_PASSWORD="your-keystore-password"
export KEY_ALIAS="your-key-alias"
export KEY_PASSWORD="your-key-password"

./gradlew assembleRelease
```

---

## 5. Technical Stack & Features

- **Minimum SDK:** Android 7.0 (API 24)
- **Target & Compile SDK:** Android 15 (API 36) — fully optimized and tested for Android 14 (API 34) and newer.
- **UI Framework:** 100% Jetpack Compose with Material Design 3 and Edge-to-Edge display.
- **Local Offline Storage:** SQLite managed via Room Database (`AppDatabase`, `EventEntity`, `TaskEntity`) for zero-latency offline performance and automatic data persistence across app restarts.
- **Notifications & Reminders:** `AlarmManager` with `NotificationCompat`, high-priority heads-up channel, and Android 14+ safe exact-alarm permissions with idle wake-up fallback.
- **Back Navigation:** Native `BackHandler` integration for closing dialogs, search, and returning smoothly to the primary Calendar view.
