package ir.mahdiparastesh.sexbook.base

import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity

abstract class BaseDialog<Parent> : DialogFragment() where Parent : FragmentActivity {
    @Suppress("UNCHECKED_CAST")
    val c: Parent by lazy { activity as Parent }

    interface SearchableStat {
        var lookingFor: String?

        fun lookForIt(text: String) =
            lookingFor?.let { it != "" && text.contains(it, true) } == true
    }
}
