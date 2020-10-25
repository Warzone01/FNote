package com.kirdevelopment.fnotes.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import com.kirdevelopment.fnotes.R
import com.kirdevelopment.fnotes.database.NotesDatabase
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

class MainActivity : AppCompatActivity() {

    private val REQUEST_CODE_ADD_NOTE: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val imageAddNoteMain: ImageView = findViewById(R.id.imageAddNoteMain) // Main button to add note

        imageAddNoteMain.setOnClickListener {
            startActivityForResult(Intent(applicationContext,
                CreateNoteActivity::class.java),
                REQUEST_CODE_ADD_NOTE) // new activity whe we press the button (add new note)
        }
        getNotes()
    }

    private fun getNotes(){ // function getting notes from database

        doAsync {
            val list = NotesDatabase
                .getDatabase(applicationContext)
                .noteDao()
                .getAllNotes() //new list of notes (with main params(title and datetime))
            uiThread {
                Log.d("MY_NOTES", list.toString()) // show all notes in logs
            }
        }
    }
}