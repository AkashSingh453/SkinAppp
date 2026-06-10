package com.example.skinappp.ui.doctors

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.skinappp.data.dto.AppointmentResponse
import com.example.skinappp.data.dto.NearbyDoctorResponse
import com.example.skinappp.domain.repository.DoctorRepository
import com.example.skinappp.util.Resource
import com.example.skinappp.util.Resource.*
import com.google.android.gms.location.FusedLocationProviderClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class DoctorViewModel @Inject constructor(
    private val doctorRepository: DoctorRepository,
    private val fusedLocationClient: FusedLocationProviderClient
) : ViewModel() {
    private val _doctorList = MutableStateFlow<Resource<List<NearbyDoctorResponse>>?>(null)
    val doctorList = _doctorList.asStateFlow()

    private val _locationState = MutableStateFlow<Resource<Boolean>?>(null)
    val locationState = _locationState.asStateFlow()

    @SuppressLint("MissingPermission")
    fun fetchNearbyDoctors() {
        val radius : Double = 4000.0
        viewModelScope.launch {
            _doctorList.value = Loading
            try {
                val location = fusedLocationClient.lastLocation.await()
                try {
                     val res = doctorRepository.getNearbyDoctors(location.latitude, location.longitude , radius)
                    _doctorList.value = Resource.Success(res)
                }catch(e : retrofit2.HttpException){
                    _doctorList.value = Resource.Error(e)
                }catch ( e : Exception){
                    _doctorList.value = Resource.Error(e)
                }
            }catch (e : Exception){
                _doctorList.value = Resource.Error(e)
            }
        }
    }

    fun getDoctorFromCache(doctorId: Int): NearbyDoctorResponse? {
        return doctorRepository.getCachedDoctorById(doctorId)
    }

    fun onPermissionDenied() {
        _locationState.value = Resource.Success(true)
    }

}
