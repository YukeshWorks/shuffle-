# RingShift — Android ringer scheduler

This repository contains the RingShift Android app source in the `ringshift/` directory.

RingShift switches Android between Ring, Vibrate, and Silent using Android's native AudioManager API. It supports user-created daily schedules that run through Android AlarmManager even when the app is closed, and restores schedules after reboot.

The app requires the user to grant:
- Notification Policy Access (to change ringer mode)
- Alarms & reminders / exact alarm access (for on-time schedules)

The APK is published under GitHub Releases after a successful Android build.

This is a standalone project; other branches and existing apps are not modified.
