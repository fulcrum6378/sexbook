package ir.mahdiparastesh.sexbook

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.icu.util.Calendar
import android.icu.util.GregorianCalendar
import android.os.*
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.edit
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.navigation.NavigationView
import ir.mahdiparastesh.sexbook.Fun.calendar
import ir.mahdiparastesh.sexbook.Fun.createFilterYm
import ir.mahdiparastesh.sexbook.Fun.toDefaultType
import ir.mahdiparastesh.sexbook.data.*
import ir.mahdiparastesh.sexbook.databinding.MainBinding
import ir.mahdiparastesh.sexbook.list.ReportAdap
import ir.mahdiparastesh.sexbook.more.BaseActivity
import ir.mahdiparastesh.sexbook.more.CalendarManager
import ir.mahdiparastesh.sexbook.more.Delay
import ir.mahdiparastesh.sexbook.more.Lister
import ir.mahdiparastesh.sexbook.stat.*
import java.io.File
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.abs
import kotlin.system.exitProcess

class Main : BaseActivity(), NavigationView.OnNavigationItemSelectedListener,
    Toolbar.OnMenuItemClickListener, Lister {
    private val b: MainBinding by lazy { MainBinding.inflate(layoutInflater) }
    private val exporter = Exporter(this)
    private var calManager: CalendarManager? = null
    private var exiting = false
    private val drawerGravity = GravityCompat.START
    private val menus = arrayOf(R.menu.page_sex_tlb, R.menu.sort)
    /*private lateinit var adBanner: AdView
    private var adBannerLoaded = false*/

    override var countBadge: BadgeDrawable? = null

    companion object {
        var handler: Handler? = null
        // var showAdAfterRecreation = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(b.root)
        toolbar(b.toolbar, R.string.app_name)

        handler = object : Handler(Looper.getMainLooper()) {
            @Suppress("UNCHECKED_CAST")
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    Work.VIEW_ALL -> m.onani.value = msg.obj as ArrayList<Report>
                    Work.C_VIEW_ALL -> m.liefde = CopyOnWriteArrayList(
                        msg.obj as List<Crush>
                    ).apply {
                        if (isEmpty()) return@apply
                        if (sp.getBoolean(Settings.spCalOutput, false) &&
                            CalendarManager.checkPerm(this@Main)
                        ) calManager = CalendarManager(this@Main, this)

                        // notify if any birthday is around
                        if ((Fun.now() - sp.getLong(Settings.spLastNotifiedBirthAt, 0L)
                                    ) < Settings.notifyBirthAfterLastTime ||
                            sp.getBoolean(Settings.spPauseBirthdaysNtf, false)
                        ) return@apply
                        for (it in this) if (it.notifyBirth) it.bCalendar()?.also { birth ->
                            var now: Calendar = GregorianCalendar()
                            var bir: Calendar = GregorianCalendar()
                            if (!sp.getBoolean(
                                    Settings.spGregorianForBirthdays,
                                    Settings.spGregorianForBirthdaysDef
                                )
                            ) {
                                now = (now as GregorianCalendar).toDefaultType(this@Main)
                                bir = (bir as GregorianCalendar).toDefaultType(this@Main)
                            }
                            // do NOT alter the "birth" instance!
                            val dist = now.timeInMillis - bir.apply {
                                this.timeInMillis = birth.timeInMillis
                                this[Calendar.YEAR] = now[Calendar.YEAR]
                            }.timeInMillis
                            if (dist in
                                -(sp.getInt(Settings.spNotifyBirthDaysBefore, 3) * Fun.A_DAY)
                                ..Fun.A_DAY
                            ) notifyBirth(it, dist)
                        }
                    }
                    Work.C_REPLACE_ALL -> calManager?.replaceEvents(msg.obj as List<Crush>)
                    Work.P_VIEW_ALL -> m.places.value = (msg.obj as ArrayList<Place>)
                    Work.G_VIEW_ALL -> m.guesses.value = (msg.obj as ArrayList<Guess>).apply {
                        sortWith(Guess.Sort())
                        instillGuesses()
                    }
                    Work.CRUSH_ALTERED ->
                        //(msg.obj as List<Crush?>).also { calManager?.updateEvent(it[0], it[1]) }
                        m.liefde?.also { calManager?.replaceEvents(it) }
                }
            }
        }

        // Loading
        if (m.loaded) b.body.removeView(b.load)
        else if (night()) b.loadIV.colorFilter = themePdcf()

        // Navigation
        object : ActionBarDrawerToggle(
            this, b.root, b.toolbar, R.string.sOpen, R.string.close
        ) {
            override fun onDrawerOpened(drawerView: View) {
                super.onDrawerOpened(drawerView)
                m.navOpen = true
                /*if (::adBanner.isInitialized && !adBannerLoaded) {
                    adBanner.loadAd(AdRequest.Builder().build())
                    adBannerLoaded = true
                }*/
            }

            override fun onDrawerClosed(drawerView: View) {
                super.onDrawerClosed(drawerView)
                m.navOpen = false
            }
        }.apply {
            b.root.addDrawerListener(this@apply)
            isDrawerIndicatorEnabled = true
            syncState()
        }
        b.nav.setNavigationItemSelectedListener(this)
        b.toolbar.navigationIcon?.colorFilter = themePdcf()

        // Pager
        b.pager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = 2
            override fun createFragment(i: Int): Fragment = if (i == 0) PageSex() else PageLove()
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
        b.pager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            private var firstTime = true
            override fun onPageSelected(i: Int) {
                if (firstTime) {
                    firstTime = false; return; }
                b.toolbar.menu.clear()
                b.toolbar.inflateMenu(menus[i])
                m.currentPage = i
                count(if (i == 0) null else m.liefde?.size ?: 0)
            }
        })

        // Miscellaneous
        m.onani.observe(this) { instilledGuesses = false }
        if (m.navOpen) b.root.openDrawer(drawerGravity)
        /*if (showAdAfterRecreation) {
            loadInterstitial("ca-app-pub-9457309151954418/1225353463") { true }
            showAdAfterRecreation = false
        }*/
        if (sp.contains("prefersMasculine")) sp.edit {
            remove("prefersMasculine")
            if (sp.getInt(Settings.spPageLoveSortBy, 0) == 4) putInt(Settings.spPageLoveSortBy, 5)
        }
        File(c.cacheDir, "calendar_index.json").also { if (it.exists()) it.delete() }

        intent.check(true)
        addOnNewIntentListener { it.check() }
        if (m.liefde == null) Work(c, Work.C_VIEW_ALL).start()
        if (m.places.value == null) Work(c, Work.P_VIEW_ALL).start()
        if (m.guesses.value == null) Work(c, Work.G_VIEW_ALL).start()
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
        if (item.itemId in arrayOf(R.id.momSum, R.id.momRec, R.id.momMix)
            && !summarize(true)
        ) {
            uiToast(R.string.noRecords); return true
        } else if (item.itemId in arrayOf(R.id.momPop, R.id.momGrw, R.id.momTst) && !summarize()) {
            uiToast(R.string.noStat); return true; }

        when (item.itemId) {
            R.id.momSum -> SummaryDialog().show(supportFragmentManager, SummaryDialog.TAG)
            R.id.momRec -> Recency().show(supportFragmentManager, Recency.TAG)
            R.id.momPop -> goTo(Adorability::class)
            R.id.momGrw -> goTo(Growth::class)
            R.id.momTst -> goTo(Taste::class)
            R.id.momMix -> goTo(Mixture::class)
            R.id.momPlc -> goTo(Places::class)
            R.id.momEst -> goTo(Estimation::class)
            R.id.momImport -> exporter.launchImport()
            R.id.momExport -> exporter.launchExport()
            R.id.momSend -> exporter.send()
            R.id.momSettings -> goTo(Settings::class)
        }
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        b.toolbar.inflateMenu(menus[m.currentPage])
        b.toolbar.setOnMenuItemClickListener(this)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.findItem(Fun.findSortMenuItemId(sp.getInt(Settings.spPageLoveSortBy, 0)))
            ?.isChecked = true
        menu?.findItem(
            if (sp.getBoolean(Settings.spPageLoveSortAsc, true))
                R.id.sortAsc else R.id.sortDsc
        )?.isChecked = true
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        when (item.itemId) {
            // PageSex (R.menu.page_sex_tlb):
            R.id.mtCrush -> b.pager.setCurrentItem(1, true)

            // PageLove (R.menu.sort):
            else -> {
                sp.edit {
                    val value = Fun.sort(item.itemId)
                    if (value is Int) putInt(Settings.spPageLoveSortBy, value)
                    else if (value is Boolean) putBoolean(Settings.spPageLoveSortAsc, value)
                }
                pageLove()?.prepareList()
            }
        }
        return true
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun onBackPressed() {
        if (b.root.isDrawerOpen(drawerGravity)) {
            b.root.closeDrawer(drawerGravity); return; }
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


    private fun pageSex(): PageSex? =
        supportFragmentManager.findFragmentByTag("f${b.pager.adapter?.getItemId(0)}") as? PageSex

    private fun pageLove(): PageLove? =
        supportFragmentManager.findFragmentByTag("f${b.pager.adapter?.getItemId(1)}") as? PageLove

    var intentViewId: Long? = null
    private fun Intent.check(isOnCreate: Boolean = false) {
        when (action) {
            Action.ADD.s -> pageSex()?.messages?.add(Work.SPECIAL_ADD)
            Action.RELOAD.s -> {
                m.resetData()
                // showAdAfterRecreation = true
                if (!isOnCreate) recreate()
            }
            Action.VIEW.s -> (try {
                dataString?.toLong()
            } catch (e: NumberFormatException) {
                null
            })?.also { id ->
                if (!isOnCreate && m.onani.value != null)
                    m.findGlobalIndexOfReport(id)
                        .also { if (it != -1) pageSex()?.resetAllReports(it) }
                else intentViewId = id
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

    /**
     * Summarises the sex records for statistics.
     *
     * This operation doesn't take so much time, generating the views for statistics takes that long!
     *
     * @param ignoreIfItsLessThanAMonth ignores returning false if date range of sex records doesn't
     *                                  extend one month
     * @return true if statisticisation was possible
     */
    fun summarize(ignoreIfItsLessThanAMonth: Boolean = false): Boolean {
        if (m.onani.value.isNullOrEmpty()) return false
        var nExcluded = 0
        var filtered: List<Report> = m.onani.value!!

        // Filter by time
        if (sp.getBoolean(Settings.spStatSinceCb, false))
            filtered = filtered.filter { it.time >= sp.getLong(Settings.spStatSince, 0) }
                .also { nExcluded += filtered.size - it.size }
        if (sp.getBoolean(Settings.spStatUntilCb, false))
            filtered = filtered.filter { it.time < sp.getLong(Settings.spStatUntil, 0) }
                .also { nExcluded += filtered.size - it.size }

        // Check if it can draw any visual charts;
        // this is possible only if the range of the sex records exceeds one month.
        if (filtered.isEmpty()) return false
        else if (!ignoreIfItsLessThanAMonth && // if it's empty, minOf will throw NoSuchElementException!
            filtered.minOf { it.time }.calendar(this).createFilterYm().toString() ==
            filtered.maxOf { it.time }.calendar(this).createFilterYm().toString()
        ) return false

        // Filter by type
        val allowedTypes = Fun.allowedSexTypes(sp)
        if (allowedTypes.size < Fun.sexTypesCount)
            filtered = filtered.filter { it.type in allowedTypes }
                .also { nExcluded += filtered.size - it.size }

        m.summary = Summary(filtered, nExcluded).apply {
            // Filter if only crushes wanted
            if (sp.getBoolean(Settings.spStatOnlyCrushes, false)) {
                val liefde = m.liefde?.map { it.key }
                if (!liefde.isNullOrEmpty()) scores = HashMap(scores.filter { it.key in liefde })
                    .also {
                        nonCrush = it.values.sumOf { orgasms ->
                            orgasms.sumOf { o -> o.value.toDouble() }
                        }.toFloat()
                        actual -= nonCrush
                    }
            }
        }
        (pageSex()?.b?.rv?.adapter as? ReportAdap)?.crushSuggester?.update()
        return true
    }

    private fun notifyBirth(crush: Crush, dist: Long) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) return

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
        m.onani.value = m.onani.value?.filter { !it.guess }?.let { ArrayList(it) }
        for (g in m.guesses.value!!.filter { it.able }) {
            if (!g.checkValid()) continue
            var time = g.sinc
            val share = (86400000.0 / g.freq).toLong()

            while (time <= g.till) {
                m.onani.value!!.add(Report(time, g.crsh ?: "", g.type, g.plac))
                time += share
            }
        }
        m.onani.value!!.sortWith(Report.Sort())
        instilledGuesses = true
    }


    enum class Action(val s: String) {
        RELOAD("${Main::class.java.`package`!!.name}.ACTION_RELOAD"),
        ADD("${Main::class.java.`package`!!.name}.ACTION_ADD"),
        VIEW("${Main::class.java.`package`!!.name}.ACTION_VIEW"),
    }
}

/* TODO:
  * Problems:
  * After whirling around the app sorting feature of PageLove doesn't work!
  * Searching in Summary and Recency is so immature!
  * -
  * Extension:
  * Add SPANISH translation
  * Statisticise delays in hours between orgasms
  * Eye and hair colours for Crush
  * Export data to TXT
  * -
  * Why is it fucked up after a package reinstall? Does it happen for others?
  */
