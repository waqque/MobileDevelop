package com.example.weatherapp

data class WeatherInfo(
    val cityName: String,
    val tempCelsius: Double,
    val humidity: Int,
    val windSpeed: Double,
    val windDeg: Int,
    val condition: String
)
