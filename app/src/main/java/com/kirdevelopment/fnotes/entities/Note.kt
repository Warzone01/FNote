package com.kirdevelopment.fnotes.entities

import androidx.annotation.NonNull
import androidx.room.*
import java.io.Serializable

/*
* Item class of notes
* it's all fields can be in one single note
* */

@Entity(tableName = "notes")
class Note(@PrimaryKey(autoGenerate = true) var id: Int?, // id of note

        @ColumnInfo(name = "title") var title:String, // title of note

        @ColumnInfo(name = "date_time") var dateTime:String, // datetime (making auto)

        @ColumnInfo(name = "subtitle") var subtitle:String, // subtitle of note

        @ColumnInfo(name = "note_text") var noteText:String, // note text

        @ColumnInfo(name = "image_path") var imagePath:String, // image path (if we use image)

        @ColumnInfo(name = "color") var color:String, // color of note

        @ColumnInfo(name = "web_link") var webLink:String // web link (if we want)
        ): Serializable {

    constructor():this(null,
        "",
        "",
        "",
        "",
        "",
        "",
        "") // Constructor for note item

    @NonNull
    override fun toString(): String { // this function returned main information for main screen menu
        return title + " : " + dateTime
    }
}