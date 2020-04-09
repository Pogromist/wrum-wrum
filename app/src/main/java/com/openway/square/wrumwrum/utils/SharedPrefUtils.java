package com.openway.square.wrumwrum.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import static android.content.Context.MODE_PRIVATE;

public class SharedPrefUtils {

    public static final String SHARED_PREFERENCES_KEY = "data_base";

    public static void saveString(Context context, final String key, final String value) {
        SharedPreferences preferences = context.getSharedPreferences(SHARED_PREFERENCES_KEY, MODE_PRIVATE);
        preferences.edit().putString(key, value).apply();
    }

    public static String getString(Context context, final String key) {
        SharedPreferences preferences = context.getSharedPreferences(SHARED_PREFERENCES_KEY, MODE_PRIVATE);
        return preferences.getString(key, "");
    }

    public static void remove(Context context, final String key) {
        SharedPreferences preferences = context.getSharedPreferences(SHARED_PREFERENCES_KEY, MODE_PRIVATE);
        preferences.edit().remove(key).apply();
    }
}
