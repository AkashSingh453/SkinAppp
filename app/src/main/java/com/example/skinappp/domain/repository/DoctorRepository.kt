package com.example.skinappp.domain.repository

import com.example.skinappp.data.dto.NearbyDoctorResponse
import com.example.skinappp.data.remote.SkinApiService
import com.example.skinappp.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DoctorRepository @Inject constructor(
    private val api: SkinApiService
)  {

    private var cachedDoctors: List<NearbyDoctorResponse> = emptyList()
    suspend fun getNearbyDoctors(lat: Double, lon: Double, radius: Double): List<NearbyDoctorResponse> {
        val response = api.getNearbyDoctors(lat, lon, radius)
        // 3. Save the result to our memory cache before returning it!
        cachedDoctors = response
        return response
    }
    fun getCachedDoctorById(doctorId: Int): NearbyDoctorResponse? {
        if( cachedDoctors.size <= doctorId ) return null
        return cachedDoctors[doctorId]
    }
}

