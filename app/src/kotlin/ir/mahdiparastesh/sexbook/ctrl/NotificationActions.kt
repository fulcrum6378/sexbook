package ir.mahdiparastesh.sexbook.ctrl

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import ir.mahdiparastesh.sexbook.Sexbook
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.experimental.and

class NotificationActions : BroadcastReceiver() {

    companion object {
        const val ACTION_TURN_OFF_BIRTHDAY_NOTIFICATION =
            "ir.mahdiparastesh.sexbook.TURN_OFF_BIRTHDAY_NOTIFICATION"
        const val EXTRA_CRUSH_KEY = "crush_key"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val c = context.applicationContext as Sexbook

        when (intent.action) {
            ACTION_TURN_OFF_BIRTHDAY_NOTIFICATION -> {
                val crushKey = intent.getStringExtra(EXTRA_CRUSH_KEY)!!

                CoroutineScope(Dispatchers.IO).launch {
                    val rowsAffected = c.dao.cTurnOffBNtf(crushKey)
                    if (rowsAffected == 1) c.people[crushKey]?.apply {
                        status = status and (1 shl 4).inv().toShort()
                    }
                }
            }
        }
    }
}
