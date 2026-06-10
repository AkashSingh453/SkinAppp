package com.example.skinappp.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.skinappp.data.dto.MedicationPlanDto
import com.example.skinappp.data.local.db.MedicationDao
import com.example.skinappp.data.local.db.MedicationEntity
import com.example.skinappp.data.local.db.MedicationScheduleDao
import com.example.skinappp.data.local.db.MedicationScheduleEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MedicationReminderManager @Inject constructor(
    private val context: Context,
    private val medicationDao: MedicationDao,
    private val scheduleDao: MedicationScheduleDao
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    suspend fun scheduleMedications(appointmentId: String, plans: List<MedicationPlanDto>) {
        withContext(Dispatchers.IO) {
            val formatter = DateTimeFormatter.ofPattern("HH:mm")
            
            plans.forEach { plan ->
                // 1. Insert Medication
                val medEntity = MedicationEntity(
                    appointmentId = appointmentId,
                    name = plan.medicationName,
                    dosage = plan.dosage,
                    frequencyPerDay = plan.frequencyPerDay,
                    durationInDays = plan.durationInDays,
                    instructions = plan.instructions,
                    startDateMillis = System.currentTimeMillis()
                )
                val medicationId = medicationDao.insert(medEntity).toInt()

                // 2. Generate schedules
                val schedules = mutableListOf<MedicationScheduleEntity>()
                val calendar = Calendar.getInstance()

                for (dayOffset in 0 until plan.durationInDays) {
                    plan.specificTimes.forEach { timeStr ->
                        try {
                            val localTime = LocalTime.parse(timeStr, formatter)
                            val schedCalendar = calendar.clone() as Calendar
                            schedCalendar.add(Calendar.DAY_OF_YEAR, dayOffset)
                            schedCalendar.set(Calendar.HOUR_OF_DAY, localTime.hour)
                            schedCalendar.set(Calendar.MINUTE, localTime.minute)
                            schedCalendar.set(Calendar.SECOND, 0)
                            schedCalendar.set(Calendar.MILLISECOND, 0)

                            // Only schedule for future times
                            if (schedCalendar.timeInMillis > System.currentTimeMillis()) {
                                schedules.add(
                                    MedicationScheduleEntity(
                                        medicationId = medicationId,
                                        scheduledTimeMillis = schedCalendar.timeInMillis
                                    )
                                )
                            }
                        } catch (e: Exception) {
                            Log.e("MedicationReminder", "Failed to parse time $timeStr", e)
                        }
                    }
                }
                
                // 3. Insert schedules
                scheduleDao.insertAll(schedules)

                Log.i("MedicationReminder", "Generated ${schedules.size} schedules for ${plan.medicationName}")
            }
            
            // Re-register all upcoming
            registerUpcomingAlarms()
        }
    }

    suspend fun registerUpcomingAlarms() {
        withContext(Dispatchers.IO) {
            // Get up to 10 upcoming pending schedules
            val upcomingSchedules = scheduleDao.getUpcomingSchedules(System.currentTimeMillis())
            
            upcomingSchedules.forEach { schedule ->
                val med = medicationDao.getMedicationById(schedule.medicationId)
                if (med != null && med.isActive) {
                    setAlarm(med, schedule)
                }
            }
        }
    }

    private fun setAlarm(medication: MedicationEntity, schedule: MedicationScheduleEntity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            Log.w("MedicationReminder", "Cannot schedule exact alarms. Permission missing.")
            val settingsIntent = Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(settingsIntent)
            return
        }

        val intent = Intent(context, MedicationAlarmReceiver::class.java).apply {
            putExtra("MEDICATION_ID", medication.id)
            putExtra("SCHEDULE_ID", schedule.id)
            putExtra("MEDICATION_NAME", medication.name)
            putExtra("DOSAGE", medication.dosage)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            schedule.id, // unique ID per schedule
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                schedule.scheduledTimeMillis,
                pendingIntent
            )
            Log.i("MedicationReminder", "Set alarm for ${medication.name} at ${schedule.scheduledTimeMillis}")
        } catch (e: SecurityException) {
            Log.e("MedicationReminder", "Security exception setting alarm", e)
        }
    }
}
