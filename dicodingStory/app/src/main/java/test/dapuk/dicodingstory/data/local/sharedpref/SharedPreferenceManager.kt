package test.dapuk.dicodingstory.data.local.sharedpref

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import test.dapuk.dicodingstory.data.remote.response.LoginResult


class SharedPreferenceManager(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("SESSION", Context.MODE_PRIVATE)

    fun saveSession(userId: String, name: String, token: String) {
        val editor = sharedPreferences.edit()
        editor.putString("userId", userId)
        editor.putString("name", name)
        editor.putString("token", token)
        editor.apply()
        Log.d("saved session : ", "UserId: $userId, Name: $name, Token: $token")
    }

    fun getSession(): LoginResult? {
        val userId = sharedPreferences.getString("userId", null)
        val name = sharedPreferences.getString("name", null)
        val token = sharedPreferences.getString("token", null)
        Log.d("current session : ", "UserId: $userId, Name: $name, Token: $token")
        if (userId != null && name != null && token != null) {
            return LoginResult(userId, name, token)
        } else {
            return null
        }

    }

    fun clearSession() {
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()
    }
}

