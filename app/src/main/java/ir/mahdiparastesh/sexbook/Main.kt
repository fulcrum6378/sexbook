package ir.mahdiparastesh.sexbook

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Intent
import android.graphics.Typeface
import android.os.*
import android.view.*
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import ir.mahdiparastesh.sexbook.Fun.Companion.c
import ir.mahdiparastesh.sexbook.Fun.Companion.dm
import ir.mahdiparastesh.sexbook.Fun.Companion.pdcf
import ir.mahdiparastesh.sexbook.data.Crush
import ir.mahdiparastesh.sexbook.data.Exporter
import ir.mahdiparastesh.sexbook.data.Report
import ir.mahdiparastesh.sexbook.data.Work
import ir.mahdiparastesh.sexbook.databinding.MainBinding
import ir.mahdiparastesh.sexbook.stat.*
import java.util.*
import kotlin.system.exitProcess

// adb connect 192.168.1.20:

class Main : AppCompatActivity() {
    private lateinit var b: MainBinding
    private lateinit var m: Model
    private lateinit var tbTitle: TextView
    private lateinit var exporter: Exporter
    private lateinit var toggleNav: ActionBarDrawerToggle

    companion object {
        lateinit var handler: Handler
        var dateFont: Typeface? = null

        fun summarize(m: Model): Boolean = if (m.onani.value != null && m.onani.value!!.size > 0) {
            m.summary.value = Summary(m.onani.value!!); true
        } else false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        supportFragmentManager.fragmentFactory = PageFactory()
        super.onCreate(savedInstanceState)
        b = MainBinding.inflate(layoutInflater)
        m = ViewModelProvider(this, Model.Factory()).get("Model", Model::class.java)
        setContentView(b.root)
        Fun.init(this)
        b.pager.adapter = PageAdapter(this)
        exporter = Exporter(this)


        // Handler
        handler = object : Handler(Looper.getMainLooper()) {
            @Suppress("UNCHECKED_CAST")
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    Work.VIEW_ALL -> m.onani.value = msg.obj as ArrayList<Report>
                    Work.C_VIEW_ALL -> m.liefde.value = (msg.obj as ArrayList<Crush>)
                        .apply { sortWith(Crush.Sort()) }
                }
            }
        }

        // Loading
        if (m.loaded.value!!) b.body.removeView(b.load) // its direct parent not "b.root"
        else if (Fun.night) pdcf().apply { b.loadIV.colorFilter = this }

        // Toolbar
        setSupportActionBar(b.toolbar)
        for (g in 0 until b.toolbar.childCount) {
            var getTitle = b.toolbar.getChildAt(g)
            if (getTitle is TextView &&
                getTitle.text.toString() == resources.getString(R.string.app_name)
            ) tbTitle = getTitle
        }
        if (::tbTitle.isInitialized) tbTitle.setTypeface(dateFont, Typeface.BOLD)

        // Navigation
        toggleNav = ActionBarDrawerToggle(
            this, b.root, b.toolbar, R.string.navOpen, R.string.navClose
        ).apply {
            b.root.addDrawerListener(this)
            isDrawerIndicatorEnabled = true
            syncState()
        }
        b.toolbar.navigationIcon?.apply { colorFilter = pdcf() }
        b.nav.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.momSum -> {
                    if (summarize(m)) AlertDialog.Builder(this).apply {
                        setTitle("${resources.getString(R.string.momSum)} (" + m.onani.value!!.size + ")")
                        setView(ViewPager2(c).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            adapter = SumAdapter(this@Main)
                        })
                        setPositiveButton(R.string.ok, null)
                        setCancelable(true)
                    }.create().apply {
                        show()
                        Fun.fixADButton(getButton(AlertDialog.BUTTON_POSITIVE))
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
                        setView(m.recency.value!!.draw(layoutInflater))
                        setPositiveButton(R.string.ok, null)
                        setCancelable(true)
                    }.create().apply {
                        show()
                        Fun.fixADButton(getButton(AlertDialog.BUTTON_POSITIVE))
                    };true; }
                R.id.momImport -> exporter.import()
                R.id.momExport -> exporter.export(m.onani.value, m.liefde.value)
                R.id.momExportExcel -> {
                    // TODO
                    true
                }
                R.id.momSettings -> {
                    startActivity(Intent(this, Settings::class.java)); true; }
                else -> super.onOptionsItemSelected(it)
            }
        }

        Work(Work.VIEW_ALL).start()
        Work(Work.C_VIEW_ALL).start()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        recreate()
    }

    var exiting = false
    override fun onBackPressed() {
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

    /*override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.toolbar, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) =*/


    fun load(sd: Long = 1500, dur: Long = 1000) {
        if (m.loaded.value!!) return
        val value = -dm.widthPixels.toFloat() * 1.2f
        ObjectAnimator.ofFloat(b.load, "translationX", value).apply {
            startDelay = sd
            duration = dur
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    b.body.removeView(b.load)
                    m.loaded.value = true
                }
            })
            start()
        }
    }


    private inner class SumAdapter(that: Main) : FragmentStateAdapter(that) {
        override fun getItemCount(): Int = 3

        override fun createFragment(i: Int): Fragment = when (i) {
            1 -> SumCloud()
            2 -> SumPie()
            else -> SumChips()
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
                else -> super.instantiate(classLoader, className)
            }
    }
}
