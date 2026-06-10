package com.example.skinappp.ui.auth

import androidx.compose.ui.geometry.Rect
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.skinappp.DoctorCreateRequest
import com.example.skinappp.data.local.UserPreferencesManager
import com.example.skinappp.di.AppModule
import com.example.skinappp.domain.repository.AuthRepository
import com.example.skinappp.util.Resource
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userPrefs: UserPreferencesManager
) : ViewModel() {

    private val _loginState = MutableStateFlow<Resource<Unit>?>(null)
    val loginState = _loginState.asStateFlow()

    fun login(email: String, password: String) {
        _loginState.value = Resource.Loading
        viewModelScope.launch {
            try {
                val response = authRepository.login(email, password)
                userPrefs.saveAuthResponse(
                    token    = response.token,
                    userId   = response.userId,
                    email    = response.email,
                    provider = "EMAIL"
                )
                _loginState.value = Resource.Success(Unit)
            }catch ( e : retrofit2.HttpException){
                _loginState.value = Resource.Error(e)
            }catch (e : Exception){
                _loginState.value = Resource.Error(e)
            }
        }
    }

    fun register(email: String, password: String, fullName: String, phone: String?) {
        _loginState.value = Resource.Loading
        viewModelScope.launch {
            try {
                val response = authRepository.register(email, password, fullName, phone)
                userPrefs.saveAuthResponse(
                    token       = response.token,
                    userId      = response.userId,
                    email       = response.email,
                    displayName = fullName,          // user typed their name at registration
                    provider    = "EMAIL"
                )
                _loginState.value = Resource.Success(Unit)
            }catch ( e : retrofit2.HttpException){
                _loginState.value = Resource.Error(e)
            }catch (e : Exception){
                _loginState.value = Resource.Error(e)
            }

        }
    }

    fun googleSignIn(account: GoogleSignInAccount) {
        _loginState.value = Resource.Loading
        val idToken = account.idToken
        if (idToken == null) {
            _loginState.value = Resource.Error(Exception("Google Sign-In misconfigured: no ID token returned. Check your WEB_CLIENT_ID."))
            return
        }
        // Extract name fields from the Google account — the backend response won't include these
        val displayName = account.displayName       // e.g. "Sarah Williams"
        val givenName   = account.givenName
        viewModelScope.launch {
            try {
                val response = authRepository.googleSignIn(idToken)
                userPrefs.saveAuthResponse(
                    token       = response.token,
                    userId      = response.userId,
                    email       = response.email,
                    provider    = "GOOGLE"
                )
                userPrefs.saveGoogleProfile(displayName, givenName)
                _loginState.value = Resource.Success(Unit)
            }catch ( e : retrofit2.HttpException){
                _loginState.value = Resource.Error(e)
            }catch (e : Exception){
                _loginState.value = Resource.Error(e)
            }

        }// e.g. "Sarah" (fallback)
    }
    val webClientId: String = AppModule.WEB_CLIENT_ID

    fun addDoctor(doctorCreateRequest: DoctorCreateRequest){
        viewModelScope.launch {
            authRepository.addDoctor(doctorCreateRequest)
        }
    }
}
