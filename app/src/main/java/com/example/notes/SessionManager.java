package com.example.notes;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import java.util.HashMap;

public class SessionManager {
    // Shared Preferences
    private final SharedPreferences pref;
    
    // Editor for Shared preferences
    private final Editor editor;
    
    // Context
    private final Context _context;
    
    // Shared pref mode
    private final int PRIVATE_MODE = 0;
    
    // Sharedpref file name
    private static final String PREF_NAME = "AndroidHivePref";
    
    // All Shared Preferences Keys
    private static final String IS_LOGIN = "IsLoggedIn";
    
    // User name (make variable public to access from outside)
    public static final String KEY_ID = "id";
    public static final String KEY_NAME = "name";
    
    // Email address (make variable public to access from outside)
    public static final String KEY_EMAIL = "email";
    
    // Constructor
    public SessionManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }
    
    /**
     * Create login session
     * */
    public void createLoginSession(long id, String name) {
        // Storing login value as TRUE
        editor.putBoolean(IS_LOGIN, true);
        
        // Storing id and name in pref
        editor.putLong(KEY_ID, id);
        editor.putString(KEY_NAME, name);
        
        // commit changes
        editor.apply();
    }   
    
    /**
     * Check login method will check user login status
     * If false it will redirect user to login page
     * Else won't do anything
     * */
    public void checkLogin() {
        // Check login status
        if (!this.isLoggedIn()) {
            // user is not logged in redirect him to Login Activity
            Intent i = new Intent(_context, LoginActivity.class);
            // Closing all the Activities
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            
            // Add new Flag to start new Activity
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            
            // Staring Login Activity
            _context.startActivity(i);
        }
    }
    
    /**
     * Get stored session data
     * */
    public HashMap<String, String> getUserDetails() {
        HashMap<String, String> user = new HashMap<>();
        // user id
        user.put(KEY_ID, String.valueOf(pref.getLong(KEY_ID, 0)));
        
        // user name
        user.put(KEY_NAME, pref.getString(KEY_NAME, null));
        
        // return user
        return user;
    }
    
    /**
     * Clear session details and redirect to login screen
     * */
    public void logoutUser() {
        // Clearing all data from Shared Preferences
        editor.clear();
        editor.apply();
        
        // After logout redirect user to Login Activity
        Intent i = new Intent(_context, LoginActivity.class);
        // Closing all the Activities
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        
        // Staring Login Activity
        _context.startActivity(i);
    }
    
    /**
     * Get stored username
     * @return username or null if not logged in
     */
    public String getUsername() {
        return pref.getString(KEY_NAME, null);
    }
    
    /**
     * Get stored user ID
     * @return user ID or -1 if not logged in
     */
    public long getUserId() {
        return pref.getLong(KEY_ID, -1);
    }
    
    /**
     * Check if user is logged in
     * @return boolean - true if user is logged in, false otherwise
     */
    public boolean isLoggedIn() {
        return pref.getBoolean(IS_LOGIN, false);
    }
}
