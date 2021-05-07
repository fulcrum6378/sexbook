package com.mahdiparastesh.mbcounter

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Typeface
import android.os.*
import android.view.*
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.get
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.mahdiparastesh.mbcounter.Fun.Companion.c
import com.mahdiparastesh.mbcounter.Fun.Companion.color
import com.mahdiparastesh.mbcounter.Fun.Companion.dm
import com.mahdiparastesh.mbcounter.Fun.Companion.dp
import com.mahdiparastesh.mbcounter.Fun.Companion.exit
import com.mahdiparastesh.mbcounter.Fun.Companion.explode
import com.mahdiparastesh.mbcounter.Fun.Companion.now
import com.mahdiparastesh.mbcounter.Fun.Companion.z
import com.mahdiparastesh.mbcounter.Summary.Recency
import com.mahdiparastesh.mbcounter.adap.ReportAdap
import com.mahdiparastesh.mbcounter.adap.ReportAdap.Companion.Sort
import com.mahdiparastesh.mbcounter.data.Exporter
import com.mahdiparastesh.mbcounter.data.Filter
import com.mahdiparastesh.mbcounter.data.Report
import com.mahdiparastesh.mbcounter.data.Work
import com.mahdiparastesh.mbcounter.databinding.MainBinding
import com.mahdiparastesh.mbcounter.more.SolarHijri
import java.util.*
import kotlin.collections.ArrayList

// adb connect 192.168.1.3:

@Suppress("UNCHECKED_CAST")
class Main : AppCompatActivity() {
    private lateinit var b: MainBinding
    private lateinit var tbTitle: TextView
    private lateinit var m: Model
    private lateinit var exporter: Exporter
    var adapter: ReportAdap? = null
    var adding = false

    companion object {
        lateinit var handler: Handler

        const val workActionTimeout: Long = 5000
        var dateFont: Typeface? = null
        var masturbation = ArrayList<Report>()
        var saveOnBlur = false
        var scrollOnFocus = false
        var filters: ArrayList<Filter>? = null
        var listFilter = 0
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = MainBinding.inflate(layoutInflater)
        m = ViewModelProvider(this, Model.Factory()).get("Model", Model::class.java)
        setContentView(b.root)
        Fun.init(this)
        exporter = Exporter(this)

        // Toolbar
        setSupportActionBar(b.toolbar)
        for (g in 0 until b.toolbar.childCount) {
            var getTitle = b.toolbar.getChildAt(g)
            if (getTitle is TextView &&
                getTitle.text.toString() == resources.getString(R.string.app_name)
            ) tbTitle = getTitle
        }
        if (::tbTitle.isInitialized) tbTitle.setTypeface(dateFont, Typeface.BOLD)

        // Handler
        val that = this
        handler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    Work.VIEW_ALL -> if (msg.obj != null) {
                        m.onani.value = msg.obj as ArrayList<Report>
                        resetAllMasturbations()
                        load()
                    }
                    Work.VIEW_ONE -> if (msg.obj != null) when (msg.arg1) {
                        Work.ADD_NEW_ITEM -> {
                            if (m.onani.value == null) m.onani.value = ArrayList()
                            m.onani.value!!.add(msg.obj as Report)
                            resetAllMasturbations()
                            adding = false
                            explode(c, b.add)
                        }
                    }
                    Work.INSERT_ONE -> if (msg.obj != null) Work(
                        c, handler, Work.VIEW_ONE, listOf(msg.obj as Long, Work.ADD_NEW_ITEM)
                    ).start()
                    Work.REPLACE_ALL ->
                        Toast.makeText(c, R.string.importDone, Toast.LENGTH_LONG).show()
                    Work.UPDATE_ONE -> {
                        if (m.onani.value != null) {
                            if (masturbation.contains(m.onani.value!![msg.arg1])) {
                                val nominalPos = masturbation.indexOf(m.onani.value!![msg.arg1])
                                masturbation[nominalPos] = m.onani.value!![msg.arg1]
                                adapter?.notifyItemChanged(nominalPos)
                            }
                            if (msg.arg2 == 2) resetAllMasturbations()
                        }
                        if (msg.arg2 == 1) exit(that)
                    }
                    Work.DELETE_ONE -> if (m.onani.value != null) {
                        if (masturbation.contains(m.onani.value!![msg.arg1])) {
                            val nominalPos = masturbation.indexOf(m.onani.value!![msg.arg1])
                            masturbation.remove(m.onani.value!![msg.arg1])
                            m.onani.value!!.remove(m.onani.value!![msg.arg1])
                            filters?.let {
                                if (it.size > listFilter) it[listFilter].items =
                                    it[listFilter].items.apply { remove(msg.arg1) }
                            }
                            adapter?.notifyItemRemoved(nominalPos)
                        }
                    }
                    Work.SCROLL -> b.rv.smoothScrollBy(0, msg.obj as Int)
                }
            }
        }

        // List
        var span = 1
        if (dm.widthPixels > 0) span = ((dm.widthPixels / dm.density) / 190f).toInt()
        if (span < 1) span = 1
        b.rv.layoutManager = StaggeredGridLayoutManager(span, StaggeredGridLayoutManager.VERTICAL)
        b.add.setOnClickListener {
            if (adding) return@setOnClickListener
            if (filters != null) filterList(filters!!.size - 1)
            adding = true
            Work(c, handler, Work.INSERT_ONE, listOf(Report(now(), ""))).start()
            object : CountDownTimer(workActionTimeout, workActionTimeout) {
                override fun onTick(p0: Long) {}
                override fun onFinish() {
                    adding = false; }
            }
        }
        Work(c, handler, Work.VIEW_ALL).start()

        // Lists' Filtering
        b.spnFilterMark.setColorFilter(color(R.color.spnFilterMark))
        b.spnFilter.setOnTouchListener { _, _ -> spnFilterTouched = true; false }
        b.spnFilter.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onNothingSelected(adapterView: AdapterView<*>?) {}
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View, i: Int, l: Long) {
                if (spnFilterTouched) filterList(i)
            }
        }

        // Night Mode
        if (Fun.night) Fun.pdcf(c).apply {
            b.loadIV.colorFilter = this
            b.addIV.colorFilter = this
        }
    }

    override fun onPause() {
        super.onPause()
        saveFocused()
    }

    var exiting = false
    override fun onBackPressed() {
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
        if (!saveFocused()) exit(this)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar, menu)
        return super.onCreateOptionsMenu(menu)// DON"T PUT HERE THINGS THAT NEED THE LAYOUT LOADED.
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.momRec -> {
            if (summarize()) AlertDialog.Builder(this).apply {
                setTitle(resources.getString(R.string.momRec))
                setView(recLayout())
                setPositiveButton(R.string.ok, null)
                setCancelable(true)
            }.create().apply {
                show()
                Fun.fixADButton(getButton(AlertDialog.BUTTON_POSITIVE))
            };true; }
        R.id.momSum -> {
            if (summarize()) AlertDialog.Builder(this).apply {
                setTitle("${resources.getString(R.string.momSum)} (" + m.onani.value!!.size + ")")
                setView(sumLayout())
                setPositiveButton(R.string.ok, null)
                setCancelable(true)
            }.create().apply {
                show()
                Fun.fixADButton(getButton(AlertDialog.BUTTON_POSITIVE))
            };true; }
        R.id.momImport -> exporter.import()
        R.id.momExport -> exporter.export(m.onani.value)
        else -> super.onOptionsItemSelected(item)
    }


    var loaded = false
    fun load(sd: Long = 1500, dur: Long = 1000) {
        if (loaded) return
        val value = -dm.widthPixels.toFloat() * 1.2f
        ObjectAnimator.ofFloat(b.load, "translationX", value).apply {
            startDelay = sd
            duration = dur
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    b.body.removeView(b.load)
                }
            })
            start()
        }
    }

    fun saveFocused(): Boolean {
        var isFocused = false
        for (f in 0 until b.rv.childCount) {
            var i = b.rv.getChildAt(f) as ViewGroup
            var et = i.getChildAt(ReportAdap.notesPos) as EditText
            if (et.hasFocus()) {
                ReportAdap.saveET(
                    c,
                    et,
                    ReportAdap.allPos(masturbation, b.rv.getChildLayoutPosition(i), m.onani.value),
                    m.onani.value,
                    true
                )
                isFocused = true
            }
        }
        return isFocused
    }

    var spnFilterTouched = false
    fun resetAllMasturbations() {
        Collections.sort(m.onani.value!!, Sort())
        filters = filter(m.onani.value!!)
        filterList(filters!!.size - 1)
        if (filters != null) {
            val titles = ArrayList<String>().apply {
                for (f in filters!!.indices) add(filters!![f].titleInShamsi(c))
            }
            b.spnFilter.adapter = ArrayAdapter(c, R.layout.spinner_1, titles)
                .apply { setDropDownViewResource(R.layout.spinner_1_dd) }
            spnFilterTouched = false
            listFilter = filters!!.size - 1
            b.spnFilter.setSelection(listFilter, true)
        }
    }

    fun filter(reports: ArrayList<Report>): ArrayList<Filter> {
        val filters: ArrayList<Filter> = ArrayList()
        for (r in reports.indices) {
            val sh = SolarHijri(Calendar.getInstance().apply { timeInMillis = reports[r].time })
            var filterExists = false
            for (f in filters.indices) if (filters[f].year == sh.Y && filters[f].month == sh.M) {
                filterExists = true
                filters[f] = filters[f].apply { put(r) }
            }
            if (!filterExists)
                filters.add(Filter(sh.Y, sh.M, ArrayList<Int>().apply { add(r) }))
        }
        return filters
    }

    fun filterList(i: Int) {
        if (m.onani.value == null) {
            masturbation.clear(); return; }
        listFilter = i
        masturbation.clear()
        if (filters == null) for (o in m.onani.value!!) masturbation.add(o)
        else if (!filters.isNullOrEmpty())
            for (o in filters!![listFilter].items) masturbation.add(m.onani.value!![o])
        saveOnBlur = false
        scrollOnFocus = false
        if (adapter == null) {
            adapter = ReportAdap(c, masturbation, this, m.onani.value)
            b.rv.adapter = adapter
        } else adapter!!.notifyDataSetChanged()
    }

    fun summarize(): Boolean = if (m.onani.value != null && m.onani.value!!.size > 0) {
        m.summary.value = Summary(m.onani.value!!); true
    } else false

    @SuppressLint("InflateParams")
    fun sumLayout() = (layoutInflater.inflate(R.layout.summary, null) as ScrollView).apply {
        for (r in m.summary.value!!.results().calculations) (this[0] as LinearLayout).addView(
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
                    setTextColor(color(R.color.mrvPopupButtons))
                })
                for (crush in r.value) addView(
                    Chip(ContextThemeWrapper(c, R.style.AppTheme), null, 0).apply {
                        layoutParams = ChipGroup.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                        text = crush
                        setOnClickListener {
                            m.crush.value = crush
                            startActivity(Intent(this@Main, Statistics::class.java))
                        }
                    })
            })
    }

    @SuppressLint("InflateParams", "SetTextI18n")
    fun recLayout() = (layoutInflater.inflate(R.layout.summary, null) as ScrollView).apply {
        val recency = ArrayList<Recency>()
        m.summary.value!!.scores.forEach { (name, erections) -> // API 24+: WITHOUT PARENTHESES
            var mostRecent = 0L
            for (e in erections) if (e.time > mostRecent) mostRecent = e.time
            recency.add(Recency(name, mostRecent))
        }
        recency.sortBy { it.time }
        recency.reverse()
        for (r in 0 until recency.size) (this[0] as LinearLayout).addView(
            (layoutInflater.inflate(R.layout.recency, null) as ConstraintLayout).apply {
                (this[0] as TextView).text = "${r + 1}. ${recency[r].name}"
                val gre = Calendar.getInstance().apply { timeInMillis = recency[r].time }
                val jal = SolarHijri(gre)
                (this[1] as TextView).text =
                    "${z(jal.Y)}.${z(jal.M)}.${z(jal.D)} - " +
                            "${z(gre[Calendar.HOUR_OF_DAY])}:${z(gre[Calendar.MINUTE])}"
                if (r == recency.size - 1) this.removeViewAt(2)
            }
        )
    }
}
