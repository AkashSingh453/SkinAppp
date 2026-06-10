package com.example.skinappp.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DoctorResponse(
    @SerialName("doctor_id") val doctorId: String,
    @SerialName("doctor_name") val doctorName: String,
    @SerialName("phone_number") val phoneNumber: String,
    @SerialName("experience_years") val experienceYears: Int,
    val biography: String,
    @SerialName("profile_image_url") val profileImageUrl: String? = null,
    val rating: Double,
    val location: String,
    val coordinates: Coordinates,
    val fees: Fees,
    @SerialName("working_hours") val workingHours: Map<String, List<String>>
)

@Serializable
data class NearbyDoctorResponse(
    val doctor: DoctorResponse,
    @SerialName("road_distance_km") val roadDistanceKm: Double
)

@Serializable
data class Coordinates(
    val latitude: Double,
    val longitude: Double
)

@Serializable
data class Fees(
    val amount: Int,
    val currency: String = "INR"
)
