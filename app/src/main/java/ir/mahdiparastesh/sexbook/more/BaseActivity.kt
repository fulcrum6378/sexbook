package ir.mahdiparastesh.sexbook.more

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.Typeface
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.initialization.InitializationStatus
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener
import ir.mahdiparastesh.sexbook.*
import ir.mahdiparastesh.sexbook.Fun.Companion.isReady
import ir.mahdiparastesh.sexbook.data.Report
import ir.mahdiparastesh.sexbook.stat.Summary

abstract class BaseActivity : AppCompatActivity(), OnInitializationCompleteListener {
    val c: Context get() = applicationContext
    lateinit var m: Model
    lateinit var sp: SharedPreferences
    lateinit var font1: Typeface
    lateinit var font1Bold: Typeface
    var tbTitle: TextView? = null
    val dm: DisplayMetrics by lazy { resources.displayMetrics }
    val dirRtl: Boolean by lazy { c.resources.getBoolean(R.bool.dirRtl) }
    private var retryForAd = 0

    companion object {
        const val ADMOB_DELAY = 2000L
        const val MAX_AD_RETRY = 2
        lateinit var jdtpFont: Typeface // TODO: BAD
        lateinit var jdtpFontBold: Typeface // BAD
        var adsInitStatus: InitializationStatus? = null

        fun Context.night(): Boolean = resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        m = ViewModelProvider(this, Model.Factory()).get("Model", Model::class.java)

        if (this is Main) Settings.migrateSp()
        initSp()
        if (!::font1.isInitialized) {
            font1 = font()
            jdtpFont = font1
        }
        if (!::font1Bold.isInitialized) {
            font1Bold = font(true)
            jdtpFontBold = font1Bold
        }
    }

    override fun setContentView(root: View?) {
        super.setContentView(root)
        root?.layoutDirection =
            if (!dirRtl) ViewGroup.LAYOUT_DIRECTION_LTR else ViewGroup.LAYOUT_DIRECTION_RTL
    }

    override fun onStart() {
        super.onStart()
        if (adsInitStatus?.isReady() != true)
            Delay(ADMOB_DELAY) { initAdmob() }
        else onInitializationComplete(adsInitStatus!!)
    }

    override fun onInitializationComplete(adsInitStatus: InitializationStatus) {
        Companion.adsInitStatus = adsInitStatus
        if (!adsInitStatus.isReady()) {
            if (retryForAd < MAX_AD_RETRY) Delay(ADMOB_DELAY) {
                initAdmob()
                retryForAd++
            } else retryForAd = 0
            return; }
    }

    fun initSp() {
        sp = getSharedPreferences(Settings.spName, Context.MODE_PRIVATE)
    }

    fun toolbar(tb: Toolbar, title: Int) {
        setSupportActionBar(tb)
        for (g in 0 until tb.childCount) {
            val getTitle = tb.getChildAt(g)
            if (getTitle is TextView && getTitle.text.toString() == getString(title))
                tbTitle = getTitle
        }
        tbTitle?.typeface = font1Bold
        if (this !is Main) supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }
        tb.navigationIcon?.colorFilter = pdcf()
    }

    fun font(bold: Boolean = false): Typeface = Typeface.createFromAsset(
        c.assets, if (!bold) c.resources.getString(R.string.font1)
        else c.resources.getString(R.string.font1Bold)
    )

    fun dp(px: Int) = (dm.density * px.toFloat()).toInt()

    fun color(res: Int) = ContextCompat.getColor(c, res)

    fun pdcf(res: Int = R.color.CPDD) =
        PorterDuffColorFilter(ContextCompat.getColor(c, res), PorterDuff.Mode.SRC_IN)

    fun calType() = Fun.CalendarType.values()[sp.getInt(Settings.spCalType, 0)]

    private fun initAdmob() {
        retryForAd = 0
        MobileAds.initialize(c, this)
    }

    fun summarize(): Boolean =
        if (m.onani.value != null && m.onani.value!!.size > 0) {
            val nEstimated: Int
            var nExcluded = 0
            var filtered: List<Report> = m.onani.value!!
            filtered =
                filtered.filter { it.isReal }.also { nEstimated = filtered.size - it.size }
            if (sp.getBoolean(Settings.spStatSinceCb, false))
                filtered = filtered.filter { it.time > sp.getLong(Settings.spStatSince, 0) }
                    .also { nExcluded = filtered.size - it.size }
            m.summary.value = Summary(filtered, nEstimated, nExcluded); true; } else false
}
