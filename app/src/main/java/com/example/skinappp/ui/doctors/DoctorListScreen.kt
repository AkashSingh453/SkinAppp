package com.example.skinappp.ui.doctors

import android.Manifest
import android.app.Activity.RESULT_OK
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.skinappp.data.dto.NearbyDoctorResponse
import com.example.skinappp.ui.theme.*
import com.example.skinappp.util.Resource
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import kotlin.math.roundToInt

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun DoctorListScreen(
    onDoctorClick: (String) -> Unit,
    viewModel: DoctorViewModel = hiltViewModel()
) {
    val doctorListState by viewModel.doctorList.collectAsState()
    val locationState by viewModel.locationState.collectAsState()
    val context = LocalContext.current

    val isLoading = doctorListState is Resource.Loading
    val apiError = (doctorListState as? Resource.Error)?.exception?.message
    val doctors = (doctorListState as? Resource.Success)?.data ?: emptyList()
    val locationPermissionRequired = (locationState as? Resource.Success)?.data == true

    // 1. Setup Software Permission
    val locationPermission = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

    // 2. Setup Hardware GPS Launcher
    val gpsSettingLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            // User turned on GPS! Finally, fetch the doctors.
            viewModel.fetchNearbyDoctors()
        }
    }

    // 3. The reusable function to check GPS Hardware via Google Play Services
    val checkGpsAndFetch = {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000).build()
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val client = LocationServices.getSettingsClient(context)

        client.checkLocationSettings(builder.build())
            .addOnSuccessListener {
                // GPS is already ON! We are good to go.
                viewModel.fetchNearbyDoctors()
            }
            .addOnFailureListener { exception ->
                if (exception is ResolvableApiException) {
                    // GPS is OFF. Extract the specific intent to show the system dialog!
                    try {
                        val intentSenderRequest = IntentSenderRequest.Builder(exception.resolution).build()
                        gpsSettingLauncher.launch(intentSenderRequest)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
    }

    // 4. The Flow Controller: Reacts when screen opens or permission changes
    LaunchedEffect(locationPermission.status) {
        if (locationPermission.status.isGranted) {
            // Permission is good, now check hardware
            checkGpsAndFetch()
        } else {
            // Ask for permission first
            locationPermission.launchPermissionRequest()
        }
    }

    LaunchedEffect(locationPermission.status.isGranted) {
        if (!locationPermission.status.isGranted && !locationPermission.status.shouldShowRationale) {
            viewModel.onPermissionDenied()
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(AppBackground)) {
        Box(
            modifier = Modifier.fillMaxWidth()
                .background(Brush.linearGradient(listOf(TealPrimary, TealSecondary), Offset(0f, 0f), Offset(Float.POSITIVE_INFINITY, 200f)))
                .padding(20.dp)
        ) {
            Column {
                Text("Nearby Doctors", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text("Dermatologists near your location", color = Color.White.copy(0.85f), fontSize = 13.sp)
            }
        }

        when {
            locationPermissionRequired -> {
                PermissionRationaleCard { locationPermission.launchPermissionRequest() }
            }
            isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = TealPrimary)
                        Spacer(Modifier.height(12.dp))
                        Text("Finding nearby doctors...", color = TextSecondary)
                    }
                }
            }
            apiError != null -> {
                Box(Modifier.fillMaxSize().padding(20.dp), contentAlignment = Alignment.Center) {
                    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.ErrorOutline, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(48.dp))
                            Spacer(Modifier.height(8.dp))
                            Text(apiError, textAlign = androidx.compose.ui.text.style.TextAlign.Center, color = TextSecondary)
                            Spacer(Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    // If they click retry, restart the whole safety flow
                                    if (locationPermission.status.isGranted) checkGpsAndFetch()
                                    else locationPermission.launchPermissionRequest()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                            ) { Text("Retry") }
                        }
                    }
                }
            }
            doctors.isEmpty() && doctorListState != null -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No doctors found nearby", color = TextSecondary, fontSize = 16.sp)
                }
            }
            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(doctors) { ind, nearby ->
                        DoctorCard(nearby = nearby, onClick = {
                            // PRO-TIP: Pass the actual ID, not the index, so your Detail screen works!
                            onDoctorClick(ind.toString())
                        })
                    }
                }
            }
        }
    }
}

@Composable
fun DoctorCard(
    nearby: NearbyDoctorResponse,
    onClick: () -> Unit
) {
    val doc = nearby.doctor
    val distanceText = if (nearby.roadDistanceKm < 1.0)
        "${(nearby.roadDistanceKm * 1000).roundToInt()} m away"
    else
        "${"%.1f".format(nearby.roadDistanceKm)} km away"

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(56.dp).clip(CircleShape).background(TealSoft),
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Default.Person, null, tint = TealPrimary, modifier = Modifier.size(30.dp)) }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(doc.doctorName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                Text("Dermatologist", color = TextSecondary, fontSize = 13.sp)
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, null, tint = Color(0xFFFFC107), modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(2.dp))
                    Text("${"%.1f".format(doc.rating)}", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Text("  •  ${doc.experienceYears}y exp", color = TextSecondary, fontSize = 12.sp)
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Surface(color = TealSoft, shape = RoundedCornerShape(20.dp)) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.DirectionsCar, null, tint = TealPrimary, modifier = Modifier.size(13.dp))
                        Spacer(Modifier.width(3.dp))
                        Text(distanceText, color = TealPrimaryDark, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.height(6.dp))
                Text("₹${doc.fees.amount}", color = TealPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun PermissionRationaleCard(onRequestPermission: () -> Unit) {
    Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(6.dp)) {
            Column(Modifier.padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.LocationOn, null, tint = TealPrimary, modifier = Modifier.size(56.dp))
                Spacer(Modifier.height(16.dp))
                Text("Location Permission Needed", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
                Spacer(Modifier.height(8.dp))
                Text("To find dermatologists near you, SkinAI needs access to your location.", color = TextSecondary, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                Spacer(Modifier.height(20.dp))
                Button(onClick = onRequestPermission, shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)) { Text("Grant Permission") }
            }
        }
    }
}