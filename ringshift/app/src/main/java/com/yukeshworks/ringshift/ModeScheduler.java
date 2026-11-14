package com.yukeshworks.ringshift;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import java.util.Calendar;

public final class ModeScheduler {
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
            if (schedule.enabled) scheduleNext(context, schedule);
            else cancel(context, schedule.id);
        }
    }

    public static void scheduleNext(Context context, ModeSchedule schedule) {
        if (schedule == null || !schedule.enabled || !hasPolicyAccess(context) || !canScheduleExact(context)) {
            return;
        }

        Calendar next = Calendar.getInstance();
        next.set(Calendar.HOUR_OF_DAY, schedule.hour);
        next.set(Calendar.MINUTE, schedule.minute);
        next.set(Calendar.SECOND, 0);
        next.set(Calendar.MILLISECOND, 0);
        if (!next.after(Calendar.getInstance())) {
            next.add(Calendar.DAY_OF_YEAR, 1);
        }

        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (manager == null) return;

        Intent intent = new Intent(context, ModeAlarmReceiver.class);
        intent.setData(Uri.parse("ringshift://schedule/" + Uri.encode(schedule.id)));
        intent.putExtra(ModeAlarmReceiver.EXTRA_ID, schedule.id);
        intent.putExtra(ModeAlarmReceiver.EXTRA_MODE, schedule.mode);

        PendingIntent pending = PendingIntent.getBroadcast(
            context,
            schedule.id.hashCode(),
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
        Intent intent = new Intent(context, ModeAlarmReceiver.class);
        intent.setData(Uri.parse("ringshift://schedule/" + Uri.encode(id)));
        PendingIntent pending = PendingIntent.getBroadcast(
            context,
            id.hashCode(),
            intent,
            PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
        );
        if (pending != null) {
            AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (manager != null) manager.cancel(pending);
            pending.cancel();
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
