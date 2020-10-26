package com.kirdevelopment.fnotes.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.kirdevelopment.fnotes.R
import com.kirdevelopment.fnotes.adapters.NotesAdapter
import com.kirdevelopment.fnotes.database.NotesDatabase
import com.kirdevelopment.fnotes.entities.Note
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {

    private val REQUEST_CODE_ADD_NOTE: Int = 1

    private lateinit var notesRecyclerView: RecyclerView
    private var noteList: ArrayList<Note> = ArrayList()
    private lateinit var noteAdapter: NotesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val imageAddNoteMain: ImageView = findViewById(R.id.imageAddNoteMain) // Main button to add note

        imageAddNoteMain.setOnClickListener {
            startActivityForResult(Intent(applicationContext,
                CreateNoteActivity::class.java),
                REQUEST_CODE_ADD_NOTE) // new activity whe we press the button (add new note)
        }

        notesRecyclerView = findViewById(R.id.notesRecyclerView)
        notesRecyclerView.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)

        noteAdapter = NotesAdapter(noteList)
        notesRecyclerView.adapter = noteAdapter

        getNotes()
    }

    private fun getNotes(){ // function getting notes from database

        doAsync {
            val list = NotesDatabase
                .getDatabase(applicationContext)
                .noteDao()
                .getAllNotes() //new list of notes (with main params(title and datetime))
            uiThread {
                if (noteList.size == 0) {
                    noteList.addAll(list)
                    noteAdapter.notifyDataSetChanged()
                } else {
                    noteList.add(0, list.get(0))
                    noteAdapter.notifyItemInserted(0)
                }
                notesRecyclerView.smoothScrollToPosition(0)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_ADD_NOTE && resultCode == RESULT_OK){
            getNotes()
        }
    }
}


