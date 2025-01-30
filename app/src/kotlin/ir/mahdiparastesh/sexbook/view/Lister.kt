package ir.mahdiparastesh.sexbook.view

import android.annotation.SuppressLint
import android.view.ContextThemeWrapper
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.badge.BadgeUtils
import ir.mahdiparastesh.sexbook.Fun
import ir.mahdiparastesh.sexbook.base.BaseActivity

interface Lister {
    var countBadge: BadgeDrawable?

    @SuppressLint("UnsafeOptInUsageError")
    fun count(n: Int?) {
        val c = this as BaseActivity
        BadgeUtils.detachBadgeDrawable(countBadge, c.tbTitle!!)
        if (n != null && n > 0) BadgeUtils.attachBadgeDrawable(
            BadgeDrawable.create(ContextThemeWrapper(c, Fun.materialTheme)).apply {
                number = n
                backgroundColor = c.themeColor(android.R.attr.colorAccent)
                badgeTextColor = c.themeColor(android.R.attr.colorPrimary)
                countBadge = this
                maxCharacterCount = Fun.MAX_BADGE_CHAR
            }, c.tbTitle!!
        )
    }
}
