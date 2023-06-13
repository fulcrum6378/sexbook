package ir.mahdiparastesh.sexbook.more

import android.os.Handler
import android.os.Looper
import ir.mahdiparastesh.sexbook.data.Work

/** Executes codes with a specified amount of delay. */
open class Delay(timeout: Long = Work.TIMEOUT, listener: Runnable) :
    Handler(Looper.myLooper()!!) {
    init {
        postDelayed(listener, timeout)
    }
}
