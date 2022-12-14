package com.lamasya.ui.auth

import androidx.lifecycle.LiveData
import com.lamasya.data.remote.register.RegisterResponse

interface UserAuth {
    fun onSuccess(registerResponse: LiveData<RegisterResponse>)
    fun onFailure(registerResponse: LiveData<RegisterResponse>)
}