package ir.mahdiparastesh.sexbook.more

import android.os.Handler
import android.os.Looper

/** Executes codes with a specified amount of delay. */
open class Delay(timeout: Long = 5000L, listener: Runnable) :
    Handler(Looper.myLooper()!!) {
    init {
        postDelayed(listener, timeout)
    }
}
