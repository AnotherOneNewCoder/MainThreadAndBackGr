package ru.netology.nmedia.auth

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.netology.nmedia.dto.Token

class AppAuth private constructor(context: Context) {

    private val pref: SharedPreferences = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
    private val _state = MutableStateFlow<Token?>(null)
    val state = _state.asStateFlow()


    init {
        val idKey = pref.getLong(ID_KEY, 0)
        val tokenKey = pref.getString(TOKEN_KEY, null)

        if (!pref.contains(TOKEN_KEY) || tokenKey == null) {
            pref.edit { clear() }
        } else {
            _state.value = Token(idKey, tokenKey)
        }

    }

    @Synchronized
    fun setAuth(id: Long, token: String) {
        pref.edit {
            putString(TOKEN_KEY, token)
            putLong(ID_KEY, id)
        }
        _state.value = Token(id, token)
    }

    @Synchronized
    fun clear(){
        pref.edit { clear() }
        _state.value = null
    }


    companion object {

        private const val ID_KEY = "ID_KEY"
        private const val TOKEN_KEY = "TOKEN_KEY"


        private var INSTANCE: AppAuth? = null

        //2
        fun getInstance(): AppAuth = requireNotNull(INSTANCE)

        // 1
        fun initApp(context: Context) {
            INSTANCE = AppAuth(context)
        }
    }

}