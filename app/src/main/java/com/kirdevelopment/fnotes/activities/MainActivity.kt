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
import com.kirdevelopment.fnotes.listeners.NotesListener
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity(), NotesListener {

    val REQUEST_CODE_ADD_NOTE: Int = 1
    val REQUEST_CODE_UPDATE_NOTE: Int = 2
    val REQUEST_CODE_SHOW_NOTE: Int = 3

    private lateinit var notesRecyclerView: RecyclerView
    private var noteList: ArrayList<Note> = ArrayList()
    private lateinit var noteAdapter: NotesAdapter

    private var noteClickedPosition: Int = -1

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

        noteAdapter = NotesAdapter(noteList, this)
        notesRecyclerView.adapter = noteAdapter

        getNotes(REQUEST_CODE_SHOW_NOTE, false)
    }

    override fun onNoteClicked(note: Note, position: Int) {
        super.onNoteClicked(note, position)
        noteClickedPosition = position
        var intent = Intent(applicationContext, CreateNoteActivity::class.java)
        intent.putExtra("isViewOrUpdate", true)
        intent.putExtra("note", note)
        startActivityForResult(intent, REQUEST_CODE_UPDATE_NOTE)
    }

    private fun getNotes(requestCode: Int, isNoteDeleted: Boolean){ // function getting notes from database

        doAsync {
            val list = NotesDatabase
                .getDatabase(applicationContext)
                .noteDao()
                .getAllNotes() //new list of notes (with main params(title and datetime))
            uiThread {
                if (requestCode == REQUEST_CODE_SHOW_NOTE){
                    noteList.addAll(list)
                    noteAdapter.notifyDataSetChanged()
                } else if(requestCode == REQUEST_CODE_ADD_NOTE){
                    noteList.add(0, list[0])
                    noteAdapter.notifyItemInserted(0)
                    notesRecyclerView.smoothScrollToPosition(0)
                } else if (requestCode == REQUEST_CODE_UPDATE_NOTE){
                    noteList.removeAt(noteClickedPosition)


                    if (isNoteDeleted){
                        noteAdapter.notifyItemRemoved(noteClickedPosition)
                    }else{
                        noteList.add(noteClickedPosition, list[noteClickedPosition])
                        noteAdapter.notifyItemChanged(noteClickedPosition)
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_ADD_NOTE && resultCode == RESULT_OK){
            getNotes(REQUEST_CODE_ADD_NOTE, false)
        } else if (requestCode == REQUEST_CODE_UPDATE_NOTE && resultCode == RESULT_OK){
            if (data != null){
                getNotes(REQUEST_CODE_UPDATE_NOTE, data.getBooleanExtra("isNoteDeleted", false))
            }
        }
    }
}


