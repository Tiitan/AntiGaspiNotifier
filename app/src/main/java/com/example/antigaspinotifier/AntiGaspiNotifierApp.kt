package com.example.antigaspinotifier

import android.app.Application
import androidx.work.Configuration

class AntiGaspiNotifierApp () : Application(), Configuration.Provider {
    override fun getWorkManagerConfiguration() =
        Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()
}
