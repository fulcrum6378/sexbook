package ir.mahdiparastesh.sexbook

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.*
import android.view.*
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.GravityCompat
import androidx.core.view.forEach
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.initialization.InitializationStatus
import com.google.android.material.navigation.NavigationView
import ir.mahdiparastesh.sexbook.Fun.Companion.isReady
import ir.mahdiparastesh.sexbook.Fun.Companion.stylise
import ir.mahdiparastesh.sexbook.data.*
import ir.mahdiparastesh.sexbook.databinding.MainBinding
import ir.mahdiparastesh.sexbook.list.GuessAdap
import ir.mahdiparastesh.sexbook.list.ReportAdap
import ir.mahdiparastesh.sexbook.more.BaseActivity
import ir.mahdiparastesh.sexbook.more.Delay
import ir.mahdiparastesh.sexbook.more.MaterialMenu.Companion.stylise
import ir.mahdiparastesh.sexbook.stat.*
import java.util.*
import kotlin.math.abs
import kotlin.system.exitProcess

class Main : BaseActivity(), NavigationView.OnNavigationItemSelectedListener,
    Toolbar.OnMenuItemClickListener {
    private lateinit var b: MainBinding
    private val exporter = Exporter(this)
    private lateinit var adBanner: AdView
    private var adBannerLoaded = false

    companion object {
        const val NOTIFY_MAX_DISTANCE = 3
        val CHANNEL_BIRTH = Main::class.java.`package`!!.name + ".NOTIFY_BIRTHDAY"
        var handler: Handler? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = MainBinding.inflate(layoutInflater)
        setContentView(b.root)
        toolbar(b.toolbar, R.string.app_name)

        handler = object : Handler(Looper.getMainLooper()) {
            @Suppress("UNCHECKED_CAST")
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    Work.VIEW_ALL -> m.onani.value = msg.obj as ArrayList<Report>
                    Work.C_VIEW_ALL -> m.liefde.value = (msg.obj as ArrayList<Crush>).apply {
                        for (it in this) if (it.notifyBirth && it.hasFullBirth()) {
                            val now = Calendar.getInstance()
                            val bir = Calendar.getInstance()
                            bir.set(
                                now[Calendar.YEAR], it.bMonth.toInt(), it.bDay.toInt(),
                                0, 0, 0
                            )
                            val dist = now.timeInMillis - bir.timeInMillis
                            if (abs(dist) <= NOTIFY_MAX_DISTANCE * 86400000L)
                                notifyBirth(it, dist)
                        }
                    }
                    Work.P_VIEW_ALL -> m.places.value = (msg.obj as ArrayList<Place>)
                    Work.G_VIEW_ALL -> m.guesses.value = (msg.obj as ArrayList<Guess>).apply {
                        sortWith(GuessAdap.Sort())
                        instillGuesses()
                    }
                }
            }
        }

        // Loading
        if (m.loaded) b.body.removeView(b.load)
        else if (night()) b.loadIV.colorFilter = pdcf()

        // Navigation
        object : ActionBarDrawerToggle(
            this, b.root, b.toolbar, R.string.sOpen, R.string.close
        ) {
            override fun onDrawerOpened(drawerView: View) {
                super.onDrawerOpened(drawerView)
                if (::adBanner.isInitialized && !adBannerLoaded) {
                    adBanner.loadAd(AdRequest.Builder().build())
                    adBannerLoaded = true
                }
            }
        }.apply {
            b.root.addDrawerListener(this@apply)
            isDrawerIndicatorEnabled = true
            syncState()
        }
        b.toolbar.navigationIcon?.colorFilter = pdcf()
        b.nav.setNavigationItemSelectedListener(this)
        b.nav.menu.forEach { it.stylise(this@Main) }

        // Pager
        b.pager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = 2
            override fun createFragment(i: Int): Fragment =
                if (i == 0) PageSex() else PageLove()
        }
        b.pager.setPageTransformer { _, pos ->
            b.transformer.layoutParams =
                (b.transformer.layoutParams as ConstraintLayout.LayoutParams).apply {
                    horizontalBias =
                        if (pos != 0f && pos != 1f) 0.75f - (pos / 2f)
                        else abs(b.pager.currentItem.toFloat() - 0.25f)
                    matchConstraintPercentWidth = 0.25f + ((0.5f - abs(0.5f - pos)) / 2f)
                }
        }

        // Miscellaneous
        m.onani.observe(this) { instilledGuesses = false }
        if (m.showingSummary) summary()
        if (m.showingRecency) recency()

        intent.check()
        // Work(c, Work.VIEW_ALL).start()
        Work(c, Work.C_VIEW_ALL).start()
        Work(c, Work.P_VIEW_ALL).start()
        Work(c, Work.G_VIEW_ALL).start()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intent.check()
    }

    override fun onInitializationComplete(adsInitStatus: InitializationStatus) {
        super.onInitializationComplete(adsInitStatus)
        if (!adsInitStatus.isReady()) return
        adBanner = Fun.adaptiveBanner(this, "ca-app-pub-9457309151954418/9298848860")
        b.nav.addView(adBanner, FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply { gravity = Gravity.BOTTOM })
        PageLove.handler.value?.obtainMessage(Work.ADMOB_LOADED)?.sendToTarget()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.momSum -> summary()
            R.id.momPop -> if (summarize())
                startActivity(Intent(this, Popularity::class.java))
            R.id.momGrw -> if (summarize())
                startActivity(Intent(this, Growth::class.java))
            R.id.momRec -> recency()
            R.id.momPlc -> startActivity(Intent(this, Places::class.java))
            R.id.momEst -> startActivity(Intent(this, Estimation::class.java))
            R.id.momImport -> exporter.launchImport()
            R.id.momExport -> exporter.launchExport()
            R.id.momSettings -> startActivity(Intent(this, Settings::class.java))
        }
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        b.toolbar.inflateMenu(R.menu.main_tlb)
        b.toolbar.setOnMenuItemClickListener(this)
        b.toolbar.menu.forEach { it.icon?.colorFilter = pdcf() }
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        b.toolbar.menu.forEach { it.stylise(this) }
        return true
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.mtCrush -> b.pager.setCurrentItem(1, true)
        }
        return true
    }

    private var exiting = false
    override fun onBackPressed() { // Don't use super's already overriden function
        if (b.root.isDrawerOpen(GravityCompat.START)) {
            b.root.closeDrawer(GravityCompat.START); return; }
        if (!exiting) {
            exiting = true
            Delay(4000) { exiting = false }
            Toast.makeText(c, R.string.toExit, Toast.LENGTH_SHORT).show()
            return
        }
        moveTaskToBack(true)
        Process.killProcess(Process.myPid())
        exitProcess(0)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler = null
    }


    private fun Intent.check() {
        when (action) {
            Intent.ACTION_VIEW -> if (data != null)
                Exporter.import(this@Main, intent.data!!, true)
            Action.ADD.s -> PageSex.messages.add(Work.SPECIAL_ADD)
            Action.RELOAD.s -> {
                m.reset()
                recreate()
            }
        }
    }

    fun load(sd: Long = 1500, dur: Long = 1000) {
        if (m.loaded) return
        val value = -dm.widthPixels.toFloat() * 1.2f
        ObjectAnimator.ofFloat(b.load, View.TRANSLATION_X, value).apply {
            startDelay = sd
            duration = dur
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    b.body.removeView(b.load)
                    m.loaded = true
                }
            })
            start()
        }
    }

    private fun summary() {
        if (summarize()) AlertDialog.Builder(this).apply {
            setTitle("${getString(R.string.momSum)} (${m.summary.value!!.actual} / ${m.onani.value!!.size})")
            setView(ConstraintLayout(c).apply {
                addView(ViewPager2(c).apply {
                    layoutParams = ViewGroup.LayoutParams(-1, -1)
                    adapter = SumAdapter(this@Main)
                })
                // The below EditText improves the EditText focus issue when you put
                // a Fragment inside a Dialog with a ViewPager in the middle!
                addView(EditText(c).apply {
                    layoutParams = ViewGroup.LayoutParams(-1, -2)
                    visibility = View.GONE
                })
            })
            setPositiveButton(R.string.ok, null)
            setCancelable(true)
            setOnDismissListener { m.showingSummary = false }
            m.showingSummary = true
        }.show().stylise(this)
    }

    private fun recency() {
        if (summarize()) {
            m.recency.value = Recency(m.summary.value!!)
            AlertDialog.Builder(this).apply {
                setTitle(resources.getString(R.string.momRec))
                setView(m.recency.value!!.draw(this@Main))
                setPositiveButton(R.string.ok, null)
                setCancelable(true)
                setOnDismissListener { m.showingRecency = false }
                m.showingRecency = true
            }.show().stylise(this)
        }
    }

    private fun notifyBirth(crush: Crush, dist: Long) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(
                NotificationChannel(
                    CHANNEL_BIRTH, getString(R.string.birthDateNtf),
                    NotificationManager.IMPORTANCE_HIGH
                )
            )
        with(NotificationManagerCompat.from(this)) {
            notify(666, NotificationCompat.Builder(this@Main, CHANNEL_BIRTH).apply {
                setSmallIcon(R.drawable.notification)
                setContentTitle(getString(R.string.bHappyTitle, crush.visName()))
                setContentText(
                    getString(
                        if (dist < 0L) R.string.bHappyBef else R.string.bHappyAft,
                        abs(dist / 3600000L)
                    )
                )
                priority = NotificationCompat.PRIORITY_HIGH
            }.build())
        }
    }

    private var instilledGuesses = false
    fun instillGuesses() {
        if (m.onani.value == null || m.guesses.value == null || instilledGuesses) return
        m.onani.value = m.onani.value?.filter { it.isReal }?.let { ArrayList(it) }
        for (g in m.guesses.value!!) {
            if (!g.checkValid()) continue
            var time = g.sinc
            val share = (86400000.0 / g.freq).toLong()

            while (time <= g.till) {
                m.onani.value!!.add(Report(time, getString(R.string.recEstimated), g.type, g.plac))
                time += share
            }
        }
        m.onani.value!!.sortWith(ReportAdap.Sort())
        instilledGuesses = true
    }


    private inner class SumAdapter(c: Main) : FragmentStateAdapter(c) {
        override fun getItemCount(): Int = 2
        override fun createFragment(i: Int): Fragment = when (i) {
            1 -> SumPie()
            else -> SumChips()
        }
    }

    enum class Action(val s: String) {
        RELOAD("ir.mahdiparastesh.sexbook.ACTION.RELOAD"),
        ADD("ir.mahdiparastesh.sexbook.ACTION.ADD")
    }
}
