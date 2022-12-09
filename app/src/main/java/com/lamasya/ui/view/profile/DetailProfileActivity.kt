package com.lamasya.ui.view.profile

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.lamasya.R
import com.lamasya.data.model.MenuDetailProfileModel
import com.lamasya.data.remote.profile.ProfileResponse
import com.lamasya.databinding.ActivityDetailProfileBinding
import com.lamasya.ui.adapter.DetailProfileMenuAdapter
import com.lamasya.ui.auth.ProfileAuth
import com.lamasya.ui.view.main.MainActivity
import com.lamasya.ui.viewmodel.ProfileViewModel
import com.lamasya.util.logE

class DetailProfileActivity : AppCompatActivity(), ProfileAuth {
    private lateinit var binding: ActivityDetailProfileBinding
    private val profileVM: ProfileViewModel by viewModels()
    private val itemList = ArrayList<MenuDetailProfileModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        profileVM.profileAuth = this
        getProfileData()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.tittle_detail_profile)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) this.finish()
        return super.onOptionsItemSelected(item)
    }

    override fun onSuccess(profileResponse: LiveData<ProfileResponse>) {
        addItem(profileResponse)
    }

    private fun getProfileData() {
        val currentUID = MainActivity.CURRENT_UID
        profileVM.getProfile(currentUID)
    }

    private fun addItem(profileResponse: LiveData<ProfileResponse>) {
        val dataProfile = profileResponse.value
        val dataListItem = arrayOf(
            StringBuilder(dataProfile?.first_name.toString()).append(" ")
                .append(dataProfile?.last_name.toString()).toString(),
            StringBuilder(dataProfile?.email.toString()).toString(),
            StringBuilder(dataProfile?.phone.toString()).toString(),
            dataProfile?.gender.toString(),
            dataProfile?.age.toString()
        )

        itemList.clear()

        val listSize = titleListItem.size - 1
        for (i in 0..listSize) {
            itemList.add(
                MenuDetailProfileModel(
                    titleListItem[i],
                    dataListItem[i]
                )
            )
            showRecyclerList()
        }
        logE("ara list $itemList")

    }

    private fun showRecyclerList() {
        binding.apply {
            rvListDetailProfile.layoutManager = LinearLayoutManager(root.context)
            val mListDetailProfileAdapter = DetailProfileMenuAdapter(itemList)
            rvListDetailProfile.adapter = mListDetailProfileAdapter
        }
    }

    companion object {
        private val titleListItem = arrayOf("Nama Lengkap", "Email", "No Telephone", "Jenis Kelamin", "Umur")
    }

}