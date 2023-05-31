package ir.mahdiparastesh.sexbook.more

import androidx.fragment.app.Fragment
import ir.mahdiparastesh.sexbook.Main

abstract class BasePage : Fragment() {
    val c: Main by lazy { activity as Main } // don't define it as a getter.

    abstract fun prepareList()
}
