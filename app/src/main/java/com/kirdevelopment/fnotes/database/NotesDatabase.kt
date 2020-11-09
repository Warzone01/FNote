package com.kirdevelopment.fnotes.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.kirdevelopment.fnotes.dao.NoteDao
import com.kirdevelopment.fnotes.entities.Note

/*
* class creating an object in database
* */

@Database(entities = arrayOf(Note::class), version = 1, exportSchema = false)
abstract class NotesDatabase: RoomDatabase() {
    abstract fun noteDao():NoteDao
    companion object {
        private var notesDatabase:NotesDatabase? = null
        fun getDatabase(context: Context): NotesDatabase {
            if (notesDatabase == null) {
                notesDatabase = Room.databaseBuilder(
                        context,
                        NotesDatabase::class.java,
                        "/sdcard/FNotes/db/notes_db")
                        .build()
            }
            return notesDatabase as NotesDatabase
        }
    }
}