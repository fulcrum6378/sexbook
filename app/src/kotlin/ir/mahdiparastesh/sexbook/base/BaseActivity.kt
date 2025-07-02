package ir.mahdiparastesh.sexbook.base

import android.content.Intent
import android.content.res.Configuration
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Build
import android.os.SystemClock
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.DisplayMetrics
import android.util.TypedValue
import android.widget.TextView
import android.widget.Toast
import android.widget.Toolbar
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.fragment.app.FragmentActivity
import ir.mahdiparastesh.sexbook.Main
import ir.mahdiparastesh.sexbook.R
import ir.mahdiparastesh.sexbook.Settings
import ir.mahdiparastesh.sexbook.Sexbook
import ir.mahdiparastesh.sexbook.view.UiTools.vib
import kotlin.reflect.KClass

/** Abstract class for all Activity instances in this app and it extends [FragmentActivity] */
abstract class BaseActivity : FragmentActivity() {
    val c: Sexbook by lazy { applicationContext as Sexbook }
    var tbTitle: TextView? = null
    val dm: DisplayMetrics by lazy { resources.displayMetrics }
    val night: Boolean by lazy {
        resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
    }
    private var lastToast = -1L

    /** Applies custom styles and actions on the Toolbar. */
    fun configureToolbar(tb: Toolbar, @StringRes title: Int) {
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
    fun color(@ColorRes res: Int) = resources.getColor(res, theme)

    /** @return the colour value of this attribute resource from the theme */
    @ColorInt
    fun themeColor(@AttrRes attr: Int) = TypedValue().apply {
        theme.resolveAttribute(attr, this, true)
    }.data

    /** Helper function for making a colour filter for the color resource */
    fun themePdcf(@AttrRes res: Int = com.google.android.material.R.attr.colorOnPrimary) =
        PorterDuffColorFilter(themeColor(res), PorterDuff.Mode.SRC_IN)

    /** A predetermined colour for all charts in this application */
    val chartColour: Int by lazy {
        if (!night) color(R.color.CPV_LIGHT)
        else themeColor(com.google.android.material.R.attr.colorOnPrimary)
    }

    /** Helper function for starting an Activity */
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

    /** Proper implementation of simple vibrations across different Android APIs */
    @Suppress("DEPRECATION")
    fun shake(dur: Long = 48L) {
        if (vib == null) vib = c.sp.getBoolean(Settings.spVibration, true)
        if (!vib!!) return
        val vib = (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            (getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
        else getSystemService(VIBRATOR_SERVICE) as Vibrator)
        vib.vibrate(
            VibrationEffect.createOneShot(
                dur, VibrationEffect.DEFAULT_AMPLITUDE
            )
        )
    }
}
