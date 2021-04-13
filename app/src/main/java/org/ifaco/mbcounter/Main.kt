package org.ifaco.mbcounter

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Typeface
import android.net.Uri
import android.os.*
import android.view.*
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import org.ifaco.mbcounter.Fun.Companion.c
import org.ifaco.mbcounter.Fun.Companion.detectNight
import org.ifaco.mbcounter.data.Adap.Companion.Sort
import org.ifaco.mbcounter.Fun.Companion.dm
import org.ifaco.mbcounter.Fun.Companion.dp
import org.ifaco.mbcounter.Fun.Companion.exit
import org.ifaco.mbcounter.Fun.Companion.explode
import org.ifaco.mbcounter.Fun.Companion.night
import org.ifaco.mbcounter.Fun.Companion.now
import org.ifaco.mbcounter.Fun.Companion.pdcf
import org.ifaco.mbcounter.data.Adap
import org.ifaco.mbcounter.data.Exporter
import org.ifaco.mbcounter.data.Report
import org.ifaco.mbcounter.data.Work
import org.ifaco.mbcounter.data.Filter
import org.ifaco.mbcounter.databinding.MainBinding
import org.ifaco.mbcounter.more.SolarHijri
import java.util.*
import kotlin.collections.ArrayList

// adb connect 192.168.1.5:

@Suppress("UNCHECKED_CAST")
class Main : AppCompatActivity() {
    lateinit var b: MainBinding
    lateinit var tbTitle: TextView
    var adapter: Adap? = null
    var adding = false

    companion object {
        lateinit var handler: Handler

        const val workActionTimeout: Long = 5000
        var dateFont: Typeface? = null
        var allMasturbation: ArrayList<Report>? = null
        var masturbation = ArrayList<Report>()
        var saveOnBlur = false
        var scrollOnFocus = false
        var filters: ArrayList<Filter>? = null
        var listFilter = 0
        var selectedCrush: String? = null
        var sumResult: Summary.Result? = null
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = MainBinding.inflate(layoutInflater)
        setContentView(b.root)
        Fun.init(this)

        detectNight(resources.configuration)?.let { setNight(it) }


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
                        allMasturbation = msg.obj as ArrayList<Report>
                        resetAllMasturbations()
                        load()
                    }
                    Work.VIEW_ONE -> if (msg.obj != null) when (msg.arg1) {
                        Work.ADD_NEW_ITEM -> {
                            if (allMasturbation == null) allMasturbation = ArrayList()
                            allMasturbation!!.add(msg.obj as Report)
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
                        if (allMasturbation != null) {
                            if (masturbation.contains(allMasturbation!![msg.arg1])) {
                                val nominalPos = masturbation.indexOf(allMasturbation!![msg.arg1])
                                masturbation[nominalPos] = allMasturbation!![msg.arg1]
                                adapter?.notifyItemChanged(nominalPos)
                            }
                            if (msg.arg2 == 2) resetAllMasturbations()
                        }
                        if (msg.arg2 == 1) exit(that)
                    }
                    Work.DELETE_ONE -> if (allMasturbation != null) {
                        if (masturbation.contains(allMasturbation!![msg.arg1])) {
                            val nominalPos = masturbation.indexOf(allMasturbation!![msg.arg1])
                            masturbation.remove(allMasturbation!![msg.arg1])
                            allMasturbation!!.remove(allMasturbation!![msg.arg1])
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
        setGDLM()
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
        b.spnFilterMark.setColorFilter(ContextCompat.getColor(c, R.color.spnFilterMark))
        b.spnFilter.setOnTouchListener { _, _ -> spnFilterTouched = true; false }
        b.spnFilter.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onNothingSelected(adapterView: AdapterView<*>?) {}
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View, i: Int, l: Long) {
                if (spnFilterTouched) filterList(i)
            }
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
            }
            Toast.makeText(c, R.string.toExit, Toast.LENGTH_SHORT).show()
            return
        }
        if (!saveFocused()) exit(this)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        dm = resources.displayMetrics
        detectNight(newConfig)?.let { setNight(it) }
        setGDLM()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar, menu)
        return super.onCreateOptionsMenu(menu)//DON"T PUT HERE THINGS THAT NEED THE LAYOUT LOADED.
    }

    val reqImport = 666
    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.momImport -> {
            startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) "application/octet-stream"
                else "application/json"
            }, reqImport)
            true
        }
        R.id.momExport -> {
            if (allMasturbation != null) {
                val perm = Manifest.permission.WRITE_EXTERNAL_STORAGE
                if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState() &&
                    ContextCompat.checkSelfPermission(c, perm) !=
                    PackageManager.PERMISSION_GRANTED && Build.VERSION.SDK_INT >= 23
                ) ActivityCompat.requestPermissions(this, arrayOf(perm), permExport)
                else whereToExport()
            }
            true
        }
        R.id.momSum -> {
            if (allMasturbation != null) {
                sumResult = Summary(allMasturbation!!).result
                if (sumResult != null) AlertDialog.Builder(this).apply {
                    setTitle("${resources.getString(R.string.momSum)} (" + allMasturbation!!.size + ")")
                    setView(ScrollView(c).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                        addView(LinearLayout(c).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                            )
                            setPadding(dp(21), dp(15), dp(21), dp(15))
                            orientation = LinearLayout.VERTICAL
                            for (r in sumResult!!.calculations) addView(
                                ChipGroup(ContextThemeWrapper(c, R.style.AppTheme), null, 0).apply {
                                    layoutParams = LinearLayout.LayoutParams(
                                        ViewGroup.LayoutParams.WRAP_CONTENT,
                                        ViewGroup.LayoutParams.WRAP_CONTENT
                                    )
                                    addView(TextView(c).apply {
                                        layoutParams = ChipGroup.LayoutParams(
                                            ViewGroup.LayoutParams.WRAP_CONTENT,
                                            ViewGroup.LayoutParams.WRAP_CONTENT
                                        )
                                        setPadding(0, dp(12), 0, 0)
                                        text =
                                            (if (r.key % 1 > 0) r.key.toString()
                                            else r.key.toInt().toString()).plus(": ")
                                    })
                                    for (crush in r.value) addView(
                                        Chip(
                                            ContextThemeWrapper(c, R.style.AppTheme), null, 0
                                        ).apply {
                                            layoutParams = ChipGroup.LayoutParams(
                                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                                ViewGroup.LayoutParams.WRAP_CONTENT
                                            )
                                            text = crush
                                            setOnClickListener {
                                                selectedCrush = crush
                                                startActivity(
                                                    Intent(this@Main, Statistics::class.java)
                                                )
                                            }
                                        })
                                })
                        })
                    })
                    setPositiveButton(R.string.ok, null)
                    setCancelable(true)
                }.create().apply {
                    show()
                    Fun.fixADButton(this@Main, getButton(AlertDialog.BUTTON_POSITIVE))
                }
            }
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    val permExport = 361
    val permImport = 786
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        val b = grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
        when (requestCode) {
            permExport -> if (b) whereToExport()
            permImport -> if (b) import()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            reqImport -> if (resultCode == RESULT_OK) {
                toBeImported = data!!.data
                val perm = Manifest.permission.WRITE_EXTERNAL_STORAGE
                if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState() &&
                    ContextCompat.checkSelfPermission(c, perm) !=
                    PackageManager.PERMISSION_GRANTED && Build.VERSION.SDK_INT >= 23
                ) ActivityCompat.requestPermissions(this, arrayOf(perm), permImport)
                else import()
            }
            reqFolder -> if (resultCode == RESULT_OK && intent != null && intent.data != null) {
                val b = Exporter.export(intent.data!!)
                Toast.makeText(
                    c, if (b) R.string.exportDone else R.string.exportUndone, Toast.LENGTH_LONG
                ).show()
            }
        }
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
        ObjectAnimator.ofFloat(b.loadShadow, "translationX", value).apply {
            startDelay = sd
            duration = dur
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    b.body.removeView(b.loadShadow)
                }
            })
            start()
        }
    }

    fun setNight(n: Boolean = true) {
        night = n
        window.decorView.setBackgroundColor(
            ContextCompat.getColor(c, if (!n) R.color.body else R.color.bodyN)
        )
        if (n) b.loadIV.colorFilter = pdcf(c) else b.loadIV.clearColorFilter()
        if (n) b.addIV.colorFilter = pdcf(c) else b.addIV.clearColorFilter()
        arrangeList()
    }

    var lm: StaggeredGridLayoutManager? = null
    fun setGDLM(width: Int = dm.widthPixels) {
        var span = 1
        if (width > 0) span = ((width / dm.density) / 190f).toInt()
        if (span < 1) span = 1
        lm = StaggeredGridLayoutManager(span, StaggeredGridLayoutManager.VERTICAL)
        b.rv.layoutManager = lm
    }

    fun saveFocused(): Boolean {
        var isFocused = false
        for (f in 0 until b.rv.childCount) {
            var i = b.rv.getChildAt(f) as ViewGroup
            var et = (i.getChildAt(Adap.clPos) as ViewGroup).getChildAt(Adap.notesPos) as EditText
            if (et.hasFocus()) {
                Adap.saveET(c, et, Adap.allPos(masturbation, b.rv.getChildLayoutPosition(i)), true)
                isFocused = true
            }
        }
        return isFocused
    }

    fun resetAllMasturbations() {
        Collections.sort(allMasturbation!!, Sort())
        filters = filter(allMasturbation!!)
        filterList(filters!!.size - 1)
        if (filters != null) setSpinner()
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

    fun filterTitles(filters: ArrayList<Filter>): ArrayList<String> =
        ArrayList<String>().apply { for (f in filters.indices) add(filters[f].titleInShamsi(c)) }

    var spnFilterTouched = false
    fun setSpnFilter(options: ArrayList<String>) {
        b.spnFilter.adapter = ArrayAdapter(c, R.layout.spinner_1, options)
            .apply { setDropDownViewResource(R.layout.spinner_1_dd) }
        spnFilterTouched = false
    }

    fun setSpinner() {
        if (filters == null) return
        setSpnFilter(filterTitles(filters!!))
        listFilter = filters!!.size - 1
        b.spnFilter.setSelection(listFilter, true)
    }

    fun filterList(i: Int) {
        if (allMasturbation == null) {
            masturbation.clear(); return; }
        listFilter = i
        masturbation.clear()
        if (filters == null) for (o in allMasturbation!!) masturbation.add(o)
        else if (!filters.isNullOrEmpty())
            for (o in filters!![listFilter].items) masturbation.add(allMasturbation!![o])
        arrangeList()
    }

    fun arrangeList() {
        saveOnBlur = false
        scrollOnFocus = false
        if (adapter == null) {
            adapter = Adap(c, masturbation, this)
            b.rv.adapter = adapter
        } else adapter!!.notifyDataSetChanged()
    }

    val reqFolder = 750
    fun whereToExport() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/json"
            putExtra(Intent.EXTRA_TITLE, "exported.json")
        }
        startActivityForResult(intent, reqFolder)
    }

    var toBeImported: Uri? = null
    fun import() {
        if (toBeImported == null) return
        Exporter.import(toBeImported!!)
            ?.let { Work(c, handler, Work.REPLACE_ALL, it.toList()).start() }
        toBeImported = null
    }
}
