# RingShift Android app — v1.1.0

## Features
- Direct Ring, Vibrate, and Silent controls.
- Create repeating daily time ranges: From → Until.
- Choose the mode active during the range and the mode to restore at the end.
- Overnight ranges are supported by setting the end time earlier than the start time.
- AM/PM time selection.
- Rules run through Android AlarmManager while the app is closed.
- Daily rules are restored after device reboot, clock changes, timezone changes, and app updates.
- Existing v1.0 single-switch rules remain readable.
- Rules are stored locally on the device.
- Small vector launcher icon and lightweight UI animations.

## Required Android access
1. **Do Not Disturb / Notification Policy Access** — Android requires the user to grant this special access before an app can change the ringer mode.
2. **Alarms & reminders / exact alarm access** — required for time-accurate scheduled changes on Android 12+.

Open RingShift once after installation, grant both accesses, then add your daily ranges. You can revoke either access from Android Settings at any time.

## Important behavior
- Example: From 10:00 PM until 7:00 AM → Silent, then Ring.
- "Silent" means no ringtone and no ringer vibration; "Vibrate" means no ringtone with vibration.
- Schedules repeat daily. Avoid overlapping ranges because the last alarm to run controls the phone mode.
- The app does not contact a server and does not collect data.
- Some Android manufacturers add background restrictions. If a schedule is delayed on your phone, allow RingShift to run in the background / disable battery optimization for it in system settings.

## Build
Run `gradle assembleDebug` from this directory with JDK 17, Android SDK Platform 35, and Android Build Tools 35.0.0 installed. The APK is generated at `app/build/outputs/apk/debug/app-debug.apk`.
