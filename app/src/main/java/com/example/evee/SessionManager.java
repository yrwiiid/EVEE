package com.example.evee;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "user_session";
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public void saveUser(String id, String name, String email) {
        editor.putString("id", id);
        editor.putString("name", name);
        editor.putString("email", email);
        editor.apply();
    }

    public String getUserId() {
        return sharedPreferences.getString("id", null);
    }

    public String getUserName() {
        return sharedPreferences.getString("name", null);
    }

    public String getUserEmail() {
        return sharedPreferences.getString("email", null);
    }

    public void clearSession() {
        editor.clear();
        editor.apply();
    }

    public boolean isLoggedIn() {
        return getUserId() != null;
    }
}
