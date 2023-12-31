package ru.netology.nmedia.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import ru.netology.nmedia.auth.AppAuth
import javax.inject.Inject



@HiltViewModel
class AuthViewModel @Inject constructor(
    appAuth: AppAuth

) : ViewModel() {

    val data = appAuth.state.asLiveData(Dispatchers.Default)

    val isAuthenticated: Boolean
        get() = data.value?.token != null
}