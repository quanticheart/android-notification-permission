package com.quanticheart.permission.notification

import android.app.Application

//
// Created by Jonn Alves on 15/06/23.
//
class AppBase : Application() {
    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }
}