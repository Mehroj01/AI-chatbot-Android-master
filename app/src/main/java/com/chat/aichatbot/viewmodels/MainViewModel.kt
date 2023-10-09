package com.chat.aichatbot.viewmodels

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chat.aichatbot.models.ChatResponse
import com.chat.aichatbot.models.Message
import com.chat.aichatbot.models.MessageModule
import com.chat.aichatbot.models.RequestBody
import com.chat.aichatbot.retrofit.ApiClient
import com.chat.aichatbot.retrofit.ApiService
import com.chat.aichatbot.utils.EventLoader
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.net.SocketTimeoutException

class MainViewModel : ViewModel() {
    private val _token = MutableLiveData<String>()

    val token: LiveData<String> = _token
    private val _chatResponse = MutableLiveData<EventLoader?>()
    val chatResponse: MutableLiveData<EventLoader?> = _chatResponse
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean>
        get() = _isLoading
    private var isKeyFetched = false

    private val _freeMessageCount = MutableLiveData<Int>()
    val freeMessageCount: LiveData<Int>
        get() = _freeMessageCount

    fun getTheKey() {
        if (!isKeyFetched) {
            val remoteConfig: FirebaseRemoteConfig = Firebase.remoteConfig
            val configSettings = remoteConfigSettings {
                minimumFetchIntervalInSeconds = 3600
            }

            remoteConfig.setConfigSettingsAsync(configSettings)

            remoteConfig.fetchAndActivate().addOnCompleteListener { task ->

                if (task.isSuccessful) {
                    _token.value = remoteConfig.getString("key")
                } else {
                    _token.value = ""
                }
            }

            isKeyFetched = true
        }
    }

    fun getChatResponse(message: RequestBody, token: String) {
        _isLoading.value = true
        _chatResponse.value = null

        viewModelScope.launch {
            _chatResponse.value = EventLoader.Loading
            try {
                val response = withTimeout(42_000) {
                    ApiClient.service.getChatResponse("Bearer $token", message)
                }
                if (response.isSuccessful) {
                    _chatResponse.value = EventLoader.Success(response.body()!!)
                    _freeMessageCount.value = _freeMessageCount.value!! - 1
                }
            } catch (e: SocketTimeoutException) {
                _chatResponse.value = EventLoader.Error("Timeout error")
            } catch (e: Exception) {
                _chatResponse.value = EventLoader.Error("An error occurred: ${e.message}")
            } finally {
                _isLoading.value = false
            }

        }
    }

    fun setTheCount(count: Int) {
        _freeMessageCount.value = count
    }
}