package com.example.skinappp.ui.medication

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.skinappp.data.dto.PrescriptionAnalysisResponse
import com.example.skinappp.data.remote.SkinApiService
import com.example.skinappp.reminder.MedicationReminderManager
import com.example.skinappp.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MedicationViewModel @Inject constructor(
    private val api: SkinApiService,
    private val reminderManager: MedicationReminderManager
) : ViewModel() {

    private val TAG = "MedicationViewModel"

    private val _analysisResult = MutableStateFlow<Resource<PrescriptionAnalysisResponse>?>(null)
    val analysisResult = _analysisResult.asStateFlow()

    private val _schedulingResult = MutableStateFlow<Resource<Unit>?>(null)
    val schedulingResult = _schedulingResult.asStateFlow()

    fun analyzePrescription(appointmentId: String) {
        _analysisResult.value = Resource.Loading
        viewModelScope.launch {
            try {
                Log.i(TAG, "Requesting prescription analysis for appointment: $appointmentId")
                val response = api.analyzePrescription(appointmentId)
                _analysisResult.value = Resource.Success(response)
                Log.i(TAG, "Analysis successful. Found ${response.medications.size} medications.")
            } catch (e: Exception) {
                Log.e(TAG, "Analysis failed", e)
                _analysisResult.value = Resource.Error(e)
            }
        }
    }

    fun confirmAndSchedule(response: PrescriptionAnalysisResponse) {
        _schedulingResult.value = Resource.Loading
        viewModelScope.launch {
            try {
                reminderManager.scheduleMedications(response.appointmentId, response.medications)
                _schedulingResult.value = Resource.Success(Unit)
                Log.i(TAG, "Medications scheduled successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Scheduling failed", e)
                _schedulingResult.value = Resource.Error(e)
            }
        }
    }
    
    fun clearState() {
        _analysisResult.value = null
        _schedulingResult.value = null
    }
}
