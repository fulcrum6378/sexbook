package ir.mahdiparastesh.sexbook.misc

import android.view.ContextThemeWrapper
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import ir.mahdiparastesh.sexbook.R
import ir.mahdiparastesh.sexbook.base.BaseActivity

/** Represents a PopupMenu action. */
typealias Act = HashMap<Int, (item: MenuItem) -> Unit>

/** Helper class for making PopupMenus more quickly. */
class MaterialMenu(c: BaseActivity, v: View, res: Int, actions: Act) :
    PopupMenu(ContextThemeWrapper(c, R.style.Theme_Sexbook_Popup), v) {
    init {
        setOnMenuItemClickListener {
            if (it.itemId in actions) {
                actions[it.itemId]!!(it)
                true
            } else false
        }
        inflate(res)
    }
}
