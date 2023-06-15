package com.quanticheart.permission.notification

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import java.util.*

//
// Created by Jonn Alves on 15/06/23.
//
fun AppCompatActivity.goSettings() {
    val myAppSettings = Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.parse("package:$packageName")
    )
    myAppSettings.addCategory(Intent.CATEGORY_DEFAULT)
    myAppSettings.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    startActivity(myAppSettings)
}

fun AppCompatActivity.verifyNotificationIsEnabled(): Boolean {
    val notificationManager: NotificationManager by lazy {
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }
    return notificationManager.areNotificationsEnabled()
}

/**
 * Shows a notification to user.
 *
 * The notification won't appear if the user doesn't grant notification permission first.
 */
fun AppCompatActivity.showDummyNotification() {
    val builder = NotificationCompat.Builder(this, CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setContentTitle("Congratulations! 🎉🎉🎉")
        .setContentText("You have post a notification to Android 13!!!")
        .setPriority(NotificationCompat.PRIORITY_HIGH)

    with(NotificationManagerCompat.from(this)) {
        notify(Random().nextInt(), builder.build())
    }
}

const val CHANNEL_ID = "dummy_channel"

/**
 * Creates Notification Channel (required for API level >= 26) before sending any notification.
 */
fun Application.createNotificationChannels() {
    // If the Android Version is greater than Oreo,
    // then create the NotificationChannel
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val notificationManager: NotificationManager =
            getSystemService(Application.NOTIFICATION_SERVICE) as NotificationManager

        // Default Channel
        val name = "Test"
        val descriptionText = "Default notification"
        val channel =
            NotificationChannel(
                CHANNEL_ID,
                name,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = descriptionText
            }
        notificationManager.createNotificationChannel(channel)

        // Default Channel
        val name2 = "Test 2"
        val descriptionText2 = "Default notification 2"
        val channel2 =
            NotificationChannel(
                "${name2}_id",
                name2,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = descriptionText2
            }
        notificationManager.createNotificationChannel(channel2)

    }
}

interface NotificationPermissionCallback {
    fun accept()
    fun rationale()
    fun denied()
}

private var requestPermissionLauncher: ActivityResultLauncher<String>? = null

/**
 * Verify notification permission
 * IMPORTANT -> Always call in onStart
 * @param callback
 */
fun AppCompatActivity.verifyNotificationPermission(callback: NotificationPermissionCallback) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        when {
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) ==
                    PackageManager.PERMISSION_GRANTED ->
                callback.accept()
            shouldShowRequestPermissionRationale(android.Manifest.permission.POST_NOTIFICATIONS) ->
                callback.rationale()
            else -> {
                requestPermissionLauncher?.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        initRequestPermissionLauncher {
            if (it) {
                callback.accept()
            } else {
                callback.denied()
            }
        }
    } else callback.accept()
}

fun AppCompatActivity.initRequestPermissionLauncher(callback: ((Boolean) -> Unit)? = null) {
    if (requestPermissionLauncher == null)
        requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) {
                callback?.let { it1 -> it1(it) }
            }
}

fun AppCompatActivity.verifySendNotification(callback: () -> Unit) {
    if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS,
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                callback()
            } else {
                requestPermissionLauncher?.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        } else callback()
        // connect if not connected
    }
}