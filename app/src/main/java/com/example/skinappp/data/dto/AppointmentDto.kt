package com.example.skinappp.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AppointmentCreateRequest(
    @SerialName("patient_id") val patientId: String,
    @SerialName("doctor_id") val doctorId: String,
    @SerialName("associated_analysis_id") val associatedAnalysisId: String? = null,
    @SerialName("appointment_time") val appointmentTime: String // ISO-8601
)

@Serializable
data class AppointmentResponse(
    @SerialName("appointment_id") val appointmentId: String,
    @SerialName("patient_id") val patientId: String,
    @SerialName("doctor_id") val doctorId: String,
    @SerialName("associated_analysis_id") val associatedAnalysisId: String? = null,
    @SerialName("appointment_time") val appointmentTime: String,
    val status: String,
    @SerialName("prescription_image_url") val prescriptionImageUrl: String? = null,
    @SerialName("prescription_text") val prescriptionText: String? = null
)

@Serializable
data class AppointmentRescheduleRequest(
    @SerialName("new_appointment_time") val newAppointmentTime: String // ISO-8601
)

@Serializable
data class AppointmentStatusUpdateResponse(
    @SerialName("appointment_id") val appointmentId: String,
    val status: String,
    val message: String
)

@Serializable
data class BookedSlotResponse(
    @SerialName("appointment_time") val appointmentTime: String,
    val status: String
)

@Serializable
data class DoctorAvailabilityResponse(
    @SerialName("doctor_id") val doctorId: String,
    @SerialName("working_hours") val workingHours: Map<String, List<String>>,
    @SerialName("booked_slots") val bookedSlots: List<BookedSlotResponse>,
    @SerialName("slot_duration_minutes") val slotDurationMinutes: Int = 30
)
