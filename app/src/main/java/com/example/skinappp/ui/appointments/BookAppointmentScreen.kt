package com.example.skinappp.ui.appointments

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.skinappp.data.dto.AppointmentResponse
import com.example.skinappp.data.dto.BookedSlotResponse
import com.example.skinappp.data.dto.DoctorAvailabilityResponse
import com.example.skinappp.ui.theme.*
import com.example.skinappp.util.Resource
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

private data class AppointmentSlotUi(
    val label: String,
    val time: LocalTime,
    val isoDateTime: String,
    val isBooked: Boolean,
    val isPast: Boolean
)

@Composable
fun BookAppointmentScreen(
    doctorId: String,
    onBookingConfirmed: (String) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: AppointmentViewModel = hiltViewModel()
) {
    val bookingState by viewModel.bookedAppointment.collectAsState()
    val availabilityState by viewModel.doctorAvailability.collectAsState()
    val today = LocalDate.now()
    val dates = remember { (0..13).map { today.plusDays(it.toLong()) } }
    var selectedDate by remember { mutableStateOf(today) }
    var selectedTime by remember { mutableStateOf<AppointmentSlotUi?>(null) }
    val availability = (availabilityState as? Resource.Success<DoctorAvailabilityResponse>)?.data
    val timeSlots = remember(selectedDate, availability) {
        availability?.let { buildAppointmentSlots(selectedDate, it) } ?: emptyList()
    }

    LaunchedEffect(doctorId) {
        selectedDate = today
        selectedTime = null
        viewModel.clearBookingState()
        viewModel.loadDoctorAvailability(doctorId)
    }

    LaunchedEffect(availability) {
        val loadedAvailability = availability ?: return@LaunchedEffect
        if (!hasWorkingHours(selectedDate, loadedAvailability)) {
            dates.firstOrNull { hasWorkingHours(it, loadedAvailability) }?.let { firstWorkingDate ->
                selectedDate = firstWorkingDate
                selectedTime = null
            }
        }
    }

    LaunchedEffect(bookingState) {
        if(bookingState is Resource.Success) {
            onBookingConfirmed((bookingState as Resource.Success<AppointmentResponse>).data.appointmentId)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(AppBackground).verticalScroll(rememberScrollState())
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
                .background(Brush.linearGradient(listOf(TealPrimary, TealSecondary), Offset(0f, 0f), Offset(Float.POSITIVE_INFINITY, 200f)))
                .padding(16.dp)
        ) {
            Column {
                IconButton(onClick = onNavigateBack) { Icon(Icons.Default.ArrowBack, null, tint = Color.White) }
                Spacer(Modifier.height(4.dp))
                Text("Book Appointment", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text("Select your preferred date & time", color = Color.White.copy(0.85f), fontSize = 13.sp)
            }
        }

        Spacer(Modifier.height(20.dp))

        // Date picker
        Text("Select Date", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = TextPrimary, modifier = Modifier.padding(horizontal = 20.dp))
        Spacer(Modifier.height(12.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            itemsIndexed(dates) { _, date ->
                val isSelected = date == selectedDate
                val isToday = date == today
                val hasHours = availability?.let { hasWorkingHours(date, it) } ?: true
                Card(
                    modifier = Modifier.width(62.dp).clickable(enabled = hasHours) {
                        selectedDate = date
                        selectedTime = null
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            isSelected -> TealPrimary
                            !hasHours -> Color(0xFFE8EEF0)
                            else -> Color.White
                        }
                    ),
                    elevation = CardDefaults.cardElevation(if (isSelected && hasHours) 6.dp else 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                            fontSize = 11.sp,
                            color = when {
                                isSelected -> Color.White.copy(0.85f)
                                !hasHours -> TextSecondary.copy(alpha = 0.45f)
                                else -> TextSecondary
                            }
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            date.dayOfMonth.toString(),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                isSelected -> Color.White
                                !hasHours -> TextSecondary.copy(alpha = 0.45f)
                                else -> TextPrimary
                            }
                        )
                        if (isToday) {
                            Spacer(Modifier.height(4.dp))
                            Box(Modifier.size(5.dp).clip(RoundedCornerShape(3.dp)).background(if (isSelected) Color.White else TealPrimary))
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // Time slots
        Text("Select Time", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = TextPrimary, modifier = Modifier.padding(horizontal = 20.dp))
        Spacer(Modifier.height(12.dp))

        when (val state = availabilityState) {
            null, Resource.Loading -> {
                Box(
                    modifier = Modifier.fillMaxWidth().height(220.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = TealPrimary)
                }
            }
            is Resource.Error -> {
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        state.exception.message.toString(),
                        modifier = Modifier.padding(14.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontSize = 13.sp
                    )
                }
            }
            is Resource.Success -> {
                if (timeSlots.isEmpty()) {
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                        color = Color.White,
                        shape = RoundedCornerShape(12.dp),
                        shadowElevation = 1.dp,
                        border = ButtonDefaults.outlinedButtonBorder
                    ) {
                        Text(
                            "No appointment slots available for this day.",
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.height(220.dp)
                    ) {
                        items(timeSlots, key = { it.isoDateTime }) { slot ->
                            val isSelected = slot == selectedTime
                            val isDisabled = slot.isBooked || slot.isPast
                            Surface(
                                modifier = Modifier.clickable(enabled = !isDisabled) { selectedTime = slot },
                                shape = RoundedCornerShape(10.dp),
                                color = when {
                                    isSelected -> TealPrimary
                                    isDisabled -> Color(0xFFE8EEF0)
                                    else -> Color.White
                                },
                                shadowElevation = if (isSelected) 4.dp else 1.dp,
                                border = if (!isSelected) ButtonDefaults.outlinedButtonBorder else null
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp).fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        slot.label,
                                        textAlign = TextAlign.Center,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = when {
                                            isSelected -> Color.White
                                            isDisabled -> TextSecondary.copy(alpha = 0.55f)
                                            else -> TextPrimary
                                        }
                                    )
                                    if (slot.isBooked || slot.isPast) {
                                        Spacer(Modifier.height(2.dp))
                                        Text(
                                            if (slot.isBooked) "Booked" else "Past",
                                            textAlign = TextAlign.Center,
                                            fontSize = 10.sp,
                                            color = if (isSelected) Color.White.copy(alpha = 0.85f) else TextSecondary.copy(alpha = 0.55f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // Summary
        AnimatedVisibility(visible = selectedTime != null) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = TealSoft),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Booking Summary", fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(Modifier.height(8.dp))
                    val fmt = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")
                    Text("📅  ${selectedDate.format(fmt)}", color = TextPrimary, fontSize = 14.sp)
                    Text("🕐  ${selectedTime?.label ?: ""}", color = TextPrimary, fontSize = 14.sp)
                }
            }
        }
        when(bookingState){
            is Resource.Error -> {
                Spacer(Modifier.height(8.dp))
                Surface(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp), color = MaterialTheme.colorScheme.errorContainer, shape = RoundedCornerShape(12.dp)) {
                    Text((bookingState as Resource.Error).exception.message.toString(), modifier = Modifier.padding(14.dp), color = MaterialTheme.colorScheme.onErrorContainer, fontSize = 13.sp)
                }
            }else -> {}
        }

        Spacer(Modifier.height(20.dp))
        Button(
            onClick = {
                selectedTime?.let { slot ->
                    viewModel.bookAppointment(doctorId, slot.isoDateTime)
                }
            },
            enabled = selectedTime != null && bookingState != Resource.Loading,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).height(56.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
        ) {
            if(bookingState == Resource.Loading){
                CircularProgressIndicator(Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
                Spacer(Modifier.width(8.dp))
                Text("Booking...")
            }else{
                Text("Confirm Booking", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

        }
        Spacer(Modifier.height(32.dp))
    }
}

private fun buildAppointmentSlots(
    date: LocalDate,
    availability: DoctorAvailabilityResponse
): List<AppointmentSlotUi> {
    val slotDurationMinutes = availability.slotDurationMinutes.coerceAtLeast(1).toLong()
    val bookedDateTimes = availability.bookedSlots
        .mapNotNull { it.toLocalDateTime() }
        .toSet()
    val now = LocalDateTime.now().withSecond(0).withNano(0)
    val displayFormatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH)
    val isoFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    return availability.workingHours[date.dayName()].orEmpty()
        .flatMap { range ->
            val (rangeStart, rangeEnd) = parseWorkingRange(range) ?: return@flatMap emptyList()
            val slots = mutableListOf<AppointmentSlotUi>()
            var cursor = rangeStart

            while (!cursor.plusMinutes(slotDurationMinutes).isAfter(rangeEnd)) {
                val dateTime = date.atTime(cursor).withSecond(0).withNano(0)
                slots.add(
                    AppointmentSlotUi(
                        label = cursor.format(displayFormatter),
                        time = cursor,
                        isoDateTime = dateTime.format(isoFormatter),
                        isBooked = bookedDateTimes.contains(dateTime),
                        isPast = dateTime.isBefore(now)
                    )
                )
                cursor = cursor.plusMinutes(slotDurationMinutes)
            }

            slots
        }
        .distinctBy { it.isoDateTime }
        .sortedBy { it.time }
}

private fun hasWorkingHours(date: LocalDate, availability: DoctorAvailabilityResponse): Boolean {
    return availability.workingHours[date.dayName()].orEmpty().isNotEmpty()
}

private fun LocalDate.dayName(): String {
    return dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH)
}

private fun BookedSlotResponse.toLocalDateTime(): LocalDateTime? {
    val normalized = appointmentTime.trim()
    return runCatching {
        LocalDateTime.parse(normalized, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    }.getOrElse {
        runCatching {
            OffsetDateTime.parse(normalized).toLocalDateTime()
        }.getOrNull()
    }?.withSecond(0)?.withNano(0)
}

private fun parseWorkingRange(range: String): Pair<LocalTime, LocalTime>? {
    val parts = range.split("-").map { it.trim() }
    if (parts.size != 2) return null

    val start = parseWorkingTime(parts[0]) ?: return null
    val end = parseWorkingTime(parts[1]) ?: return null
    if (!start.isBefore(end)) return null

    return start to end
}

private fun parseWorkingTime(value: String): LocalTime? {
    return runCatching {
        LocalTime.parse(value, DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH))
    }.getOrElse {
        runCatching {
            LocalTime.parse(value, DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH))
        }.getOrNull()
    }
}
