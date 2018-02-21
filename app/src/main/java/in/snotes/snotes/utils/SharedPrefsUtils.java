package in.snotes.snotes.utils;

import android.content.Context;
import android.content.SharedPreferences;

import in.snotes.snotes.SNotesApplication;

public final class SharedPrefsUtils {

    private static SharedPreferences prefs;

    public static final String SHARED_PREFS_NAME = "notesPrefs";
    public static final String DEFAULT_APP_WIDGET_STRING = "aaaa";


    private SharedPrefsUtils() {
    }

    // instantiate once in application class
    public static void instantiate(SNotesApplication application) {
        prefs = application.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
    }


    public static int getPin() {
        String pin = prefs.getString("pin", "0000");
        return Integer.parseInt(pin);
    }

    public static void setPin(String pin) {
        prefs.edit()
                .putString("pin", pin)
                .apply();
    }

    public static void setIsPinSet(boolean isPinSet) {
        prefs.edit()
                .putBoolean("isPinSet", isPinSet)
                .apply();
    }

    public static boolean isPinSet() {
        return prefs.getBoolean("isPinSet", false);
    }

    public static void clearPrefs() {
        prefs.edit().remove("pin").apply();
        prefs.edit().remove("isPinSet").apply();
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
