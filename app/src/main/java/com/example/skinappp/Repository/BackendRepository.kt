package com.example.skinappp.Repository

import android.R.attr.bitmap
import android.graphics.Bitmap
import android.util.Log
import retrofit2.HttpException
import com.example.skinappp.ApiService.BackendApiService
import com.example.skinappp.data.Resource
import com.example.skinappp.model.BackendResponse
import com.example.skinappp.model.Product
import okhttp3.RequestBody
import java.io.ByteArrayOutputStream
import javax.inject.Inject

class BackendRepository @Inject constructor(private val api: BackendApiService) {
    suspend fun getBackendMessage() : Resource<BackendResponse>{
        return try {
            val response = api.getBackend()
            Log.d( "BackendSuccess" , "Success: ${response.data}")
            Resource.Success(response)
        }catch (e : Exception){
            Log.d( "BackendFail", "Error: ${e.message}")
            Resource.Error(exception = e)
        }
    }
    suspend fun sendForAuth(image : RequestBody) : Resource<BackendResponse> {
        return try {
            val response = api.sendForAuth(image)
            Resource.Success(response)
        }catch (e : Exception){
            Log.d( "BackendFail", "Error: ${e.message}")
            Resource.Error(exception = e)
        }
    }
    suspend fun sendProduct(product : Product) : Resource<Product> {
        return try {
            val response = api.sendproduct(product)
            Resource.Success(response)
        }catch (e : HttpException){
            val mess = e.response()?.errorBody()?.string()
            Log.d( "BackendFail", "Error: ${mess}")
            Resource.Error(exception = Exception(mess))
        }catch (e : Exception){
            Log.d( "BackendFail", "Error: ${e.message}")
            Resource.Error(exception = e)
        }
    }
}