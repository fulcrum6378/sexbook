package ir.mahdiparastesh.sexbook.base

import androidx.fragment.app.Fragment
import ir.mahdiparastesh.sexbook.page.Main

abstract class BasePage : Fragment() {
    val c: Main by lazy { activity as Main } // don't define it as a getter.
    var listEverPrepared = false

    open fun prepareList() {
        listEverPrepared = true
    }
}
