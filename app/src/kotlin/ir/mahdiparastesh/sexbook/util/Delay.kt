package ir.mahdiparastesh.sexbook.util

import android.os.Handler
import android.os.Looper

/** Executes a [Runnable] after a specified amount of delay. */
open class Delay(timeout: Long = 5000L, listener: Runnable) :
    Handler(Looper.myLooper()!!) {
    init {
        postDelayed(listener, timeout)
    }
}