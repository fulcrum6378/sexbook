package ir.mahdiparastesh.sexbook.stat.base

import android.os.Bundle
import android.view.View
import ir.mahdiparastesh.sexbook.base.BaseActivity

/** An Activity which can display charts. */
abstract class ChartActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!requirements()) {
            onBackPressedDispatcher.onBackPressed(); return; }
        setContentView(getRootView())
        if (night) window.decorView.setBackgroundColor(
            themeColor(android.R.attr.colorPrimary)
        )
    }

    abstract fun requirements(): Boolean

    abstract fun getRootView(): View
}
