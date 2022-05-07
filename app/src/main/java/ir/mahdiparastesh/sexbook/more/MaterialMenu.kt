package ir.mahdiparastesh.sexbook.more

import android.text.SpannableString
import android.view.MenuItem
import android.view.View
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.forEach

typealias Act = HashMap<Int, (item: MenuItem) -> Unit>

class MaterialMenu(val c: BaseActivity, v: View, res: Int, actions: Act) :
    PopupMenu(ContextThemeWrapper(c, c.theme), v) {
    init {
        setOnMenuItemClickListener {
            if (it.itemId in actions) {
                actions[it.itemId]!!(it)
                true
            } else false
        }
        inflate(res)
    }

    override fun show() {
        menu.forEach { it.stylise(c) }
        super.show()
    }

    companion object {
        fun MenuItem.stylise(c: BaseActivity) {
            if (title == null) return
            title = SpannableString(title).apply {
                setSpan(
                    CustomTypefaceSpan(
                        c.font1, c.dm.density * 15.5f, null
                    ), 0, length, SpannableString.SPAN_INCLUSIVE_INCLUSIVE
                )
            }
        }
    }
}
