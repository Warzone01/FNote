package com.kirdevelopment.fnotes.activities

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.gms.ads.*
import com.google.android.gms.ads.mediation.MediationAdConfiguration
import com.kirdevelopment.fnotes.R
import com.kirdevelopment.fnotes.adapters.NotesAdapter
import com.kirdevelopment.fnotes.database.NotesDatabase
import com.kirdevelopment.fnotes.entities.Note
import com.kirdevelopment.fnotes.listeners.NotesListener
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.layout_add_url.view.*
import kotlinx.android.synthetic.main.note_item.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast
import org.jetbrains.anko.uiThread
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity(), NotesListener {

    val REQUEST_CODE_ADD_NOTE: Int = 1
    val REQUEST_CODE_UPDATE_NOTE: Int = 2
    val REQUEST_CODE_SHOW_NOTE: Int = 3
    val REQUEST_CODE_SELECT_IMAGE: Int = 4
    val REQUEST_CODE_STORAGE_PERMISSION: Int = 5
    val APP_PREFERENCES: String = "count"
    val REQUEST_CODE_WRITE_TO_STORAGE = 6

    private lateinit var mInterstitialAd: InterstitialAd

    private var counterForAd: Int = 0
    private lateinit var mSettings: SharedPreferences

    private lateinit var notesRecyclerView: RecyclerView
    private var noteList: ArrayList<Note> = ArrayList()
    private lateinit var noteAdapter: NotesAdapter

    private var noteClickedPosition: Int = -1

    private var dialogAddUrl: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupPermissions()
        MediationAdConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE

        MobileAds.initialize(this) {}
        mInterstitialAd = InterstitialAd(this)
        mInterstitialAd.adUnitId = "ca-app-pub-3940256099942544/1033173712"
        mInterstitialAd.loadAd(AdRequest.Builder().build())

        mInterstitialAd.adListener = object : AdListener() {
            override fun onAdClosed() {
                mInterstitialAd.loadAd(AdRequest.Builder().build())
            }
        }

        //saving count for adMob to show ad once in 5 clicks
        mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE)
        val editor = mSettings.edit()
        counterForAd = mSettings.getInt(APP_PREFERENCES, 0)

        val imageAddNoteMain: ImageView =
            findViewById(R.id.imageAddNoteMain) // Main button to add note

        imageAddNoteMain.setOnClickListener {
            if (mInterstitialAd.isLoaded && mSettings.getInt(APP_PREFERENCES, 0) == 3) {
                mInterstitialAd.show()
                counterForAd = 0
                editor.putInt(APP_PREFERENCES, counterForAd)
                editor.apply()
                println(counterForAd)
            } else if (mInterstitialAd.isLoaded && mSettings.getInt(APP_PREFERENCES, 0) < 3) {
                editor.putInt(APP_PREFERENCES, counterForAd++)
                editor.apply()
                println(counterForAd)
                println(mSettings.getInt(APP_PREFERENCES, 0))
            }else{
                println("Some went wrong!")
            }
            startActivityForResult(
                Intent(
                    applicationContext,
                    CreateNoteActivity::class.java
                ),
                REQUEST_CODE_ADD_NOTE
            ) // new activity whe we press the button (add new note)
        }

        notesRecyclerView = findViewById(R.id.notesRecyclerView)
        notesRecyclerView.layoutManager =
            StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)

        noteAdapter = NotesAdapter(noteList, this)
        notesRecyclerView.adapter = noteAdapter

        getNotes(REQUEST_CODE_SHOW_NOTE, false)

        var inputSearch: EditText = findViewById(R.id.inputSearch)
        inputSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                noteAdapter.cancelTimer()
            }

            override fun afterTextChanged(s: Editable?) {
                if (noteList.size != 0) {
                    noteAdapter.searchNotes(s.toString())
                }
            }
        })

        imageAddNote.setOnClickListener {
            if (mInterstitialAd.isLoaded && mSettings.getInt(APP_PREFERENCES, 0) == 3) {
                mInterstitialAd.show()
                counterForAd = 0
                editor.putInt(APP_PREFERENCES, counterForAd)
                editor.apply()
                println(counterForAd)
            } else if (mInterstitialAd.isLoaded && mSettings.getInt(APP_PREFERENCES, 0) < 3) {
                editor.putInt(APP_PREFERENCES, counterForAd++)
                editor.apply()
                println(counterForAd)
                println(mSettings.getInt(APP_PREFERENCES, 0))
            }else{
                println("Some went wrong!")
            }
            startActivityForResult(
                Intent(
                    applicationContext,
                    CreateNoteActivity::class.java
                ),
                REQUEST_CODE_ADD_NOTE
            ) // new activity whe we press the button (add new note)
        }

        imageAddImage.setOnClickListener {
            if (mInterstitialAd.isLoaded && mSettings.getInt(APP_PREFERENCES, 0) == 3) {
                mInterstitialAd.show()
                counterForAd = 0
                editor.putInt(APP_PREFERENCES, counterForAd)
                editor.apply()
                println(counterForAd)
            } else if (mInterstitialAd.isLoaded && mSettings.getInt(APP_PREFERENCES, 0) < 3) {
                editor.putInt(APP_PREFERENCES, counterForAd++)
                editor.apply()
                println(counterForAd)
                println(mSettings.getInt(APP_PREFERENCES, 0))
            }else{
                println("Some went wrong!")
            }
            if (ContextCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this@MainActivity,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    REQUEST_CODE_STORAGE_PERMISSION
                )

            } else {
                selectImage()
            }
        }

        imageAddLink.setOnClickListener {
            showAddURLDialog()
        }

    }

    private fun selectImage() {
        var intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        if (intent.resolveActivity(packageManager) != null){
            startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE)
        }
    }

    private fun setupPermissions(){
        val permission = ContextCompat.checkSelfPermission(this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE)

        if(permission != PackageManager.PERMISSION_GRANTED) {
            makeRequest()
        }
    }

    private fun makeRequest(){
        ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            REQUEST_CODE_WRITE_TO_STORAGE
        )
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

        if (requestCode == REQUEST_CODE_WRITE_TO_STORAGE && grantResults.isNotEmpty()){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "all ok", Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getPathFromUri(contenUri: Uri): String{
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

    override fun onNoteClicked(note: Note, position: Int) {
        var editor = mSettings.edit()
        if (mInterstitialAd.isLoaded && mSettings.getInt(APP_PREFERENCES, 0) == 3) {
            mInterstitialAd.show()
            counterForAd = 0
            editor.putInt(APP_PREFERENCES, counterForAd)
            editor.apply()
            println(counterForAd)
        } else if (mInterstitialAd.isLoaded && mSettings.getInt(APP_PREFERENCES, 0) < 3) {
            editor.putInt(APP_PREFERENCES, counterForAd++)
            editor.apply()
            println(counterForAd)
            println(mSettings.getInt(APP_PREFERENCES, 0))
        }else{
            println("Some went wrong!")
        }
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

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_ADD_NOTE && resultCode == RESULT_OK){
            getNotes(REQUEST_CODE_ADD_NOTE, false)
        } else if (requestCode == REQUEST_CODE_UPDATE_NOTE && resultCode == RESULT_OK){
            if (data != null){
                getNotes(REQUEST_CODE_UPDATE_NOTE, data.getBooleanExtra("isNoteDeleted", false))
            }
        } else if (requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == RESULT_OK){
            if(data != null){
                var selectedImageUri: Uri? = data.data
                if (selectedImageUri != null){
                    try {
                        val editor = mSettings.edit()
                        var selectedImagePath: String = getPathFromUri(selectedImageUri)
                        var intent: Intent = Intent(applicationContext, CreateNoteActivity::class.java)
                        intent.putExtra("isFromQuickActions", true)
                        intent.putExtra("quickActionType", "image")
                        intent.putExtra("imagePath", selectedImagePath)
                        startActivityForResult(intent, REQUEST_CODE_ADD_NOTE)
                    }catch (exception:Exception){
                        Toast.makeText(this@MainActivity, exception.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun showAddURLDialog(){
        if (dialogAddUrl == null){
            var builder: AlertDialog.Builder = AlertDialog.Builder(this@MainActivity)
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
                    Toast.makeText(this@MainActivity, "Enter URL", Toast.LENGTH_SHORT).show()
                } else if(!Patterns.WEB_URL.matcher(inputURL.text.toString()).matches()){
                    Toast.makeText(this@MainActivity, "Enter valid URL", Toast.LENGTH_SHORT).show()
                }else{
                    dialogAddUrl!!.dismiss()
                    var intent: Intent = Intent(applicationContext, CreateNoteActivity::class.java)
                    intent.putExtra("isFromQuickActions", true)
                    intent.putExtra("quickActionType", "URL")
                    intent.putExtra("URL", inputURL.text.toString())
                    startActivityForResult(intent, REQUEST_CODE_ADD_NOTE)
                }
            }

            view.textCancel.setOnClickListener {
                dialogAddUrl!!.dismiss()
            }
        }
        dialogAddUrl!!.show()
    }
}


