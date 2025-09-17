package com.example.notes;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class MainActivity extends AppCompatActivity implements NoteAdapter.OnNoteClickListener {
    
    private static final String TAG = "MainActivity";
    
    private SessionManager sessionManager;
    private ListView noteListView;
    private Button newNoteButton;
    private NoteAdapter adapter;
    private DatabaseHelper databaseHelper;
    private long userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            
            // Session manager
            sessionManager = new SessionManager(getApplicationContext());
            
            // Check if user is not logged in, redirect to LoginActivity
            sessionManager.checkLogin();
            
            setContentView(R.layout.activity_main);
            
            // Set the username in the action bar
            String username = sessionManager.getUsername();
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(username != null ? "Welcome, " + username : "My Notes");
            }

            noteListView = findViewById(R.id.noteListView);
            newNoteButton = findViewById(R.id.newNoteButton);

            // Initialize DatabaseHelper
            databaseHelper = new DatabaseHelper(this);
            userId = sessionManager.getUserId();
            
            // Load notes
            loadNotes();

            // Set click listener for creating a new note
            newNoteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(MainActivity.this, NoteDetailActivity.class);
                    startActivityForResult(intent, 1);
                }
            });

            // Set long click listener for deleting a note
            noteListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
                    showDeleteDialog(adapter, position);
                    return true;
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
            showToast("An error occurred. Please try again.");
        }
    }

    private void loadNotes() {
        if (userId == -1) {
            showToast("Error: Invalid user session");
            sessionManager.logoutUser();
            return;
        }
        
        new LoadNotesTask().execute();
    }
    
    private class LoadNotesTask extends AsyncTask<Void, Void, List<Note>> {
        @Override
        protected List<Note> doInBackground(Void... voids) {
            try {
                return databaseHelper.getAllNotes(userId);
            } catch (Exception e) {
                Log.e(TAG, "Error loading notes", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<Note> notes) {
            if (notes == null) {
                showToast("Error loading notes");
                return;
            }

            if (adapter == null) {
                // First time setup
                adapter = new NoteAdapter(MainActivity.this, notes, MainActivity.this);
                noteListView.setAdapter(adapter);
            } else {
                // Update existing adapter
                adapter.clear();
                adapter.addAll(notes);
                adapter.notifyDataSetChanged();
            }

            if (notes.isEmpty()) {
                showToast("No notes found. Tap + to create a new note.");
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload notes when returning from NoteDetailActivity
        if (userId != -1) {
            loadNotes();
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Refresh notes when returning from NoteDetailActivity
        if (requestCode == 1) {
            loadNotes();
        }
    }
    
    private void showToast(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDeleteDialog(final NoteAdapter adapter, final int position) {
        final Note noteToDelete = adapter.getItem(position);
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Note");
        builder.setMessage("Are you sure you want to delete this note?");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                new AsyncTask<Void, Void, Boolean>() {
                    @Override
                    protected Boolean doInBackground(Void... voids) {
                        try {
                            databaseHelper.deleteNoteById(noteToDelete.getId());
                            return true;
                        } catch (Exception e) {
                            Log.e(TAG, "Error deleting note", e);
                            return false;
                        }
                    }

                    @Override
                    protected void onPostExecute(Boolean success) {
                        if (success) {
                            adapter.remove(noteToDelete);
                            adapter.notifyDataSetChanged();
                            showToast("Note deleted");
                        } else {
                            showToast("Error deleting note");
                        }
                    }
                }.execute();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            sessionManager.logoutUser();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    public void onNoteClick(Note note) {
        // Handle note click (open NoteDetailActivity for editing)
        Intent intent = new Intent(MainActivity.this, NoteDetailActivity.class);
        intent.putExtra("noteId", note.getId());
        startActivityForResult(intent, 1);
    }
}
