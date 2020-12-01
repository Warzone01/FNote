package com.kirdevelopment.fnotes.adapters

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Color.*
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.annotation.RequiresApi
import androidx.core.graphics.createBitmap
import androidx.recyclerview.widget.RecyclerView
import com.kirdevelopment.fnotes.R
import com.kirdevelopment.fnotes.activities.CreateNoteActivity
import com.kirdevelopment.fnotes.entities.Note
import com.kirdevelopment.fnotes.listeners.NotesListener
import com.makeramen.roundedimageview.RoundedImageView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.layout_miscellaneous.view.*
import kotlinx.android.synthetic.main.note_item.view.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.timerTask

class NotesAdapter(private var notes: List<Note>,
                   private var notesListener: NotesListener,
                   ) : RecyclerView.Adapter<NotesAdapter.NoteViewHolder>() {

    private var timer: Timer? = null
    private var notesSource:List<Note> = notes

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
        holder.layoutNote.setOnClickListener {
            notesListener.onNoteClicked(notes[position], position)
        }
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
        var imageNote: RoundedImageView

        init {
            textViewTitle = itemView.findViewById(R.id.textTitle)
            textViewSubtitle = itemView.findViewById(R.id.textSubtitle)
            textViewDateTime = itemView.findViewById(R.id.textDate)
            layoutNote = itemView.findViewById(R.id.layoutNote)
            imageNote = itemView.findViewById(R.id.imageNote)
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

            if (note.imagePath != ""){
                var bitmap = BitmapFactory.decodeFile(note.imagePath)
                imageNote.setImageBitmap(bitmap)
                imageNote.visibility = View.VISIBLE
                println(note.imagePath.toString())
            }else{
                imageNote.visibility = View.GONE
            }

        }

    }

    fun searchNotes(searchKeyWord: String){
        timer = Timer()
        timer!!.schedule(timerTask {
            if(searchKeyWord.trim().isEmpty()){
                notes = notesSource
            }else{
                var temp: ArrayList<Note> = ArrayList()
                for (note:Note in notesSource){
                    if (note.title.toLowerCase().contains(searchKeyWord.toLowerCase())
                            || note.subtitle.toLowerCase().contains(searchKeyWord.toLowerCase())
                            ||note.noteText.toLowerCase().contains(searchKeyWord.toLowerCase())){
                        temp.add(note)
                    }
                }
                notes = temp
            }
            Handler(Looper.getMainLooper()).post(Runnable {
                notifyDataSetChanged()
            })
        }, 500)
    }

    fun cancelTimer(){
        if (timer != null){
            timer!!.cancel()
        }
    }
}