package com.example.notes;
// DatabaseHelper.java
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "notes.db";
    // Increment version to force database recreation
    private static final int DATABASE_VERSION = 2;

    // Table names
    public static final String TABLE_NOTES = "notes";
    public static final String TABLE_USERS = "users";
    
    // Common column names
    public static final String COLUMN_ID = "_id";
    
    // Notes table columns
    public static final String COLUMN_HEADING = "heading";
    public static final String COLUMN_DETAILS = "details";
    public static final String COLUMN_USER_ID = "user_id";
    
    // Users table columns
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_PASSWORD = "password";

    // Table create statements
    private static final String CREATE_TABLE_NOTES = 
            "CREATE TABLE " + TABLE_NOTES + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_HEADING + " TEXT,"
            + COLUMN_DETAILS + " TEXT,"
            + COLUMN_USER_ID + " INTEGER,"
            + "FOREIGN KEY(" + COLUMN_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_ID + ")"
            + ")";

    private static final String CREATE_TABLE_USERS =
            "CREATE TABLE " + TABLE_USERS + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_USERNAME + " TEXT UNIQUE,"
            + COLUMN_PASSWORD + " TEXT"
            + ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            // Create tables
            db.execSQL(CREATE_TABLE_USERS);
            db.execSQL(CREATE_TABLE_NOTES);
            Log.d("DatabaseHelper", "Database tables created");
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error creating tables: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            // Drop older tables if they exist
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTES);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
            
            // Create tables again
            onCreate(db);
            Log.d("DatabaseHelper", "Database upgraded from version " + oldVersion + " to " + newVersion);
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error upgrading database: " + e.getMessage());
            throw e;
        }
    }
    public void deleteNoteById(long noteId) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            int deletedRows = db.delete(TABLE_NOTES, COLUMN_ID + " = ?",
                    new String[]{String.valueOf(noteId)});
            if (deletedRows > 0) {
                // Note deleted successfully
            } else {
                // Handle deletion failure
            }
        } catch (SQLException e) {
            // Handle the exception
        } finally {
            db.close();
        }
    }
    // User related methods
    public long addUser(User user) {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            
            // First check if username already exists
            if (isUsernameTaken(user.getUsername())) {
                Log.d("DatabaseHelper", "Username already exists: " + user.getUsername());
                return -1; // Username already exists
            }
            
            ContentValues values = new ContentValues();
            values.put(COLUMN_USERNAME, user.getUsername());
            // Store password as plain text
            values.put(COLUMN_PASSWORD, user.getPassword());
            
            Log.d("DatabaseHelper", "Attempting to add user: " + user.getUsername());
            
            // Insert the user
            long userId = db.insertOrThrow(TABLE_USERS, null, values);
            Log.d("DatabaseHelper", "User added successfully with ID: " + userId);
            return userId;
            
        } catch (SQLException e) {
            Log.e("DatabaseHelper", "Error adding user: " + e.getMessage(), e);
            return -1;
        } finally {
            if (db != null) {
                try {
                    db.close();
                } catch (Exception e) {
                    Log.e("DatabaseHelper", "Error closing database", e);
                }
            }
        }
    }
    
    public User getUser(String username) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        
        try {
            db = this.getReadableDatabase();
            String selectQuery = "SELECT * FROM " + TABLE_USERS + " WHERE " + COLUMN_USERNAME + " = ?";
            cursor = db.rawQuery(selectQuery, new String[]{username});
            
            if (cursor != null && cursor.moveToFirst()) {
                try {
                    User user = new User();
                    long userId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID));
                    String dbUsername = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USERNAME));
                    String dbPassword = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD));
                    
                    user.setId(userId);
                    user.setUsername(dbUsername);
                    user.setPassword(dbPassword);
                    
                    Log.d("DatabaseHelper", "Retrieved user - ID: " + userId + 
                                          ", Username: " + dbUsername + 
                                          ", Password: " + dbPassword);
                    return user;
                } catch (Exception e) {
                    Log.e("DatabaseHelper", "Error creating user object", e);
                    return null;
                }
            }
            
            return null;
            
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error getting user: " + e.getMessage(), e);
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                try {
                    db.close();
                } catch (Exception e) {
                    Log.e("DatabaseHelper", "Error closing database", e);
                }
            }
        }
    }
    
    public boolean checkUser(String username, String password) {
        Log.d("DatabaseHelper", "Checking user: " + username);
        User user = getUser(username);
        if (user != null) {
            Log.d("DatabaseHelper", "User found. Checking password...");
            boolean passwordMatches = user.checkPassword(password);
            Log.d("DatabaseHelper", "Password " + (passwordMatches ? "matches" : "does not match"));
            return passwordMatches;
        }
        Log.d("DatabaseHelper", "User not found: " + username);
        return false;
    }
    
    public boolean isUsernameTaken(String username) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = this.getReadableDatabase();
            String selectQuery = "SELECT * FROM " + TABLE_USERS + " WHERE " + COLUMN_USERNAME + " = ?";
            
            cursor = db.rawQuery(selectQuery, new String[]{username});
            return cursor != null && cursor.getCount() > 0;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            // Don't close the database connection here as it might be used by the calling method
        }
    }
    
    private String hashPassword(String password) {
        try {
            // Create MessageDigest instance for MD5
            MessageDigest md = MessageDigest.getInstance("MD5");
            // Add password bytes to digest
            md.update(password.getBytes());
            // Get the hash's bytes
            byte[] bytes = md.digest();
            // Convert it to hexadecimal format
            StringBuilder sb = new StringBuilder();
            for (byte aByte : bytes) {
                sb.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));
            }
            // Get complete hashed password in hex format
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "";
        }
    }
    
    public long insertNote(String heading, String details, long userId) {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            
            Log.d("DatabaseHelper", "Inserting note - Title: " + heading + ", User ID: " + userId);
            
            ContentValues values = new ContentValues();
            values.put(COLUMN_HEADING, heading);
            values.put(COLUMN_DETAILS, details);
            values.put(COLUMN_USER_ID, userId);
            
            long id = db.insert(TABLE_NOTES, null, values);
            
            if (id == -1) {
                Log.e("DatabaseHelper", "Failed to insert note into database");
            } else {
                Log.d("DatabaseHelper", "Note inserted successfully with ID: " + id);
                
                // Verify the note was actually inserted
                Cursor cursor = db.query(TABLE_NOTES, 
                    new String[]{COLUMN_ID, COLUMN_HEADING, COLUMN_DETAILS, COLUMN_USER_ID},
                    COLUMN_ID + "=?",
                    new String[]{String.valueOf(id)},
                    null, null, null);
                
                if (cursor != null && cursor.moveToFirst()) {
                    Log.d("DatabaseHelper", "Verification - Found note with ID: " + 
                        cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                    cursor.close();
                } else {
                    Log.e("DatabaseHelper", "Verification failed - Note not found after insertion!");
                }
            }
            
            return id;
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error inserting note: " + e.getMessage());
            e.printStackTrace();
            return -1;
        } finally {
            if (db != null) {
                try {
                    db.close();
                } catch (Exception e) {
                    Log.e("DatabaseHelper", "Error closing database", e);
                }
            }
        }
    }

    public boolean updateNote(long noteId, String heading, String details) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_HEADING, heading);
        values.put(COLUMN_DETAILS, details);

        int rowsAffected = -1;

        try {
            rowsAffected = db.update(TABLE_NOTES, values, COLUMN_ID + " = ?",
                    new String[]{String.valueOf(noteId)});
        } catch (SQLException e) {
            // Handle the exception
        } finally {
            db.close();
        }

        return rowsAffected > 0;
    }

    public List<Note> getAllNotes(long userId) {
        List<Note> notes = new ArrayList<>();
        if (userId == -1) {
            Log.e("DatabaseHelper", "Invalid user ID (-1) provided to getAllNotes");
            return notes; // Return empty list if user ID is invalid
        }
        
        SQLiteDatabase db = null;
        Cursor cursor = null;
        
        try {
            db = this.getReadableDatabase();
            Log.d("DatabaseHelper", "Querying notes for user ID: " + userId);
            
            String[] columns = {COLUMN_ID, COLUMN_HEADING, COLUMN_DETAILS};
            String selection = COLUMN_USER_ID + "=?";
            String[] selectionArgs = {String.valueOf(userId)};
            String orderBy = COLUMN_ID + " DESC";
            
            Log.d("DatabaseHelper", "Query: SELECT " + String.join(", ", columns) + 
                                 " FROM " + TABLE_NOTES + 
                                 " WHERE " + selection + 
                                 " ORDER BY " + orderBy);
            
            cursor = db.query(TABLE_NOTES, columns, selection, selectionArgs, null, null, orderBy);
            
            if (cursor == null) {
                Log.e("DatabaseHelper", "Cursor is null after query");
                return notes;
            }
            
            Log.d("DatabaseHelper", "Found " + cursor.getCount() + " notes in database");

            if (cursor.moveToFirst()) {
                do {
                    try {
                        long id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID));
                        String heading = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_HEADING));
                        String details = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DETAILS));

                        Note note = new Note();
                        note.setId(id);
                        note.setHeading(heading);
                        note.setDetails(details);
                        note.setUserId(userId);

                        Log.d("DatabaseHelper", "Adding note - ID: " + id + ", Title: " + heading);
                        notes.add(note);
                    } catch (Exception e) {
                        Log.e("DatabaseHelper", "Error reading note data: " + e.getMessage());
                        e.printStackTrace();
                    }
                } while (cursor.moveToNext());
            } else {
                Log.d("DatabaseHelper", "No notes found for user ID: " + userId);
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error in getAllNotes: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    Log.e("DatabaseHelper", "Error closing cursor: " + e.getMessage());
                }
            }
            if (db != null) {
                try {
                    db.close();
                } catch (Exception e) {
                    Log.e("DatabaseHelper", "Error closing database: " + e.getMessage());
                }
            }
        }

        Log.d("DatabaseHelper", "Returning " + notes.size() + " notes to caller");
        return notes;
    }

    public Note getNoteById(long noteId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Note note = null;

        try {
            Cursor cursor = db.query(
                    TABLE_NOTES,
                    new String[]{COLUMN_ID, COLUMN_HEADING, COLUMN_DETAILS},
                    COLUMN_ID + " = ?",
                    new String[]{String.valueOf(noteId)},
                    null,
                    null,
                    null
            );

            if (cursor != null && cursor.moveToFirst()) {
                long id = cursor.getLong(cursor.getColumnIndex(COLUMN_ID));
                String heading = cursor.getString(cursor.getColumnIndex(COLUMN_HEADING));
                String details = cursor.getString(cursor.getColumnIndex(COLUMN_DETAILS));

                note = new Note();
                note.setId(id);
                note.setHeading(heading);
                note.setDetails(details);

                cursor.close();
            }
        } catch (SQLException e) {
            // Handle the exception
        } finally {
            db.close();
        }

        return note;
    }
}
