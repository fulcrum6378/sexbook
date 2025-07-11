package ir.mahdiparastesh.sexbook.page

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.icu.util.Calendar
import android.icu.util.GregorianCalendar
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import android.widget.Toolbar
import androidx.activity.viewModels
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.net.toUri
import androidx.core.util.isEmpty
import androidx.core.util.isNotEmpty
import androidx.core.util.set
import androidx.core.util.valueIterator
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.navigation.NavigationView
import ir.mahdiparastesh.sexbook.BuildConfig
import ir.mahdiparastesh.sexbook.R
import ir.mahdiparastesh.sexbook.Sexbook
import ir.mahdiparastesh.sexbook.base.BaseActivity
import ir.mahdiparastesh.sexbook.base.Lister
import ir.mahdiparastesh.sexbook.ctrl.CalendarManager
import ir.mahdiparastesh.sexbook.ctrl.Database
import ir.mahdiparastesh.sexbook.ctrl.Exporter
import ir.mahdiparastesh.sexbook.ctrl.NotificationActions
import ir.mahdiparastesh.sexbook.ctrl.Summary
import ir.mahdiparastesh.sexbook.data.Crush
import ir.mahdiparastesh.sexbook.data.Guess
import ir.mahdiparastesh.sexbook.data.Place
import ir.mahdiparastesh.sexbook.data.Report
import ir.mahdiparastesh.sexbook.databinding.MainBinding
import ir.mahdiparastesh.sexbook.list.ReportAdap
import ir.mahdiparastesh.sexbook.stat.Adorability
import ir.mahdiparastesh.sexbook.stat.CrushesStat
import ir.mahdiparastesh.sexbook.stat.Intervals
import ir.mahdiparastesh.sexbook.stat.Mixture
import ir.mahdiparastesh.sexbook.stat.RecencyDialog
import ir.mahdiparastesh.sexbook.stat.SummaryDialog
import ir.mahdiparastesh.sexbook.stat.Taste
import ir.mahdiparastesh.sexbook.util.Delay
import ir.mahdiparastesh.sexbook.util.LongSparseArrayExt.toArrayList
import ir.mahdiparastesh.sexbook.util.NumberUtils
import ir.mahdiparastesh.sexbook.util.NumberUtils.calendar
import ir.mahdiparastesh.sexbook.util.NumberUtils.createFilterYm
import ir.mahdiparastesh.sexbook.view.ActionBarDrawerToggle
import ir.mahdiparastesh.sexbook.view.SexType
import ir.mahdiparastesh.sexbook.view.UiTools
import ir.mahdiparastesh.sexbook.view.UiTools.possessiveDeterminer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.system.exitProcess

/**
 * This is the main Activity of this app.
 *
 * - Completely loads all the [Database] into the RAM (data stored in [Sexbook])
 *   (unfortunately we need all the database on startup, although our database isn't that heavy).
 * - Creates and displays the two primary Fragments [PageSex] and [PageLove] via a [ViewPager2].
 * - Provides navigation to other activities via a [NavigationView] as a drawer.
 * - Sends birthday notifications for the user if applicable.
 */
class Main : BaseActivity(), NavigationView.OnNavigationItemSelectedListener,
    Toolbar.OnMenuItemClickListener, Lister {
    private val b: MainBinding by lazy { MainBinding.inflate(layoutInflater) }
    val vm: Model by viewModels()
    private val exporter = Exporter(this)
    private var exiting = false
    private val menus = arrayOf(R.menu.page_sex, R.menu.page_love)

    override var countBadge: BadgeDrawable? = null

    companion object {
        /** when set to true, [Main] will [recreate] in [onResume]. */
        @Volatile
        var changed = false
    }

    class Model : ViewModel() {
        var loaded = false
        var currentPage = 0
        var listFilter = -1
        var visReports = arrayListOf<Long>()
        var navOpen = false

        fun sortVisReports(c: Sexbook) {
            visReports.sortBy { c.reports[it]?.time ?: 0L }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(b.root)
        configureToolbar(b.toolbar, R.string.app_name)

        // loading
        if (vm.loaded) b.body.removeView(b.load)

        // navigation
        object : ActionBarDrawerToggle(
            this, b.root, b.toolbar, R.string.openMenu, R.string.closeMenu
        ) {
            override fun onDrawerOpened(drawerView: View) {
                super.onDrawerOpened(drawerView)
                vm.navOpen = true
            }

            override fun onDrawerClosed(drawerView: View) {
                super.onDrawerClosed(drawerView)
                vm.navOpen = false
            }
        }.apply {
            b.root.addDrawerListener(this@apply)
            isDrawerIndicatorEnabled = true
            syncState()
        }
        b.nav.setNavigationItemSelectedListener(this)
        b.toolbar.navigationIcon?.colorFilter = themePdcf()
        @Suppress("KotlinConstantConditions")
        if (BuildConfig.BUILD_TYPE == "mahdi")
            b.nav.menu.findItem(R.id.momCheckUpdates)?.isVisible = false

        // ViewPager2
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
                onPrepareOptionsMenu(b.toolbar.menu)
                vm.currentPage = i
                count(if (i == 0) null else c.liefde.size)
            }
        })

        // load all data from the database
        if (!c.dbLoaded) CoroutineScope(Dispatchers.IO).launch {
            //Log.d("ZOEY", "Began reading the database...")

            // Report
            for (r in c.dao.rGetAll()) c.reports[r.id] = r

            // Crush
            for (p in c.dao.cGetAll()) c.people[p.key] = p
            c.liefde.addAll(c.people.filter { it.value.active() }.map { it.key })
            c.unsafe.addAll(c.people.filter { it.value.unsafe() }.map { it.key })
            if (CalendarManager.checkPerm(c)) CalendarManager.initialise(c)
            if (!c.sp.contains("dbLightBrownAdded")) {
                for (updated in c.people.values) {
                    val mask = Crush.BODY_EYE_COLOUR.first.inv()
                    val cleared = updated.body and mask
                    val pos = when ((updated.body and Crush.BODY_EYE_COLOUR.first)
                            shr Crush.BODY_EYE_COLOUR.second) {
                        0 -> 0
                        1 -> 1
                        2 -> 3
                        3 -> 5
                        4 -> 4
                        5 -> 6
                        else ->
                            if (BuildConfig.DEBUG)
                                throw IllegalStateException("Unrecognised eye colour!")
                            else continue
                    }
                    updated.body = cleared or (pos shl Crush.BODY_EYE_COLOUR.second)
                    c.dao.cUpdate(updated)
                }
                c.sp.edit().putBoolean("dbLightBrownAdded", true).apply()
            }

            // Place
            for (p in c.dao.pGetAll()) {
                c.places.add(p)
                var sum = 0L
                for (r in c.reports.valueIterator())
                    if (r.place == p.id)
                        sum++
                p.sum = sum
            }
            c.places.sortWith(Place.Sort(Place.Sort.NAME))
            if (c.reports.isNotEmpty()) c.places.sortWith(Place.Sort(Place.Sort.SUM))

            // Guess
            var grId = -1L
            for (g in c.dao.gGetAll()) {
                c.guesses.add(g)
                if (!g.checkValid()) continue
                var curTime = g.since
                val share = (86400000.0 / g.frequency).toLong()

                while (curTime <= g.until) {
                    c.reports[grId] =
                        Report(grId, curTime, g.name ?: "", g.type, g.place)
                    curTime += share
                    grId--
                }
            }
            c.guesses.sortWith(Guess.Sort())


            //Log.d("ZOEY", "Finished reading the database...")
            c.dbLoaded = true
            withContext(Dispatchers.Main) {

                // notify if any birthday is near
                if ((NumberUtils.now() - c.sp.getLong(Settings.spLastNotifiedBirthAt, 0L)
                            ) >= Settings.notifyBirthAfterLastTime &&
                    !c.sp.getBoolean(Settings.spPauseBirthdaysNtf, false)
                ) for (p in c.people.values) if (p.notifyBirth()) p.birthTime?.also { birthTime ->
                    val now: Calendar = GregorianCalendar()
                    val bir: Calendar = GregorianCalendar()
                    // do NOT alter the "birth" instance!
                    val dist = now.timeInMillis - bir.apply {
                        this.timeInMillis = birthTime
                        this[Calendar.YEAR] = now[Calendar.YEAR]
                    }.timeInMillis
                    if (dist in
                        -(c.sp.getInt(
                            Settings.spNotifyBirthDaysBefore, 3
                        ) * NumberUtils.A_DAY)
                        ..NumberUtils.A_DAY
                    ) notifyBirth(p, dist)
                }

                pageSex()?.apply { if (!listEverPrepared) prepareList() }
                // guesses must be instilled before doing this^
                pageLove()?.apply { // after restart()
                    if (!listEverPrepared) {
                        summarize(true)
                        prepareList()
                    }
                }

                //notifyBirth(c.people["Zoey"]!!, 1000)
            }

            if (c.sp.contains("do_not_show_google_play_removal"))
                c.sp.edit().remove("do_not_show_google_play_removal").apply()
            if (c.sp.contains("prefersOrgType"))
                c.sp.edit().remove("prefersOrgType").apply()
            if (c.sp.contains("dbDateTimeDotSlashReplaced"))
                c.sp.edit().remove("dbDateTimeDotSlashReplaced").apply()
        }

        // miscellaneous
        if (vm.navOpen) b.root.openDrawer(GravityCompat.START)
        intent.check(true)
        addOnNewIntentListener { it.check() }
    }

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
        } else if (item.itemId in arrayOf(R.id.momPop, R.id.momTst) && !summarize()) {
            uiToast(R.string.noStat); return true
        } else if (item.itemId in
            arrayOf(R.id.momPpl, R.id.momImport, R.id.momExport, R.id.momSend)
        )
            summarize()
        else if (item.itemId == R.id.momInt && c.reports.size() <= 1) {
            uiToast(R.string.noRecords); return true
        }

        when (item.itemId) {
            R.id.momSum -> SummaryDialog().show(supportFragmentManager, "summary")
            R.id.momRec -> RecencyDialog().show(supportFragmentManager, "recency")
            R.id.momPop -> goTo(Adorability::class)
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
            R.id.momCheckUpdates -> checkForUpdates()
        }
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        b.toolbar.inflateMenu(menus[vm.currentPage])
        b.toolbar.setOnMenuItemClickListener(this)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
            menu?.setGroupDividerEnabled(true)
        menu?.findItem(
            Crush.Sort.findSortMenuItemId(
                c.sp.getInt(Settings.spPageLoveSortBy, 0)
            )
        )?.isChecked = true
        menu?.findItem(
            if (c.sp.getBoolean(Settings.spPageLoveSortAsc, true))
                R.id.sortAsc else R.id.sortDsc
        )?.isChecked = true
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        when (item.itemId) {

            // PageSex (R.menu.page_sex):
            R.id.mtCrush -> b.pager.setCurrentItem(1, true)

            // PageLove (R.menu.page_love):
            R.id.chart -> if (c.liefde.isNotEmpty()) CrushesStat().apply {
                arguments = Bundle().apply { putInt(CrushesStat.BUNDLE_WHICH_LIST, 1) }
                show(supportFragmentManager, CrushesStat.TAG)
            }
            R.id.randomCrush -> if (c.liefde.isNotEmpty()) {
                Toast.makeText(c, c.liefde.random(), Toast.LENGTH_LONG).show()
                shake()
            }
            else -> Crush.Sort.sort(item.itemId)?.also { value ->
                item.isChecked = true
                c.sp.edit().apply {
                    if (value is Int) putInt(Settings.spPageLoveSortBy, value)
                    else if (value is Boolean) putBoolean(Settings.spPageLoveSortAsc, value)
                }.apply()
                pageLove()?.prepareList()
            } ?: return false
        }
        return true
    }

    private fun checkForUpdates() {
        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://mahdiparastesh.ir/misc/sexbook/")
            )
        )
    }

    @SuppressLint("MissingSuperCall")
    @Suppress("OVERRIDE_DEPRECATION")
    override fun onBackPressed() {
        if (b.root.isDrawerOpen(GravityCompat.START)) {
            b.root.closeDrawer(GravityCompat.START)
            return
        }
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


    private fun pageSex(): PageSex? = supportFragmentManager.findFragmentByTag(
        "f${b.pager.adapter?.getItemId(0)}"
    ) as? PageSex

    fun pageLove(): PageLove? = supportFragmentManager.findFragmentByTag(
        "f${b.pager.adapter?.getItemId(1)}"
    ) as? PageLove

    var intentViewId: Long? = null
    private fun Intent.check(isOnCreate: Boolean = false) {
        when (action) {
            Action.ADD.s -> pageSex()?.add()
            Action.VIEW.s -> (try {
                dataString?.toLong()
            } catch (_: NumberFormatException) {
                null
            })?.also { id ->
                if (!isOnCreate) pageSex()?.reset(id)
                else intentViewId = id
            }
        }
    }

    fun load(sd: Long = 1500, dur: Long = 500) {
        val value = -dm.widthPixels.toFloat() * 1.2f
        ObjectAnimator.ofFloat(b.load, View.TRANSLATION_X, value).apply {
            startDelay = sd
            duration = dur
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    b.body.removeView(b.load)
                    vm.loaded = true
                }
            })
            start()
        }
    }

    /**
     * Summarises sex records for statistics.
     *
     * This operation doesn't take so much time, generating the views for statistics takes that long!
     *
     * @param ignoreIfItsLessThanAMonth ignores returning false if date range of sex records doesn't
     *                                  extend one month
     * @return true if statisticisation was possible
     */
    fun summarize(ignoreIfItsLessThanAMonth: Boolean = false): Boolean {
        if (c.reports.isEmpty()) return false
        var nExcluded = 0
        var filtered: List<Report> = c.reports.toArrayList()

        // filter by time
        if (c.sp.getBoolean(Settings.spStatSinceCb, false))
            filtered = filtered.filter { it.time >= c.sp.getLong(Settings.spStatSince, 0) }
                .also { nExcluded += filtered.size - it.size }
        if (c.sp.getBoolean(Settings.spStatUntilCb, false))
            filtered = filtered.filter { it.time < c.sp.getLong(Settings.spStatUntil, 0) }
                .also { nExcluded += filtered.size - it.size }

        // check if it can draw any visual charts;
        // this is possible only if the range of the sex records exceeds one month.
        if (filtered.isEmpty()) return false
        else if (!ignoreIfItsLessThanAMonth && // if it's empty, minOf will throw NoSuchElementException!
            filtered.minOf { it.time }.calendar(c).createFilterYm().toString() ==
            filtered.maxOf { it.time }.calendar(c).createFilterYm().toString()
        ) return false

        // filter by type
        val allowedTypes = SexType.allowedOnes(c.sp)
        if (allowedTypes.size < SexType.count)
            filtered = filtered.filter { it.type in allowedTypes }
                .also { nExcluded += filtered.size - it.size }

        // filter non-orgasm sex records if enabled
        if (!c.sp.getBoolean(Settings.spStatNonOrgasm, true))
            filtered = filtered.filter { it.orgasmed }
                .also { nExcluded += filtered.size - it.size }

        c.summary = Summary(filtered, nExcluded, c.reports.size(), c.people.keys)
        (pageSex()?.b?.rv?.adapter as? ReportAdap)?.crushSuggester?.update()
        return true
    }

    private fun notifyBirth(crush: Crush, dist: Long) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val channelBirth = Sexbook::class.java.`package`!!.name + ".NOTIFY_BIRTHDAY"
        nm.createNotificationChannel(
            NotificationChannel(
                channelBirth, getString(R.string.bHappyChannel),
                if (crush.active()) NotificationManager.IMPORTANCE_HIGH
                else NotificationManager.IMPORTANCE_LOW
            )
        )
        val ntf = Notification.Builder(this@Main, channelBirth).apply {
            setSmallIcon(R.drawable.birthday)
            setColor(color(R.color.CPV_LIGHT))

            setContentTitle(getString(R.string.bHappyTitle, crush.visName()))
            val hours = abs(dist / 3600000L).toInt()
            setContentText(
                getString(
                    if (dist < 0L) R.string.bHappyBef
                    else R.string.bHappyAft,
                    resources.getQuantityString(
                        R.plurals.hour, hours, hours
                    ),
                    possessiveDeterminer(crush.gender())
                )
            )

            if (!crush.instagram.isNullOrBlank()) addAction(
                Notification.Action.Builder(
                    null, getString(R.string.instagram),
                    PendingIntent.getActivity(
                        c, 0,
                        Intent(Intent.ACTION_VIEW, (Crush.INSTA + crush.instagram).toUri()),
                        UiTools.ntfMutability(true)
                    )
                ).build()
            )
            addAction(
                Notification.Action.Builder(
                    null, getString(R.string.bHappyTurnOff),
                    PendingIntent.getBroadcast(
                        c, 0,
                        Intent(c, NotificationActions::class.java)
                            .setAction(NotificationActions.ACTION_TURN_OFF_BIRTHDAY_NOTIFICATION)
                            .putExtra(NotificationActions.EXTRA_CRUSH_KEY, crush.key),
                        UiTools.ntfMutability(true)
                    )
                ).build()
            )
        }.build()
        nm.notify(crush.key.length + crush.visName().length, ntf)
        c.sp.edit().putLong(Settings.spLastNotifiedBirthAt, NumberUtils.now()).apply()
    }

    fun onDataChanged() {
        c.resetData()
        vm.listFilter = -1
        recreate()
    }


    enum class Action(val s: String) {
        ADD("${Sexbook::class.java.`package`!!.name}.ACTION_ADD"),
        VIEW("${Sexbook::class.java.`package`!!.name}.ACTION_VIEW"),
    }
}
