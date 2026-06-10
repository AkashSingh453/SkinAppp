package com.example.skinappp.data.remote

import com.example.skinappp.data.local.UserPreferencesManager
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val userPrefs: UserPreferencesManager
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = userPrefs.jwtToken
        val request = if (token != null) {
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            chain.request()
        }
        return chain.proceed(request)
    }
}
