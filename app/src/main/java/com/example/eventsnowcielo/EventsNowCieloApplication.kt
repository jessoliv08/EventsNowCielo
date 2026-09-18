package com.example.eventsnowcielo

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import com.example.eventsnowcielo.core.di.AppModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.ksp.generated.module

class EventsNowCieloApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@EventsNowCieloApplication)
            modules(AppModule().module)
        }
    }

    override fun registerReceiver(receiver: BroadcastReceiver?, filter: IntentFilter?): Intent? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            super.registerReceiver(receiver, filter, RECEIVER_EXPORTED)
        } else {
            super.registerReceiver(receiver, filter)
        }
    }

    override fun registerReceiver(
        receiver: BroadcastReceiver?,
        filter: IntentFilter?,
        flags: Int
    ): Intent? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val updatedFlags = if ((flags and (RECEIVER_EXPORTED or RECEIVER_NOT_EXPORTED)) == 0) {
                flags or RECEIVER_EXPORTED
            } else {
                flags
            }
            super.registerReceiver(receiver, filter, updatedFlags)
        } else {
            super.registerReceiver(receiver, filter, flags)
        }
    }

    override fun registerReceiver(
        receiver: BroadcastReceiver?,
        filter: IntentFilter?,
        broadcastPermission: String?,
        scheduler: android.os.Handler?
    ): Intent? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            super.registerReceiver(receiver, filter, broadcastPermission, scheduler, RECEIVER_EXPORTED)
        } else {
            super.registerReceiver(receiver, filter, broadcastPermission, scheduler)
        }
    }

    override fun registerReceiver(
        receiver: BroadcastReceiver?,
        filter: IntentFilter?,
        broadcastPermission: String?,
        scheduler: android.os.Handler?,
        flags: Int
    ): Intent? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val updatedFlags = if ((flags and (RECEIVER_EXPORTED or RECEIVER_NOT_EXPORTED)) == 0) {
                flags or RECEIVER_EXPORTED
            } else {
                flags
            }
            super.registerReceiver(receiver, filter, broadcastPermission, scheduler, updatedFlags)
        } else {
            super.registerReceiver(receiver, filter, broadcastPermission, scheduler, flags)
        }
    }
}
