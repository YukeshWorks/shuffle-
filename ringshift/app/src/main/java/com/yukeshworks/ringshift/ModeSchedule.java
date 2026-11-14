package com.yukeshworks.ringshift;

import org.json.JSONException;
import org.json.JSONObject;

public class ModeSchedule {
    public String id;
    public int hour;
    public int minute;
    public String mode;
    public boolean enabled;
    public boolean hasEnd;
    public int endHour;
    public int endMinute;
    public String endMode;

    // Backward-compatible constructor for older single-switch rules already saved on a phone.
    public ModeSchedule(String id, int hour, int minute, String mode, boolean enabled) {
        this(id, hour, minute, mode, enabled, false, 0, 0, "RING");
    }

    public ModeSchedule(String id, int hour, int minute, String mode, boolean enabled,
                        boolean hasEnd, int endHour, int endMinute, String endMode) {
        this.id = id;
        this.hour = hour;
        this.minute = minute;
        this.mode = mode;
        this.enabled = enabled;
        this.hasEnd = hasEnd;
        this.endHour = endHour;
        this.endMinute = endMinute;
        this.endMode = endMode == null ? "RING" : endMode;
    }

    public JSONObject toJson() throws JSONException {
        JSONObject object = new JSONObject();
        object.put("id", id);
        object.put("hour", hour);
        object.put("minute", minute);
        object.put("mode", mode);
        object.put("enabled", enabled);
        object.put("hasEnd", hasEnd);
        object.put("endHour", endHour);
        object.put("endMinute", endMinute);
        object.put("endMode", endMode);
        return object;
    }

    public static ModeSchedule fromJson(JSONObject object) throws JSONException {
        return new ModeSchedule(
            object.getString("id"),
            object.getInt("hour"),
            object.getInt("minute"),
            object.getString("mode"),
            object.optBoolean("enabled", true),
            object.optBoolean("hasEnd", false),
            object.optInt("endHour", 0),
            object.optInt("endMinute", 0),
            object.optString("endMode", "RING")
        );
    }
}
