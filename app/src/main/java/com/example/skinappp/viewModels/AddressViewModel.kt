package com.example.skinappp.viewModels

import android.app.Activity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.skinappp.LocationTracker
import com.example.skinappp.Repository.RevAddRepo
import com.example.skinappp.data.AddressDao
import com.example.skinappp.data.Resource
import com.example.skinappp.model.AddressResponse
import com.example.skinappp.model.SavedAddress
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import androidx.activity.result.IntentSenderRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

@HiltViewModel
class AddressViewModel @Inject constructor(
    private val repo: RevAddRepo,
    private val locationTracker: LocationTracker,
    private  val saver : AddressDao
) : ViewModel() {
    private val data_ = MutableStateFlow<Resource<AddressResponse>>(Resource.Loading)
    val data: StateFlow<Resource<AddressResponse>> = data_

    private val sa_ = MutableStateFlow<Resource<List<SavedAddress>>>(Resource.Loading)
    val sa : StateFlow<Resource<List<SavedAddress>>> = sa_

    private val _gpsLaunchSignal = Channel<IntentSenderRequest>()
    val gpsLaunchSignal = _gpsLaunchSignal.receiveAsFlow()

// 2. When calling the tracker (likely inside a ViewModel or launched from UI)
// You pass the launcher call into the lambda
    init {
        getadr()
    }

    fun fetchLoc() {
        viewModelScope.launch(Dispatchers.Default) {
            data_.value = Resource.Loading
            val loc = locationTracker.getCurrentLocation { intentRequest ->
                viewModelScope.launch(Dispatchers.Default) {
                    _gpsLaunchSignal.send(intentRequest)
                }
            }
            if (loc != null) {
                val geta = async { repo.getaddress(loc.latitude, loc.longitude) }
                data_.value = geta.await()
          //      data_.value = repo.getaddress(loc.latitude, loc.longitude)
            } else {
                data_.value = Resource.Error(Exception("Location not found"))
            }
            if ( data_.value is Resource.Success ){
                saver.insert(
                    SavedAddress(
                        uuid = UUID.randomUUID(),
                        (data_.value as Resource.Success).data.query.lat,
                        (data_.value as Resource.Success).data.query.lon,
                        (data_.value as Resource.Success).data.features.get(0).properties.city
                    )
                )
            }
        }

    }

    fun deltAddr( adr : SavedAddress ){
        viewModelScope.launch { saver.delete(adr) }
    }
    fun  getadr(){
        viewModelScope.launch {
            sa_.value = Resource.Loading
            try {
                saver.getAddr().collect { listOfAddresses ->
                    if (listOfAddresses.isNotEmpty()) {
                        sa_.value = Resource.Success(listOfAddresses)
                    } else {
                        sa_.value = Resource.Success(emptyList())
                    }
                }
            } catch (e: Exception) {
                sa_.value = Resource.Error(e)
            }
        }
    }
    fun fetchAddress(lat: Double, lon: Double) {
        viewModelScope.launch {
            data_.value = Resource.Loading
            data_.value = repo.getaddress(lat, lon);
        }
    }
}