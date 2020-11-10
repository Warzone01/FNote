package com.kirdevelopment.fnotes.activities

import android.Manifest
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.media.Image
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.kirdevelopment.fnotes.R
import com.kirdevelopment.fnotes.database.NotesDatabase
import com.kirdevelopment.fnotes.entities.Note
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_create_note.*
import kotlinx.android.synthetic.main.layout_add_url.view.*
import kotlinx.android.synthetic.main.layout_add_url.view.textCancel
import kotlinx.android.synthetic.main.layout_delete_note.view.*
import kotlinx.android.synthetic.main.layout_miscellaneous.*
import kotlinx.android.synthetic.main.layout_miscellaneous.view.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.find
import org.jetbrains.anko.toast
import org.jetbrains.anko.uiThread
import java.io.*
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
    private lateinit var textWebURL: TextView
    private lateinit var layoutWebURL: LinearLayout

    private lateinit var viewSubtitleIndicator: View

    private lateinit var selectedNoteColor: String
    private lateinit var selectedImagePath:String

    private val REQUEST_CODE_STORAGE_PERMISSION: Int = 1
    private val REQUEST_CODE_SELECT_IMAGE = 2
    private val REQUEST_CODE_WRITE_TO_STORAGE = 6

    private var dialogAddUrl: AlertDialog? = null
    private var dialogDeleteNote: AlertDialog? = null

    private var alreadyAvailableNote: Note? = null

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
        textWebURL = findViewById(R.id.textWebURl)
        layoutWebURL = findViewById(R.id.layoutWebUrl)

        nDb = NotesDatabase.getDatabase(applicationContext) // get note database

        //on click button done save note
        var imageViewSave: ImageView = findViewById(R.id.imageViewDoneButton)
        imageViewSave.setOnClickListener {
            saveNote()
        }

        selectedNoteColor = "#333333"
        selectedImagePath = ""

        if (intent.getBooleanExtra("isViewOrUpdate", false)){
            alreadyAvailableNote = intent.getSerializableExtra("note") as Note
            setViewOrUpdateNote()
        }

        imageRemoveWebURL.setOnClickListener {
            textWebURL.text = ""
            layoutWebURL.visibility = View.GONE
        }

        imageRemoveImage.setOnClickListener {
            imageNote.setImageBitmap(null)
            imageNote.visibility = View.GONE
            imageRemoveImage.visibility = View.GONE
            selectedImagePath = ""
        }

        if(intent.getBooleanExtra("isFromQuickActions", false)){
            var type:String? = intent.getStringExtra("quickActionType")
            if (type != null){
                if (type == "image"){
                    selectedImagePath = intent.getStringExtra("imagePath")

                    val bitmap = BitmapFactory.decodeFile(selectedImagePath)
                    val imgWidth = bitmap.width / 3
                    val imgHeight = bitmap.height / 3

                    Picasso.get()
                            .load("file:$selectedImagePath")
                            .resize(imgWidth, imgHeight)
                            .centerInside()
                            .into(imageNote);

//                    imageNote.setImageBitmap(bitmap)
                    imageNote.visibility = View.VISIBLE
                    imageRemoveImage.visibility = View.VISIBLE

                } else if (type == "URL"){
                    textWebURL.text = intent.getStringExtra("URL")
                    layoutWebURL.visibility = View.VISIBLE
                }
            }
        }

        initMiscellaneous()
        setSubtitleIndicatorColor()
    }



    private fun setViewOrUpdateNote(){
        inputNoteTitle?.setText(alreadyAvailableNote!!.title)
        inputNoteSubtitle?.setText(alreadyAvailableNote!!.subtitle)
        inputNoteText?.setText(alreadyAvailableNote!!.noteText)
        textDateTime?.setText(alreadyAvailableNote!!.dateTime)
        if (alreadyAvailableNote!!.imagePath != "" && alreadyAvailableNote!!.imagePath.trim().isNotEmpty()){

            val bitmap = BitmapFactory.decodeFile(alreadyAvailableNote!!.imagePath)
            val imgWidth = bitmap.width / 3
            val imgHeight = bitmap.height / 3

            Picasso.get()
                    .load("file:${alreadyAvailableNote!!.imagePath}")
                    .resize(imgWidth, imgWidth)
                    .centerInside()
                    .into(imageNote)

            imageNote.visibility = View.VISIBLE
            imageRemoveImage.visibility = View.VISIBLE
            selectedImagePath = alreadyAvailableNote!!.imagePath
        }

        if (alreadyAvailableNote!!.webLink != "" && alreadyAvailableNote!!.webLink.trim().isNotEmpty()){
            textWebURL.text = alreadyAvailableNote!!.webLink
            layoutWebURL.visibility = View.VISIBLE
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
        note.color = selectedNoteColor
        note.imagePath = selectedImagePath

        if (layoutWebURL.visibility == View.VISIBLE){
            note.webLink = textWebURL.text.toString()
        }

        if (alreadyAvailableNote != null){
            note.id = alreadyAvailableNote!!.id
        }


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

        if (alreadyAvailableNote != null && alreadyAvailableNote!!.color != "" && alreadyAvailableNote!!.color.trim().isNotEmpty()){
            when(alreadyAvailableNote!!.color){
                    "#FDBE3B" -> layoutMiscellaneous.viewColor2.performClick()
                    "#FF4842" -> layoutMiscellaneous.viewColor3.performClick()
                    "#3A52Fc" -> layoutMiscellaneous.viewColor4.performClick()
                    "#000000" -> layoutMiscellaneous.viewColor5.performClick()
            }
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

        layoutMiscellaneous.layoutAddUrl.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            showAddURLDialog()
        }

        if (alreadyAvailableNote != null){
            layoutMiscellaneous.layoutDeleteNote.visibility = View.VISIBLE
            layoutMiscellaneous.layoutDeleteNote.setOnClickListener {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                showDeleteNoteDialog()
            }
        }
    }

    private fun showDeleteNoteDialog(){
        if (dialogDeleteNote == null){
            var builder: AlertDialog.Builder = AlertDialog.Builder(this@CreateNoteActivity)
            var view: View = LayoutInflater.from(this@CreateNoteActivity).inflate(
                    R.layout.layout_delete_note,
                    findViewById<ViewGroup>(R.id.layoutDeleteNoteContainer)
            )
            builder.setView(view)

            dialogDeleteNote = builder.create()
            if (dialogDeleteNote!!.window != null){
                dialogDeleteNote!!.window?.setBackgroundDrawable(ColorDrawable(0))
            }
            view.textDeleteNote.setOnClickListener {
                doAsync {
                    alreadyAvailableNote?.let { it1 ->
                        NotesDatabase.getDatabase(applicationContext).noteDao()
                                .deleteNote(it1)
                    }
                    uiThread {
                        var intent:Intent = Intent()
                        intent.putExtra("isNoteDeleted", true)
                        setResult(RESULT_OK, intent)
                        finish()
                    }
                }
            }

            view.textCancel.setOnClickListener {
                dialogDeleteNote!!.dismiss()
            }
        }

        dialogDeleteNote!!.show()

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
//                        var inputStream: InputStream? = contentResolver.openInputStream(selectedImageUri)
//                        var bitmap:Bitmap = BitmapFactory.decodeStream(inputStream)

                        selectedImagePath = getPathFromUri(selectedImageUri)

                        val bitmap = BitmapFactory.decodeFile(selectedImagePath)
                        val imgWidth = bitmap.width / 3
                        val imgHeght = bitmap.height / 3

                        Picasso.get()
                                .load("file:$selectedImagePath")
                                .resize(imgWidth, imgHeght)
                                .centerInside()
                                .into(imageNote)

                        imageNote.visibility = View.VISIBLE
                        imageRemoveImage.visibility = View.VISIBLE



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

    private fun showAddURLDialog(){
        if (dialogAddUrl == null){
            var builder: AlertDialog.Builder = AlertDialog.Builder(this@CreateNoteActivity)
            val view: View = LayoutInflater.from(this).inflate(
                    R.layout.layout_add_url,
                    findViewById<ViewGroup>(R.id.layoutAddUrlContainer)
            )
            builder.setView(view)

            dialogAddUrl = builder.create()

            if(dialogAddUrl!!.window != null){
                dialogAddUrl!!.window!!.setBackgroundDrawable(ColorDrawable(0))
            }
            val inputURL:EditText = view.findViewById(R.id.inputUrl)
            inputURL.requestFocus()

            view.textAdd.setOnClickListener {
                if (inputURL.text.toString().trim().isEmpty()){
                    Toast.makeText(this@CreateNoteActivity, "Enter URL", Toast.LENGTH_SHORT).show()
                } else if(!Patterns.WEB_URL.matcher(inputURL.text.toString()).matches()){
                    Toast.makeText(this@CreateNoteActivity, "Enter valid URL", Toast.LENGTH_SHORT).show()
                }else{
                    textWebURL.text = inputURL.text.toString()
                    layoutWebURL.visibility = View.VISIBLE
                    dialogAddUrl!!.dismiss()
                }
            }

            view.textCancel.setOnClickListener {
                dialogAddUrl!!.dismiss()
            }
        }
        dialogAddUrl!!.show()
    }

    private fun saveImageToExternalStorage(bitmap: Bitmap): Uri{

        var path = File("/sdcard/FNotes/Images/")
        path.mkdirs()
        val file = File(path, "${UUID.randomUUID()}.jpg")

        try{
            val stream: OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream)
            stream.flush()
            stream.close()
            Toast.makeText(this, "image saved successful", Toast.LENGTH_SHORT).show()
        }catch (e: IOException){
            e.printStackTrace()
            Toast.makeText(this, "Error to save image", Toast.LENGTH_SHORT).show()
        }

        selectedImagePath = file.toString()
        return Uri.parse(file.absolutePath)
    }
}






