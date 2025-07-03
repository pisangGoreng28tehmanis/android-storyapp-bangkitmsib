package test.dapuk.dicodingstory.ui.login

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import kotlinx.coroutines.launch
import retrofit2.HttpException
import test.dapuk.dicodingstory.data.remote.response.StoryResponse
import test.dapuk.dicodingstory.data.remote.retrofit.ApiConfig
import test.dapuk.dicodingstory.utils.EspressoIdlingResource

class LoginViewModel : ViewModel() {
    private val _loginSuccess = MutableLiveData<Boolean>()
    val loginSuccess: LiveData<Boolean> = _loginSuccess

    private val _loginErr = MutableLiveData<String>()
    val loginErr: LiveData<String> = _loginErr

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _loginUserId = MutableLiveData<String>()
    val loginUserId: LiveData<String> = _loginUserId

    private val _loginName = MutableLiveData<String>()
    val loginName: LiveData<String> = _loginName

    private val _loginToken = MutableLiveData<String>()
    val loginToken: LiveData<String> = _loginToken

    fun login(email: String, password: String) {
        viewModelScope.launch {
            EspressoIdlingResource.increment()
            _isLoading.value = true
            val apiResponse = ApiConfig.getApiService()
            try {
                val response = apiResponse.login(email, password)
                Log.d("pesan", "${response.message}")
                if (response.message == "success") {
                    Log.d(
                        "LoginResponse",
                        "UserId: ${response.loginResult.userId}, Name: ${response.loginResult.name}, Token: ${response.loginResult.token}"
                    )
                    _loginToken.value = response.loginResult.token
                    _loginName.value = response.loginResult.name
                    _loginUserId.value = response.loginResult.userId
                    _loginSuccess.value = true
                } else {
                    Log.d("ada yang salah dengan data login", response.message)
                }
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                val errorResponse = Gson().fromJson(errorBody, StoryResponse::class.java)
                Log.e("Login Error", "Message: ${errorResponse.message}")
                _loginErr.value = errorResponse.message
            } catch (e: Exception) {
                Log.e("gagal", "${e.message}")
            } finally {
                _isLoading.value = false
                EspressoIdlingResource.decrement()
            }
        }
    }
}