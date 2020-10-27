package com.kirdevelopment.fnotes.listeners

import com.kirdevelopment.fnotes.entities.Note

interface NotesListener {
    fun onNoteClicked(note: Note, position:Int){}

}