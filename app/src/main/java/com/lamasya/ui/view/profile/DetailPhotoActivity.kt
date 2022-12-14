package com.lamasya.ui.view.profile

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.lamasya.R
import com.lamasya.databinding.ActivityDetailPhotoActivityBinding
import com.lamasya.ui.view.main.MainActivity
import com.lamasya.util.toast

@Suppress("DEPRECATION")
class DetailPhotoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailPhotoActivityBinding
    private lateinit var remoteStorage: StorageReference
    private var firestore = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityDetailPhotoActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        super.onCreate(savedInstanceState)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        title = getString(R.string.tittle_detail_photo)
        remoteStorage = FirebaseStorage.getInstance().reference.child("Images/Profile/")
        getPhotoFromGalery()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val menuInflate = menuInflater
        menuInflate.inflate(R.menu.save_button_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.home -> {
                finish()
                return true
            }
            R.id.menuSaveData -> {
                finish()
                updatePhotoProfile()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private val launcherIntentGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val selectedImg: Uri = result.data?.data as Uri
            IMAGE_URI = selectedImg
            binding.imvDetailPhoto.setImageURI(selectedImg)
        }
    }

    private fun getPhotoFromGalery() {
        val intent = Intent()
        intent.action = Intent.ACTION_GET_CONTENT
        intent.type = "image/*"
        val chooser = Intent.createChooser(intent, "Choose a Picture")
        launcherIntentGallery.launch(chooser)
    }

    private fun updatePhotoProfile() {
        val currentUID = MainActivity.CURRENT_UID
        remoteStorage = remoteStorage.child(System.currentTimeMillis().toString())
        IMAGE_URI.let {
            remoteStorage.putFile(it).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    remoteStorage.downloadUrl.addOnSuccessListener { uri ->
                        firestore.collection("detail_user").document(currentUID)
                            .update(
                                "profile_pict", uri.toString()
                            ).addOnCompleteListener { it ->
                                if (it.isSuccessful) {
                                    toast(getString(R.string.update_success))
                                    finish()
                                } else {
                                    toast(getString(R.string.update_unsuccess))
                                    finish()
                                }
                            }
                    }
                }
            }
        }
    }


    companion object {
        lateinit var IMAGE_URI: Uri
    }
}