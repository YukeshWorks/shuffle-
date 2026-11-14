package com.yukeshworks.ringshift;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowInsets;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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
    private TextView nextChange;
    private TextView policyStatus;
    private TextView alarmStatus;
    private LinearLayout rulesList;

    private interface ModeSelected {
        void onSelected(String mode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);
        if (android.os.Build.VERSION.SDK_INT >= 29) {
            getWindow().setNavigationBarContrastEnforced(false);
        }
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        buildScreen();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshScreen();
        if (ModeScheduler.hasPolicyAccess(this) && ModeScheduler.canScheduleExact(this)) {
            ModeScheduler.scheduleAll(this);
            refreshScreen();
        }
    }

    private void buildScreen() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setClipToPadding(false);
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

        nextChange = text("NEXT · Checking schedules…", 12, LIME, true);
        nextChange.setPadding(0, dp(12), 0, 0);
        statusCard.addView(nextChange, fullWidth());

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
        Button policyButton = button("Open sound-mode settings", LIME, BG);
        policyButton.setOnClickListener(v -> openPolicyAccess());
        accessCard.addView(policyButton, marginParams(0, dp(10), 0, 0));

        alarmStatus = text("", 13, TEXT, true);
        alarmStatus.setPadding(0, dp(14), 0, 0);
        accessCard.addView(alarmStatus, fullWidth());
        Button alarmButton = button("Open exact-alarm settings", CARD_2, TEXT);
        alarmButton.setOnClickListener(v -> openAlarmAccess());
        accessCard.addView(alarmButton, marginParams(0, dp(9), 0, 0));

        TextView scheduleTitle = text("Daily schedules", 18, TEXT, true);
        scheduleTitle.setPadding(0, 0, 0, dp(4));
        page.addView(scheduleTitle, fullWidth());

        TextView scheduleHint = text(
            "Set a start and end time. Your chosen mode stays active in between, then RingShift switches back automatically.",
            12, MUTED, false
        );
        scheduleHint.setPadding(0, 0, 0, dp(11));
        page.addView(scheduleHint, fullWidth());

        LinearLayout scheduleCard = card();
        scheduleCard.setPadding(dp(14), dp(12), dp(14), dp(14));
        page.addView(scheduleCard, marginParams(0, 0, 0, 16));

        rulesList = new LinearLayout(this);
        rulesList.setOrientation(LinearLayout.VERTICAL);
        scheduleCard.addView(rulesList, fullWidth());

        Button addButton = button("+  Add time range", LIME, BG);
        addButton.setOnClickListener(v -> addSchedule());
        scheduleCard.addView(addButton, marginParams(0, dp(8), 0, 0));

        TextView footer = text(
            "Rules repeat every day and stay on this phone. For overnight hours, set an end time earlier than the start time. Avoid overlapping ranges.",
            11, MUTED, false
        );
        footer.setGravity(Gravity.CENTER);
        footer.setPadding(dp(6), dp(6), dp(6), dp(10));
        page.addView(footer, fullWidth());

        setContentView(scroll);

        // Android 15 draws app content edge-to-edge. Explicitly inset the scroll content
        // so the heading never sits underneath status icons or the bottom gesture bar.
        scroll.setOnApplyWindowInsetsListener((view, insets) -> {
            int topInset = insets.getSystemWindowInsetTop();
            int bottomInset = insets.getSystemWindowInsetBottom();
            page.setPadding(dp(20), dp(20) + topInset, dp(20), dp(28) + bottomInset);
            return insets.consumeSystemWindowInsets();
        });
        scroll.requestApplyInsets();

        page.setAlpha(0f);
        page.setTranslationY(dp(7));
        page.animate().alpha(1f).translationY(0f).setDuration(260).start();
    }

    private void refreshScreen() {
        if (currentMode == null || rulesList == null) return;

        AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        String modeLabel = "Ring";
        if (audio != null) {
            int mode = audio.getRingerMode();
            if (mode == AudioManager.RINGER_MODE_VIBRATE) modeLabel = "Vibrate only";
            else if (mode == AudioManager.RINGER_MODE_SILENT) modeLabel = "Silent";
        }
        if (!modeLabel.contentEquals(currentMode.getText())) {
            currentMode.setText(modeLabel);
            currentMode.setAlpha(0.55f);
            currentMode.animate().alpha(1f).setDuration(180).start();
        }

        boolean policy = ModeScheduler.hasPolicyAccess(this);
        boolean exact = ModeScheduler.canScheduleExact(this);

        policyStatus.setText((policy ? "✓ " : "1. ") + "Notification Policy Access  " + (policy ? "Granted" : "Needed"));
        policyStatus.setTextColor(policy ? LIME : TEXT);
        alarmStatus.setText((exact ? "✓ " : "2. ") + "Alarms & reminders  " + (exact ? "Granted" : "Needed"));
        alarmStatus.setTextColor(exact ? LIME : TEXT);

        ArrayList<ModeSchedule> schedules = ScheduleStore.getAll(this);
        nextChange.setText(buildNextChangeSummary(schedules));

        rulesList.removeAllViews();
        if (schedules.isEmpty()) {
            TextView empty = text("No time ranges yet. Add one to automate your sound mode.", 13, MUTED, false);
            empty.setPadding(dp(3), dp(8), dp(3), dp(8));
            rulesList.addView(empty, fullWidth());
        } else {
            for (ModeSchedule schedule : schedules) addScheduleRow(schedule);
        }
    }

    private String buildNextChangeSummary(ArrayList<ModeSchedule> schedules) {
        Calendar now = Calendar.getInstance();
        Calendar best = null;
        String target = null;
        int bestPhase = ModeScheduler.PHASE_START;

        for (ModeSchedule schedule : schedules) {
            if (!schedule.enabled) continue;
            Calendar start = ModeScheduler.nextOccurrence(schedule.hour, schedule.minute);
            if (best == null || start.before(best)) {
                best = start;
                target = schedule.mode;
                bestPhase = ModeScheduler.PHASE_START;
            }

            if (schedule.hasEnd) {
                Calendar end = ModeScheduler.nextOccurrence(schedule.endHour, schedule.endMinute);
                if (best == null || end.before(best)) {
                    best = end;
                    target = schedule.endMode;
                    bestPhase = ModeScheduler.PHASE_END;
                }
            }
        }

        if (best == null) return "NEXT · No enabled time ranges";
        String day = best.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR)
            ? "Today" : "Tomorrow";
        String action = bestPhase == ModeScheduler.PHASE_END ? "Return to " : "Switch to ";
        return "NEXT · " + day + " " + formatTime(best.get(Calendar.HOUR_OF_DAY), best.get(Calendar.MINUTE))
            + " · " + action + prettyMode(target);
    }

    private void addScheduleRow(ModeSchedule schedule) {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(dp(10), dp(9), dp(7), dp(9));
        row.setBackground(roundRect(CARD_2, 14));

        LinearLayout details = new LinearLayout(this);
        details.setOrientation(LinearLayout.VERTICAL);
        details.setGravity(Gravity.CENTER_VERTICAL);

        String state = schedule.enabled ? "" : "  ·  PAUSED";
        String title;
        String detail;
        if (schedule.hasEnd) {
            title = formatTime(schedule.hour, schedule.minute) + "  →  "
                + formatTime(schedule.endHour, schedule.endMinute) + state;
            detail = prettyMode(schedule.mode) + "  →  " + prettyMode(schedule.endMode) + "  ·  Every day";
            if (schedule.endHour * 60 + schedule.endMinute <= schedule.hour * 60 + schedule.minute) {
                detail += "  ·  Overnight";
            }
        } else {
            // Older saved rules are kept and displayed as their original single daily switch.
            title = "At " + formatTime(schedule.hour, schedule.minute) + state;
            detail = "Switch to " + prettyMode(schedule.mode) + "  ·  Every day";
        }

        TextView timeText = text(title, 14, schedule.enabled ? TEXT : MUTED, true);
        TextView detailText = text(detail, 11, MUTED, false);
        detailText.setPadding(0, dp(4), 0, 0);
        details.addView(timeText, fullWidth());
        details.addView(detailText, fullWidth());
        row.addView(details, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        Button toggle = smallButton(schedule.enabled ? "ON" : "OFF",
            schedule.enabled ? LIME : CARD, schedule.enabled ? BG : MUTED);
        toggle.setOnClickListener(v -> {
            schedule.enabled = !schedule.enabled;
            ScheduleStore.upsert(this, schedule);
            if (schedule.enabled) {
                if (ModeScheduler.canScheduleExact(this)) ModeScheduler.scheduleAll(this);
                else {
                    Toast.makeText(this, "Allow Alarms & reminders to activate this range.", Toast.LENGTH_LONG).show();
                    openAlarmAccess();
                }
            } else {
                ModeScheduler.cancel(this, schedule.id);
            }
            refreshScreen();
        });
        row.addView(toggle, new LinearLayout.LayoutParams(dp(54), dp(40)));

        Button remove = smallButton("×", CARD, RED);
        remove.setTextSize(19);
        remove.setOnClickListener(v -> new AlertDialog.Builder(this, AlertDialog.THEME_DEVICE_DEFAULT_DARK)
            .setTitle("Delete time range?")
            .setMessage("This daily rule will be removed from this phone.")
            .setNegativeButton("Keep", (d, w) -> {})
            .setPositiveButton("Delete", (d, w) -> {
                ModeScheduler.cancel(this, schedule.id);
                ScheduleStore.delete(this, schedule.id);
                refreshScreen();
            }).show());
        LinearLayout.LayoutParams removeParams = new LinearLayout.LayoutParams(dp(40), dp(40));
        removeParams.leftMargin = dp(5);
        row.addView(remove, removeParams);

        int index = rulesList.getChildCount();
        rulesList.addView(row, marginParams(0, 0, 0, dp(7)));
        row.setAlpha(0f);
        row.setTranslationY(dp(5));
        row.animate().alpha(1f).translationY(0f).setStartDelay(index * 28L).setDuration(190).start();
    }

    private void addSchedule() {
        if (!ModeScheduler.hasPolicyAccess(this)) {
            new AlertDialog.Builder(this, AlertDialog.THEME_DEVICE_DEFAULT_DARK)
                .setTitle("Allow sound-mode access")
                .setMessage("Android requires Notification Policy Access before RingShift can change Ring, Vibrate, or Silent.")
                .setNegativeButton("Cancel", (d, w) -> {})
                .setPositiveButton("Open Settings", (d, w) -> openPolicyAccess())
                .show();
            return;
        }
        showTimeRangeDialog();
    }

    private void showTimeRangeDialog() {
        Calendar start = Calendar.getInstance();
        start.add(Calendar.MINUTE, 1);
        start.set(Calendar.SECOND, 0);
        start.set(Calendar.MILLISECOND, 0);

        Calendar end = (Calendar) start.clone();
        end.add(Calendar.HOUR_OF_DAY, 1);

        final int[] startTime = {start.get(Calendar.HOUR_OF_DAY), start.get(Calendar.MINUTE)};
        final int[] endTime = {end.get(Calendar.HOUR_OF_DAY), end.get(Calendar.MINUTE)};
        final String[] activeMode = {"SILENT"};
        final String[] returnMode = {"RING"};

        Dialog dialog = new Dialog(this);
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(20), dp(20), dp(20), dp(18));
        panel.setBackground(roundRect(CARD, 22));

        TextView title = text("Create a time range", 21, TEXT, true);
        panel.addView(title, fullWidth());
        TextView hint = text("Choose when the quiet or vibration mode should begin and end.", 12, MUTED, false);
        hint.setPadding(0, dp(5), 0, dp(16));
        panel.addView(hint, fullWidth());

        LinearLayout timeRow = new LinearLayout(this);
        timeRow.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout fromColumn = new LinearLayout(this);
        fromColumn.setOrientation(LinearLayout.VERTICAL);
        TextView fromLabel = text("FROM", 10, MUTED, true);
        fromLabel.setLetterSpacing(0.1f);
        fromColumn.addView(fromLabel, fullWidth());
        Button fromButton = button(formatTime(startTime[0], startTime[1]), CARD_2, TEXT);
        fromButton.setTextSize(15);
        fromButton.setOnClickListener(v -> showTimePicker("Start time", startTime[0], startTime[1], (h, m) -> {
            startTime[0] = h;
            startTime[1] = m;
            fromButton.setText(formatTime(h, m));
        }));
        fromColumn.addView(fromButton, marginParams(0, dp(7), 0, 0));

        LinearLayout toColumn = new LinearLayout(this);
        toColumn.setOrientation(LinearLayout.VERTICAL);
        TextView toLabel = text("UNTIL", 10, MUTED, true);
        toLabel.setLetterSpacing(0.1f);
        toColumn.addView(toLabel, fullWidth());
        Button untilButton = button(formatTime(endTime[0], endTime[1]), CARD_2, TEXT);
        untilButton.setTextSize(15);
        untilButton.setOnClickListener(v -> showTimePicker("End time", endTime[0], endTime[1], (h, m) -> {
            endTime[0] = h;
            endTime[1] = m;
            untilButton.setText(formatTime(h, m));
        }));
        toColumn.addView(untilButton, marginParams(0, dp(7), 0, 0));

        timeRow.addView(fromColumn, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        LinearLayout.LayoutParams toParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        toParams.leftMargin = dp(10);
        timeRow.addView(toColumn, toParams);
        panel.addView(timeRow, marginParams(0, 0, 0, dp(15)));

        TextView activeLabel = text("MODE DURING THIS RANGE", 10, MUTED, true);
        activeLabel.setLetterSpacing(0.08f);
        panel.addView(activeLabel, fullWidth());
        Button activeButton = button("Silent  ▾", CARD_2, LIME);
        activeButton.setGravity(Gravity.CENTER);
        activeButton.setOnClickListener(v -> showModePicker("Mode during this range", activeMode[0], selected -> {
            activeMode[0] = selected;
            activeButton.setText(prettyMode(selected) + "  ▾");
            activeButton.setTextColor(LIME);
        }));
        panel.addView(activeButton, marginParams(0, dp(7), 0, dp(14)));

        TextView returnLabel = text("AFTER THE END TIME, SWITCH BACK TO", 10, MUTED, true);
        returnLabel.setLetterSpacing(0.04f);
        panel.addView(returnLabel, fullWidth());
        Button returnButton = button("Ring  ▾", CARD_2, TEXT);
        returnButton.setOnClickListener(v -> showModePicker("Mode after the range", returnMode[0], selected -> {
            returnMode[0] = selected;
            returnButton.setText(prettyMode(selected) + "  ▾");
        }));
        panel.addView(returnButton, marginParams(0, dp(7), 0, dp(10)));

        TextView overnightHint = text(
            "Repeats every day. An end time earlier than the start time means the range continues overnight.",
            11, MUTED, false
        );
        overnightHint.setPadding(0, dp(3), 0, dp(15));
        panel.addView(overnightHint, fullWidth());

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        Button cancel = button("Cancel", CARD_2, TEXT);
        cancel.setOnClickListener(v -> dialog.dismiss());
        Button save = button("Save range", LIME, BG);
        save.setOnClickListener(v -> {
            if (startTime[0] == endTime[0] && startTime[1] == endTime[1]) {
                Toast.makeText(this, "Start and end times must be different.", Toast.LENGTH_LONG).show();
                return;
            }

            ModeSchedule schedule = new ModeSchedule(
                String.valueOf(System.currentTimeMillis()),
                startTime[0], startTime[1], activeMode[0], true,
                true, endTime[0], endTime[1], returnMode[0]
            );
            ScheduleStore.upsert(this, schedule);
            dialog.dismiss();

            if (ModeScheduler.canScheduleExact(this)) {
                ModeScheduler.scheduleAll(this);
                Toast.makeText(this, "Daily time range saved.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Saved. Allow Alarms & reminders to activate it.", Toast.LENGTH_LONG).show();
                openAlarmAccess();
            }
            refreshScreen();
        });
        actions.addView(cancel, new LinearLayout.LayoutParams(0, dp(50), 1f));
        LinearLayout.LayoutParams saveParams = new LinearLayout.LayoutParams(0, dp(50), 1f);
        saveParams.leftMargin = dp(8);
        actions.addView(save, saveParams);
        panel.addView(actions, fullWidth());

        dialog.setContentView(panel);
        dialog.show();
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            WindowManager.LayoutParams attributes = window.getAttributes();
            attributes.dimAmount = 0.62f;
            window.setAttributes(attributes);
            window.setLayout(getResources().getDisplayMetrics().widthPixels - dp(36),
                WindowManager.LayoutParams.WRAP_CONTENT);
            window.setGravity(Gravity.CENTER);
        }
        panel.setAlpha(0f);
        panel.setTranslationY(dp(8));
        panel.animate().alpha(1f).translationY(0f).setDuration(180).start();
    }

    private interface TimeSelected {
        void onSelected(int hour, int minute);
    }

    private void showTimePicker(String title, int hour, int minute, TimeSelected callback) {
        TimePickerDialog picker = new TimePickerDialog(
            this,
            AlertDialog.THEME_DEVICE_DEFAULT_DARK,
            (view, selectedHour, selectedMinute) -> callback.onSelected(selectedHour, selectedMinute),
            hour, minute, false // 12-hour clock with a clear AM/PM selector.
        );
        picker.setTitle(title);
        picker.show();
    }

    private void showModePicker(String title, String selectedMode, ModeSelected callback) {
        String[] labels = {"Ring", "Vibrate", "Silent"};
        String[] values = {"RING", "VIBRATE", "SILENT"};
        int selectedIndex = "VIBRATE".equals(selectedMode) ? 1 : "SILENT".equals(selectedMode) ? 2 : 0;

        new AlertDialog.Builder(this, AlertDialog.THEME_DEVICE_DEFAULT_DARK)
            .setTitle(title)
            .setSingleChoiceItems(labels, selectedIndex, (dialog, which) -> {
                callback.onSelected(values[which]);
                dialog.dismiss();
            })
            .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
            .show();
    }

    private void applyMode(String mode) {
        if (!ModeScheduler.hasPolicyAccess(this)) {
            new AlertDialog.Builder(this, AlertDialog.THEME_DEVICE_DEFAULT_DARK)
                .setTitle("Permission needed")
                .setMessage("Allow Notification Policy Access in Android Settings before changing the ringer mode.")
                .setNegativeButton("Cancel", (d, w) -> {})
                .setPositiveButton("Open Settings", (d, w) -> openPolicyAccess())
                .show();
            return;
        }

        ModeScheduler.applyMode(this, mode);
        refreshScreen();
        Toast.makeText(this, prettyMode(mode) + " mode enabled", Toast.LENGTH_SHORT).show();
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

    private String formatTime(int hour, int minute) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        return new SimpleDateFormat("h:mm a", Locale.getDefault()).format(cal.getTime());
    }

    private String prettyMode(String mode) {
        if ("VIBRATE".equals(mode)) return "Vibrate";
        if ("SILENT".equals(mode)) return "Silent";
        return "Ring";
    }

    private Button modeButton(String label, String mode, int background, int foreground) {
        Button result = button(label, background, foreground);
        result.setTextSize(13);
        result.setOnClickListener(v -> applyMode(mode));
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
        addPressAnimation(result);
        return result;
    }

    private void addPressAnimation(Button target) {
        target.setOnTouchListener((view, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                view.animate().scaleX(0.975f).scaleY(0.975f).setDuration(75).start();
            } else if (event.getAction() == MotionEvent.ACTION_UP
                || event.getAction() == MotionEvent.ACTION_CANCEL) {
                view.animate().scaleX(1f).scaleY(1f).setDuration(125).start();
            }
            return false; // Preserve the normal button click.
        });
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
