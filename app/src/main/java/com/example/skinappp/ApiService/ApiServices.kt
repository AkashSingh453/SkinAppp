package com.example.skinappp.ApiService

import com.example.skinappp.model.AddressResponse
import com.example.skinappp.model.BackendResponse
import com.example.skinappp.model.Product
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface RevApiService {
    @GET("v1/geocode/reverse")
    suspend fun getReverseGeocode(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("apiKey") apiKey: String =  "3246256222d44cf0a5ee12712d0ce69c"
    ): AddressResponse
}

interface BackendApiService {
    @GET("/abc")
    suspend fun getBackend(): BackendResponse

    @POST("/Upload")
    suspend fun sendForAuth(
        @Body image : RequestBody
    ) : BackendResponse

    @POST("/product")
    suspend fun sendproduct(
        @Body product: Product
    ) : Product

    @Multipart
    @POST("multiform")
    suspend fun uploadData(
        // For simple string fields
        @Part("name") name: RequestBody,
        // For the actual file
        @Part file: List< MultipartBody.Part >
    ): String
}