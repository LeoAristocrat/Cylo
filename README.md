<p align="center">
  <img src="fastlane/metadata/android/en-US/images/icon.png" width="128" height="128" alt="Cylo App Icon" />
</p>

# Cylo — Mindful Focus, Planning & Daily Rhythm

> **Crafted by Leo Aristocrat**  
> An intentional, distraction-free productivity workspace for Android that merges deep focus intervals, structured task planning, habit analytics, and holistic rest monitoring.

[![GitHub](https://img.shields.io/badge/GitHub-LeoAristocrat%2FCylo-6C5CE7?logo=github)](https://github.com/LeoAristocrat/Cylo)
[![License](https://img.shields.io/badge/License-PolyForm%20Noncommercial-blue)](LICENSE.md)
[![Platform](https://img.shields.io/badge/Platform-Android-green?logo=android)](https://www.android.com)

Cylo approaches productivity as an organic rhythm: purposeful focus periods, disciplined execution, reflective analysis, and restorative recovery. Engineered from the ground up with **Kotlin**, **Jetpack Compose**, and **Material 3 Expressive**, Cylo delivers a fluid, AMOLED-optimized experience that feels right at home on modern Android.

---

## Screenshots

<div align="center">
  <table>
    <tr>
      <td align="center" width="25%">
        <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/Screenshot_01_Focus_Dial.png" width="220" alt="Focus Timer - Concentric Dial" /><br/>
        <sub><b>Focus — Concentric Dial</b></sub>
      </td>
      <td align="center" width="25%">
        <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/Screenshot_02_Focus_Flip.png" width="220" alt="Focus Timer - Flip Clock" /><br/>
        <sub><b>Focus — Flip Clock</b></sub>
      </td>
      <td align="center" width="25%">
        <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/Screenshot_03_Plan.png" width="220" alt="Plan - Tasks & Sessions" /><br/>
        <sub><b>Plan — Tasks & Sessions</b></sub>
      </td>
      <td align="center" width="25%">
        <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/Screenshot_04_Analyze.png" width="220" alt="Analyze - Overview & Activity Heatmap" /><br/>
        <sub><b>Analyze — Activity & Trends</b></sub>
      </td>
    </tr>
  </table>
</div>

---

## Key Pillars

### ⏱️ Immersive Flow States (Focus)
* **Sculpted Clock Designs**: Seamlessly swap between distinctive visual expressions tailored to your focus style — from mechanical **Flip Cards** and precision **Concentric Dials** to breathing pulses and minimalist arcs.
* **Purpose-Driven Allocations**: Associate focus intervals with custom color-coded tags and establish daily allocation targets.
* **Frictionless Transitions**: Automated short/long break sequences, optional ambient display preservation, and smart Do-Not-Disturb (DND) suppression.
* **Live System Countdown**: Unobtrusive foreground service status with responsive media-style controls.

### 📈 Temporal Reflection & Analytics (Analyze)
* **GitHub-Style Activity Heatmap**: Visualize long-term discipline and daily commitment through a continuous calendar intensity map.
* **Multi-Scale Insights**: Evaluate personal output across daily, weekly, monthly, and yearly horizons with tag distribution donuts and hourly concentration curves.
* **Streak Dynamics**: Stay motivated by tracking active consistency streaks alongside your lifetime bests.

### 📋 Intentional Task Stacks (Plan)
* **Targeted Session Planning**: Define high-leverage tasks and estimate how many focus blocks are needed to reach completion.
* **Ergonomic Workflow**: Fluid gesture-based task management with quick completions, smooth reordering, and tag organization designed for one-handed use.

### 🌙 Holistic Balance (Sleep & Movement)
* **Restorative Sleep Tracking**: Monitor sleep regularity and quality metrics via Android Health Connect two-way synchronization and Google Play Sleep API sensing.
* **Passive Activity Sensing**: Count your steps throughout the day using hardware-level pedometer integration with zero battery drain.

### 🎨 Personalization & Sovereignty
* **Material You Expressive**: Adapts organically to your dynamic system wallpaper colors, with pitch-black pure OLED dark mode and curated color themes.
* **Launcher Widgets**: Check your consistency streak, activity heatmap, or sleep recovery score directly on your home screen.
* **100% Offline & Private**: All data stays strictly on your device. Easily export and import your full history via clean JSON files.

---

## Tech Stack & Architecture

* **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) with Material 3 Expressive, Edge-to-Edge window insets
* **Navigation Architecture**: AndroidX Navigation 3 (`androidx.navigation3`)
* **Reactive Concurrency**: Kotlin Coroutines & StateFlow with Lifecycle-aware ViewModels
* **Local Persistence**: [Room Database](https://developer.android.com/training/data-storage/room) (SQLite) with Kotlinx Serialization
* **Health & Sensor Integrations**:
  * Android Health Connect API
  * Google Play Services Sleep API
  * Hardware Pedometer Sensor (`Sensor.TYPE_STEP_COUNTER`)
* **Background Tasks**: Foreground Services with low-overhead notification lifecycles

---

## Building from Source

### Prerequisites
* JDK 21 or higher (OpenJDK 21 / Android Studio JBR recommended)
* Android SDK (compileSdk / targetSdk: Android 15 / API 37)
* Minimum Supported OS: Android 10 (API level 29)

### Build Commands

```bash
# Compile debug APK
./gradlew assembleDebug

# Run unit tests
./gradlew testDebugUnitTest

# Run code linter
./gradlew lint

# Compile signed release APK
./gradlew assembleRelease
```

### Release Signing Setup

Release keystore details can be passed via environment variables or a local configuration file:
1. Copy `keystore.properties.example` to `keystore.properties` in the project root.
2. Provide your signing credentials:
   * `KEYSTORE_FILE`
   * `KEYSTORE_PASSWORD`
   * `KEY_ALIAS`
   * `KEY_PASSWORD`

If no release signing properties are detected, Gradle will automatically build with the debug keystore.

---

## Provenance & Attribution

Cylo is an independent evolution, rebrand, and portfolio project designed and maintained by **Leo Aristocrat**, derived from the open-source project *Kimon* by `zenzeros`.

* **Repository**: [LeoAristocrat/Cylo](https://github.com/LeoAristocrat/Cylo)
* **Upstream Project**: Kimon by `zenzeros`
* **Original License**: [PolyForm Noncommercial License 1.0.0](LICENSE.md)
* **Derivative Works**: Copyright (c) 2026 Leo Aristocrat. Available under PolyForm Noncommercial License 1.0.0.
