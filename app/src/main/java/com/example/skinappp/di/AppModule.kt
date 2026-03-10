package com.example.skinappp.di

import android.content.Context
import androidx.room.Room
import com.example.skinappp.ApiService.BackendApiService
import com.example.skinappp.ApiService.RevApiService
import com.example.skinappp.data.AddressDao
import com.example.skinappp.data.AddressDatabase
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class BackendRetrofit

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class RevRetrofit

    private const val REV_BASE_URL = "https://api.geoapify.com/"

    @Provides
    @Singleton
    @RevRetrofit
    fun provideRevRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(REV_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(@RevRetrofit retrofit: Retrofit): RevApiService {
        return retrofit.create(RevApiService::class.java)
    }

    private const val BackURL = "http://192.168.0.100:8080"

    @Provides
    @Singleton
    @BackendRetrofit
    fun provideBackendRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BackURL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideBackendService(@BackendRetrofit retrofit: Retrofit): BackendApiService {
        return retrofit.create(BackendApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideFusedLocationProviderClient(
        @ApplicationContext app: Context
    ): FusedLocationProviderClient {
        return LocationServices.getFusedLocationProviderClient(app)
    }

    @Singleton
    @Provides
    fun provideTrackerDao(trackerDatabase: AddressDatabase): AddressDao
            = trackerDatabase.addressDao()


    @Singleton
    @Provides
    fun providesAppDatabase(@ApplicationContext context: Context): AddressDatabase
            = Room.databaseBuilder(
        context,
        AddressDatabase::class.java,
        "savedAddress")
        .fallbackToDestructiveMigration()
        .build()
}


