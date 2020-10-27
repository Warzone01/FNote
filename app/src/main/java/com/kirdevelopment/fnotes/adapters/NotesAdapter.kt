package com.kirdevelopment.fnotes.adapters

import android.graphics.Color
import android.graphics.Color.*
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.view.LayoutInflater
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.kirdevelopment.fnotes.R
import com.kirdevelopment.fnotes.activities.CreateNoteActivity
import com.kirdevelopment.fnotes.entities.Note
import kotlinx.android.synthetic.main.layout_miscellaneous.view.*
import kotlinx.android.synthetic.main.note_item.view.*

class NotesAdapter(private var notes: List<Note>) : RecyclerView.Adapter<NotesAdapter.NoteViewHolder>() {

    @NonNull
    override fun onCreateViewHolder(@NonNull parent: ViewGroup, viewType: Int): NoteViewHolder {
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

    class NoteViewHolder(@NonNull itemView: View) : RecyclerView.ViewHolder(itemView) {

        var textViewTitle: TextView
        var textViewSubtitle: TextView
        var textViewDateTime: TextView
        var layoutNote: LinearLayout

        init {
            textViewTitle = itemView.findViewById(R.id.textTitle)
            textViewSubtitle = itemView.findViewById(R.id.textSubtitle)
            textViewDateTime = itemView.findViewById(R.id.textDate)
            layoutNote = itemView.findViewById(R.id.layoutNote)
        }

        fun setNote(note: Note){
            textViewTitle.text = note.title
            if (note.subtitle.trim().isEmpty()){
                textViewSubtitle.visibility = View.GONE
            } else {
                textViewSubtitle.text = note.subtitle
            }
            textViewDateTime.text = note.dateTime

            val gradientDrawable = layoutNote.background as GradientDrawable
            if (note.color != "") {
                gradientDrawable.setColor(Color.parseColor(note.color))
            }else{
                gradientDrawable.setColor(Color.parseColor("#333333"))
            }
        }

    }

}