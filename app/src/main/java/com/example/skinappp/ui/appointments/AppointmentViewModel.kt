package com.example.skinappp.ui.appointments

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.skinappp.data.dto.AppointmentResponse
import com.example.skinappp.data.dto.AppointmentStatusUpdateResponse
import com.example.skinappp.data.dto.DoctorAvailabilityResponse
import com.example.skinappp.domain.repository.AppointmentRepository
import com.example.skinappp.domain.repository.AuthRepository
import com.example.skinappp.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppointmentViewModel @Inject constructor(
    private val appointmentRepository: AppointmentRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    companion object {
        private const val TAG = "AppointmentViewModel"
    }

    private val _myAppointments = MutableStateFlow<Resource<List<AppointmentResponse>>?>(null)
    val myAppointments = _myAppointments.asStateFlow()

    private val _bookedAppointment = MutableStateFlow<Resource<AppointmentResponse>?>(null)
    val bookedAppointment = _bookedAppointment.asStateFlow()

    private val _rescheduledAppointment = MutableStateFlow<Resource<AppointmentResponse>?>(null)
    val rescheduledAppointment = _rescheduledAppointment.asStateFlow()

    private val _cancelledAppointment = MutableStateFlow<Resource<AppointmentStatusUpdateResponse>?>(null)
    val cancelledAppointment = _cancelledAppointment.asStateFlow()

    private val _doctorAvailability = MutableStateFlow<Resource<DoctorAvailabilityResponse>?>(null)
    val doctorAvailability = _doctorAvailability.asStateFlow()

    private val _selectedAppointment = MutableStateFlow<AppointmentResponse?>(null)
    val selectedAppointment = _selectedAppointment.asStateFlow()

    init {
        Log.d(TAG, "ViewModel initialized, calling loadAppointments()")
        loadAppointments()
    }

    fun loadAppointments() {
        val userId = authRepository.getUserId()
        if (userId == null) {
            Log.w(TAG, "loadAppointments: userId is null, returning early.")
            return
        }

        Log.i(TAG, "loadAppointments: Fetching appointments for user $userId")
        _myAppointments.value = Resource.Loading

        viewModelScope.launch {
            try {
                val response = appointmentRepository.getMyAppointments(userId)
                Log.i(TAG, "loadAppointments: Successfully fetched ${response.size} appointments")
                _myAppointments.value = Resource.Success(response)
            } catch (e: retrofit2.HttpException) {
                Log.e(TAG, "loadAppointments: HttpException occurred - ${e.message}", e)
                _myAppointments.value = Resource.Error(e)
            } catch (e: Exception) {
                Log.e(TAG, "loadAppointments: General Exception occurred - ${e.message}", e)
                _myAppointments.value = Resource.Error(e)
            }
        }
    }

    fun bookAppointment(doctorId: String, appointmentTime: String, analysisId: String? = null) {
        val patientId = authRepository.getUserId()
        if (patientId == null) {
            Log.w(TAG, "bookAppointment: patientId is null, cannot book appointment.")
            return
        }

        Log.i(TAG, "bookAppointment: Attempting to book with doctor $doctorId at $appointmentTime")
        _bookedAppointment.value = Resource.Loading

        viewModelScope.launch {
            try {
                val response = appointmentRepository.bookAppointment(patientId, doctorId, appointmentTime, analysisId)
                Log.i(TAG, "bookAppointment: Successfully booked appointment with ID ${response.appointmentId}")
                _bookedAppointment.value = Resource.Success(response)
            } catch (e: retrofit2.HttpException) {
                Log.e(TAG, "bookAppointment: HttpException occurred - ${e.message.toString()}", e)
                _bookedAppointment.value = Resource.Error(e)
            } catch (e: Exception) {
                Log.e(TAG, "bookAppointment: General Exception occurred - ${e.message}", e)
                _bookedAppointment.value = Resource.Error(e)
            }
        }
    }

    fun loadDoctorAvailability(doctorId: String) {
        if (doctorId.isBlank()) {
            Log.w(TAG, "loadDoctorAvailability: doctorId is blank, returning early.")
            return
        }

        Log.i(TAG, "loadDoctorAvailability: Fetching availability for doctor $doctorId")
        _doctorAvailability.value = Resource.Loading

        viewModelScope.launch {
            try {
                val response = appointmentRepository.getDoctorAvailability(doctorId)
                Log.i(
                    TAG,
                    "loadDoctorAvailability: fetched ${response.bookedSlots.size} booked slots for doctor $doctorId"
                )
                _doctorAvailability.value = Resource.Success(response)
            } catch (e: retrofit2.HttpException) {
                Log.e(TAG, "loadDoctorAvailability: HttpException occurred - ${e.message}", e)
                _doctorAvailability.value = Resource.Error(e)
            } catch (e: Exception) {
                Log.e(TAG, "loadDoctorAvailability: General Exception occurred - ${e.message}", e)
                _doctorAvailability.value = Resource.Error(e)
            }
        }
    }

    fun rescheduleAppointment(appointmentId: String, newAppointmentTime: String) {
        Log.i(TAG, "rescheduleAppointment: Attempting to reschedule appointment $appointmentId to $newAppointmentTime")
        _rescheduledAppointment.value = Resource.Loading

        viewModelScope.launch {
            try {
                val response = appointmentRepository.rescheduleAppointment(appointmentId, newAppointmentTime)
                Log.i(TAG, "rescheduleAppointment: Successfully rescheduled appointment with ID ${response.appointmentId}")
                _rescheduledAppointment.value = Resource.Success(response)
                loadAppointments() // Refresh list
            } catch (e: retrofit2.HttpException) {
                Log.e(TAG, "rescheduleAppointment: HttpException occurred - ${e.message}", e)
                _rescheduledAppointment.value = Resource.Error(e)
            } catch (e: Exception) {
                Log.e(TAG, "rescheduleAppointment: General Exception occurred - ${e.message}", e)
                _rescheduledAppointment.value = Resource.Error(e)
            }
        }
    }

    fun cancelAppointment(appointmentId: String) {
        Log.i(TAG, "cancelAppointment: Attempting to cancel appointment $appointmentId")
        _cancelledAppointment.value = Resource.Loading

        viewModelScope.launch {
            try {
                val response = appointmentRepository.cancelAppointment(appointmentId)
                Log.i(TAG, "cancelAppointment: Successfully cancelled appointment with ID ${response.appointmentId}")
                _cancelledAppointment.value = Resource.Success(response)
                loadAppointments() // Refresh list
            } catch (e: retrofit2.HttpException) {
                Log.e(TAG, "cancelAppointment: HttpException occurred - ${e.message}", e)
                _cancelledAppointment.value = Resource.Error(e)
            } catch (e: Exception) {
                Log.e(TAG, "cancelAppointment: General Exception occurred - ${e.message}", e)
                _cancelledAppointment.value = Resource.Error(e)
            }
        }
    }

    fun selectAppointment(appointment: AppointmentResponse) {
        _selectedAppointment.value = appointment
    }

    fun clearBookingState() {
        Log.d(TAG, "clearBookingState: Clearing booked appointment state")
        _bookedAppointment.value = null
    }

    fun clearError() {
        clearBookingState()
        _rescheduledAppointment.value = null
        _cancelledAppointment.value = null
    }

    fun getUserId(): String? {
        val userId = authRepository.getUserId()
        Log.d(TAG, "getUserId: Retrieved $userId")
        return userId
    }
}
