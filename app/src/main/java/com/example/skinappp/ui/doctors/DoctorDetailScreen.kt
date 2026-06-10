package com.example.skinappp.ui.doctors

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.skinappp.data.dto.DoctorResponse
import com.example.skinappp.ui.theme.*

@Composable
fun DoctorDetailScreen(
    doctorId: String,
    onBookAppointment: (String) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: DoctorViewModel = hiltViewModel()
) {
    // Collect the selected doctor directly from the ViewModel
    val nearby = viewModel.getDoctorFromCache(doctorId.toInt())
    Log.d("bjw" , nearby.toString())
    val doctor = nearby?.doctor

    Column(
        modifier = Modifier.fillMaxSize().background(AppBackground).verticalScroll(rememberScrollState())
    ) {
        // Header
        Box(
            modifier = Modifier.fillMaxWidth()
                .background(Brush.linearGradient(listOf(TealPrimary, TealSecondary), Offset(0f, 0f), Offset(Float.POSITIVE_INFINITY, 300f)))
                .padding(16.dp)
        ) {
            Column {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                }
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(72.dp).clip(CircleShape).background(Color.White.copy(0.25f)),
                        contentAlignment = Alignment.Center
                    ) { Icon(Icons.Default.Person, null, tint = Color.White, modifier = Modifier.size(42.dp)) }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(doctor?.doctorName ?: "Doctor", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                        Text("Dermatologist", color = Color.White.copy(0.85f), fontSize = 14.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, null, tint = Color(0xFFFFC107), modifier = Modifier.size(16.dp))
                            Text(" ${"%.1f".format(doctor?.rating ?: 0.0)}", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
        }

        if (doctor == null) {
            Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                Text("Doctor not found", color = TextSecondary)
            }
            return@Column
        }

        Spacer(Modifier.height(16.dp))

        // Stats row
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatChip(Icons.Default.WorkHistory, "${doctor.experienceYears}y", "Experience", Modifier.weight(1f))
            StatChip(Icons.Default.AttachMoney, "₹${doctor.fees.amount}", "Fees", Modifier.weight(1f))
            nearby?.let { StatChip(Icons.Default.DirectionsCar, "${"%.1f".format(it.roadDistanceKm)} km", "Distance", Modifier.weight(1f)) }
        }

        Spacer(Modifier.height(16.dp))

        // About
        DetailCard(title = "About") {
            Text(doctor.biography, color = TextSecondary, fontSize = 14.sp, lineHeight = 22.sp)
        }

        // Contact
        DetailCard(title = "Contact") {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Phone, null, tint = TealPrimary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(10.dp))
                Text(doctor.phoneNumber, color = TextPrimary, fontSize = 15.sp)
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, null, tint = TealPrimary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(10.dp))
                Text(doctor.location, color = TextPrimary, fontSize = 15.sp)
            }
        }

        // Working hours
        if (doctor.workingHours.isNotEmpty()) {
            DetailCard(title = "Working Hours") {
                doctor.workingHours.forEach { (day, slots) ->
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
                        Text(day, fontWeight = FontWeight.SemiBold, color = TextPrimary, modifier = Modifier.width(90.dp))
                        Text(slots.joinToString(", "), color = TextSecondary, fontSize = 13.sp)
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Button(
            onClick = { onBookAppointment(doctor.doctorId) },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).height(56.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
        ) {
            Icon(Icons.Default.CalendarToday, null)
            Spacer(Modifier.width(8.dp))
            Text("Book Appointment", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
fun StatChip(icon: androidx.compose.ui.graphics.vector.ImageVector, value: String, label: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp)) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = TealPrimary, modifier = Modifier.size(22.dp))
            Spacer(Modifier.height(4.dp))
            Text(value, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 15.sp)
            Text(label, color = TextSecondary, fontSize = 11.sp)
        }
    }
}

@Composable
fun DetailCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
            Spacer(Modifier.height(10.dp))
            content()
        }
    }
}