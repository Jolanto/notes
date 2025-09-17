package com.example.notes;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class NoteAdapter extends ArrayAdapter<Note> {

    private Context context;
    private List<Note> notes;
    private OnNoteClickListener onNoteClickListener;

    public NoteAdapter(Context context, List<Note> notes, OnNoteClickListener onNoteClickListener) {
        super(context, 0, notes);
        this.context = context;
        this.notes = notes;
        this.onNoteClickListener = onNoteClickListener;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final Note note = getItem(position);
        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_note, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.noteTitle.setText(note.getHeading());

        // Set click listener for editing a note
        viewHolder.noteTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onNoteClickListener != null) {
                    onNoteClickListener.onNoteClick(note);
                }
            }
        });

        // Set click listener for delete button
        viewHolder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDeleteDialog(position);
            }
        });

        // Set long click listener for deleting a note
        convertView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                showDeleteDialog(position);
                return true;
            }
        });

        return convertView;
    }

    // Show delete confirmation dialog
    private void showDeleteDialog(final int position) {
        new AlertDialog.Builder(context)
            .setTitle("Delete Note")
            .setMessage("Are you sure you want to delete this note?")
            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    // Get the note to delete
                    final Note noteToDelete = getItem(position);
                    
                    // Remove from the list
                    notes.remove(position);
                    notifyDataSetChanged();
                    
                    // Delete from database in background
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            DatabaseHelper db = new DatabaseHelper(context);
                            db.deleteNoteById(noteToDelete.getId());
                            
                            // Show toast on UI thread
                            ((android.app.Activity)context).runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(context, "Note deleted", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }).start();
                }
            })
            .setNegativeButton(android.R.string.no, null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show();
    }

    // ViewHolder pattern for better performance
    private static class ViewHolder {
        TextView noteTitle;
        ImageButton deleteButton;

        ViewHolder(View view) {
            noteTitle = view.findViewById(R.id.noteTitle);
            deleteButton = view.findViewById(R.id.deleteButton);
        }
    }

    public interface OnNoteClickListener {
        void onNoteClick(Note note);
    }
}
