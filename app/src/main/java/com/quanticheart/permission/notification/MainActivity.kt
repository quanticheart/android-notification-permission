package com.quanticheart.permission.notification

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<Button>(R.id.button_show_notification).setOnClickListener {
            verifySendNotification {
                showDummyNotification()
            }
        }
        // Refresh UI.
        refreshUI()
    }

    /**
     * Refresh UI elements.
     */
    private fun refreshUI() {
        findViewById<TextView>(R.id.text_notification_enabled).text =
            if (verifyNotificationIsEnabled()) "TRUE" else "FALSE"
    }

    override fun onStart() {
        super.onStart()
        verifyNotificationPermission(object : NotificationPermissionCallback {
            override fun accept() {
                Log.e(ContentValues.TAG, "User accepted the notifications!")
                showDummyNotification()
                refreshUI()
            }

            override fun rationale() {
                Snackbar.make(
                    findViewById(R.id.parent_layout),
                    "The user denied the notifications ):",
                    Snackbar.LENGTH_LONG
                )
                    .setAction("Settings") {
                        goSettings()
                    }
                    .show()
            }

            override fun denied() {
                Snackbar.make(
                    findViewById<View>(android.R.id.content).rootView,
                    "Please grant Notification permission from App Settings",
                    Snackbar.LENGTH_LONG
                ).show()
            }
        })
    }
}