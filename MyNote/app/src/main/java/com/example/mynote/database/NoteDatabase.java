package com.example.mynote.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.mynote.dao.DAO;
import com.example.mynote.entiti.Note;

@Database(entities = {Note.class}, version = 1, exportSchema = false)
public abstract class NoteDatabase extends RoomDatabase {
    private static NoteDatabase noteDatabase;

    public static synchronized NoteDatabase getDatabase(Context cnt){
        if(noteDatabase == null){
            noteDatabase = Room.databaseBuilder(cnt, NoteDatabase.class, "note_db").build();
        }
        return noteDatabase;
    }

    public abstract DAO noteDao();
}
