package ir.mahdiparastesh.sexbook.base

import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import ir.mahdiparastesh.sexbook.util.NumberUtils

abstract class BaseDialog<Activity> : DialogFragment() where Activity : FragmentActivity {
    @Suppress("UNCHECKED_CAST")
    val c: Activity by lazy { activity as Activity }

    abstract class BaseDialogCompanion {
        private var lastCreation = 0L

        fun isDuplicate(): Boolean {
            if ((NumberUtils.now() - lastCreation <= 1000L)) return true
            lastCreation = NumberUtils.now()
            return false
        }
    }

    interface SearchableStat {
        var lookingFor: String?

        fun lookForIt(text: String) =
            lookingFor?.let { it != "" && text.contains(it, true) } == true
    }
}
