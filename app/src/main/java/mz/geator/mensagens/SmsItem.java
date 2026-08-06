package mz.geator.mensagens;

import org.json.JSONException;
import org.json.JSONObject;

public final class SmsItem {
    public String id;
    public String sender;
    public String body;
    public long timestamp;
    public String status;
    public String lastError;
    public int attempts;

    public JSONObject toJson() throws JSONException {
        JSONObject o = new JSONObject();
        o.put("id", id);
        o.put("sender", sender);
        o.put("body", body);
        o.put("timestamp", timestamp);
        o.put("status", status);
        o.put("lastError", lastError);
        o.put("attempts", attempts);
        return o;
    }

    public static SmsItem fromJson(JSONObject o) {
        SmsItem item = new SmsItem();
        item.id = o.optString("id");
        item.sender = o.optString("sender");
        item.body = o.optString("body");
        item.timestamp = o.optLong("timestamp");
        item.status = o.optString("status", "pending");
        item.lastError = o.optString("lastError");
        item.attempts = o.optInt("attempts", 0);
        return item;
    }
}
