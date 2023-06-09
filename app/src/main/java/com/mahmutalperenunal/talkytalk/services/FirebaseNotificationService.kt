package com.mahmutalperenunal.talkytalk.services

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.res.ResourcesCompat
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.mahmutalperenunal.talkytalk.R
import com.mahmutalperenunal.talkytalk.activity.Message
import com.mahmutalperenunal.talkytalk.constants.AppConstants
import com.mahmutalperenunal.talkytalk.util.AppUtil
import java.util.*


class FirebaseNotificationService : FirebaseMessagingService() {

    private val appUtil = AppUtil()

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        updateToken(token)
    }


    @SuppressLint("ObsoleteSdkInt")
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        if (remoteMessage.data.isNotEmpty()) {

            val map: Map<String, String> = remoteMessage.data

            val title = map["title"]
            val message = map["message"]
            val hisId = map["hisId"]
            val hisImage = map["hisImage"]
            val chatId = map["chatId"]

            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O)
                createOreoNotification(title!!, message!!, hisId!!, hisImage!!, chatId!!)
            else createNormalNotification(title!!, message!!, hisId!!, hisImage!!, chatId!!)

        }


    }


    private fun updateToken(token: String) {

        val databaseReference =
            FirebaseDatabase.getInstance().getReference("Users").child(appUtil.getUID()!!)
        val map: MutableMap<String, Any> = HashMap()
        map["token"] = token
        databaseReference.updateChildren(map)

    }

    private fun createNormalNotification(
        title: String,
        message: String,
        hisId: String,
        hisImage: String,
        chatId: String
    ) {

        val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val builder = NotificationCompat.Builder(this, AppConstants.CHANNEL_ID)
        builder.setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setAutoCancel(true)
            .setColor(ResourcesCompat.getColor(resources, R.color.purple_700, null))
            .setSound(uri)

        val intent = Intent(this, Message::class.java)

        intent.putExtra("hisId", hisId)
        intent.putExtra("hisImage", hisImage)
        intent.putExtra("chatId", chatId)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )
        builder.setContentIntent(pendingIntent)
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(Random().nextInt(85 - 65), builder.build())

    }

    @SuppressLint("ObsoleteSdkInt")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createOreoNotification(
        title: String,
        message: String,
        hisId: String,
        hisImage: String,
        chatId: String
    ) {

        val channel = NotificationChannel(
            AppConstants.CHANNEL_ID,
            "Message",
            NotificationManager.IMPORTANCE_HIGH
        )

        channel.setShowBadge(true)
        channel.enableLights(true)
        channel.enableVibration(true)
        channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE

        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)

        val intent = Intent(this, Message::class.java)

        intent.putExtra("hisId", hisId)
        intent.putExtra("hisImage", hisImage)
        intent.putExtra("chatId", chatId)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = Notification.Builder(this, AppConstants.CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setAutoCancel(true)
            .setColor(ResourcesCompat.getColor(resources, R.color.purple_700, null))
            .setContentIntent(pendingIntent)
            .build()

        manager.notify(100, notification)
    }


}