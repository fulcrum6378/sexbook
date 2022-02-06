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
import android.text.SpannableString
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.GravityCompat
import androidx.core.view.forEach
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import ir.mahdiparastesh.sexbook.data.*
import ir.mahdiparastesh.sexbook.databinding.MainBinding
import ir.mahdiparastesh.sexbook.list.GuessAdap
import ir.mahdiparastesh.sexbook.list.ReportAdap
import ir.mahdiparastesh.sexbook.more.BaseActivity
import ir.mahdiparastesh.sexbook.more.CustomTypefaceSpan
import ir.mahdiparastesh.sexbook.stat.*
import kotlin.math.abs
import kotlin.system.exitProcess

class Main : BaseActivity(true) {
    private lateinit var b: MainBinding
    private lateinit var exporter: Exporter
    private lateinit var toggleNav: ActionBarDrawerToggle

    companion object {
        lateinit var handler: Handler
        const val NOTIFY_MAX_DISTANCE = 3
        val CHANNEL_BIRTH = Main::class.java.`package`!!.name + ".NOTIFY_BIRTHDAY"

        fun summarize(m: Model): Boolean = if (m.onani.value != null && m.onani.value!!.size > 0) {
            m.summary.value = Summary(m.onani.value!!.filter { it.isReal }); true
        } else false
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
        else if (night) b.loadIV.colorFilter = pdcf()

        // Navigation
        toggleNav = ActionBarDrawerToggle(
            this, b.root, b.toolbar, R.string.navOpen, R.string.navClose
        ).apply {
            b.root.addDrawerListener(this)
            isDrawerIndicatorEnabled = true
            syncState()
        }
        b.toolbar.navigationIcon?.colorFilter = pdcf()
        b.nav.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.momSum -> {
                    if (summarize(m)) AlertDialog.Builder(this).apply {
                        setTitle(
                            "${resources.getString(R.string.momSum)} (${m.onani.value!!.size})"
                        )
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
                    }.create().apply {
                        show()
                        fixADButton(getButton(AlertDialog.BUTTON_POSITIVE))
                    }; true; }
                R.id.momPop -> {
                    if (summarize(m))
                        startActivity(Intent(this, Popularity::class.java))
                    true; }
                R.id.momGrw -> {
                    if (summarize(m))
                        startActivity(Intent(this, Growth::class.java))
                    true; }
                R.id.momRec -> {
                    m.recency.value = Recency(m.summary.value!!)
                    if (summarize(m)) AlertDialog.Builder(this).apply {
                        setTitle(resources.getString(R.string.momRec))
                        setView(m.recency.value!!.draw(this@Main))
                        setPositiveButton(R.string.ok, null)
                        setCancelable(true)
                    }.create().apply {
                        show()
                        fixADButton(getButton(AlertDialog.BUTTON_POSITIVE))
                    };true; }
                R.id.momPlc -> {
                    startActivity(Intent(this, Places::class.java)); true; }
                R.id.momEst -> {
                    startActivity(Intent(this, Estimation::class.java)); true; }
                R.id.momImport -> exporter.launchImport()
                R.id.momExport -> exporter.launchExport()
                R.id.momSettings -> {
                    startActivity(Intent(this, Settings::class.java)); true; }
                else -> super.onOptionsItemSelected(it)
            }
        }
        //b.nav.addHeaderView()
        b.nav.menu.forEach {
            val mNewTitle = SpannableString(it.title)
            mNewTitle.setSpan(
                CustomTypefaceSpan("", font1, dm.density * 16f), 0,
                mNewTitle.length, SpannableString.SPAN_INCLUSIVE_INCLUSIVE
            )
            it.title = mNewTitle
        }
        exporter = Exporter(this)

        // Pager
        b.pager.adapter = PageAdapter(this)
        b.pager.setPageTransformer { _, pos ->
            b.transformer.layoutParams =
                (b.transformer.layoutParams as ConstraintLayout.LayoutParams).apply {
                    horizontalBias =
                        if (pos != 0f && pos != 1f) 0.75f - (pos / 2f)
                        else abs(b.pager.currentItem.toFloat() - 0.25f)
                    matchConstraintPercentWidth = 0.25f + ((0.5f - abs(0.5f - pos)) / 2f)
                }
        }

        // Observers
        m.onani.observe(this) { instilledGuesses = false }

        checkIntent(intent)
        // Work(c, Work.VIEW_ALL).start()
        Work(c, Work.C_VIEW_ALL).start()
        Work(c, Work.P_VIEW_ALL).start()
        Work(c, Work.G_VIEW_ALL).start()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent != null) checkIntent(intent)
    }

    var exiting = false
    override fun onBackPressed() { // Don't use super's already overriden function
        if (b.root.isDrawerOpen(GravityCompat.START)) {
            b.root.closeDrawer(GravityCompat.START); return; }
        if (!exiting) {
            exiting = true
            object : CountDownTimer(4000, 4000) {
                override fun onTick(p0: Long) {}
                override fun onFinish() {
                    exiting = false
                }
            }.start()
            Toast.makeText(c, R.string.toExit, Toast.LENGTH_SHORT).show()
            return
        }
        moveTaskToBack(true)
        Process.killProcess(Process.myPid())
        exitProcess(1)
    }


    fun checkIntent(intent: Intent) {
        when (intent.action) {
            Intent.ACTION_VIEW -> if (intent.data != null)
                Exporter.import(this, intent.data!!, true)
            Action.ADD.s -> PageSex.messages.add(Work.SPECIAL_ADD)
            Action.RELOAD.s -> {
                m.onani.value = null
                m.visOnani.value?.clear()
                m.liefde.value = null
                m.places.value = null
                m.guesses.value = null
                recreate()
            }
        }
    }

    fun load(sd: Long = 1500, dur: Long = 1000) {
        if (m.loaded) return
        val value = -dm.widthPixels.toFloat() * 1.2f
        ObjectAnimator.ofFloat(b.load, "translationX", value).apply {
            startDelay = sd
            duration = dur
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    b.body.removeView(b.load)
                    m.loaded = true
                }
            })
            start()
        }
    }

    private fun notifyBirth(crush: Crush, dist: Long) {
        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            .createNotificationChannel(
                NotificationChannel(
                    CHANNEL_BIRTH, "Birthday Notification", NotificationManager.IMPORTANCE_HIGH
                ).apply { description = "Birthday Notification" })
        with(NotificationManagerCompat.from(this)) {
            notify(666, NotificationCompat.Builder(this@Main, CHANNEL_BIRTH).apply {
                setSmallIcon(R.mipmap.launcher_round)
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

    var instilledGuesses = false
    fun instillGuesses() {
        if (m.onani.value == null || m.guesses.value == null || instilledGuesses) return
        for (g in m.guesses.value!!) {
            if (!g.checkValid()) continue
            var time = g.sinc
            val share = (86400000.0 / g.freq).toLong()

            while (time <= g.till) {
                m.onani.value!!.add(Report(time, "[ESTIMATED]", g.type, g.plac))
                time += share
            }
        }
        m.onani.value!!.sortWith(ReportAdap.Sort())
        instilledGuesses = true
    }


    private inner class SumAdapter(val c: Main) : FragmentStateAdapter(c) {
        override fun getItemCount(): Int = 3

        override fun createFragment(i: Int): Fragment = when (i) {
            1 -> SumCloud()
            2 -> SumPie()
            else -> SumChips(c)
        }
    }

    private inner class PageAdapter(val that: Main) : FragmentStateAdapter(that) {
        override fun getItemCount(): Int = 2

        override fun createFragment(i: Int): Fragment =
            if (i == 0) PageSex(that) else PageLove(that)
    }

    private inner class PageFactory : FragmentFactory() {
        override fun instantiate(classLoader: ClassLoader, className: String): Fragment =
            when (className) {
                PageSex::class.java.name -> PageSex(this@Main)
                PageLove::class.java.name -> PageLove(this@Main)
                SumChips::class.java.name -> PageLove(this@Main)
                else -> super.instantiate(classLoader, className)
            }
    }

    enum class Action(val s: String) {
        RELOAD("ir.mahdiparastesh.sexbook.ACTION.RELOAD"),
        ADD("ir.mahdiparastesh.sexbook.ACTION.ADD")
    }
}
