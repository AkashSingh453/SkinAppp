package com.example.skinappp.di

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.skinappp.DigitClassifier
import com.example.skinappp.data.local.UserPreferencesManager
import com.example.skinappp.data.local.db.MedicationDao
import com.example.skinappp.data.local.db.MedicationDatabase
import com.example.skinappp.data.local.db.MedicationScheduleDao
import com.example.skinappp.data.remote.AuthInterceptor
import com.example.skinappp.data.remote.SkinApiService
import com.example.skinappp.reminder.MedicationReminderManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // ─── Replace with your deployed backend URL ───────────────────────────────
    private const val baseUrl = "http://192.168.31.81:8080/"
    // ─────────────────────────────────────────────────────────────────────────

    // Web Client ID from Google Cloud Console → APIs & Services → Credentials
    // Format: XXXXXXXXX-xxxx.apps.googleusercontent.com
    const val WEB_CLIENT_ID = "181294455162-kotdbiutnssrnqvuogbsej4g2hdptr83.apps.googleusercontent.com"

    @Provides
    @Singleton
    fun provideEncryptedSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        return EncryptedSharedPreferences.create(
            context,
            "skin_app_secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, json: Json): Retrofit =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

    @Provides
    @Singleton
    fun provideSkinApiService(retrofit: Retrofit): SkinApiService =
        retrofit.create(SkinApiService::class.java)

    @Provides
    @Singleton
    fun provideDigitClassifier(@ApplicationContext context: Context): DigitClassifier =
        DigitClassifier(context)

    @Provides
    @Singleton
    fun provideFusedLocationClient(@ApplicationContext context: Context): FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    @Provides
    @Singleton
    fun provideMedicationDatabase(@ApplicationContext context: Context): MedicationDatabase {
        return androidx.room.Room.databaseBuilder(
            context,
            MedicationDatabase::class.java,
            "medication_db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideMedicationDao(db: MedicationDatabase): MedicationDao = db.medicationDao()

    @Provides
    fun provideMedicationScheduleDao(db: MedicationDatabase): MedicationScheduleDao = db.medicationScheduleDao()

    @Provides
    @Singleton
    fun provideMedicationReminderManager(
        @ApplicationContext context: Context,
        medicationDao: MedicationDao,
        scheduleDao: MedicationScheduleDao
    ): MedicationReminderManager {
        return MedicationReminderManager(context, medicationDao, scheduleDao)
    }
}
