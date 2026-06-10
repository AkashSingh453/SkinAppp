package com.example.skinappp.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserRegisterRequest(
    val email: String,
    val password: String,
    @SerialName("full_name") val fullName: String,
    @SerialName("phone_number") val phoneNumber: String? = null
)

@Serializable
data class UserLoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class GoogleAuthRequest(
    @SerialName("id_token") val idToken: String
)

@Serializable
data class AuthResponse(
    val token: String,
    @SerialName("user_id") val userId: String,
    val email: String
)

@Serializable
data class UserProfileResponse(
    @SerialName("user_id") val userId: String,
    val email: String,
    @SerialName("auth_provider") val authProvider: String,
    @SerialName("account_status") val accountStatus: String,
    val profile: UserProfile,
    @SerialName("medical_context") val medicalContext: MedicalContext? = null,
    val settings: UserSettings = UserSettings()
)

@Serializable
data class UserProfile(
    @SerialName("full_name") val fullName: String,
    @SerialName("phone_number") val phoneNumber: String? = null
)

@Serializable
data class MedicalContext(
    @SerialName("skin_type") val skinType: String? = null
)

@Serializable
data class UserSettings(
    @SerialName("push_notifications_enabled") val pushNotificationsEnabled: Boolean = true
)
