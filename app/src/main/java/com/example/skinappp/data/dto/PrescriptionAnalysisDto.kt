package com.example.skinappp.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MedicationPlanDto(
    @SerialName("medication_name") val medicationName: String,
    val dosage: String,
    @SerialName("frequency_per_day") val frequencyPerDay: Int,
    @SerialName("specific_times") val specificTimes: List<String>,
    @SerialName("duration_in_days") val durationInDays: Int,
    val instructions: String
)

@Serializable
data class PrescriptionAnalysisResponse(
    @SerialName("appointment_id") val appointmentId: String,
    @SerialName("raw_text") val rawText: String,
    val medications: List<MedicationPlanDto>,
    val summary: String
)
