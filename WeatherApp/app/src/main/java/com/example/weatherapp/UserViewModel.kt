// UserViewModel.kt
package com.example.weatherapp

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.weatherapp.network.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Response

class UserViewModel : ViewModel() {
    private val apiService: ApiService = RetrofitClient.getApiService()

    fun addUserToDatabase(email: String, password: String, city: String) {
        val user = User(email = email, password = password, city = city)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response: Response<ApiResponse> = apiService.addUser(user)
                if (response.isSuccessful) {
                    Log.d("UserViewModel", "User added: ${response.body()?.message}")
                } else {
                    Log.e("UserViewModel", "Error: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("UserViewModel", "Network error", e)
            }
        }
    }
}