package ru.netology.nmedia.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel

import kotlinx.coroutines.launch

import ru.netology.nmedia.dto.User
import ru.netology.nmedia.model.FeedModelState
import ru.netology.nmedia.repository.AuthRepository


import javax.inject.Inject

@HiltViewModel
class LogInViewModel @Inject constructor(
    private val repository :AuthRepository
): ViewModel() {

    private val _data = MutableLiveData<User>()
    val data: LiveData<User>
        get() = _data
    private val _state = MutableLiveData<FeedModelState>()
    val state: LiveData<FeedModelState>
        get() = _state

    fun login(login: String, passwd: String) {
        viewModelScope.launch {
            try {
                val user = repository.authenticateUser(login, passwd)
                _data.value = user
            } catch (e: Exception) {
                _state.postValue(FeedModelState(loggingError = true))
            }
        }
    }


}