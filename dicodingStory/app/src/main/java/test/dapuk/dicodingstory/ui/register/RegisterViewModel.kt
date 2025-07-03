package test.dapuk.dicodingstory.ui.register

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.launch
import retrofit2.HttpException
import test.dapuk.dicodingstory.data.remote.response.StoryResponse
import test.dapuk.dicodingstory.data.remote.retrofit.ApiConfig

class RegisterViewModel : ViewModel() {
    private val _registerSuccess = MutableLiveData<Boolean>()
    val registerSuccess: LiveData<Boolean> = _registerSuccess

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _registerErr = MutableLiveData<String>()
    val registerErr: LiveData<String> = _registerErr

    fun register(name: String, email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val apiResponse = ApiConfig.getApiService()
            try {
                val response = apiResponse.createUser(name, email, password)
                Log.d("RegisterViewModel", "Registration successful: ${response.message}")
                _registerSuccess.value = true
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                if (!errorBody.isNullOrEmpty()) {
                    try {
                        val errorResponse = Gson().fromJson(errorBody, StoryResponse::class.java)
                        _registerErr.value = errorResponse.message
                        Log.e("RegisterViewModel", "Error: ${errorResponse.message}")
                    } catch (jsonEx: JsonSyntaxException) {
                        _registerErr.value = "Failed to parse error response."
                        Log.e("RegisterViewModel", "Error parsing response: ${jsonEx.message}")
                    }
                } else {
                    _registerErr.value = "Unknown error occurred."
                    Log.e("RegisterViewModel", "Empty error body.")
                }
            } catch (e: Exception) {
                _registerErr.value = e.message ?: "An unexpected error occurred."
                Log.e("RegisterViewModel", "Exception: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
}
