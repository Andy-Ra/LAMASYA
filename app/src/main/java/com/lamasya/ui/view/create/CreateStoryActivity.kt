package com.lamasya.ui.view.create

import android.Manifest
import android.content.Intent
import android.content.Intent.ACTION_GET_CONTENT
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.lamasya.databinding.ActivityCreateStoryBinding
import com.lamasya.ui.view.camera.CameraActivity
import com.lamasya.ui.view.main.MainActivity
import com.lamasya.ui.viewmodel.CreateViewModel
import com.lamasya.util.LoadingDialog
import com.lamasya.util.rotateBitmap
import java.io.*

@Suppress("DEPRECATION")
class CreateStoryActivity : AppCompatActivity() {
    private lateinit var pict: String
    private lateinit var binding: ActivityCreateStoryBinding
    private lateinit var storageRef: StorageReference
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var firebaseauth: FirebaseAuth
    private var imageUri: Uri? = null
    private val createVM : CreateViewModel by viewModels()


    private val loading = LoadingDialog(this)

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (!allPermissionsGranted()) {
                Toast.makeText(
                    this,
                    "Tidak mendapatkan permission.",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(
                this,
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }
        initVars()
        registerClickEvents()
        getData()
    }

    private fun getData() {
        val currentUID = MainActivity.CURRENT_UID
        firebaseFirestore.collection("detail_user").document(currentUID).get()
            .addOnSuccessListener { documents ->
                pict = documents.getString("profile_pict").toString()
                val fName = documents.getString("first_name")
                val lName = documents.getString("last_name")
                binding.tvNama.text = StringBuilder(fName.toString()).append(" ").append(lName)

                if (pict != "null") {
                    Glide.with(this)
                        .load(pict)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(binding.mealImageOrder)
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error getting documents: ", Toast.LENGTH_SHORT).show()
            }


    }

    private fun registerClickEvents() {
        binding.btnCamera.setOnClickListener { startCamera() }
        binding.btnGallery.setOnClickListener { startGallery() }
        binding.btnUpload.setOnClickListener { uploadImage2() }
    }

    private fun uploadImage2() {
        val uid = firebaseauth.currentUser?.uid
        val desc = binding.edDescription.text.toString()
        val situation = binding.situation.selectedItem.toString()
        if (imageUri != null) {
            loading.isLoading(true)
            createVM.uploadStory(uid, situation, desc, imageUri)
            createVM.uploadStoryData.observe(
                this,) {
                if (it) {
                    Toast.makeText(this, "Upload Success", Toast.LENGTH_SHORT).show()
                    finish()
                    loading.isLoading(false)
                } else {
                    Toast.makeText(this, "Upload Failed", Toast.LENGTH_SHORT).show()
                    loading.isLoading(false)
                }
            }
        } else {
            Toast.makeText(this, "Lampirkan Foto", Toast.LENGTH_SHORT).show()
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun startCamera() {
        val intent = Intent(this, CameraActivity::class.java)
        launcherIntentCameraX.launch(intent)
    }

    private fun startGallery() {
        val intent = Intent()
        intent.action = ACTION_GET_CONTENT
        intent.type = "image/*"
        val chooser = Intent.createChooser(intent, "Choose a Picture")
        launcherIntentGallery.launch(chooser)
    }

    private val launcherIntentCameraX = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == CAMERA_X_RESULT) {
            val myFile = it.data?.getSerializableExtra("picture") as File
            val isBackCamera = it.data?.getBooleanExtra("isBackCamera", true) as Boolean

            val result = rotateBitmap(
                BitmapFactory.decodeFile(myFile.path),
                isBackCamera
            )
            imageUri = myFile.toUri()
            binding.previewImageView.setImageBitmap(result)
        }
    }

    private val launcherIntentGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val selectedImg: Uri = result.data?.data as Uri
            imageUri = selectedImg
            binding.previewImageView.setImageURI(selectedImg)
        }
    }

    private fun initVars() {
        storageRef = FirebaseStorage.getInstance().reference.child("Images")
        firebaseFirestore = FirebaseFirestore.getInstance()
        firebaseauth = Firebase.auth
    }

    companion object {
        const val CAMERA_X_RESULT = 200
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        private const val REQUEST_CODE_PERMISSIONS = 10
    }
}