package com.example.skinappp.data.local.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "medications")
data class MedicationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val appointmentId: String,
    val name: String,
    val dosage: String,
    val frequencyPerDay: Int,
    val durationInDays: Int,
    val instructions: String,
    val startDateMillis: Long,
    val isActive: Boolean = true
)

@Entity(
    tableName = "medication_schedules",
    foreignKeys = [
        ForeignKey(
            entity = MedicationEntity::class,
            parentColumns = ["id"],
            childColumns = ["medicationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("medicationId")]
)
data class MedicationScheduleEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val medicationId: Int,
    val scheduledTimeMillis: Long,
    val isTaken: Boolean = false,
    val takenAtMillis: Long? = null
)

@Dao
interface MedicationDao {
    @Insert
    suspend fun insert(medication: MedicationEntity): Long

    @Query("SELECT * FROM medications WHERE isActive = 1")
    fun getActiveMedications(): Flow<List<MedicationEntity>>

    @Query("SELECT * FROM medications WHERE appointmentId = :appointmentId")
    suspend fun getMedicationsForAppointment(appointmentId: String): List<MedicationEntity>

    @Query("SELECT * FROM medications WHERE id = :id")
    suspend fun getMedicationById(id: Int): MedicationEntity?
}

@Dao
interface MedicationScheduleDao {
    @Insert
    suspend fun insertAll(schedules: List<MedicationScheduleEntity>)

    @Update
    suspend fun update(schedule: MedicationScheduleEntity)

    @Query("SELECT * FROM medication_schedules WHERE medicationId = :medicationId ORDER BY scheduledTimeMillis ASC")
    fun getSchedulesForMedication(medicationId: Int): Flow<List<MedicationScheduleEntity>>

    @Query("SELECT * FROM medication_schedules WHERE scheduledTimeMillis > :currentTimeMillis AND isTaken = 0 ORDER BY scheduledTimeMillis ASC LIMIT 10")
    suspend fun getUpcomingSchedules(currentTimeMillis: Long): List<MedicationScheduleEntity>

    @Query("SELECT * FROM medication_schedules WHERE isTaken = 0 AND scheduledTimeMillis >= :fromTimeMillis ORDER BY scheduledTimeMillis ASC")
    suspend fun getAllPendingSchedules(fromTimeMillis: Long): List<MedicationScheduleEntity>
    
    @Query("SELECT * FROM medication_schedules WHERE id = :id")
    suspend fun getScheduleById(id: Int): MedicationScheduleEntity?
}

@Database(entities = [MedicationEntity::class, MedicationScheduleEntity::class], version = 1, exportSchema = false)
abstract class MedicationDatabase : RoomDatabase() {
    abstract fun medicationDao(): MedicationDao
    abstract fun medicationScheduleDao(): MedicationScheduleDao
}
