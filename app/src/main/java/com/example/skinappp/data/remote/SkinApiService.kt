package com.example.skinappp.data.remote

import com.example.skinappp.DoctorCreateRequest
import com.example.skinappp.data.dto.*
import retrofit2.http.*

interface SkinApiService {

    @POST("auth/register")
    suspend fun register(@Body request: UserRegisterRequest): AuthResponse

    @POST("auth/login")
    suspend fun login(@Body request: UserLoginRequest): AuthResponse

    @POST("auth/google")
    suspend fun googleAuth(@Body request: GoogleAuthRequest): AuthResponse

    @GET("api/doctors/nearby")
    suspend fun getNearbyDoctors(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("radius") radius: Double = 5000.0
    ): List<NearbyDoctorResponse>

    @POST("api/appointments")
    suspend fun bookAppointment(@Body request: AppointmentCreateRequest): AppointmentResponse

    @POST("api/appointments/{id}/reschedule")
    suspend fun rescheduleAppointment(
        @Path("id") appointmentId: String,
        @Body request: AppointmentRescheduleRequest
    ): AppointmentResponse

    @PATCH("api/appointments/{id}/cancel")
    suspend fun cancelAppointment(
        @Path("id") appointmentId: String
    ): AppointmentStatusUpdateResponse

    @GET("api/users/{userId}/appointments")
    suspend fun getAppointments(@Path("userId") userId: String): List<AppointmentResponse>

    @GET("api/doctors/{doctorId}/availability")
    suspend fun getDoctorAvailability(@Path("doctorId") doctorId: String): DoctorAvailabilityResponse

    @POST("api/appointments/{id}/analyze-prescription")
    suspend fun analyzePrescription(@Path("id") appointmentId: String): PrescriptionAnalysisResponse

    @POST("/doctors")
    suspend fun SendDoctors(
        @Body doctor : DoctorCreateRequest
    ) : Unit
}
