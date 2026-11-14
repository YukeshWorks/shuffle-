package com.yukeshworks.ringshift;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.util.Log;

public class ModeAlarmReceiver extends BroadcastReceiver {
    public static final String EXTRA_ID = "schedule_id";
    public static final String EXTRA_MODE = "ringer_mode";
    private static final String TAG = "RingShift";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) return;
        String id = intent.getStringExtra(EXTRA_ID);
        ModeSchedule schedule = id == null ? null : ScheduleStore.getById(context, id);
        if (schedule == null || !schedule.enabled) return;

        if (ModeScheduler.hasPolicyAccess(context)) {
            try {
                AudioManager audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                if (audio != null) {
                    audio.setRingerMode(toAudioMode(schedule.mode));
                    Log.i(TAG, "Applied " + schedule.mode + " for " + schedule.id);
                }
            } catch (SecurityException exception) {
                Log.w(TAG, "Android refused ringer mode change", exception);
            }
        } else {
            Log.w(TAG, "Notification Policy Access is not granted");
        }

        // Re-arm the next daily occurrence. The rule is stored locally on this device.
        ModeScheduler.scheduleNext(context, schedule);
    }

    private int toAudioMode(String mode) {
        if ("VIBRATE".equals(mode)) return AudioManager.RINGER_MODE_VIBRATE;
        if ("SILENT".equals(mode)) return AudioManager.RINGER_MODE_SILENT;
        return AudioManager.RINGER_MODE_NORMAL;
    }
}
