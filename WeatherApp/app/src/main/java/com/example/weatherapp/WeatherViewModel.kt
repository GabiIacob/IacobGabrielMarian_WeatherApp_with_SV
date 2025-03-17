package com.example.weatherapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherapp.network.RetrofitClient
import com.example.weatherapp.network.WeatherResponse
import com.google.firebase.appdistribution.gradle.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Response

class WeatherViewModel : ViewModel() {
    private val _weatherData = MutableStateFlow<WeatherResponse?>(null)
    val weatherData: StateFlow<WeatherResponse?> = _weatherData

    private val apiService: com.example.weatherapp.ApiService = RetrofitClient.getApiService()

    fun fetchWeather(city: String) {
        viewModelScope.launch {
            try {
                val response: Response<WeatherResponse> = apiService.getWeather(city)
                if (response.isSuccessful) {
                    _weatherData.value = response.body()
                } else {
                }
            } catch (e: Exception) {
            }
        }
    }
}