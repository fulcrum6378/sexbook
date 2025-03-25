package ir.mahdiparastesh.sexbook.base

import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity

abstract class BaseDialog<Activity> : DialogFragment() where Activity : FragmentActivity {
    @Suppress("UNCHECKED_CAST")
    val c: Activity by lazy { activity as Activity }

    interface SearchableStat {
        var lookingFor: String?

        fun lookForIt(text: String) =
            lookingFor?.let { it != "" && text.contains(it, true) } == true
    }
}
