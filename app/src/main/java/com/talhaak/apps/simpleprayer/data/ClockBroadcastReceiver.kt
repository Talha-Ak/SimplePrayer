package com.talhaak.apps.simpleprayer.data

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class ClockBroadcastReceiver(
    val context: Context
) {
    val minuteTickFlow: Flow<Unit> = callbackFlow {
        trySendBlocking(Unit)

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == Intent.ACTION_TIME_TICK) {
                    trySendBlocking(Unit)
                    trySend(Unit)
                }
            }
        }

        val intent = IntentFilter(Intent.ACTION_TIME_TICK)
        ContextCompat.registerReceiver(
            context,
            receiver,
            intent,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )

        awaitClose {
            context.unregisterReceiver(receiver)
        }
    }
}
