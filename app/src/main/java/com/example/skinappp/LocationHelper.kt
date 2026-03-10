package com.example.skinappp

import android.Manifest
import android.content.Context
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.util.Log
import androidx.activity.result.IntentSenderRequest
import androidx.core.content.ContextCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.Priority
import com.google.android.gms.location.SettingsClient
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.Task
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class LocationTracker @Inject constructor(
    private val locationClient: FusedLocationProviderClient,
    @ApplicationContext private val context: Context,
) {
    suspend fun getCurrentLocation(makeRequest: (intentSenderRequest: IntentSenderRequest) -> Unit = {}): Location? {
        val hasAccessFineLocationPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val hasAccessCoarseLocationPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED


        if (!hasAccessCoarseLocationPermission && !hasAccessFineLocationPermission) {
            return null
        }
        val settingsClient: SettingsClient = LocationServices.getSettingsClient(context)
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000).build()
        val settingsRequest = LocationSettingsRequest.Builder().addLocationRequest(locationRequest).build()
        try {
            settingsClient.checkLocationSettings(settingsRequest).await()
        } catch (exception: Exception) {
            if (exception is ResolvableApiException) {
                try {
                    // If GPS is OFF, we create the IntentSenderRequest and pass it to the UI
                    val intentSenderRequest = IntentSenderRequest.Builder(exception.resolution).build()
                    makeRequest(intentSenderRequest)
                } catch (e: IntentSender.SendIntentException) {
                    Log.e("LocationTracker", "Error with IntentSender", e)
                }
            }
            return null // Return null because GPS isn't ready yet
        }
        return try {
            locationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                CancellationTokenSource().token
            ).await()
        } catch (e: Exception) {
            null
        }
    }
}