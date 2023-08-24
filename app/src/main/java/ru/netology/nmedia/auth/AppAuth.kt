package ru.netology.nmedia.auth

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

import ru.netology.nmedia.api.PostApiService
import ru.netology.nmedia.dto.PushToken
import ru.netology.nmedia.dto.Token
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class AppAuth @Inject constructor(
    @ApplicationContext
    private val context: Context,



) {

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
        sendPushToken()

    }

    @Synchronized
    fun setAuth(id: Long, token: String) {
        pref.edit {
            putString(TOKEN_KEY, token)
            putLong(ID_KEY, id)
        }
        _state.value = Token(id, token)
        sendPushToken()
    }

    @Synchronized
    fun clear() {
        pref.edit { clear() }
        _state.value = null
        sendPushToken()
    }

    @InstallIn(SingletonComponent::class)
    @EntryPoint
    interface AppAuthEntryPoint {
        fun  getApiService(): PostApiService
    }

    fun sendPushToken(token: String? = null) {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val tokenDto = PushToken(token ?: Firebase.messaging.token.await())
                val entryPoint = EntryPointAccessors.fromApplication(context, AppAuthEntryPoint::class.java)
                entryPoint.getApiService().sendPushToken(tokenDto)
                //PostApi.retrofitService.sendPushToken(tokenDto)
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }


    companion object {

        private const val ID_KEY = "ID_KEY"
        private const val TOKEN_KEY = "TOKEN_KEY"


//        private var INSTANCE: AppAuth? = null
//
//        //2
//        fun getInstance(): AppAuth = requireNotNull(INSTANCE)
//
//        // 1
//        fun initApp(context: Context) {
//            INSTANCE = AppAuth(context)
//        }

    }

}
