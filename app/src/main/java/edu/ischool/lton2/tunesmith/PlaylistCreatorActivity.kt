package edu.ischool.lton2.tunesmith

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


class PlaylistCreatorActivity: AppCompatActivity() {
    lateinit var img: ImageView
    lateinit var name: EditText
    lateinit var description: EditText
    lateinit var createBtn: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.playlist_creator)

        img = findViewById(R.id.imgPlaylist)
        name = findViewById(R.id.namePlaylist)
        description = findViewById(R.id.descriptionPlaylist)
        createBtn = findViewById(R.id.createPlaylist)
        img.setOnClickListener {
            checkAndRequestPermission()
        }
        createBtn.setOnClickListener {
            if(name.text.toString().trim() != "") {
                // button action to create playlist
            }
        }

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
             // doesnt ask again for permission and disables clickc on the image after denied ?????? or does it >:(( it deos
//            else {
//                val alert = AlertDialog.Builder(this)
//                alert.setTitle("Access Error")
//                alert.setMessage("Image access not granted")
//                alert.setNegativeButton("Close") {dialog, which ->
//                    dialog.dismiss()
//                }
//            }
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
            cursor.close()
            val bitmap = BitmapFactory.decodeFile(picturePath)
            img.setImageBitmap(Bitmap.createScaledBitmap(bitmap, 150, 150, false));
        }
    }
}