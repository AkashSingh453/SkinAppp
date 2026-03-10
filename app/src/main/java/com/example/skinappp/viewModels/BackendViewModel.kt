package com.example.skinappp.viewModels

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.skinappp.R
import com.example.skinappp.Repository.BackendRepository
import com.example.skinappp.data.Resource
import com.example.skinappp.model.AddressResponse
import com.example.skinappp.model.BackendResponse
import com.example.skinappp.model.Product
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.RequestBody
import java.io.ByteArrayOutputStream
import javax.inject.Inject

@HiltViewModel
class BackendViewModel @Inject constructor(
    private val repository: BackendRepository
) : ViewModel() {
    private val data_ = MutableStateFlow<Resource<Product>>(Resource.Loading)
    val data: StateFlow<Resource<Product>> = data_
    init {
        sendProduct(Product("kkk","dddd",0))
    }
    fun getBackendMessage() {
        viewModelScope.launch {
            data_.value = Resource.Loading
     //       data_.value = repository.getBackendMessage()
        }
    }
    fun sendAuth(name: RequestBody) {
        viewModelScope.launch {
            data_.value = Resource.Loading
      //      data_.value = repository.sendForAuth(name)
        }
    }
    fun sendProduct(product : Product) {
        viewModelScope.launch {
            data_.value = Resource.Loading
            data_.value = repository.sendProduct(product)
        }
    }
}