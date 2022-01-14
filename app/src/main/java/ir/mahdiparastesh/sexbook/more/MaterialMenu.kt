package ir.mahdiparastesh.sexbook.more

import android.text.SpannableString
import android.view.MenuItem
import android.view.View
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.forEach
import ir.mahdiparastesh.sexbook.R

typealias Act = HashMap<Int, (item: MenuItem) -> Unit>

class MaterialMenu(val c: BaseActivity, v: View, res: Int, actions: Act) :
    PopupMenu(ContextThemeWrapper(c, R.style.AppTheme), v) {
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
        menu.forEach {
            val mNewTitle = SpannableString(it.title)
            mNewTitle.setSpan(
                CustomTypefaceSpan("", c.font1, BaseActivity.dm.density * 16f), 0,
                mNewTitle.length, SpannableString.SPAN_INCLUSIVE_INCLUSIVE
            )
            it.title = mNewTitle
        }
        super.show()
    }
}
