package ir.mahdiparastesh.sexbook

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
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
import android.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
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
import ir.mahdiparastesh.sexbook.more.ActionBarDrawerToggle
import ir.mahdiparastesh.sexbook.more.BaseActivity
import ir.mahdiparastesh.sexbook.more.CalendarManager
import ir.mahdiparastesh.sexbook.more.Delay
import ir.mahdiparastesh.sexbook.more.Lister
import ir.mahdiparastesh.sexbook.stat.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.system.exitProcess

class Main : BaseActivity(), NavigationView.OnNavigationItemSelectedListener,
    Toolbar.OnMenuItemClickListener, Lister {
    private val b: MainBinding by lazy { MainBinding.inflate(layoutInflater) }
    private val exporter = Exporter(this)
    private var exiting = false
    private val drawerGravity = GravityCompat.START
    private val menus = arrayOf(R.menu.page_sex_tlb, R.menu.sort)
    /*private lateinit var adBanner: AdView
    private var adBannerLoaded = false*/

    override var countBadge: BadgeDrawable? = null

    companion object {
        /** when set to true, Main will recreate() in onResume(). */
        var changed = false
        //var showAdAfterRecreation = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(b.root)
        toolbar(b.toolbar, R.string.app_name)
        m.db = Database.Builder(c).build()
        m.dao = m.db.dao()

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

        // Load all data from the database
        CoroutineScope(Dispatchers.IO).launch {
            val rp = if (m.onani == null) m.dao.rGetAll() else null
            val cr = if (m.liefde == null) m.dao.cGetAll() else null
            val pl = if (m.places == null) m.dao.pGetAll() else null
            val gs = if (m.guesses == null) m.dao.gGetAll() else null
            withContext(Dispatchers.Main) {
                rp?.also { m.onani = ArrayList(it) }
                cr?.also { m.people = ArrayList(it) }
                m.getCrushes()?.apply {
                    m.liefde = this
                    if (isEmpty()) return@apply
                    if (sp.getBoolean(Settings.spCalOutput, false) &&
                        CalendarManager.checkPerm(this@Main)
                    ) m.calManager = CalendarManager(this@Main, this)

                    // notify if any birthday is around
                    if ((Fun.now() - sp.getLong(Settings.spLastNotifiedBirthAt, 0L)
                                ) < Settings.notifyBirthAfterLastTime ||
                        sp.getBoolean(Settings.spPauseBirthdaysNtf, false)
                    ) return@apply
                    for (it in this) if (it.notifyBirth()) it.bCalendar()?.also { birth ->
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
                pl?.let { ArrayList(it) }?.apply {
                    m.places = this
                    if (m.onani != null) for (p in indices) {
                        var sum = 0L
                        for (r in m.onani!!)
                            if (r.plac == this[p].id)
                                sum++
                        this[p].sum = sum
                    }
                    sortWith(Place.Sort(Place.Sort.NAME))
                    if (m.onani != null) sortWith(Place.Sort(Place.Sort.SUM))
                }
                gs?.also {
                    m.guesses = ArrayList(it.sortedWith(Guess.Sort()))
                    instillGuesses()
                }
                pageSex()?.prepareList() // guesses must be instilled before doing this.
            }
        }

        // Miscellaneous
        if (m.navOpen) b.root.openDrawer(drawerGravity)
        /*if (showAdAfterRecreation) {
            loadInterstitial("ca-app-pub-9457309151954418/1225353463") { true }
            showAdAfterRecreation = false
        }*/
        intent.check(true)
        addOnNewIntentListener { it.check() }
    }

    /*override fun onInitializationComplete(adsInitStatus: InitializationStatus) {
        super.onInitializationComplete(adsInitStatus)
        if (!adsInitStatus.isReady()) return
        adBanner = Fun.adaptiveBanner(this, "ca-app-pub-9457309151954418/9298848860")
        b.nav.addView(adBanner, FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply { gravity = Gravity.BOTTOM })
        pageLove()?.loadAd()
    }*/

    override fun onResume() {
        super.onResume()
        if (changed) {
            changed = false
            onDataChanged()
            return; }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        if (item.itemId in arrayOf(R.id.momSum, R.id.momRec, R.id.momMix)
            && !summarize(true)
        ) {
            uiToast(R.string.noRecords); return true
        } else if (item.itemId in arrayOf(R.id.momPop, R.id.momGrw, R.id.momTst) && !summarize()) {
            uiToast(R.string.noStat); return true
        } else if (item.itemId == R.id.momPpl) summarize()

        when (item.itemId) {
            R.id.momSum -> SummaryDialog().show(supportFragmentManager, SummaryDialog.TAG)
            R.id.momRec -> Recency().show(supportFragmentManager, Recency.TAG)
            R.id.momPop -> goTo(Adorability::class)
            R.id.momGrw -> goTo(Growth::class)
            R.id.momMix -> goTo(Mixture::class)
            R.id.momInt -> goTo(Intervals::class)
            R.id.momTst -> goTo(Taste::class)

            R.id.momPpl -> goTo(People::class)
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

    @SuppressLint("MissingSuperCall")
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
        m.db.close()
        super.onDestroy()
    }


    private fun pageSex(): PageSex? =
        supportFragmentManager.findFragmentByTag("f${b.pager.adapter?.getItemId(0)}") as? PageSex

    fun pageLove(): PageLove? =
        supportFragmentManager.findFragmentByTag("f${b.pager.adapter?.getItemId(1)}") as? PageLove

    var intentViewId: Long? = null
    private fun Intent.check(isOnCreate: Boolean = false) {
        when (action) {
            Action.ADD.s -> pageSex()?.add()
            Action.VIEW.s -> (try {
                dataString?.toLong()
            } catch (e: NumberFormatException) {
                null
            })?.also { id ->
                if (!isOnCreate && m.onani != null)
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
        if (m.onani.isNullOrEmpty()) return false
        var nExcluded = 0
        var filtered: List<Report> = m.onani!!

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

        // Filter non-orgasm sex records if enabled
        if (!sp.getBoolean(Settings.spStatNonOrgasm, true))
            filtered = filtered.filter { it.ogsm }
                .also { nExcluded += filtered.size - it.size }

        m.summary = Summary(filtered, nExcluded, m.onani!!.size).apply {
            // Filter if only crushes wanted
            if (sp.getBoolean(Settings.spStatOnlyCrushes, false)) {
                val liefde = m.liefde?.map { it.key }
                if (!liefde.isNullOrEmpty()) scores = HashMap(scores.filter { it.key in liefde })
                    .also {
                        nonCrush = apparent - it.values.sumOf { orgasms ->
                            orgasms.sumOf { o -> o.value.toDouble() }
                        }.toFloat()
                        apparent -= nonCrush
                    }
            }
        }
        (pageSex()?.b?.rv?.adapter as? ReportAdap)?.crushSuggester?.update()
        return true
    }

    private fun notifyBirth(crush: Crush, dist: Long) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ||
            ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            == PackageManager.PERMISSION_GRANTED
        ) (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).also { nm ->
            val channelBirth = Main::class.java.`package`!!.name + ".NOTIFY_BIRTHDAY"
            nm.createNotificationChannel(
                NotificationChannel(
                    channelBirth, getString(R.string.birthDateNtf),
                    NotificationManager.IMPORTANCE_HIGH
                )
            )
            nm.notify(
                crush.key.length + crush.visName().length,
                NotificationCompat.Builder(this@Main, channelBirth).apply {
                    setSmallIcon(R.drawable.notification)
                    setContentTitle(getString(R.string.bHappyTitle, crush.visName()))
                    setContentText(
                        getString(
                            if (dist < 0L) R.string.bHappyBef
                            else R.string.bHappyAft,
                            abs(dist / 3600000L)
                        )
                    )
                    priority = NotificationCompat.PRIORITY_HIGH
                }.build()
            )
            sp.edit().putLong(Settings.spLastNotifiedBirthAt, Fun.now()).apply()
        }
    }

    private fun instillGuesses() {
        for (g in m.guesses!!.filter { it.able }) {
            if (!g.checkValid()) continue
            var time = g.sinc
            val share = (86400000.0 / g.freq).toLong()

            while (time <= g.till) {
                m.onani!!.add(Report(time, g.crsh ?: "", g.type, g.plac))
                time += share
            }
        }
        m.onani!!.sortWith(Report.Sort())
    }

    fun onDataChanged() {
        m.resetData()
        // showAdAfterRecreation = true
        recreate()
    }


    enum class Action(val s: String) {
        ADD("${Main::class.java.`package`!!.name}.ACTION_ADD"),
        VIEW("${Main::class.java.`package`!!.name}.ACTION_VIEW"),
    }
}

/* TODO:
  * Problems:
  * Tearing at the bottom of Identify
  * Material v1.11.0
  * Searching in Summary and Recency is so immature!
  * -
  * Extension:
  * Taste (needs ViewPager2 to be implemented first!)
  * "Turn off notifications for this Crush" on the notification
  */
