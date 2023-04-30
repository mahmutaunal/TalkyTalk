package com.mahmutalperenunal.talkytalk.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.mahmutalperenunal.talkytalk.model.UserModel
import com.mahmutalperenunal.talkytalk.repository.AppRepo

class ProfileViewModel : ViewModel() {

    private var appRepo = AppRepo.StaticFunction.getInstance()

    fun getUser(): LiveData<UserModel> {
        return appRepo.getUser()
    }

    fun updateStatus(status: String) {
        appRepo.updateStatus(status)

    }

    fun updateName(userName: String?) {
        appRepo.updateName(userName!!)
    }

    fun updateImage(imagePath: String) {
        appRepo.updateImage(imagePath)
    }


}