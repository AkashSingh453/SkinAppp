package com.example.skinappp.Repository

import android.util.Log
import com.example.skinappp.ApiService.RevApiService
import com.example.skinappp.LocationTracker
import com.example.skinappp.data.AddressDao
import com.example.skinappp.data.Resource
import com.example.skinappp.model.AddressResponse
import com.example.skinappp.model.SavedAddress
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject

class RevAddRepo
      @Inject constructor(
          private  val api : RevApiService,
          private val addressDao: AddressDao
      )
{
    suspend fun getaddress(lat: Double, lon: Double) : Resource<AddressResponse>{
         return try {
             val response = api.getReverseGeocode(lat , lon)
             Log.d( "RevAddRepo" , response.features.toString() )
             Resource.Success(response)
         }catch (e : Exception){
             Resource.Error(exception = e)
         }
    }
    suspend fun getSavedAddresses(): Flow<List<SavedAddress>> {
        return addressDao.getAddr()
    }
}