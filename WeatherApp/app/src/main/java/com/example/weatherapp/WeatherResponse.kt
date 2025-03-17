package com.example.weatherapp.network

import com.google.gson.annotations.SerializedName

data class WeatherResponse(
    val oras: String = "",
    val prognoza: List<Forecast> = emptyList()
)

data class Forecast(
    val data: String = "",
    val temp: Double = 0.0,
    @SerializedName("temp_min")
    val tempMin: Double = 0.0,

    @SerializedName("temp_max")
    val tempMax: Double = 0.0,
    val umiditate: Int = 0,
    val presiune: Int = 0,
    val descriere: String = "",
    val vant: Wind = Wind()
)

data class Wind(
    val viteza: Double = 0.0,
    val directie: Int = 0
)
