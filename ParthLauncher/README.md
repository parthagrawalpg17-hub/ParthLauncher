# Parth Launcher

A lightweight native Android home-screen replacement built with Java and Android SDK APIs. It discovers installed launcher apps, displays their real icons, supports instant search, dynamic time-based greetings, tactile animations, and a small settings panel.

## Build locally (without Android Studio)

Requirements: JDK 17 and Android SDK with platform 35/build-tools 35.0.0.

```bash
./gradlew assembleDebug
```

APK: `app/build/outputs/apk/debug/app-debug.apk`

On Windows use `gradlew.bat assembleDebug`.

## GitHub Actions

`.github/workflows/build.yml` runs on pushes to `main` and manually through **Actions → Build Parth Launcher → Run workflow**. It installs Java 17, Android platform 35/build-tools 35.0.0, builds the debug APK, and uploads an artifact named `ParthLauncher-debug`.

## GitHub Codespaces

Open the repository in a Codespace, then run:

```bash
./gradlew assembleDebug
```

The wrapper downloads the pinned Gradle distribution automatically. No Android Studio is required.

## Install

Download `app-debug.apk` (or the GitHub Actions artifact), copy it to an Android phone, allow installation from the source you used if Android asks, and install it.

## Set as default launcher

After installation, press the Android Home button and choose **Parth Launcher** when the system asks which Home app to use. You can also open **Settings → Apps → Default apps → Home app** (wording varies by Android version) and select Parth Launcher. The built-in settings shortcut also opens the system Home settings screen.

## Features

- Real installed applications and real application icons
- Dynamic greeting: morning / afternoon / evening / night
- Current time and date
- Fast case-insensitive app search
- Four-column responsive app grid
- Press-scale feedback and lightweight animations
- Launcher HOME + DEFAULT intent filters
- Basic launcher settings
- Offline-first; no analytics, account, or network permission

## Known Android limitations

Android controls the exact appearance and availability of the default Home-app selector, so wording differs by manufacturer and Android version. Some system or work-profile apps may not expose a normal launcher activity and therefore will not appear in the grid. The launcher intentionally avoids intrusive permissions and background services.
