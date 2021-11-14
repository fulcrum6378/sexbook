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
import android.widget.AdapterView.OnItemSelectedListener
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import ir.mahdiparastesh.sexbook.Fun.Companion.c
import ir.mahdiparastesh.sexbook.Fun.Companion.color
import ir.mahdiparastesh.sexbook.Fun.Companion.dm
import ir.mahdiparastesh.sexbook.Fun.Companion.dp
import ir.mahdiparastesh.sexbook.Fun.Companion.explode
import ir.mahdiparastesh.sexbook.Fun.Companion.now
import ir.mahdiparastesh.sexbook.adap.ReportAdap
import ir.mahdiparastesh.sexbook.adap.ReportAdap.Companion.Sort
import ir.mahdiparastesh.sexbook.data.Exporter
import ir.mahdiparastesh.sexbook.data.Filter
import ir.mahdiparastesh.sexbook.data.Report
import ir.mahdiparastesh.sexbook.data.Work
import ir.mahdiparastesh.sexbook.databinding.MainBinding
import ir.mahdiparastesh.sexbook.more.Jalali
import ir.mahdiparastesh.sexbook.stat.Popularity
import ir.mahdiparastesh.sexbook.stat.Recency
import ir.mahdiparastesh.sexbook.stat.Singular
import ir.mahdiparastesh.sexbook.stat.Sum
import java.util.*
import kotlin.collections.ArrayList
import kotlin.system.exitProcess

// adb connect 192.168.1.20:

@Suppress("UNCHECKED_CAST")
class Main : AppCompatActivity() {
    private lateinit var b: MainBinding
    private lateinit var m: Model
    private lateinit var tbTitle: TextView
    private lateinit var exporter: Exporter
    var adapter: ReportAdap? = null
    var adding = false

    companion object {
        lateinit var handler: Handler

        const val workActionTimeout = 5000L
        var dateFont: Typeface? = null
        var reports = ArrayList<Report>()
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
                    Work.INSERT_ONE -> if (msg.obj != null)
                        Work(
                            Work.VIEW_ONE, listOf(msg.obj as Long, Work.ADD_NEW_ITEM)
                        ).start()
                    Work.REPLACE_ALL -> {
                        Toast.makeText(c, R.string.importDone, Toast.LENGTH_LONG).show()
                        Work(Work.VIEW_ALL).start()
                    }
                    Work.UPDATE_ONE -> {
                        if (m.onani.value != null) {
                            if (reports.contains(m.onani.value!![msg.arg1])) {
                                val nominalPos = reports.indexOf(m.onani.value!![msg.arg1])
                                reports[nominalPos] = m.onani.value!![msg.arg1]
                                if (msg.arg2 == 0) adapter?.notifyItemChanged(nominalPos)
                            }
                            if (msg.arg2 == 0) resetAllMasturbations()
                        }
                    }
                    Work.DELETE_ONE -> if (m.onani.value != null) {
                        if (reports.contains(m.onani.value!![msg.arg1])) {
                            val nominalPos = reports.indexOf(m.onani.value!![msg.arg1])
                            reports.remove(m.onani.value!![msg.arg1])
                            m.onani.value!!.remove(m.onani.value!![msg.arg1])
                            filters?.let {
                                if (it.size > listFilter) it[listFilter].items =
                                    it[listFilter].items.apply { remove(msg.arg1) }
                            }
                            adapter?.notifyItemRemoved(nominalPos)
                        }
                        resetAllMasturbations()
                    }
                    Work.SCROLL -> b.rv.smoothScrollBy(0, msg.obj as Int)
                }
            }
        }

        // List
        b.add.setOnClickListener {
            if (adding) return@setOnClickListener
            if (filters != null) filterList(filters!!.size - 1)
            adding = true
            Work(Work.INSERT_ONE, listOf(Report(now(), "", 1, ""))).start()
            object : CountDownTimer(workActionTimeout, workActionTimeout) {
                override fun onTick(p0: Long) {}
                override fun onFinish() {
                    adding = false; }
            }
        }
        Work(Work.VIEW_ALL).start()

        // Lists' Filtering
        b.spnFilterMark.setColorFilter(color(R.color.spnFilterMark))
        b.spnFilter.setOnTouchListener { _, _ -> spnFilterTouched = true; false }
        b.spnFilter.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onNothingSelected(av: AdapterView<*>?) {}
            override fun onItemSelected(av: AdapterView<*>?, v: View?, i: Int, l: Long) {
                if (spnFilterTouched) filterList(i)
            }
        }

        // Night Mode
        if (Fun.night) Fun.pdcf(c).apply {
            b.loadIV.colorFilter = this
            b.addIV.colorFilter = this
        }

        // Already loaded
        if (m.loaded.value!!) b.body.removeView(b.load)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        recreate()
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
        moveTaskToBack(true)
        Process.killProcess(Process.myPid())
        exitProcess(1)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.toolbar, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
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
        else -> super.onOptionsItemSelected(item)
    }


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

    var spnFilterTouched = false
    var filteredOnce = false
    fun resetAllMasturbations() {
        Collections.sort(m.onani.value!!, Sort())
        filters = filter(m.onani.value!!)
        val maxPage = filters!!.size - 1
        filterList(
            if (filters!!.size > 0 && (!filteredOnce || listFilter > maxPage))
                maxPage else listFilter
        )
        if (filters != null) {
            val titles = ArrayList<String>().apply {
                for (f in filters!!.indices) add(filters!![f].title(c))
            }
            b.spnFilter.adapter = ArrayAdapter(c, R.layout.spinner_1, titles)
                .apply { setDropDownViewResource(R.layout.spinner_1_dd) }
            spnFilterTouched = false
            b.spnFilter.setSelection(listFilter, true)
        }
        filteredOnce = true
    }

    fun filter(reports: ArrayList<Report>): ArrayList<Filter> {
        val filters: ArrayList<Filter> = ArrayList()
        for (r in reports.indices) {
            val lm = Calendar.getInstance().apply { timeInMillis = reports[r].time }
            var ym = arrayOf(lm[Calendar.YEAR], lm[Calendar.MONTH])
            if (Fun.calType() == Fun.CalendarType.JALALI)
                Jalali(lm).apply { ym = arrayOf(Y, M) }
            var filterExists = false
            for (f in filters.indices) if (filters[f].year == ym[0] && filters[f].month == ym[1]) {
                filterExists = true
                filters[f] = filters[f].apply { put(r) }
            }
            if (!filterExists)
                filters.add(Filter(ym[0], ym[1], ArrayList<Int>().apply { add(r) }))
        }
        return filters
    }

    fun filterList(i: Int = listFilter) {
        if (m.onani.value == null) {
            reports.clear(); return; }
        listFilter = i
        reports.clear()
        if (filters == null) for (o in m.onani.value!!) reports.add(o)
        else if (!filters.isNullOrEmpty())
            for (o in filters!![listFilter].items)
                if (m.onani.value!!.size > o)
                    reports.add(m.onani.value!![o])
        if (adapter == null) {
            summarize()
            adapter = ReportAdap(reports, this, m)
            b.rv.adapter = adapter
        } else adapter!!.notifyDataSetChanged()
    }

    fun summarize(): Boolean = if (m.onani.value != null && m.onani.value!!.size > 0) {
        m.summary.value = Sum(m.onani.value!!); true
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
}
