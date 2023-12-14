package edu.ischool.lton2.tunesmith

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.NumberPicker
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


class PlaylistCreatorActivity: AppCompatActivity() {
    lateinit var img: ImageView
    lateinit var name: EditText
    lateinit var description: EditText
    lateinit var createBtn: Button
    lateinit var numberPicker: NumberPicker
    var picture: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.playlist_creator)
        findViewById<TextView>(R.id.txtWarning).text = "WARNING: Use an image under 256KB"
        img = findViewById(R.id.imgPlaylist)
        name = findViewById(R.id.namePlaylist)
        description = findViewById(R.id.descriptionPlaylist)
        createBtn = findViewById(R.id.createPlaylist)
        numberPicker = findViewById(R.id.numberPicker)

        img.setOnClickListener {
            checkAndRequestPermission()
        }
        numberPicker.minValue = 0
        numberPicker.maxValue = getSongNumbers().size -1
        numberPicker.displayedValues = getSongNumbers()

        createBtn.setOnClickListener {
            if(name.text.toString().trim() != "") {
                // button action to create playlist
                var array = getSongNumbers()
                val newPlaylist = Playlist (
                    name.text.toString(),
                    description.text.toString(),
                    picture, // if picture is "" then use default/ drawable picture
                    limitSongNumbers(array[numberPicker.value])
                    // launch intent / bundle to playlistview
                )
                val playlistViewIntent = Intent(this, PlaylistViewActivity::class.java)
                val bundle = this.intent.extras
                bundle?.putParcelable("Playlist", newPlaylist)
                bundle?.putInt("nSongs", numberPicker.maxValue - numberPicker.value + 1)
                Log.i("Creator", "${numberPicker.value} songs wanted")
                playlistViewIntent.putExtras(bundle!!)
                startActivity(playlistViewIntent)
            } else {
                Toast.makeText(this, "Please enter a playlist name", Toast.LENGTH_SHORT).show()
            }
        }

    }
    private fun getSongNumbers(): Array<String> {
        // Create a list of numbers based on the number of songs
        return (1..30).map { it.toString() }.reversed().toTypedArray() // replace example
    }
    private fun limitSongNumbers(number: String): List<Song> { // replace example
        return example.take(number.toInt())

    }
    private fun onClickImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, 1)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 2) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onClickImage()
            }
        }
    }

    private fun hasReadStoragePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
                this,
            android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
    }

    // Request the read external storage permission
    private fun requestReadStoragePermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), 2
        )
    }

    // Check if the app has permission to read media images (API level 33 or higher)
    private fun hasReadMediaImagesPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // For devices with API level less than TIRAMISU, use READ_EXTERNAL_STORAGE
            hasReadStoragePermission()
        }
    }

    // Request the read media images permission (API level 33 or higher)
    private fun requestReadMediaImagesPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.READ_MEDIA_IMAGES), 2)
        } else {
            // For devices with API level less than TIRAMISU, use READ_EXTERNAL_STORAGE
            requestReadStoragePermission()
        }
    }

    // Check and request the necessary permission
    private fun checkAndRequestPermission() {
        if (hasReadMediaImagesPermission()) {
            onClickImage()
        } else {
            requestReadMediaImagesPermission()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null) {
            val selectedImageUri = data.data
            val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)

            val cursor = contentResolver.query(selectedImageUri!!, filePathColumn, null, null, null)
            cursor!!.moveToFirst()

            val columnIndex = cursor.getColumnIndex(filePathColumn[0])
            val picturePath = cursor.getString(columnIndex)
            Log.i("photo select activity", "before cursor.close()")
            cursor.close()
            picture = picturePath
            val bitmap = BitmapFactory.decodeFile(picturePath)
            img.setImageBitmap(Bitmap.createScaledBitmap(bitmap, 150, 150, false));
        }
    }
}