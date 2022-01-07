package com.example.mynote;

import com.example.mynote.entiti.Note;

public interface NoteListeners {
    void onNoteClicked(Note note, int position);
}
