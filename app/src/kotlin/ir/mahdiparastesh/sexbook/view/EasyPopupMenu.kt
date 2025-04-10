package ir.mahdiparastesh.sexbook.view

import android.view.ContextThemeWrapper
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import ir.mahdiparastesh.sexbook.R
import ir.mahdiparastesh.sexbook.base.BaseActivity

/** Helper class for creating [PopupMenu]s more quickly */
class EasyPopupMenu(
    c: BaseActivity, v: View, res: Int, vararg actions: Pair<Int, (item: MenuItem) -> Unit>
) : PopupMenu(ContextThemeWrapper(c, R.style.Theme_Sexbook_Popup), v) {

    init {
        val map = hashMapOf<Int, (item: MenuItem) -> Unit>()
        for (act in actions) map[act.first] = act.second
        setOnMenuItemClickListener {
            if (it.itemId in map) {
                map[it.itemId]!!(it)
                true
            } else false
        }
        inflate(res)
    }
}
