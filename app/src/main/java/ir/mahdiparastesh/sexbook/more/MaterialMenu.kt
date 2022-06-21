package ir.mahdiparastesh.sexbook.more

import android.view.MenuItem
import android.view.View
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.PopupMenu
import ir.mahdiparastesh.sexbook.R

typealias Act = HashMap<Int, (item: MenuItem) -> Unit>

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
