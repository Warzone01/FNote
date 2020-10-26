package com.kirdevelopment.fnotes.adapters

import android.view.LayoutInflater
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kirdevelopment.fnotes.R
import com.kirdevelopment.fnotes.entities.Note
import kotlinx.android.synthetic.main.note_item.view.*

class NotesAdapter: RecyclerView.Adapter<NotesAdapter.NoteViewHolder> {

    private lateinit var notes: List<Note>

    constructor(notes: List<Note>) {
        this.notes = notes
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        return NoteViewHolder(
                LayoutInflater.from(parent.context).inflate(
                        R.layout.note_item,
                        parent,
                        false
                )
        )
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.setNote(notes[position])
    }

    override fun getItemCount(): Int {
        return notes.size
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    class NoteViewHolder: RecyclerView.ViewHolder{

        lateinit var textViewTitle: TextView
        lateinit var textViewSubtitle: TextView
        lateinit var textViewDateTime: TextView

        constructor(itemView: View) : super(itemView){
            textViewTitle = itemView.findViewById(R.id.textTitle)
            textViewSubtitle = itemView.findViewById(R.id.textSubtitle)
            textViewDateTime = itemView.findViewById(R.id.textDate)
        }

        fun setNote(note: Note){
            textViewTitle.text = note.title
            if (note.subtitle.trim().isEmpty()){
                textViewSubtitle.visibility = View.GONE
            } else {
                textViewSubtitle.text = note.subtitle
            }
            textViewDateTime.text = note.dateTime
        }

    }

}