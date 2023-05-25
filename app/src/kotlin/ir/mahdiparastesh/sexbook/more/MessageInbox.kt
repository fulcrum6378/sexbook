package ir.mahdiparastesh.sexbook.more

import android.os.Handler
import androidx.lifecycle.MutableLiveData

/**
 * Helper class for controlling messages that are sent into a Handler.
 * It extends LinkedHashSet because a Set doesn't allow duplicates.
 */
class MessageInbox(val handler: MutableLiveData<Handler?>) : LinkedHashSet<Int>() {

    override fun add(element: Int): Boolean {//
        return if (handler.value != null) {
            handler.value?.obtainMessage(element)?.sendToTarget()
            false
        } else super.add(element)
    }

    override fun clear() {
        if (handler.value != null)
            forEach { handler.value?.obtainMessage(it)?.sendToTarget() }
        super.clear()
    }
}
