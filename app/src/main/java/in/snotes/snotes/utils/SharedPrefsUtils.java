package in.snotes.snotes.utils;

import android.content.Context;
import android.content.SharedPreferences;

import in.snotes.snotes.R;
import in.snotes.snotes.SNotesApplication;

public final class SharedPrefsUtils {

    private static SharedPreferences prefs;

    public static final String SHARED_PREFS_NAME = "notesPrefs";
    public static final String DEFAULT_APP_WIDGET_STRING = "aaaa";

    private static String KEY_PIN;

    private SharedPrefsUtils() {
    }

    // instantiate once in application class
    public static void instantiate(SNotesApplication application) {
        KEY_PIN = application.getString(R.string.pin);
        prefs = application.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
    }


    public static String getPin() {
        return prefs.getString(KEY_PIN, "0000");
    }

    public static void setPin(String pin) {
        prefs.edit()
                .putString(KEY_PIN, pin)
                .apply();
    }


    public static void clearPrefs() {
        prefs.edit().remove(KEY_PIN).apply();
    }


    public static void saveWidgetDataToPrefs(int appWidgetId, String noteId) {
        prefs.edit()
                .putString(String.valueOf(appWidgetId), noteId)
                .apply();
    }

    public static String getWidgetDataFromPrefs(int appWidgetId) {
        return prefs.getString(String.valueOf(appWidgetId), DEFAULT_APP_WIDGET_STRING);
    }

}
