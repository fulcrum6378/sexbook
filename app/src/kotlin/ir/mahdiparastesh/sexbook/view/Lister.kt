package ir.mahdiparastesh.sexbook.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.view.ContextThemeWrapper
import android.view.View
import androidx.annotation.DrawableRes
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.badge.BadgeUtils
import ir.mahdiparastesh.sexbook.R
import ir.mahdiparastesh.sexbook.base.BaseActivity

interface Lister {
    var countBadge: BadgeDrawable?

    /** Shows a counting badge attached to the Toolbar's title. */
    @SuppressLint("UnsafeOptInUsageError")
    fun count(n: Int?) {
        val c = this as BaseActivity
        BadgeUtils.detachBadgeDrawable(countBadge, c.tbTitle!!)
        if (n != null && n > 0) BadgeUtils.attachBadgeDrawable(
            BadgeDrawable.create(ContextThemeWrapper(c, UiTools.materialTheme)).apply {
                number = n
                backgroundColor = c.themeColor(android.R.attr.textColor)
                badgeTextColor = c.themeColor(android.R.attr.colorPrimary)
                countBadge = this
                maxCharacterCount = UiTools.MAX_BADGE_CHAR
            }, c.tbTitle!!
        )
    }

    /** Creates and executes an explosion effect on this View. */
    @SuppressLint("UseCompatLoadingForDrawables")
    fun explosionEffect(
        v: View, dur: Long = 522, @DrawableRes src: Int = R.drawable.button_light,
        alpha: Float = 1f, max: Float = 4f
    ) {
        if (v.parent !is ConstraintLayout) return
        val parent = v.parent as ConstraintLayout
        val ex = View(this as BaseActivity).apply {
            background = getDrawable(src)
            this.alpha = alpha
        }
        parent.addView(
            ex, parent.indexOfChild(v),
            ConstraintLayout.LayoutParams(0, 0).apply {
                topToTop = v.id; leftToLeft = v.id; rightToRight = v.id; bottomToBottom = v.id
            })

        val explode = AnimatorSet().setDuration(dur)
        val hide = ObjectAnimator.ofFloat(ex, View.ALPHA, 0f)
        hide.startDelay = explode.duration / 4
        explode.apply {
            playTogether(
                ObjectAnimator.ofFloat(ex, View.SCALE_X, ex.scaleX * max),
                ObjectAnimator.ofFloat(ex, View.SCALE_Y, ex.scaleY * max),
                hide
            )
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    parent.removeView(ex)
                }
            })
            start()
        }
    }
}
