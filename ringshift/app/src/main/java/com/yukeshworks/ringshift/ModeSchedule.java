package com.yukeshworks.ringshift;

import org.json.JSONException;
import org.json.JSONObject;

public class ModeSchedule {
    public String id;
    public int hour;
    public int minute;
    public String mode;
    public boolean enabled;

    public ModeSchedule(String id, int hour, int minute, String mode, boolean enabled) {
        this.id = id;
        this.hour = hour;
        this.minute = minute;
        this.mode = mode;
        this.enabled = enabled;
    }

    public JSONObject toJson() throws JSONException {
        JSONObject object = new JSONObject();
        object.put("id", id);
        object.put("hour", hour);
        object.put("minute", minute);
        object.put("mode", mode);
        object.put("enabled", enabled);
        return object;
    }

    public static ModeSchedule fromJson(JSONObject object) throws JSONException {
        return new ModeSchedule(
            object.getString("id"),
            object.getInt("hour"),
            object.getInt("minute"),
            object.getString("mode"),
            object.optBoolean("enabled", true)
        );
    }
}
