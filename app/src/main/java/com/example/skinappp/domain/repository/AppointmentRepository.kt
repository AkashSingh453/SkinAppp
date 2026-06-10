package com.example.skinappp.domain.repository

import com.example.skinappp.data.dto.AppointmentCreateRequest
import com.example.skinappp.data.dto.AppointmentRescheduleRequest
import com.example.skinappp.data.dto.AppointmentResponse
import com.example.skinappp.data.dto.AppointmentStatusUpdateResponse
import com.example.skinappp.data.dto.DoctorAvailabilityResponse
import com.example.skinappp.data.remote.SkinApiService
import javax.inject.Inject

class AppointmentRepository @Inject constructor(
    private val api: SkinApiService
) {
    suspend fun bookAppointment(
        patientId: String,
        doctorId: String,
        appointmentTime: String,
        analysisId: String?
    ): AppointmentResponse {
        return api.bookAppointment(
                AppointmentCreateRequest(patientId, doctorId, analysisId, appointmentTime)
            )
    }

    suspend fun getMyAppointments(userId: String): List<AppointmentResponse> {
        return api.getAppointments(userId)
    }

    suspend fun rescheduleAppointment(
        appointmentId: String,
        newAppointmentTime: String
    ): AppointmentResponse {
        return api.rescheduleAppointment(
            appointmentId,
            AppointmentRescheduleRequest(newAppointmentTime)
        )
    }

    suspend fun cancelAppointment(appointmentId: String): AppointmentStatusUpdateResponse {
        return api.cancelAppointment(appointmentId)
    }

    suspend fun getDoctorAvailability(doctorId: String): DoctorAvailabilityResponse {
        return api.getDoctorAvailability(doctorId)
    }
}
