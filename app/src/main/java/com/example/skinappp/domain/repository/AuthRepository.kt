package com.example.skinappp.domain.repository

import com.example.skinappp.DoctorCreateRequest
import com.example.skinappp.data.dto.AuthResponse
import com.example.skinappp.data.dto.GoogleAuthRequest
import com.example.skinappp.data.dto.UserLoginRequest
import com.example.skinappp.data.dto.UserRegisterRequest
import com.example.skinappp.data.local.UserPreferencesManager
import com.example.skinappp.data.remote.SkinApiService
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val api: SkinApiService,
    private val userPrefs: UserPreferencesManager
) {
    suspend fun addDoctor(doctorCreateRequest: DoctorCreateRequest){
        api.SendDoctors(doctorCreateRequest)
    }

    suspend fun register(
        email: String,
        password: String,
        fullName: String,
        phoneNumber: String?
    ) : AuthResponse{
        return api.register(UserRegisterRequest(email, password, fullName, phoneNumber))
    }

    suspend fun login(email: String, password: String): AuthResponse {
        return api.login(UserLoginRequest(email, password))
    }

    suspend fun googleSignIn(
        idToken: String
    ): AuthResponse {
        return api.googleAuth(GoogleAuthRequest(idToken))
    }

    suspend fun logout() = userPrefs.clearAll()
    fun isLoggedIn() = userPrefs.isLoggedIn
    fun getUserId() = userPrefs.userId
    fun getFullName() = userPrefs.displayName
}