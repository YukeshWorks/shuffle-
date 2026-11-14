package com.yukeshworks.ringshift;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity {
    private static final int BG = Color.rgb(10, 15, 25);
    private static final int CARD = Color.rgb(21, 29, 43);
    private static final int CARD_2 = Color.rgb(28, 39, 56);
    private static final int TEXT = Color.rgb(245, 247, 250);
    private static final int MUTED = Color.rgb(159, 174, 194);
    private static final int LIME = Color.rgb(199, 255, 94);
    private static final int RED = Color.rgb(255, 111, 125);

    private TextView currentMode;
    private TextView policyStatus;
    private TextView alarmStatus;
    private LinearLayout rulesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);
        getWindow().getDecorView().setSystemUiVisibility(0);
        buildScreen();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshScreen();
        if (ModeScheduler.hasPolicyAccess(this) && ModeScheduler.canScheduleExact(this)) {
            ModeScheduler.scheduleAll(this);
        }
    }

    private void buildScreen() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(BG);

        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(20), dp(20), dp(20), dp(28));
        scroll.addView(page);

        TextView eyebrow = text("ANDROID SOUND CONTROL", 11, LIME, true);
        eyebrow.setLetterSpacing(0.14f);
        page.addView(eyebrow, fullWidth());

        TextView title = text("RingShift", 32, TEXT, true);
        title.setPadding(0, dp(5), 0, 0);
        page.addView(title, fullWidth());

        TextView subtitle = text("Your sound mode, on your schedule.", 14, MUTED, false);
        subtitle.setPadding(0, dp(3), 0, dp(18));
        page.addView(subtitle, fullWidth());

        LinearLayout statusCard = card();
        statusCard.setPadding(dp(18), dp(16), dp(18), dp(16));
        page.addView(statusCard, marginParams(0, 0, 0, 16));

        statusCard.addView(text("CURRENT PHONE MODE", 10, MUTED, true), fullWidth());
        currentMode = text("Checking…", 26, TEXT, true);
        currentMode.setPadding(0, dp(7), 0, dp(3));
        statusCard.addView(currentMode, fullWidth());
        statusCard.addView(text("Changes apply to the phone's actual ringer.", 12, MUTED, false), fullWidth());

        TextView quickTitle = text("Quick switch", 18, TEXT, true);
        quickTitle.setPadding(0, dp(2), 0, dp(10));
        page.addView(quickTitle, fullWidth());

        LinearLayout quickRow = new LinearLayout(this);
        quickRow.setOrientation(LinearLayout.HORIZONTAL);
        page.addView(quickRow, marginParams(0, 0, 0, 18));
        quickRow.addView(modeButton("Ring", "RING", LIME, BG), weightedParams());
        quickRow.addView(modeButton("Vibrate", "VIBRATE", CARD_2, TEXT), weightedParams());
        quickRow.addView(modeButton("Silent", "SILENT", CARD_2, TEXT), weightedParams());

        TextView setupTitle = text("Android access", 18, TEXT, true);
        setupTitle.setPadding(0, 0, 0, dp(9));
        page.addView(setupTitle, fullWidth());

        LinearLayout accessCard = card();
        accessCard.setPadding(dp(16), dp(15), dp(16), dp(15));
        page.addView(accessCard, marginParams(0, 0, 0, 18));

        policyStatus = text("", 13, TEXT, true);
        accessCard.addView(policyStatus, fullWidth());
        Button policyButton = button("Allow sound-mode access", LIME, BG);
        policyButton.setOnClickListener(v -> openPolicyAccess());
        accessCard.addView(policyButton, marginParams(0, dp(10), 0, 0));

        alarmStatus = text("", 13, TEXT, true);
        alarmStatus.setPadding(0, dp(14), 0, 0);
        accessCard.addView(alarmStatus, fullWidth());
        Button alarmButton = button("Allow exact schedules", CARD_2, TEXT);
        alarmButton.setOnClickListener(v -> openAlarmAccess());
        accessCard.addView(alarmButton, marginParams(0, dp(9), 0, 0));

        TextView scheduleTitle = text("Daily schedules", 18, TEXT, true);
        scheduleTitle.setPadding(0, 0, 0, dp(4));
        page.addView(scheduleTitle, fullWidth());

        TextView scheduleHint = text("Choose when the phone should switch. Rules repeat every day, even when RingShift is closed.", 12, MUTED, false);
        scheduleHint.setPadding(0, 0, 0, dp(11));
        page.addView(scheduleHint, fullWidth());

        LinearLayout scheduleCard = card();
        scheduleCard.setPadding(dp(14), dp(12), dp(14), dp(14));
        page.addView(scheduleCard, marginParams(0, 0, 0, 16));

        rulesList = new LinearLayout(this);
        rulesList.setOrientation(LinearLayout.VERTICAL);
        scheduleCard.addView(rulesList, fullWidth());

        Button addButton = button("+  Add daily schedule", LIME, BG);
        addButton.setOnClickListener(v -> addSchedule());
        scheduleCard.addView(addButton, marginParams(0, dp(8), 0, 0));

        TextView footer = text("Schedules are stored only on this phone. Android requires the two special accesses above. Turn them off any time in Settings.", 11, MUTED, false);
        footer.setGravity(Gravity.CENTER);
        footer.setPadding(dp(6), dp(6), dp(6), dp(10));
        page.addView(footer, fullWidth());

        setContentView(scroll);
    }

    private void refreshScreen() {
        AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (audio != null) {
            int mode = audio.getRingerMode();
            if (mode == AudioManager.RINGER_MODE_VIBRATE) currentMode.setText("Vibrate only");
            else if (mode == AudioManager.RINGER_MODE_SILENT) currentMode.setText("Silent");
            else currentMode.setText("Ring");
        }

        boolean policy = ModeScheduler.hasPolicyAccess(this);
        boolean exact = ModeScheduler.canScheduleExact(this);

        policyStatus.setText((policy ? "✓ " : "1. ") + "Notification Policy Access  " + (policy ? "Granted" : "Needed"));
        policyStatus.setTextColor(policy ? LIME : TEXT);
        alarmStatus.setText((exact ? "✓ " : "2. ") + "Alarms & reminders  " + (exact ? "Granted" : "Needed"));
        alarmStatus.setTextColor(exact ? LIME : TEXT);

        rulesList.removeAllViews();
        ArrayList<ModeSchedule> schedules = ScheduleStore.getAll(this);
        if (schedules.isEmpty()) {
            TextView empty = text("No schedules yet. Add a time to get started.", 13, MUTED, false);
            empty.setPadding(dp(3), dp(4), dp(3), dp(8));
            rulesList.addView(empty, fullWidth());
        } else {
            for (ModeSchedule schedule : schedules) {
                addScheduleRow(schedule);
            }
        }
    }

    private void addScheduleRow(ModeSchedule schedule) {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(dp(10), dp(9), dp(7), dp(9));
        row.setBackground(roundRect(CARD_2, 14));
        rulesList.addView(row, marginParams(0, 0, 0, dp(7)));

        String time = new SimpleDateFormat("h:mm a", Locale.getDefault())
            .format(dateForTime(schedule.hour, schedule.minute));
        String state = schedule.enabled ? "" : "  ·  PAUSED";
        TextView info = text(time + "   " + schedule.mode + state, 14, schedule.enabled ? TEXT : MUTED, true);
        row.addView(info, new LinearLayout.LayoutParams(0, dp(44), 1f));

        Button toggle = smallButton(schedule.enabled ? "ON" : "OFF", schedule.enabled ? LIME : CARD, schedule.enabled ? BG : MUTED);
        toggle.setOnClickListener(v -> {
            schedule.enabled = !schedule.enabled;
            ScheduleStore.upsert(this, schedule);
            if (schedule.enabled) {
                ModeScheduler.scheduleNext(this, schedule);
            } else {
                ModeScheduler.cancel(this, schedule.id);
            }
            refreshScreen();
        });
        row.addView(toggle, new LinearLayout.LayoutParams(dp(58), dp(40)));

        Button remove = smallButton("×", CARD, RED);
        remove.setTextSize(19);
        remove.setOnClickListener(v -> new AlertDialog.Builder(this)
            .setTitle("Delete schedule?")
            .setMessage("This daily rule will be removed.")
            .setNegativeButton("Keep", (d, w) -> {})
            .setPositiveButton("Delete", (d, w) -> {
                ModeScheduler.cancel(this, schedule.id);
                ScheduleStore.delete(this, schedule.id);
                refreshScreen();
            }).show());
        LinearLayout.LayoutParams removeParams = new LinearLayout.LayoutParams(dp(42), dp(40));
        removeParams.leftMargin = dp(5);
        row.addView(remove, removeParams);
    }

    private void addSchedule() {
        if (!ModeScheduler.hasPolicyAccess(this)) {
            new AlertDialog.Builder(this)
                .setTitle("Allow sound-mode access")
                .setMessage("Android requires Notification Policy Access before RingShift can change Ring, Vibrate, or Silent.")
                .setNegativeButton("Cancel", (d, w) -> {})
                .setPositiveButton("Open Settings", (d, w) -> openPolicyAccess())
                .show();
            return;
        }

        java.util.Calendar now = java.util.Calendar.getInstance();
        TimePickerDialog picker = new TimePickerDialog(this, (view, hour, minute) -> {
            String[] modes = {"Ring", "Vibrate", "Silent"};
            new AlertDialog.Builder(this)
                .setTitle("Set mode at " + String.format(Locale.getDefault(), "%02d:%02d", hour, minute))
                .setItems(modes, (dialog, which) -> {
                    String mode = which == 0 ? "RING" : which == 1 ? "VIBRATE" : "SILENT";
                    ModeSchedule schedule = new ModeSchedule(
                        String.valueOf(System.currentTimeMillis()), hour, minute, mode, true
                    );
                    ScheduleStore.upsert(this, schedule);

                    if (ModeScheduler.canScheduleExact(this)) {
                        ModeScheduler.scheduleNext(this, schedule);
                        Toast.makeText(this, "Daily schedule added", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Saved. Allow Alarms & reminders to activate it.", Toast.LENGTH_LONG).show();
                        openAlarmAccess();
                    }
                    refreshScreen();
                }).show();
        }, now.get(java.util.Calendar.HOUR_OF_DAY), now.get(java.util.Calendar.MINUTE), false);
        picker.setTitle("Choose daily switch time");
        picker.show();
    }

    private void applyMode(String mode) {
        if (!ModeScheduler.hasPolicyAccess(this)) {
            new AlertDialog.Builder(this)
                .setTitle("Permission needed")
                .setMessage("Allow Notification Policy Access in Android Settings before changing the ringer mode.")
                .setNegativeButton("Cancel", (d, w) -> {})
                .setPositiveButton("Open Settings", (d, w) -> openPolicyAccess())
                .show();
            return;
        }

        int target = "VIBRATE".equals(mode) ? AudioManager.RINGER_MODE_VIBRATE
            : "SILENT".equals(mode) ? AudioManager.RINGER_MODE_SILENT
            : AudioManager.RINGER_MODE_NORMAL;

        try {
            AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            if (audio != null) audio.setRingerMode(target);
            refreshScreen();
            Toast.makeText(this, mode.substring(0, 1) + mode.substring(1).toLowerCase(Locale.ROOT) + " mode enabled", Toast.LENGTH_SHORT).show();
        } catch (SecurityException exception) {
            Toast.makeText(this, "Android did not allow the mode change. Check Notification Policy Access.", Toast.LENGTH_LONG).show();
        }
    }

    private void openPolicyAccess() {
        try {
            ModeScheduler.openPolicySettings(this);
        } catch (Exception exception) {
            Toast.makeText(this, "Open Settings → Special app access → Do Not Disturb access.", Toast.LENGTH_LONG).show();
        }
    }

    private void openAlarmAccess() {
        try {
            ModeScheduler.openExactAlarmSettings(this);
        } catch (Exception exception) {
            Toast.makeText(this, "Open Settings → Apps → Special app access → Alarms & reminders.", Toast.LENGTH_LONG).show();
        }
    }

    private Date dateForTime(int hour, int minute) {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(java.util.Calendar.HOUR_OF_DAY, hour);
        cal.set(java.util.Calendar.MINUTE, minute);
        return cal.getTime();
    }

    private Button modeButton(String label, String mode, int background, int foreground) {
        Button result = button(label, background, foreground);
        result.setTextSize(13);
        result.setOnClickListener(v -> applyMode(mode));
        LinearLayout.LayoutParams lp = weightedParams();
        lp.setMargins(0, 0, dp(7), 0);
        result.setLayoutParams(lp);
        return result;
    }

    private Button button(String label, int background, int foreground) {
        Button result = new Button(this);
        result.setText(label);
        result.setTextColor(foreground);
        result.setTextSize(14);
        result.setAllCaps(false);
        result.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        result.setPadding(dp(10), dp(10), dp(10), dp(10));
        result.setBackground(roundRect(background, 14));
        result.setMinHeight(dp(48));
        result.setStateListAnimator(null);
        return result;
    }

    private Button smallButton(String label, int background, int foreground) {
        Button result = button(label, background, foreground);
        result.setTextSize(12);
        result.setMinHeight(dp(40));
        result.setPadding(dp(4), 0, dp(4), 0);
        return result;
    }

    private LinearLayout card() {
        LinearLayout result = new LinearLayout(this);
        result.setOrientation(LinearLayout.VERTICAL);
        result.setBackground(roundRect(CARD, 20));
        return result;
    }

    private TextView text(String value, int size, int color, boolean bold) {
        TextView result = new TextView(this);
        result.setText(value);
        result.setTextSize(size);
        result.setTextColor(color);
        result.setTypeface(Typeface.DEFAULT, bold ? Typeface.BOLD : Typeface.NORMAL);
        return result;
    }

    private GradientDrawable roundRect(int color, int radius) {
        GradientDrawable shape = new GradientDrawable();
        shape.setColor(color);
        shape.setCornerRadius(dp(radius));
        return shape;
    }

    private LinearLayout.LayoutParams fullWidth() {
        return new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private LinearLayout.LayoutParams marginParams(int left, int top, int right, int bottom) {
        LinearLayout.LayoutParams lp = fullWidth();
        lp.setMargins(left, top, right, bottom);
        return lp;
    }

    private LinearLayout.LayoutParams weightedParams() {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(50), 1f);
        lp.setMargins(0, 0, dp(5), 0);
        return lp;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
