package com.example.skinappp.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootCompletedReceiver : BroadcastReceiver() {

    @Inject
    lateinit var reminderManager: MedicationReminderManager

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == "android.intent.action.QUICKBOOT_POWERON") {
            Log.i("BootCompletedReceiver", "Device rebooted. Re-registering alarms.")
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    reminderManager.registerUpcomingAlarms()
                    Log.i("BootCompletedReceiver", "Successfully re-registered alarms.")
                } catch (e: Exception) {
                    Log.e("BootCompletedReceiver", "Error re-registering alarms on boot", e)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
