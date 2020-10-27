package com.kirdevelopment.fnotes.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.kirdevelopment.fnotes.R
import com.kirdevelopment.fnotes.database.NotesDatabase
import com.kirdevelopment.fnotes.entities.Note
import kotlinx.android.synthetic.main.layout_miscellaneous.view.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast
import org.jetbrains.anko.uiThread
import java.io.InputStream
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class CreateNoteActivity : AppCompatActivity() {

    private var inputNoteTitle: EditText? = null
    private var inputNoteSubtitle: EditText? = null
    private var inputNoteText: EditText? = null
    private var textDateTime: TextView? = null
    private lateinit var nDb: NotesDatabase
    private lateinit var imageNote: ImageView

    private lateinit var viewSubtitleIndicator: View

    private lateinit var selectedNoteColor: String
    private lateinit var selectedImagePath:String

    private val REQUEST_CODE_STORAGE_PERMISSION: Int = 1
    private val REQUEST_CODE_SELECT_IMAGE = 2

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
        textDateTime!!.text = SimpleDateFormat(
                "EEEE, dd MMMM yyyy HH:mm a",
                Locale.getDefault())
                .format(Date())
        viewSubtitleIndicator = findViewById(R.id.viewSubtitleIndicator)
        imageNote = findViewById(R.id.imageNote)

        nDb = NotesDatabase.getDatabase(applicationContext) // get note database

        //on click button done save note
        var imageViewSave: ImageView = findViewById(R.id.imageViewDoneButton)
        imageViewSave.setOnClickListener {
            saveNote()
        }

        selectedNoteColor = "#333333"
        selectedImagePath = ""

        initMiscellaneous()
        setSubtitleIndicatorColor()
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
        note.color = selectedNoteColor
        note.imagePath = selectedImagePath



        doAsync {
            nDb.noteDao().insertNote(note) //add note in database
            uiThread {
                val intent = Intent()
                setResult(RESULT_OK, intent) // when all ok, close activity
                finish()
            }
        }
    }

    private fun initMiscellaneous() {
        val layoutMiscellaneous: LinearLayout = findViewById(R.id.layoutMiscellaneous)
        val bottomSheetBehavior: BottomSheetBehavior<LinearLayout> = BottomSheetBehavior.from(layoutMiscellaneous)

        layoutMiscellaneous.textMiscellaneous.setOnClickListener {
            if (bottomSheetBehavior.state != BottomSheetBehavior.STATE_EXPANDED){
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            } else{
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            }
        }

        val imageViewColor1: ImageView = layoutMiscellaneous.imageColor1
        val imageViewColor2: ImageView = layoutMiscellaneous.imageColor2
        val imageViewColor3: ImageView = layoutMiscellaneous.imageColor3
        val imageViewColor4: ImageView = layoutMiscellaneous.imageColor4
        val imageViewColor5: ImageView = layoutMiscellaneous.imageColor5

        layoutMiscellaneous.viewColor1.setOnClickListener {
            selectedNoteColor = "#333333"
            imageViewColor1.setImageResource(R.drawable.ic_done)
            imageViewColor2.setImageResource(0)
            imageViewColor3.setImageResource(0)
            imageViewColor4.setImageResource(0)
            imageViewColor5.setImageResource(0)
            setSubtitleIndicatorColor()
        }

        layoutMiscellaneous.viewColor2.setOnClickListener {
            selectedNoteColor = "#FDBE3B"
            imageViewColor1.setImageResource(0)
            imageViewColor2.setImageResource(R.drawable.ic_done)
            imageViewColor3.setImageResource(0)
            imageViewColor4.setImageResource(0)
            imageViewColor5.setImageResource(0)
            setSubtitleIndicatorColor()
        }

        layoutMiscellaneous.viewColor3.setOnClickListener {
            selectedNoteColor = "#FF4842"
            imageViewColor1.setImageResource(0)
            imageViewColor2.setImageResource(0)
            imageViewColor3.setImageResource(R.drawable.ic_done)
            imageViewColor4.setImageResource(0)
            imageViewColor5.setImageResource(0)
            setSubtitleIndicatorColor()
        }

        layoutMiscellaneous.viewColor4.setOnClickListener {
            selectedNoteColor = "#3A52Fc"
            imageViewColor1.setImageResource(0)
            imageViewColor2.setImageResource(0)
            imageViewColor3.setImageResource(0)
            imageViewColor4.setImageResource(R.drawable.ic_done)
            imageViewColor5.setImageResource(0)
            setSubtitleIndicatorColor()
        }

        layoutMiscellaneous.viewColor5.setOnClickListener {
            selectedNoteColor = "#000000"
            imageViewColor1.setImageResource(0)
            imageViewColor2.setImageResource(0)
            imageViewColor3.setImageResource(0)
            imageViewColor4.setImageResource(0)
            imageViewColor5.setImageResource(R.drawable.ic_done)
            setSubtitleIndicatorColor()
        }

        layoutMiscellaneous.layoutAddImage.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            if (ContextCompat.checkSelfPermission(
                            applicationContext,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(
                        this@CreateNoteActivity,
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                        REQUEST_CODE_STORAGE_PERMISSION
                )
            }else {
                selectImage()
            }
        }
    }

    private fun setSubtitleIndicatorColor(){
        val gradientDrawable:GradientDrawable = viewSubtitleIndicator.background as GradientDrawable
        gradientDrawable.setColor(Color.parseColor(selectedNoteColor))
    }

    private fun selectImage() {
        var intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        if (intent.resolveActivity(packageManager) != null){
            startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION && grantResults.isNotEmpty()){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                selectImage()
            }else{
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == RESULT_OK){
            if (data != null){
                var selectedImageUri:Uri? = data.data
                if (selectedImageUri != null){
                    try {

                        var inputStream: InputStream? = contentResolver.openInputStream(selectedImageUri)
                        var bitmap:Bitmap = BitmapFactory.decodeStream(inputStream)
                        imageNote.setImageBitmap(bitmap)
                        imageNote.visibility = View.VISIBLE

                        selectedImagePath = getPathFromUri(selectedImageUri)

                    }catch (exception:Exception){
                        Toast.makeText(this, exception.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getPathFromUri(contenUri:Uri): String{
        var filePath:String
        var cursor: Cursor? = contentResolver.query(contenUri, null, null, null)
        if (cursor == null){
            filePath = contenUri.path.toString()
        } else {
            cursor.moveToFirst()
            var index:Int = cursor.getColumnIndex("_data")
            filePath = cursor.getString(index)
            cursor.close()
        }
        return filePath
    }

}






