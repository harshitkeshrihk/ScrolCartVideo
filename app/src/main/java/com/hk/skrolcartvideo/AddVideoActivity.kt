package com.hk.skrolcartvideo


import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.widget.MediaController
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.hk.skrolcartvideo.databinding.ActivityAddVideoBinding


class AddVideoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddVideoBinding

    lateinit var actionBar: ActionBar

    private val VIDEO_PICK_GALLERY_CODE = 100
    private val VIDEO_PICK_CAMERA_CODE = 101
    private val CAMERA_CODE=102

    private lateinit var cameraPermissions: Array<String>

    private lateinit var progressDialog: ProgressDialog

    private var videoUri:Uri? = null

    private var title:String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddVideoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        actionBar = supportActionBar!!
        actionBar.title = "Add New Video"
        actionBar.setDisplayHomeAsUpEnabled(true)
        actionBar.setDisplayShowHomeEnabled(true)

        cameraPermissions = arrayOf(android.Manifest.permission.CAMERA,android.Manifest.permission.WRITE_EXTERNAL_STORAGE)

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setMessage("Uploading Video...")
        progressDialog.setCanceledOnTouchOutside(false)

        binding.uploadVideoBtn.setOnClickListener{
            title = binding.titleEt.text.toString().trim()
            if(TextUtils.isEmpty(title)){
                Toast.makeText(this,"Title is required",Toast.LENGTH_SHORT).show()
            }else if(videoUri == null){
                Toast.makeText(this,"Pick the video first",Toast.LENGTH_SHORT).show()
            }else{
                uploadVideo()
            }
        }

        binding.pickVideoFab.setOnClickListener{
            pickVideo()
        }
    }

    private fun uploadVideo() {
        progressDialog.show()

        val timestamp = ""+ System.currentTimeMillis()

        val filePathAndName = "Videos/video_$timestamp"

        val storageReference = FirebaseStorage.getInstance().getReference(filePathAndName)
        storageReference.putFile(videoUri!!)
            .addOnSuccessListener { taskSnapShot->
                val uriTask = taskSnapShot.storage.downloadUrl
                while (!uriTask.isSuccessful);
                val downloadUri = uriTask.result
                if(uriTask.isSuccessful){
                    val hashMap = HashMap<String,Any>()
                    hashMap["id"]="$timestamp"
                    hashMap["title"]="$title"
//                    hashMap["timestamp"]="$timestamp"
                    hashMap["videoUri"]="$downloadUri"

                    val dbReference = FirebaseDatabase.getInstance().getReference("Videos")
                    dbReference.child(timestamp)
                        .setValue(hashMap)
                        .addOnSuccessListener {
                            progressDialog.dismiss()
                            Toast.makeText(this,"Video Uploaded",Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener{  e->
                            progressDialog.dismiss()
                            Toast.makeText(this,"${e.message}",Toast.LENGTH_SHORT).show()
                        }
                }

            }
            .addOnFailureListener{ e->
                progressDialog.dismiss()
                Toast.makeText(this,"${e.message}",Toast.LENGTH_SHORT).show()
            }

    }

    private fun pickVideo(){
         val options = arrayOf("Camera", "Gallery")

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Pick Video From")
            .setItems(options){ _,i ->
                if(i==0){
                    if(!checkCameraPermissions()){
                        requestCameraPermissions()
                    }else{
                        pickVideoCamera()
                    }
                }else{
                    pickVideoGallery()
                }
            }.show()
    }

    private fun requestCameraPermissions(){
        ActivityCompat.requestPermissions(this,cameraPermissions,CAMERA_CODE)
    }

    private fun checkCameraPermissions():Boolean{
        val result1 = ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        val result2 = ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED

        return result1 && result2
    }

    private fun pickVideoGallery(){
        val intent = Intent()
        intent.type = "video/*"
        intent.action = Intent.ACTION_GET_CONTENT

        startActivityForResult(
            Intent.createChooser(intent,"Choose video"),
            VIDEO_PICK_GALLERY_CODE
        )
    }

    private fun pickVideoCamera(){
        val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        startActivityForResult(intent,VIDEO_PICK_GALLERY_CODE)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when(requestCode){
            CAMERA_CODE ->
                if (grantResults.isNotEmpty()){
                    val cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
                    val storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED
                    if(cameraAccepted && storageAccepted){
                        pickVideoCamera()
                    }else{
                        Toast.makeText(this,"Permissions denied", Toast.LENGTH_SHORT).show()
                    }
                }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(resultCode == RESULT_OK){
            if(requestCode == VIDEO_PICK_CAMERA_CODE) {
                videoUri = data!!.data
                setVideo()
            }else if (requestCode == VIDEO_PICK_GALLERY_CODE){
                videoUri = data!!.data
                setVideo()
            }
        }else{
            Toast.makeText(this,"Cancelled",Toast.LENGTH_SHORT).show()
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun setVideo() {
        val mediaController = MediaController(this)
        mediaController.setAnchorView(binding.videoView)

        binding.videoView.setMediaController(mediaController)
        binding.videoView.setVideoURI(videoUri)
        binding.videoView.requestFocus()
        binding.videoView.setOnPreparedListener{
            binding.videoView.pause()
            //imp
        }

    }
}