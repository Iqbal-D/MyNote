package com.example.mynote.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;


import com.example.mynote.entiti.Note;

import java.util.List;

@Dao
public interface DAO {

    @Query("SELECT * FROM nyatat ORDER BY id DESC")
    List<Note> getNotes();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertNote(Note note);

    @Delete
    void deleteNote(Note note);
}
