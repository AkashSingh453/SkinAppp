package com.example.skinappp.reminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.skinappp.MainActivity
import com.example.skinappp.R

class MedicationAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val medicationId = intent.getIntExtra("MEDICATION_ID", -1)
        val scheduleId = intent.getIntExtra("SCHEDULE_ID", -1)
        val medicationName = intent.getStringExtra("MEDICATION_NAME") ?: "Medicine"
        val dosage = intent.getStringExtra("DOSAGE") ?: ""

        Log.i("MedicationAlarm", "Alarm triggered for $medicationName (ID: $medicationId, Schedule: $scheduleId)")

        showNotification(context, medicationName, dosage, scheduleId)
    }

    private fun showNotification(context: Context, name: String, dosage: String, scheduleId: Int) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "medication_reminders"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Medication Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders to take your medication"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            scheduleId,
            openAppIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        // TODO: Implement "Mark as Taken" and "Snooze" actions in a separate service or receiver

        
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Replace with your app's icon
            .setContentTitle("Time for your medication")
            .setContentText("Please take $name ($dosage)")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(scheduleId, notification)
    }
}
