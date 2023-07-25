package ru.netology.nmedia.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import kotlinx.coroutines.Dispatchers
import ru.netology.nmedia.auth.AppAuth

class AuthViewModel : ViewModel() {

    val data = AppAuth.getInstance().state.asLiveData(Dispatchers.Default)

    val isAuthenticated: Boolean
        get() = data.value?.token != null
}