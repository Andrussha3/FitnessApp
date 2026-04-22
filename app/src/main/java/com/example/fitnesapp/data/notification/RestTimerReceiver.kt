package com.example.fitnesapp.data.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.fitnesapp.MainActivity
import com.example.fitnesapp.R

class RestTimerReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "rest_timer"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(channelId, "Таймер отдыха", NotificationManager.IMPORTANCE_DEFAULT)
            )
        }
        val openIntent = PendingIntent.getActivity(
            context,
            44,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        manager.notify(
            44,
            NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Отдых завершен")
                .setContentText("Можно переходить к следующему подходу")
                .setContentIntent(openIntent)
                .setAutoCancel(true)
                .build()
        )
    }
}
