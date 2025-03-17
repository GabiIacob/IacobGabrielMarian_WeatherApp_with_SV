package com.example.weatherapp

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import com.google.android.gms.location.LocationServices
import java.util.Locale

class LocationHelper(private val context: Context) {
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    fun getCurrentCity(callback: (String?) -> Unit) {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val cityName = getCityNameFromCoordinates(location.latitude, location.longitude)
                callback(cityName)
            } else {
                callback(null)
            }
        }
    }

    private fun getCityNameFromCoordinates(latitude: Double, longitude: Double): String? {
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            addresses?.getOrNull(0)?.locality
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
