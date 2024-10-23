package ir.mahdiparastesh.sexbook.base

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.icu.util.GregorianCalendar
import android.icu.util.IndianCalendar
import android.os.Bundle
import android.os.SystemClock
import android.util.DisplayMetrics
import android.util.TypedValue
import android.widget.TextView
import android.widget.Toast
import android.widget.Toolbar
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import ir.mahdiparastesh.sexbook.Main
import ir.mahdiparastesh.sexbook.Model
import ir.mahdiparastesh.sexbook.Settings
import ir.mahdiparastesh.sexbook.misc.HumanistIranianCalendar
import java.util.Locale
import kotlin.reflect.KClass

/** Abstract class for all Activity instances in this app and it extends FragmentActivity. */
abstract class BaseActivity : FragmentActivity()/*, OnInitializationCompleteListener*/ {
    val c: Context get() = applicationContext
    lateinit var m: Model
    lateinit var sp: SharedPreferences
    var tbTitle: TextView? = null
    val dm: DisplayMetrics by lazy { resources.displayMetrics }
    private var lastToast = -1L

    companion object {
        /** @return true if the night mode is on */
        fun Context.night(): Boolean = resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        m = ViewModelProvider(this, Model.Factory())["Model", Model::class.java]
        sp = getSharedPreferences(Settings.spName, MODE_PRIVATE)
    }

    /** Applies custom styles and actions on the Toolbar. */
    fun toolbar(tb: Toolbar, @StringRes title: Int) {
        setActionBar(tb)
        for (g in 0 until tb.childCount) {
            val getTitle = tb.getChildAt(g)
            if (getTitle is TextView && getTitle.text.toString() == getString(title))
                tbTitle = getTitle
        }
        if (this !is Main) {
            actionBar?.apply {
                setDisplayHomeAsUpEnabled(true)
                setDisplayShowHomeEnabled(true)
            }
            tb.setNavigationOnClickListener { @Suppress("DEPRECATION") onBackPressed() }
        }
        tb.navigationIcon?.colorFilter = themePdcf()
    }

    /** Converts "px" to "dp". */
    fun dp(px: Int) = (dm.density * px.toFloat()).toInt()

    /** Helper function for getting a colour from resources. */
    @ColorInt
    fun color(@ColorRes res: Int) = ContextCompat.getColor(c, res)

    /** @return the colour value of this attribute resource from the theme. */
    @ColorInt
    fun themeColor(@AttrRes attr: Int) = TypedValue().apply {
        theme.resolveAttribute(attr, this, true)
    }.data

    /** Helper function for making a colour filter for the color resource. */
    fun themePdcf(@AttrRes res: Int = com.google.android.material.R.attr.colorOnPrimary) =
        PorterDuffColorFilter(themeColor(res), PorterDuff.Mode.SRC_IN)

    /**
     * @return the chosen calendar type, if no choice made, chooses it using their default Locale
     */
    fun calType() = arrayOf(
        GregorianCalendar::class.java,
        HumanistIranianCalendar::class.java,
        IndianCalendar::class.java
    )[sp.getInt(
        Settings.spCalType, when (Locale.getDefault().country) {
            "IR" -> 1; "IN" -> 2; else -> 0
        }
    )]

    /** Helper function for starting an Activity. */
    fun goTo(
        activity: KClass<*>, finish: Boolean = false, onIntent: (Intent.() -> Unit)? = null
    ): Boolean {
        val intent = Intent(this, activity.java)
        onIntent?.also { intent.it() }
        startActivity(intent)
        if (finish) finish() // Delay(1000) { finish() }
        // The phone's home screen may appear if there are no active activities at the moment.
        return true
    }

    /** Controls a kind of Toast that can be repeated so much by the user. */
    fun uiToast(@StringRes res: Int) {
        if ((SystemClock.elapsedRealtime() - lastToast) < 2000L) return
        Toast.makeText(this, res, Toast.LENGTH_SHORT).show()
        lastToast = SystemClock.elapsedRealtime()
    }
}
