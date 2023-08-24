package ru.netology.nmedia.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import ru.netology.nmedia.dto.MediaUpload
import ru.netology.nmedia.dto.User
import ru.netology.nmedia.model.FeedModelState
import ru.netology.nmedia.model.PhotoModel
import ru.netology.nmedia.repository.AuthRepository
import java.io.File
import javax.inject.Inject


@HiltViewModel
class RegistrViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _data = MutableLiveData<User>()
    val data: LiveData<User>
        get() = _data
    private val _state = MutableLiveData<FeedModelState>()
    val state: LiveData<FeedModelState>
        get() = _state
    private val _avatar = MutableLiveData<PhotoModel?>(null)
    val avatar: LiveData<PhotoModel?>
        get() = _avatar


    fun register(login: String, passwd: String, name: String) {
        viewModelScope.launch {
            try {
                val user = repository.registerUser(login, passwd, name)
                _data.value = user
            } catch (e: Exception) {
                _state.postValue(FeedModelState(loggingError = true))
            }
        }
    }



    fun registerWithPhoto(login: String, passwd: String, name: String, upload: MediaUpload) {
        viewModelScope.launch {
            try {
                _avatar.value?.file?.let { file ->
                    val user = repository.registerWithPhoto(
                        login = login,
                        passwd = passwd,
                        name = name,
                        upload = MediaUpload(file)
                    )
                    _data.value = user
                }

            } catch (e: Exception) {
                _state.postValue(FeedModelState(loggingError = true))
            }
        }
    }



    fun clearPhoto() {
        _avatar.value = null
    }

    fun changeAvatar(uri: Uri?, file: File?) {
        _avatar.value = PhotoModel(uri, file)
    }


}