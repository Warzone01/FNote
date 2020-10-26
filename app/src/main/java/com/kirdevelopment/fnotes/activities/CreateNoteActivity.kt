package com.kirdevelopment.fnotes.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.kirdevelopment.fnotes.R
import com.kirdevelopment.fnotes.database.NotesDatabase
import com.kirdevelopment.fnotes.entities.Note
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.text.SimpleDateFormat
import java.util.*

class CreateNoteActivity : AppCompatActivity() {

    private var inputNoteTitle: EditText? = null
    private var inputNoteSubtitle: EditText? = null
    private var inputNoteText: EditText? = null
    private var textDateTime: TextView? = null
    private lateinit var nDb: NotesDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_note)

        val imageBack: ImageView = findViewById(R.id.imageViewBack)
        imageBack.setOnClickListener { onBackPressed() }

        //inti all fields
        inputNoteTitle = findViewById(R.id.inputNoteTitle)
        inputNoteSubtitle = findViewById(R.id.inputNoteSubtitle)
        inputNoteText = findViewById(R.id.inputNote)
        textDateTime = findViewById(R.id.textDateTime)
        textDateTime!!.text = SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm a", Locale.getDefault()).format(Date())

        nDb = NotesDatabase.getDatabase(applicationContext) // get note database

        //on click button done save note
        var imageViewSave: ImageView = findViewById(R.id.imageViewDoneButton)
        imageViewSave.setOnClickListener {
            saveNote()
        }
    }

    private fun saveNote(){
        if (inputNoteTitle!!.text.toString().trim().isEmpty()) { // Show error if note title empty
            Toast.makeText(this, "Note title can't be empty!", Toast.LENGTH_SHORT).show()
            return
        }else if (inputNoteSubtitle!!.text.toString().trim().isEmpty()
                &&inputNoteText!!.text.toString().trim().isEmpty()){ // if text and subtitle empty - show error
            Toast.makeText(this, "Note cant't be empty!", Toast.LENGTH_SHORT).show()
            return
        }

        //init note fields
        val note = Note()
        note.title = inputNoteTitle!!.text.toString()
        note.subtitle = inputNoteSubtitle!!.text.toString()
        note.noteText = inputNoteText!!.text.toString()
        note.dateTime = textDateTime!!.text.toString()

        doAsync {
            nDb.noteDao().insertNote(note) //add note in database
            uiThread {
                val intent = Intent()
                setResult(RESULT_OK, intent) // when all ok, close activity
                finish()
            }
        }

    }


}