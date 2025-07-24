package com.example.universalyoganative;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "yoga_session";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_ROLE = "user_role";

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private Context context;

    public SessionManager(Context context) {
        this.context = context;
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = preferences.edit();
    }

    /**
     * Create login session
     */
    public void createLoginSession(User user) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putLong(KEY_USER_ID, user.getId());
        editor.putString(KEY_USER_NAME, user.getName());
        editor.putString(KEY_USER_EMAIL, user.getEmail());
        editor.putString(KEY_USER_ROLE, user.getRole());
        editor.apply();
    }

    /**
     * Check if user is logged in
     */
    public boolean isLoggedIn() {
        return preferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    /**
     * Get logged in user info
     */
    public User getLoggedInUser() {
        if (!isLoggedIn()) {
            return null;
        }

        User user = new User();
        user.setId(preferences.getLong(KEY_USER_ID, -1));
        user.setName(preferences.getString(KEY_USER_NAME, ""));
        user.setEmail(preferences.getString(KEY_USER_EMAIL, ""));
        user.setRole(preferences.getString(KEY_USER_ROLE, "admin"));
        return user;
    }

    /**
     * Get user ID
     */
    public long getUserId() {
        return preferences.getLong(KEY_USER_ID, -1);
    }

    /**
     * Get user name
     */
    public String getUserName() {
        return preferences.getString(KEY_USER_NAME, "");
    }

    /**
     * Get user email
     */
    public String getUserEmail() {
        return preferences.getString(KEY_USER_EMAIL, "");
    }

    /**
     * Get user role
     */
    public String getUserRole() {
        return preferences.getString(KEY_USER_ROLE, "admin");
    }

    /**
     * Check if user is admin
     */
    public boolean isAdmin() {
        return "admin".equalsIgnoreCase(getUserRole());
    }

    /**
     * Logout user and clear session
     */
    public void logoutUser() {
        editor.clear();
        editor.apply();
    }

    /**
     * Update user session info
     */
    public void updateUserSession(User user) {
        editor.putString(KEY_USER_NAME, user.getName());
        editor.putString(KEY_USER_EMAIL, user.getEmail());
        editor.putString(KEY_USER_ROLE, user.getRole());
        editor.apply();
    }
} 