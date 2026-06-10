package com.example.skinappp.ui.home

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.skinappp.data.dto.AppointmentResponse
import com.example.skinappp.domain.repository.AppointmentRepository
import com.example.skinappp.domain.repository.AuthRepository
import com.example.skinappp.ui.appointments.AppointmentViewModel
import com.example.skinappp.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

data class UserState(
    val userName: String = "",
    val userEmail: String = ""
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _userState = MutableStateFlow<UserState>(UserState())
    val userState = _userState.asStateFlow()
    init {
        val name = authRepository.getFullName() ?: authRepository.getUserId()?.take(8) ?: "User"
        _userState.value = UserState(name)
    }
}
