package com.example.weatherapp

import Main
import Weather

data class WeatherForecastResponse(
    val list: List<WeatherForecast>
)

data class WeatherForecast(
    val dt_txt: String,
    val main: Main,
    val weather: List<Weather>,
)
