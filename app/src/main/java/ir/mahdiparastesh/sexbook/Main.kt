package ir.mahdiparastesh.sexbook

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.os.*
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.navigation.NavigationView
import ir.mahdiparastesh.sexbook.data.*
import ir.mahdiparastesh.sexbook.databinding.MainBinding
import ir.mahdiparastesh.sexbook.list.GuessAdap
import ir.mahdiparastesh.sexbook.list.ReportAdap
import ir.mahdiparastesh.sexbook.more.BaseActivity
import ir.mahdiparastesh.sexbook.more.Delay
import ir.mahdiparastesh.sexbook.stat.*
import kotlin.math.abs
import kotlin.system.exitProcess

class Main : BaseActivity(), NavigationView.OnNavigationItemSelectedListener,
    Toolbar.OnMenuItemClickListener {
    private lateinit var b: MainBinding
    private val exporter = Exporter(this)
    private var pageSex: PageSex? = null
    private var pageLove: PageLove? = null
    private var exiting = false
    /*private lateinit var adBanner: AdView
    private var adBannerLoaded = false*/

    companion object {
        const val NOTIFY_MAX_DISTANCE = 3
        var handler: Handler? = null
        // var showAdAfterRecreation = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        supportFragmentManager.fragmentFactory = PageFactory()
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
                        if ((Fun.now() - sp.getLong(Settings.spLastNotifiedBirthAt, 0L)
                                    ) < Settings.notifyBirthAfterLastTime
                        ) return@apply
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
        /*object : */ActionBarDrawerToggle(
            this, b.root, b.toolbar, R.string.sOpen, R.string.close
        )/* {
            override fun onDrawerOpened(drawerView: View) {
                super.onDrawerOpened(drawerView)
                if (::adBanner.isInitialized && !adBannerLoaded) {
                    adBanner.loadAd(AdRequest.Builder().build())
                    adBannerLoaded = true
                }
            }
        }*/.apply {
            b.root.addDrawerListener(this@apply)
            isDrawerIndicatorEnabled = true
            syncState()
        }
        b.nav.setNavigationItemSelectedListener(this)
        b.toolbar.navigationIcon?.colorFilter = pdcf()

        // Pager
        b.pager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = 2
            override fun createFragment(i: Int): Fragment =
                if (i == 0) PageSex().also { pageSex = it }
                else PageLove().also { pageLove = it }
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
        /*if (showAdAfterRecreation) {
            loadInterstitial("ca-app-pub-9457309151954418/1225353463") { true }
            showAdAfterRecreation = false
        }*/
        if (sp.contains("first_shift_for_indians"))
            sp.edit().remove("first_shift_for_indians").apply()

        intent.check(true)
        Work(c, Work.C_VIEW_ALL).start()
        Work(c, Work.P_VIEW_ALL).start()
        Work(c, Work.G_VIEW_ALL).start()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intent.check()
    }

    /*override fun onInitializationComplete(adsInitStatus: InitializationStatus) {
        super.onInitializationComplete(adsInitStatus)
        if (!adsInitStatus.isReady()) return
        adBanner = Fun.adaptiveBanner(this, "ca-app-pub-9457309151954418/9298848860")
        b.nav.addView(adBanner, FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply { gravity = Gravity.BOTTOM })
        PageLove.handler.value?.obtainMessage(Work.ADMOB_LOADED)?.sendToTarget()
    }*/

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.momSum -> summary()
            R.id.momRec -> recency()
            R.id.momPop -> if (summarize())
                startActivity(Intent(this, Adorability::class.java))
            R.id.momGrw -> if (summarize())
                startActivity(Intent(this, Growth::class.java))
            R.id.momMix -> if (!m.onani.value.isNullOrEmpty())
                startActivity(Intent(this, Mixture::class.java))
            R.id.momPlc -> startActivity(Intent(this, Places::class.java))
            R.id.momEst -> startActivity(Intent(this, Estimation::class.java))
            R.id.momImport -> exporter.launchImport()
            R.id.momExport -> exporter.launchExport()
            R.id.momSend -> exporter.send()
            R.id.momSettings -> startActivity(Intent(this, Settings::class.java))
        }
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        b.toolbar.inflateMenu(R.menu.main_tlb)
        b.toolbar.setOnMenuItemClickListener(this)
        return true
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.mtCrush -> b.pager.setCurrentItem(1, true)
        }
        return true
    }

    @Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
    override fun onBackPressed() { // Don't use super's already overridden function
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


    var intentToGlobalIndexOfItem: Int? = null
    private fun Intent.check(isOnCreate: Boolean = false) {
        when (action) {
            Intent.ACTION_VIEW -> data?.also { Exporter.import(this@Main, it) }
            Action.ADD.s -> pageSex?.messages?.add(Work.SPECIAL_ADD)
            Action.RELOAD.s -> {
                m.reset()
                // showAdAfterRecreation = true
                recreate()
            }
            Action.VIEW.s -> (try {
                intent.data?.toString()?.toLong()
            } catch (e: NumberFormatException) {
                null
            })?.also { id ->
                val gIndex = m.onani.value?.indexOfFirst { it.id == id }
                if (gIndex != -1) {
                    if (!isOnCreate) pageSex?.resetAllReports(gIndex)
                    else intentToGlobalIndexOfItem = gIndex
                }
            }
        }
    }

    fun load(sd: Long = 1500, dur: Long = 500) {
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

    fun summarize(): Boolean = if (m.onani.value != null && m.onani.value!!.isNotEmpty()) {
        var nExcluded = 0
        var filtered: List<Report> = m.onani.value!!

        // Filter by time
        if (sp.getBoolean(Settings.spStatSinceCb, false))
            filtered = filtered.filter { it.time > sp.getLong(Settings.spStatSince, 0) }
                .also { nExcluded += filtered.size - it.size }

        // Filter by type
        val allowedTypes = Fun.allowedSexTypes(sp)
        if (allowedTypes.size < Fun.sexTypesCount)
            filtered = filtered.filter { it.type in allowedTypes }
                .also { nExcluded += filtered.size - it.size }

        m.summary = Summary(filtered, nExcluded); true; } else false

    private fun summary() {
        val vp2 = ViewPager2(this@Main).apply {
            layoutParams = ViewGroup.LayoutParams(-1, -1)
            adapter = SumAdapter(this@Main)
        }
        if (summarize()) AlertDialog.Builder(this).apply {
            setTitle("${getString(R.string.summary)} (${m.summary!!.actual} / ${m.onani.value!!.size})")
            setView(ConstraintLayout(this@Main).apply {
                addView(vp2)
                // The below EditText improves the EditText focus issue when you put
                // a Fragment inside a Dialog with a ViewPager in the middle!
                addView(EditText(this@Main).apply {
                    layoutParams = ViewGroup.LayoutParams(-1, -2)
                    visibility = View.GONE
                })
            })
            setPositiveButton(android.R.string.ok, null)
            setNeutralButton(R.string.chart, null)
            setCancelable(true)
            setOnDismissListener {
                m.showingSummary = false
                m.lookingFor = null
            }
            m.showingSummary = true
        }.show().apply {
            getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener { vp2.currentItem = 1 }
        }
    }

    private fun recency() {
        if (!summarize()) return
        m.recency = Recency(m.summary!!)
        AlertDialog.Builder(this).apply {
            setTitle(resources.getString(R.string.recency))
            setView(m.recency!!.draw(this@Main))
            setPositiveButton(android.R.string.ok, null)
            setCancelable(true)
            setOnDismissListener {
                m.showingRecency = false
                m.lookingFor = null
            }
            m.showingRecency = true
        }.show()
    }

    private fun notifyBirth(crush: Crush, dist: Long) {
        val channelBirth = Main::class.java.`package`!!.name + ".NOTIFY_BIRTHDAY"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(
                NotificationChannel(
                    channelBirth, getString(R.string.birthDateNtf),
                    NotificationManager.IMPORTANCE_HIGH
                )
            )
        NotificationManagerCompat.from(this).notify(
            crush.key.length + crush.visName().length,
            NotificationCompat.Builder(this@Main, channelBirth).apply {
                setSmallIcon(R.drawable.notification)
                setContentTitle(getString(R.string.bHappyTitle, crush.visName()))
                setContentText(
                    getString(
                        if (dist < 0L) R.string.bHappyBef else R.string.bHappyAft,
                        abs(dist / 3600000L)
                    )
                )
                priority = NotificationCompat.PRIORITY_HIGH
            }.build()
        )
        sp.edit().putLong(Settings.spLastNotifiedBirthAt, Fun.now()).apply()
    }

    private var instilledGuesses = false
    fun instillGuesses() {
        if (m.onani.value == null || m.guesses.value == null || instilledGuesses) return
        m.onani.value = m.onani.value?.filter { it.isReal }?.let { ArrayList(it) }
        for (g in m.guesses.value!!.filter { it.able }) {
            if (!g.checkValid()) continue
            var time = g.sinc
            val share = (86400000.0 / g.freq).toLong()

            while (time <= g.till) {
                m.onani.value!!.add(Report(time, g.crsh ?: "", g.type, g.plac))
                time += share
            }
        }
        m.onani.value!!.sortWith(ReportAdap.Sort())
        instilledGuesses = true
    }


    private inner class PageFactory : FragmentFactory() {
        override fun instantiate(loader: ClassLoader, name: String): Fragment = when (name) {
            PageSex::class.java.name -> PageSex().also { pageSex = it }
            PageLove::class.java.name -> PageLove().also { pageLove = it }
            else -> super.instantiate(loader, name)
        }
    }

    private inner class SumAdapter(c: Main) : FragmentStateAdapter(c) {
        override fun getItemCount(): Int = 2
        override fun createFragment(i: Int): Fragment = when (i) {
            1 -> SumPie()
            else -> SumChips()
        }
    }

    enum class Action(val s: String) {
        RELOAD("${Main::class.java.`package`!!.name}.ACTION_RELOAD"),
        ADD("${Main::class.java.`package`!!.name}.ACTION_ADD"),
        VIEW("${Main::class.java.`package`!!.name}.ACTION_VIEW"),
    }
}

/* TODO:
  * Extension:
  * Multi-optional sorting feature for Crushes
  * "First met" for Crush
  * Design some widgets
  * Putting the events { crush birthdays, etc } customisably into the calendar (through system accounts).
  * Multiple accounts for Sexbook, registered as Android accounts
  *
  * Notes:
  * summarize() doesn't take so much time, generating the views for statistics takes that long.
  */
