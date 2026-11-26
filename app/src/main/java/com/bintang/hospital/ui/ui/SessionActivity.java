package com.bintang.hospital.ui.ui;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionActivity {

    private static final String KEY_TOKEN = "access_token";

    private final SharedPreferences preferences;

    public SessionActivity(Context context) {
        preferences = context.getSharedPreferences("HospitalSession", Context.MODE_PRIVATE);
    }

    public void saveToken(String token) {
        preferences.edit().putString(KEY_TOKEN, token).apply();
    }

    public String getToken() {
        return preferences.getString(KEY_TOKEN, null);
    }

    public void clearSession() {
        preferences.edit().clear().apply();
    }
}