package com.motionapps.sensortemplate.activities.components

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.VIBRATOR_SERVICE
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.annotation.NonNull
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.motionapps.sensortemplate.activities.Main

import com.motionapps.sensortemplate.R
import com.motionapps.sensortemplate.service.DetectionService


class Notify {

    companion object Inform{
        /**
         * @param context
         * @param title - string for header of notification
         * @param text - string for body of the notification
         * @return Notification - object, which can be posted to foreground service / notification manager
         *
         * notification also contains button to stop service
         * method can be modified for any kind of notification
         */
        fun createForegroundNotification(@NonNull context: Context, title: String, text: String = ""): Notification{
            createChannel(
                context
            )

            val cancel = Intent(DetectionService.STOP_SERVICE)
            val cancelService = PendingIntent.getBroadcast(
                context, 4,
                cancel, PendingIntent.FLAG_CANCEL_CURRENT
            )

            val notifyIntent = Intent(context, Main::class.java)
            notifyIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

            val notifyPendingIntent = PendingIntent.getActivity(
                context, 0,
                notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT
            )

            val icon: Int = R.drawable.ic_android_black

            val mBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(icon)
                .setContentTitle(title)
                .setContentText(text)
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText(text)
                )
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(notifyPendingIntent)
                .addAction(R.drawable.ic_cancel, context.getString(R.string.notification_stop_service),
                    cancelService)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mBuilder.color = ContextCompat.getColor(context, R.color.white)
            }

            return mBuilder.build()
        }

        /**
         * @param context
         * @param notification - created notification - for example from method above
         * @param id - integer of existing notification
         */
        fun updateNotification(context: Context, notification: Notification, id: Int){
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(id, notification)
        }
        /**
         * @param context
         * @param id - integer of existing notification
         */
        fun cancelNotification(context: Context, id: Int) {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.cancel(id)
        }

        private var channelNotification: NotificationChannel ?= null

        private var CHANNEL_ID: String = "9874"
        private var CHANNEL_NAME: String = "DetectionNotifications"

        /**
         * @param context
         * creates channel for notifications and their updates - available only since Oreo
         */
        private fun createChannel(@NonNull context: Context){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && channelNotification == null){
                val importance: Int = NotificationManager.IMPORTANCE_HIGH
                val description = "The channel to inform about state of the detection"

                channelNotification = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME, importance)
                channelNotification!!.description = description

                val manager: NotificationManagerCompat ?=
                    context.getSystemService(NotificationManagerCompat::class.java)

                manager?.createNotificationChannel(channelNotification!!)

            }
        }

        /**
         * @param context
         * one shot vibration of phone
         */

        fun vibrate(context: Context) {
            val vibrator = context.getSystemService(VIBRATOR_SERVICE) as Vibrator?
            if (Build.VERSION.SDK_INT >= 26) {
                vibrator?.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
            }else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(200)
            }
        }
    }

}