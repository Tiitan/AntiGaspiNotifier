package com.example.antigaspinotifier

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Load Worker UUID from "shared preference"
        val preferences = applicationContext.getSharedPreferences("AntiGaspiNotifier", MODE_PRIVATE)
        val workerIdStr = preferences.getString("WorkerId", "")
        var workerId: UUID? = if (!workerIdStr.isNullOrEmpty()) UUID.fromString(workerIdStr) else null

        val workManager = WorkManager.getInstance(applicationContext)
        if (workerId == null) // First application run, worker needs to be setup
        {
            val workRequest = PeriodicWorkRequestBuilder<ApiCallerWorker>(
                15, TimeUnit.MINUTES)
                .addTag("AntiGaspiWorker")
                .build()
            val id = workManager.enqueueUniquePeriodicWork("AntiGaspiWorker", ExistingPeriodicWorkPolicy.REPLACE, workRequest)
            workerId = workRequest.id

            // Save Worker UUID to "shared preference"
            val prefEditor = preferences.edit()
            prefEditor.putString("WorkerId", workerId.toString())
            prefEditor.apply()

            Log.i("AntiGaspiNotifier", "AntiGaspiNotifier first run: worker setup $workerId")
        }
        else
        {
            Log.i("AntiGaspiNotifier", "AntiGaspiNotifier new run: existing worker loaded $workerId")
        }


    }
}