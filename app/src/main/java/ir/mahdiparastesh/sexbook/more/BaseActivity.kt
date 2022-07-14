package ir.mahdiparastesh.sexbook.more

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.icu.util.GregorianCalendar
import android.icu.util.IndianCalendar
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.annotation.MainThread
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.ads.*
import com.google.android.gms.ads.initialization.InitializationStatus
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import ir.mahdiparastesh.sexbook.*
import ir.mahdiparastesh.sexbook.Fun.isReady
import ir.mahdiparastesh.sexbook.R
import ir.mahdiparastesh.sexbook.data.Report
import ir.mahdiparastesh.sexbook.stat.Summary

abstract class BaseActivity : AppCompatActivity(), OnInitializationCompleteListener {
    val c: Context get() = applicationContext
    lateinit var m: Model
    lateinit var sp: SharedPreferences
    private var tbTitle: TextView? = null
    val dm: DisplayMetrics by lazy { resources.displayMetrics }
    private val dirRtl by lazy { c.resources.getBoolean(R.bool.dirRtl) }
    var interstitialAd: InterstitialAd? = null
    var loadingAd = false
    var showingAd = false
    private var retryForAd = 0

    companion object {
        const val ADMOB_DELAY = 2000L
        const val MAX_AD_RETRY = 2
        var adsInitStatus: InitializationStatus? = null

        fun Context.night(): Boolean = resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        m = ViewModelProvider(this, Model.Factory())["Model", Model::class.java]
        sp = getSharedPreferences(Settings.spName, Context.MODE_PRIVATE)
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

    fun toolbar(tb: Toolbar, @StringRes title: Int) {
        setSupportActionBar(tb)
        for (g in 0 until tb.childCount) {
            val getTitle = tb.getChildAt(g)
            if (getTitle is TextView && getTitle.text.toString() == getString(title))
                tbTitle = getTitle
        }
        if (this !is Main) {
            supportActionBar?.apply {
                setDisplayHomeAsUpEnabled(true)
                setDisplayShowHomeEnabled(true)
            }
            tb.setNavigationOnClickListener { onBackPressed() }
        }
        tb.navigationIcon?.colorFilter = pdcf()
    }

    fun dp(px: Int) = (dm.density * px.toFloat()).toInt()

    fun color(@ColorRes res: Int) = ContextCompat.getColor(c, res)

    fun pdcf(@ColorRes res: Int = R.color.CPDD) =
        PorterDuffColorFilter(ContextCompat.getColor(c, res), PorterDuff.Mode.SRC_IN)

    fun calType() = arrayOf(
        GregorianCalendar::class.java,
        PersianCalendar::class.java,
        IndianCalendar::class.java
    )[sp.getInt(Settings.spCalType, 0)] // TODO defValue based on locale

    private fun initAdmob() {
        retryForAd = 0
        MobileAds.initialize(c, this)
    }

    @MainThread
    fun loadInterstitial(adUnitId: String, autoPlay: () -> Boolean) {
        if (adsInitStatus?.isReady() != true) {
            if (retryForAd < MAX_AD_RETRY) Delay(ADMOB_DELAY) {
                loadInterstitial(adUnitId) { false }
                retryForAd++
            } else retryForAd = 0
            return; }
        if (interstitialAd != null || loadingAd) return
        loadingAd = true
        InterstitialAd.load(
            c, adUnitId, AdRequest.Builder().build(), object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    loadingAd = false
                    if (BuildConfig.DEBUG)
                        Toast.makeText(c, "onAdFailedToLoad $adError", Toast.LENGTH_LONG).show()
                }

                override fun onAdLoaded(ad: InterstitialAd) {
                    loadingAd = false
                    interstitialAd = ad.apply { fullScreenContentCallback = InterstitialCallback() }
                    if (autoPlay()) showInterstitial()
                }
            })
    }

    @MainThread
    fun showInterstitial() {
        if (!showingAd) interstitialAd?.show(this@BaseActivity)
    }

    fun summarize(): Boolean = if (m.onani.value != null && m.onani.value!!.size > 0) {
        val nEstimated: Int
        var nExcluded = 0
        var filtered: List<Report> = m.onani.value!!
        filtered = filtered.filter { it.isReal }.also { nEstimated = filtered.size - it.size }
        if (sp.getBoolean(Settings.spStatSinceCb, false))
            filtered = filtered.filter { it.time > sp.getLong(Settings.spStatSince, 0) }
                .also { nExcluded = filtered.size - it.size }
        m.summary.value = Summary(filtered, nEstimated, nExcluded); true; } else false

    inner class InterstitialCallback : FullScreenContentCallback() {
        override fun onAdShowedFullScreenContent() {
            showingAd = true
        }

        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
            showingAd = false
            interstitialAd = null
        }

        override fun onAdDismissedFullScreenContent() {
            showingAd = false
            interstitialAd = null
        }
    }
}
