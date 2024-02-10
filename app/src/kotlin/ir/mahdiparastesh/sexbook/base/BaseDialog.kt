package ir.mahdiparastesh.sexbook.base

import androidx.fragment.app.DialogFragment

abstract class BaseDialog : DialogFragment() {
    val c: BaseActivity by lazy { activity as BaseActivity }

    interface SearchableStat {
        var lookingFor: String?

        fun lookForIt(text: String) =
            lookingFor?.let { it != "" && text.contains(it, true) } ?: false
    }
}
