package com.yukeshworks.ringshift;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public final class ScheduleStore {
    private static final String PREFS = "ringshift_settings";
    private static final String RULES = "daily_rules";

    private ScheduleStore() {}

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static ArrayList<ModeSchedule> getAll(Context context) {
        ArrayList<ModeSchedule> result = new ArrayList<>();
        String raw = prefs(context).getString(RULES, "[]");
        try {
            JSONArray array = new JSONArray(raw);
            for (int i = 0; i < array.length(); i++) {
                JSONObject item = array.optJSONObject(i);
                if (item != null) {
                    try {
                        result.add(ModeSchedule.fromJson(item));
                    } catch (JSONException ignored) {}
                }
            }
        } catch (JSONException ignored) {}
        return result;
    }

    public static ModeSchedule getById(Context context, String id) {
        for (ModeSchedule schedule : getAll(context)) {
            if (schedule.id.equals(id)) return schedule;
        }
        return null;
    }

    public static void save(Context context, ArrayList<ModeSchedule> schedules) {
        JSONArray array = new JSONArray();
        for (ModeSchedule schedule : schedules) {
            try {
                array.put(schedule.toJson());
            } catch (JSONException ignored) {}
        }
        prefs(context).edit().putString(RULES, array.toString()).apply();
    }

    public static void upsert(Context context, ModeSchedule value) {
        ArrayList<ModeSchedule> list = getAll(context);
        boolean replaced = false;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).id.equals(value.id)) {
                list.set(i, value);
                replaced = true;
                break;
            }
        }
        if (!replaced) list.add(value);
        save(context, list);
    }

    public static void delete(Context context, String id) {
        ArrayList<ModeSchedule> list = getAll(context);
        ArrayList<ModeSchedule> kept = new ArrayList<>();
        for (ModeSchedule value : list) {
            if (!value.id.equals(id)) kept.add(value);
        }
        save(context, kept);
    }
}
