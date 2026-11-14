# RingShift Android app

## Features
- Direct Ring, Vibrate, and Silent controls.
- Add multiple daily time rules.
- Rules run through Android AlarmManager while the app is closed.
- Daily rules are restored after device reboot, clock changes, timezone changes, and app updates.
- Rules are stored locally on the device.

## Required Android access
1. **Do Not Disturb / Notification Policy Access** — Android requires the user to grant this special access before an app can change the ringer mode.
2. **Alarms & reminders / exact alarm access** — required for time-accurate scheduled changes on Android 12+.

Open RingShift once after installation, grant both accesses, then add your daily rules. You can revoke either access from Android Settings at any time.

## Important behavior
- "Silent" means no ringtone and no ringer vibration; "Vibrate" means no ringtone with vibration.
- Schedules repeat daily.
- The app does not contact a server and does not collect data.
- Some Android manufacturers add background restrictions. If a schedule is delayed on your phone, allow RingShift to run in the background / disable battery optimization for it in system settings.

## Build
Run `gradle assembleDebug` from this directory with JDK 17, Android SDK Platform 35, and Android Build Tools 35.0.0 installed. The APK is generated at `app/build/outputs/apk/debug/app-debug.apk`.
