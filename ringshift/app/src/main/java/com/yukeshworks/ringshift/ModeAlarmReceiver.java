package com.yukeshworks.ringshift;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ModeAlarmReceiver extends BroadcastReceiver {
    public static final String EXTRA_ID = "schedule_id";
    public static final String EXTRA_PHASE = "schedule_phase";
    private static final String TAG = "RingShift";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) return;
        String id = intent.getStringExtra(EXTRA_ID);
        int phase = intent.getIntExtra(EXTRA_PHASE, ModeScheduler.PHASE_START);
        ModeSchedule schedule = id == null ? null : ScheduleStore.getById(context, id);
        if (schedule == null || !schedule.enabled) return;
        if (phase == ModeScheduler.PHASE_END && !schedule.hasEnd) return;

        String mode = phase == ModeScheduler.PHASE_END ? schedule.endMode : schedule.mode;
        if (ModeScheduler.hasPolicyAccess(context)) {
            ModeScheduler.applyMode(context, mode);
            Log.i(TAG, "Applied " + mode + " for " + schedule.id + " phase=" + phase);
        } else {
            Log.w(TAG, "Notification Policy Access is not granted");
        }

        // Each phase is a daily alarm. Re-arm only the phase that just ran.
        ModeScheduler.scheduleNext(context, schedule, phase);
    }
}
