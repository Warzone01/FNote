//package com.kirdevelopment.fnotes.database
//
//import androidx.room.DatabaseConfiguration
//import androidx.room.InvalidationTracker
//import androidx.sqlite.db.SupportSQLiteOpenHelper
//import com.kirdevelopment.fnotes.dao.NoteDao
//
//class MainNoteDatabate: NotesDatabase() {
//    override fun noteDao(): NoteDao {
//        return noteDao()
//    }
//
//    override fun createOpenHelper(config: DatabaseConfiguration?): SupportSQLiteOpenHelper {
//
//
//    override fun createInvalidationTracker(): InvalidationTracker {
//
//    }
//
//    override fun clearAllTables() {
//
//    }
//}