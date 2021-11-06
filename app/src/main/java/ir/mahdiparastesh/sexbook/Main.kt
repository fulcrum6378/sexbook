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
import androidx.constraintlayout.widget.ConstraintLayout
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
import ir.mahdiparastesh.sexbook.Fun.Companion.z
import ir.mahdiparastesh.sexbook.adap.ReportAdap
import ir.mahdiparastesh.sexbook.adap.ReportAdap.Companion.Sort
import ir.mahdiparastesh.sexbook.data.Exporter
import ir.mahdiparastesh.sexbook.data.Filter
import ir.mahdiparastesh.sexbook.data.Report
import ir.mahdiparastesh.sexbook.data.Work
import ir.mahdiparastesh.sexbook.databinding.MainBinding
import ir.mahdiparastesh.sexbook.more.Jalali
import ir.mahdiparastesh.sexbook.stat.Popularity
import ir.mahdiparastesh.sexbook.stat.Singular
import ir.mahdiparastesh.sexbook.stat.Sum
import ir.mahdiparastesh.sexbook.stat.Sum.Recency
import java.util.*
import kotlin.collections.ArrayList
import kotlin.system.exitProcess

// adb connect 192.168.1.20:

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

        const val workActionTimeout = 5000L
        var dateFont: Typeface? = null
        var masturbation = ArrayList<Report>()
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
                    Work.INSERT_ONE -> if (msg.obj != null) Work(
                        c, handler, Work.VIEW_ONE, listOf(msg.obj as Long, Work.ADD_NEW_ITEM)
                    ).start()
                    Work.REPLACE_ALL -> {
                        Toast.makeText(c, R.string.importDone, Toast.LENGTH_LONG).show()
                        Work(c, handler, Work.VIEW_ALL).start()
                    }
                    Work.UPDATE_ONE -> {
                        if (m.onani.value != null) {
                            if (masturbation.contains(m.onani.value!![msg.arg1])) {
                                val nominalPos = masturbation.indexOf(m.onani.value!![msg.arg1])
                                masturbation[nominalPos] = m.onani.value!![msg.arg1]
                                if (msg.arg2 == 0) adapter?.notifyItemChanged(nominalPos)
                            }
                            if (msg.arg2 == 0) resetAllMasturbations()
                        }
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
            Work(c, handler, Work.INSERT_ONE, listOf(Report(now(), "", 1, ""))).start()
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

        // Already loaded
        if (m.loaded.value!!) b.body.removeView(b.load)
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
            if (summarize()) AlertDialog.Builder(this).apply {
                setTitle(resources.getString(R.string.momRec))
                setView(recLayout())
                setPositiveButton(R.string.ok, null)
                setCancelable(true)
            }.create().apply {
                show()
                Fun.fixADButton(getButton(AlertDialog.BUTTON_POSITIVE))
            };true; }
        R.id.momImport -> exporter.import()
        R.id.momExport -> exporter.export(m.onani.value)
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
            val sh = Jalali(Calendar.getInstance().apply { timeInMillis = reports[r].time })
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
            for (o in filters!![listFilter].items)
                if (m.onani.value!!.size > o)
                    masturbation.add(m.onani.value!![o])
        if (adapter == null) {
            adapter = ReportAdap(c, masturbation, this, m.onani.value)
            b.rv.adapter = adapter
        } else adapter!!.notifyDataSetChanged()
    }

    fun summarize(): Boolean = if (m.onani.value != null && m.onani.value!!.size > 0) {
        m.summary.value = Sum(m.onani.value!!); true
    } else false

    @SuppressLint("InflateParams")
    fun sumLayout() = (layoutInflater.inflate(R.layout.sum, null) as ScrollView).apply {
        val ll = this[0] as LinearLayout
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) (ll[0] as EditText).apply {
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?, start: Int, count: Int, after: Int
                ) {
                }

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
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
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
        })
    }

    @SuppressLint("InflateParams", "SetTextI18n")
    fun recLayout() = (layoutInflater.inflate(R.layout.sum, null) as ScrollView).apply {
        val recency = ArrayList<Recency>()
        m.summary.value!!.scores.forEach { (name, erections) -> // API 24+: WITHOUT PARENTHESES
            var mostRecent = 0L
            for (e in erections) if (e.time > mostRecent) mostRecent = e.time
            recency.add(Recency(name, mostRecent))
        }
        recency.sortBy { it.time }
        recency.reverse()

        val ll = this[0] as LinearLayout
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) (ll[0] as EditText).apply {
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?, start: Int, count: Int, after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    val ss = s.toString()
                    for (i in 1 until ll.childCount) (ll[i] as ConstraintLayout).apply {
                        val tv = this[0] as TextView
                        val look = tv.text.toString().substring(tv.text.toString().indexOf(".") + 2)
                        val col = color(
                            if (ss != "" && look.contains(ss, true))
                                R.color.recencySearch else R.color.recency
                        )
                        tv.setTextColor(col)
                        (this[1] as TextView).setTextColor(col)
                    }
                }
            })
        }
        for (r in 0 until recency.size) ll.addView(
            (layoutInflater.inflate(R.layout.recency, null) as ConstraintLayout).apply {
                (this[0] as TextView).text = "${r + 1}. ${recency[r].name}"
                val gre = Calendar.getInstance().apply { timeInMillis = recency[r].time }
                val jal = Jalali(gre)
                (this[1] as TextView).text =
                    "${z(jal.Y)}.${z(jal.M + 1)}.${z(jal.D)} - " +
                            "${z(gre[Calendar.HOUR_OF_DAY])}:${z(gre[Calendar.MINUTE])}"
                if (r == recency.size - 1) this.removeViewAt(2)
            }
        )
    }
}
