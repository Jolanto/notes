package com.example.notes;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.app.AlertDialog;

import androidx.appcompat.app.AppCompatActivity;

public class NoteDetailActivity extends AppCompatActivity {

    private EditText noteHeadingEditText;
    private EditText noteDetailsEditText;
    private Button saveNoteButton;

    private long noteId;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Session manager
        sessionManager = new SessionManager(getApplicationContext());
        
        // Check if user is not logged in, redirect to LoginActivity
        sessionManager.checkLogin();
        
        setContentView(R.layout.activity_note_detail);
        
        // Set the title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(noteId == -1 ? "New Note" : "Edit Note");
        }

        noteHeadingEditText = findViewById(R.id.noteHeadingEditText);
        noteDetailsEditText = findViewById(R.id.noteDetailsEditText);
        saveNoteButton = findViewById(R.id.saveNoteButton);

        // Get noteId from the intent (if available)
        noteId = getIntent().getLongExtra("noteId", -1);

        if (noteId != -1) {
            // Load existing note details for editing
            loadNoteDetails(noteId);
        }

        // Set click listener for saving a note
        saveNoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveNote();
            }
        });
    }

    private void loadNoteDetails(long noteId) {
        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        Note note = databaseHelper.getNoteById(noteId);

        if (note != null) {
            noteHeadingEditText.setText(note.getHeading());
            noteDetailsEditText.setText(note.getDetails());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (noteId != -1) {
            getMenuInflater().inflate(R.menu.note_detail_menu, menu);
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        
        if (id == android.R.id.home) {
            // Handle the back button in the action bar
            onBackPressed();
            return true;
        } else if (id == R.id.action_delete) {
            // Handle delete action
            deleteNote();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
    
    private void deleteNote() {
        if (noteId != -1) {
            new AlertDialog.Builder(this)
                .setTitle("Delete Note")
                .setMessage("Are you sure you want to delete this note?")
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    DatabaseHelper databaseHelper = new DatabaseHelper(this);
                    databaseHelper.deleteNoteById(noteId);
                    Toast.makeText(this, "Note deleted", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton(android.R.string.no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
        } else {
            finish();
        }
    }
    
    private void saveNote() {
        String heading = noteHeadingEditText.getText().toString().trim();
        String details = noteDetailsEditText.getText().toString().trim();

        // Validate input
        if (TextUtils.isEmpty(heading)) {
            noteHeadingEditText.setError("Please enter a heading");
            noteHeadingEditText.requestFocus();
            return;
        }

        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        long userId = sessionManager.getUserId();

        if (noteId == -1) {
            // New note
            long newNoteId = databaseHelper.insertNote(heading, details, userId);
            if (newNoteId != -1) {
                Toast.makeText(this, "Note saved successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Error saving note", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Existing note
            boolean updated = databaseHelper.updateNote(noteId, heading, details);
            if (updated) {
                Toast.makeText(this, "Note updated successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Error updating note", Toast.LENGTH_SHORT).show();
            }
        }

        // Navigate back to the main page
        finish();
    }
}

