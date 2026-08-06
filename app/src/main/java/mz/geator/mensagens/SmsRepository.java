package mz.geator.mensagens;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class SmsRepository {
    private static final String PREFS = "geator_sms_store";
    private static final String KEY_ITEMS = "items";
    private static final Object LOCK = new Object();

    private SmsRepository() {}

    public static List<SmsItem> all(Context context) {
        synchronized (LOCK) {
            List<SmsItem> result = new ArrayList<>();
            String raw = prefs(context).getString(KEY_ITEMS, "[]");
            try {
                JSONArray array = new JSONArray(raw);
                for (int i = 0; i < array.length(); i++) {
                    result.add(SmsItem.fromJson(array.getJSONObject(i)));
                }
            } catch (Exception ignored) {
            }
            result.sort(Comparator.comparingLong((SmsItem x) -> x.timestamp).reversed());
            return result;
        }
    }

    public static void upsert(Context context, SmsItem item) {
        synchronized (LOCK) {
            List<SmsItem> items = all(context);
            boolean replaced = false;
            for (int i = 0; i < items.size(); i++) {
                if (items.get(i).id.equals(item.id)) {
                    items.set(i, item);
                    replaced = true;
                    break;
                }
            }
            if (!replaced) items.add(item);
            if (items.size() > 300) items = items.subList(0, 300);
            save(context, items);
        }
    }

    public static boolean exists(Context context, String id) {
        for (SmsItem item : all(context)) {
            if (item.id.equals(id)) return true;
        }
        return false;
    }

    public static List<SmsItem> pending(Context context) {
        List<SmsItem> pending = new ArrayList<>();
        for (SmsItem item : all(context)) {
            if (!"sent".equals(item.status)) pending.add(item);
        }
        return pending;
    }

    private static void save(Context context, List<SmsItem> items) {
        JSONArray array = new JSONArray();
        for (SmsItem item : items) {
            try {
                array.put(item.toJson());
            } catch (Exception ignored) {
            }
        }
        prefs(context).edit().putString(KEY_ITEMS, array.toString()).apply();
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }
}
