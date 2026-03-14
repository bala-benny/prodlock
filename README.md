# Prodlock

**Prodlock** is a productivity-focused Android application designed to help users regain control over their screen time. It gamifies the use of distracting apps by requiring users to "earn" their digital leisure time through the completion of real-world tasks.

## 🚀 Features

- **Task-Based Rewards**: Create custom tasks (e.g., "Study", "Workout", "Clean Room") and assign them a reward in minutes. Complete tasks to fill your "Screen Time Wallet".
- **Screen Time Wallet**: A central hub that tracks your earned minutes. You can only access "blocked" apps when you have a balance and have started an active session.
- **Smart App Blocking**: Automatically monitors and blocks distracting apps. If you try to open a restricted app without an active session, you are automatically redirected back to the Home screen.
    - **Currently Blocked Apps**: YouTube, Instagram, Facebook, Netflix, and Clash of Clans.
- **Floating Session Timer**: When a session is active, a floating timer overlay appears at the top of the screen while you are using restricted apps, keeping you aware of your remaining time.
- **Usage Analytics & Prediction**: 
    - Analyzes your daily app usage using the Android `UsageStatsManager`.
    - Predicts your monthly usage based on current habits to help you set better goals.
- **Modern UI**: Built entirely with **Jetpack Compose** and **Material 3**, featuring a clean dashboard with elevated cards and intuitive controls.

## 🛠️ How It Works

1.  **Add Tasks**: Define productive activities and their time value.
2.  **Earn Minutes**: Tap a task to "complete" it and add those minutes to your wallet.
3.  **Start a Session**: Click "Use all time" to start a countdown. 
4.  **Controlled Access**: The `BlockService` (an Accessibility Service) checks if the foreground app is on the restricted list. If it is, and the timer isn't running, it triggers a "Home" action to block access.

## 📱 Project Structure

- `blocking/BlockService.kt`: The core `AccessibilityService` handling app detection, blocking logic, and the floating WindowManager overlay.
- `WalletManager.kt`: A singleton manager that handles the countdown timer state and the minute balance.
- `TaskManager.kt`: Manages the list of tasks (in-memory persistence).
- `UsageAnalyzer.kt`: Interfaces with `UsageStatsManager` for analytics and monthly predictions.
- `MainActivity.kt`: The main dashboard UI built with Jetpack Compose.
- `AppDetector.kt`: Helper utility for identifying the current foreground application.

## ⚙️ Permissions Required

To function correctly, Prodlock requires the following sensitive permissions:
1.  **Accessibility Service**: **(Crucial)** Used to detect app launches and display the timer overlay.
2.  **Usage Access**: Used to calculate usage statistics and predictions.
3.  **Display Over Other Apps**: Allows the timer to remain visible while you are using other applications.

## 🛠️ Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Design System**: Material Design 3 (M3)
- **Architecture**: Singleton Pattern for state management (Wallet/Tasks).
- **Android APIs**: AccessibilityService, UsageStatsManager, WindowManager (Overlays).

---
*Developed as a tool for digital wellbeing.*
