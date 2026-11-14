package com.yukeshworks.ringshift;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import java.util.Calendar;

public final class ModeScheduler {
    public static final int PHASE_START = 0;
    public static final int PHASE_END = 1;

    private ModeScheduler() {}

    public static boolean hasPolicyAccess(Context context) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        return manager != null && manager.isNotificationPolicyAccessGranted();
    }

    public static boolean canScheduleExact(Context context) {
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (manager == null) return false;
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.S || manager.canScheduleExactAlarms();
    }

    public static void scheduleAll(Context context) {
        for (ModeSchedule schedule : ScheduleStore.getAll(context)) {
            if (schedule.enabled) {
                scheduleNext(context, schedule);
                if (schedule.hasEnd && isCurrentlyInsideRange(schedule, Calendar.getInstance())) {
                    applyMode(context, schedule.mode);
                }
            } else {
                cancel(context, schedule.id);
            }
        }
    }

    public static void scheduleNext(Context context, ModeSchedule schedule) {
        if (schedule == null) return;
        scheduleNext(context, schedule, PHASE_START);
        if (schedule.hasEnd) {
            scheduleNext(context, schedule, PHASE_END);
        } else {
            cancelPhase(context, schedule.id, PHASE_END);
        }
    }

    public static void scheduleNext(Context context, ModeSchedule schedule, int phase) {
        if (schedule == null || !schedule.enabled || !hasPolicyAccess(context) || !canScheduleExact(context)) {
            return;
        }
        if (phase == PHASE_END && !schedule.hasEnd) return;

        int hour = phase == PHASE_END ? schedule.endHour : schedule.hour;
        int minute = phase == PHASE_END ? schedule.endMinute : schedule.minute;

        Calendar next = nextOccurrence(hour, minute);
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (manager == null) return;

        Intent intent = new Intent(context, ModeAlarmReceiver.class);
        intent.setData(Uri.parse("ringshift://schedule/" + Uri.encode(schedule.id) + "/" + phase));
        intent.putExtra(ModeAlarmReceiver.EXTRA_ID, schedule.id);
        intent.putExtra(ModeAlarmReceiver.EXTRA_PHASE, phase);

        PendingIntent pending = PendingIntent.getBroadcast(
            context,
            requestCode(schedule.id, phase),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        try {
            manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, next.getTimeInMillis(), pending);
        } catch (SecurityException ignored) {
            // The user may revoke exact-alarm access after creating a rule.
        }
    }

    public static void cancel(Context context, String id) {
        cancelPhase(context, id, PHASE_START);
        cancelPhase(context, id, PHASE_END);
    }

    private static void cancelPhase(Context context, String id, int phase) {
        Intent intent = new Intent(context, ModeAlarmReceiver.class);
        intent.setData(Uri.parse("ringshift://schedule/" + Uri.encode(id) + "/" + phase));
        PendingIntent pending = PendingIntent.getBroadcast(
            context,
            requestCode(id, phase),
            intent,
            PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
        );
        if (pending != null) {
            AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (manager != null) manager.cancel(pending);
            pending.cancel();
        }
    }

    private static int requestCode(String id, int phase) {
        return 31 * id.hashCode() + phase + 17;
    }

    public static Calendar nextOccurrence(int hour, int minute) {
        Calendar next = Calendar.getInstance();
        next.set(Calendar.HOUR_OF_DAY, hour);
        next.set(Calendar.MINUTE, minute);
        next.set(Calendar.SECOND, 0);
        next.set(Calendar.MILLISECOND, 0);
        if (!next.after(Calendar.getInstance())) next.add(Calendar.DAY_OF_YEAR, 1);
        return next;
    }

    public static boolean isCurrentlyInsideRange(ModeSchedule schedule, Calendar now) {
        if (schedule == null || !schedule.hasEnd) return false;
        int current = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE);
        int start = schedule.hour * 60 + schedule.minute;
        int end = schedule.endHour * 60 + schedule.endMinute;
        if (end > start) return current >= start && current < end;
        return current >= start || current < end; // Overnight range.
    }

    public static void applyMode(Context context, String mode) {
        if (!hasPolicyAccess(context)) return;
        int target = "VIBRATE".equals(mode) ? AudioManager.RINGER_MODE_VIBRATE
            : "SILENT".equals(mode) ? AudioManager.RINGER_MODE_SILENT
            : AudioManager.RINGER_MODE_NORMAL;
        try {
            AudioManager audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            if (audio != null) audio.setRingerMode(target);
        } catch (SecurityException ignored) {
            // Notification Policy Access can be revoked at any time.
        }
    }

    public static void openPolicySettings(Context context) {
        Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static void openExactAlarmSettings(Context context) {
        Intent intent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
            intent.setData(Uri.parse("package:" + context.getPackageName()));
        } else {
            intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.parse("package:" + context.getPackageName()));
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}
