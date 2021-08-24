package com.example.netologydiploma.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.netologydiploma.R
import com.example.netologydiploma.auth.AppAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import javax.inject.Inject
import android.app.PendingIntent

import android.content.Intent
import com.example.netologydiploma.model.PushModel

import com.example.netologydiploma.ui.MainActivity
import com.google.gson.Gson
import com.google.gson.JsonObject
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONObject

@AndroidEntryPoint
class FCMService: FirebaseMessagingService() {
    private val content = "content"
    private val channelId = "remote"

    @Inject
    lateinit var auth : AppAuth



    override fun onMessageReceived(message: RemoteMessage) {
        val notificationObject  = Gson().fromJson(message.data[content], PushModel::class.java)
        val notificationContent = notificationObject.content

          showNotification(notificationContent)
    }

    /**
     * Called if the FCM registration token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the
     * FCM registration token is initially generated so this is where you would retrieve the token.
     */
    override fun onNewToken(token: String) {
        auth.sendPushTokenToServer(token)

    }


    private fun showNotification(content: String?) {

        val intent = Intent(this, MainActivity::class.java)
        // Here FLAG_ACTIVITY_CLEAR_TOP flag is set to clear
        // the activities present in the activity stack,
        // on the top of the Activity that is to be launched
        // Here FLAG_ACTIVITY_CLEAR_TOP flag is set to clear
        // the activities present in the activity stack,
        // on the top of the Activity that is to be launched
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        // Pass the intent to PendingIntent to start the
        // next Activity
        // Pass the intent to PendingIntent to start the
        // next Activity
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT
        )

       val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setAutoCancel(true)
            .setOnlyAlertOnce(true)
            .setContentTitle("New notification")
            .setContentText(content)
           .setContentIntent(pendingIntent)
           .build()

        val name = "Server notifications"
        val descriptionText = "Notifications from remote server"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(channelId, name, importance).apply {
            description = descriptionText
        }
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
        manager.notify(0, notification)
    }
}