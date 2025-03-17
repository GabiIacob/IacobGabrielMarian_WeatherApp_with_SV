package com.example.weatherapp

import LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import com.example.weatherapp.network.LoginRequest
import com.example.weatherapp.network.WeatherResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @POST("/add_user")
    suspend fun addUser(@Body user: User): Response<ApiResponse>
    @POST("/api/login")
    fun login(@Body loginRequest: LoginRequest): Call<LoginResponse>
    @GET("/vreme")
    suspend fun getWeather(@Query("oras") city: String): Response<WeatherResponse>
}
