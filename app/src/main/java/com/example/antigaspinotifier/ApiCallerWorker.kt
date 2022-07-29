package com.example.antigaspinotifier

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.work.Worker
import androidx.work.WorkerParameters
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import kotlin.random.Random

data class Item(
    val name: String,
    val availableQuantity: Int
)

class ApiCallerWorker(appContext: Context, workerParams: WorkerParameters) : Worker(appContext, workerParams) {
    private val context = appContext
    private val param = workerParams

    private fun fetchAPI(): JSONObject? {
        val request = Request.Builder().url("https://www.mon-marche.fr/api/search?text=anti-gaspi").build()
        val client = OkHttpClient()
        var jsonOutput : JSONObject? = null
        client.newCall(request).execute().use { response ->
            if (response.isSuccessful)
                jsonOutput = JSONObject(response.body?.string() ?: "")
            else
                Log.e("AntiGaspiNotifier", "bad response ${response.code}")
            response.close()
        }
        return jsonOutput
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel("AntiGaspiNotif", "AntiGaspiNotif", NotificationManager.IMPORTANCE_DEFAULT).apply {
            description = "AntiGaspiNotifier notification channel"
        }
        // Register the channel with the system
        val notificationManager: NotificationManager = getSystemService(context, NotificationManager::class.java) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun sendNotification(gaspiList: List<Item>) {
        createNotificationChannel()

        val gaspiStringBuilder = StringBuilder()
        gaspiList.forEach{
            gaspiStringBuilder.append("${it.name}: ${it.availableQuantity}\n")
        }

        val builder = NotificationCompat.Builder(context, "AntiGaspiNotif")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("AntiGaspi Dispo!")
            .setContentText(gaspiStringBuilder.toString())
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        val rand = Random.nextInt(0, 9999)
        with(NotificationManagerCompat.from(context)) {
            notify(rand, builder.build())
        }
    }

    override fun doWork(): Result {
        val jsonResult = fetchAPI() ?: return Result.failure()
        val gaspiList = mutableListOf<Item>()
        val items = jsonResult.getJSONObject("articles").getJSONArray("items")
        for (i in 0 until items.length()) {
            val item = items.getJSONObject(i)
            val availableQuantity = item.optInt("availableQuantity")
            if (availableQuantity > 0)
                gaspiList.add(Item(item.optString("name", "name undefined"), availableQuantity))
        }

        if (gaspiList.isNotEmpty())
            sendNotification(gaspiList)
        return Result.success()
    }
}
