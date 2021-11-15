package ir.mahdiparastesh.sexbook

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Typeface
import android.os.*
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import ir.mahdiparastesh.sexbook.Fun.Companion.c
import ir.mahdiparastesh.sexbook.Fun.Companion.color
import ir.mahdiparastesh.sexbook.Fun.Companion.dm
import ir.mahdiparastesh.sexbook.Fun.Companion.dp
import ir.mahdiparastesh.sexbook.Fun.Companion.pdcf
import ir.mahdiparastesh.sexbook.data.Exporter
import ir.mahdiparastesh.sexbook.databinding.MainBinding
import ir.mahdiparastesh.sexbook.stat.Popularity
import ir.mahdiparastesh.sexbook.stat.Recency
import ir.mahdiparastesh.sexbook.stat.Singular
import ir.mahdiparastesh.sexbook.stat.Summary
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
        var dateFont: Typeface? = null
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
                    if (summarize()) AlertDialog.Builder(this).apply {
                        setTitle("${resources.getString(R.string.momSum)} (" + m.onani.value!!.size + ")")
                        setView(sumLayout())
                        setPositiveButton(R.string.ok, null)
                        setCancelable(true)
                    }.create().apply {
                        show()
                        Fun.fixADButton(getButton(AlertDialog.BUTTON_POSITIVE))
                    }; true; }
                R.id.momPop -> {
                    if (summarize())
                        startActivity(Intent(this, Popularity::class.java))
                    true; }
                R.id.momRec -> {
                    m.recency.value = Recency(m.summary.value!!)
                    if (summarize()) AlertDialog.Builder(this).apply {
                        setTitle(resources.getString(R.string.momRec))
                        setView(m.recency.value!!.draw(layoutInflater))
                        setPositiveButton(R.string.ok, null)
                        setCancelable(true)
                    }.create().apply {
                        show()
                        Fun.fixADButton(getButton(AlertDialog.BUTTON_POSITIVE))
                    };true; }
                R.id.momImport -> exporter.import()
                R.id.momExport -> exporter.export(m.onani.value)
                R.id.momExportExcel -> {
                    // TODO
                    true
                }
                R.id.momSettings -> {
                    startActivity(Intent(this, Settings::class.java)); true; }
                else -> super.onOptionsItemSelected(it)
            }
        }
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

    fun summarize(): Boolean = if (m.onani.value != null && m.onani.value!!.size > 0) {
        m.summary.value = Summary(m.onani.value!!); true
    } else false

    @SuppressLint("InflateParams")
    fun sumLayout() = (layoutInflater.inflate(R.layout.sum, null) as ScrollView).apply {
        val ll = this[0] as LinearLayout
        (ll[0] as EditText).apply {
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, r: Int, c: Int, a: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    val ss = s.toString()
                    for (i in 1 until ll.childCount) {
                        val cg = ll[i] as ChipGroup
                        for (y in 1 until cg.childCount) (cg[y] as Chip).apply {
                            chipBackgroundColor = getColorStateList(
                                if (ss != "" && text.toString().contains(ss, true))
                                    R.color.chip_search else R.color.chip_normal
                            )
                        }
                    }
                }
            })
        }
        for (r in m.summary.value!!.results().calculations) ll.addView(
            ChipGroup(ContextThemeWrapper(c, R.style.AppTheme), null, 0).apply {
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
                )
                addView(TextView(c).apply {
                    layoutParams = ChipGroup.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    setPadding(0, dp(12), 0, 0)
                    text = (if (r.key % 1 > 0) r.key.toString()
                    else r.key.toInt().toString()).plus(": ")
                    setTextColor(color(R.color.recency))
                    textSize = dm.density * 5
                })
                for (crush in r.value) addView(
                    Chip(ContextThemeWrapper(c, R.style.AppTheme), null, 0).apply {
                        layoutParams = ChipGroup.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                        text = crush
                        setTextColor(color(R.color.chipText))
                        chipBackgroundColor = getColorStateList(R.color.chip_normal)
                        setOnClickListener {
                            m.crush.value = crush
                            startActivity(Intent(this@Main, Singular::class.java))
                        }
                    })
            })
        ll.addView(TextView(c).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setPadding(0, dp(7), 0, 0)
            text = getString(R.string.unknown, m.summary.value!!.unknown.toString())
            setTextColor(color(R.color.searchHint))
        })
    }


    private inner class PageAdapter(val that: Main) :
        FragmentStateAdapter(that) {
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
