package mz.geator.mensagens;

import android.content.Context;
import android.content.SharedPreferences;

public final class ConfigStore {
    private static final String PREFS = "geator_config";
    private static final String KEY_URL = "url";
    private static final String KEY_TOKEN = "token";

    private ConfigStore() {}

    public static void save(Context context, String url, String token) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_URL, normalizeUrl(url))
                .putString(KEY_TOKEN, token == null ? "" : token.trim())
                .apply();
    }

    public static String url(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getString(KEY_URL, "");
    }

    public static String token(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getString(KEY_TOKEN, "");
    }

    private static String normalizeUrl(String url) {
        String value = url == null ? "" : url.trim();
        while (value.endsWith("/")) {
            value = value.substring(0, value.length() - 1);
        }
        return value;
    }
}
