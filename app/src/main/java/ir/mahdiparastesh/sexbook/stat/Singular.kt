package ir.mahdiparastesh.sexbook.stat

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.provider.DocumentsContract
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.anychart.AnyChart
import com.anychart.chart.common.dataentry.DataEntry
import com.anychart.chart.common.dataentry.ValueDataEntry
import com.anychart.enums.Anchor
import com.anychart.enums.HoverMode
import com.anychart.enums.Position
import com.anychart.enums.TooltipPositionMode
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import ir.mahdiparastesh.sexbook.Fun
import ir.mahdiparastesh.sexbook.Fun.Companion.c
import ir.mahdiparastesh.sexbook.Model
import ir.mahdiparastesh.sexbook.R
import ir.mahdiparastesh.sexbook.data.Crush
import ir.mahdiparastesh.sexbook.data.Report
import ir.mahdiparastesh.sexbook.data.Work
import ir.mahdiparastesh.sexbook.databinding.IdentifyBinding
import ir.mahdiparastesh.sexbook.databinding.SingularBinding
import ir.mahdiparastesh.sexbook.more.Jalali
import java.util.*
import kotlin.collections.ArrayList

@Suppress("UNCHECKED_CAST")
class Singular : AppCompatActivity() {
    private lateinit var b: SingularBinding
    private lateinit var m: Model
    private var crush: Crush? = null

    @SuppressLint("InflateParams", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = SingularBinding.inflate(layoutInflater)
        m = ViewModelProvider(this, Model.Factory()).get("Model", Model::class.java)
        setContentView(b.root)
        Fun.init(this)

        if (m.onani.value == null || m.summary.value == null || m.crush.value == null) {
            onBackPressed(); return; }
        val data: MutableList<DataEntry> = ArrayList()
        val history = m.summary.value!!.scores[m.crush.value]
        sinceTheBeginning(m.onani.value!!)
            .forEach { data.add(ValueDataEntry(it, calcHistory(history!!, it))) }

        // Handler
        handler = object : Handler(Looper.getMainLooper()) {
            @Suppress("UNCHECKED_CAST")
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    Work.C_VIEW_ONE -> crush = msg.obj as Crush?
                    Work.C_INSERT_ONE, Work.C_UPDATE_ONE ->
                        Work(Work.C_VIEW_ONE, listOf(m.crush.value!!), handler).start()
                }
            }
        }

        AnyChart.column().apply {
            column(data).fill("#FFD422").stroke("#FFD422")
                .tooltip()
                .titleFormat("{%X}")
                .position(Position.CENTER_BOTTOM)
                .anchor(Anchor.CENTER_BOTTOM)
                .offsetX(0.0)
                .offsetY(5.0)
                .format("{%Value}{groupsSeparator: }")
            animation(true)
            title(m.crush.value)
            yScale().minimum(0.0)
            yAxis(0).labels().format("{%Value}{groupsSeparator: }")
            tooltip().positionMode(TooltipPositionMode.POINT)
            interactivity().hoverMode(HoverMode.BY_X)
            background(resources.getString(R.string.anyChartBG))
            b.main.setChart(this)
        }

        // Night Mode
        if (Fun.night) {
            window.decorView.setBackgroundColor(Fun.color(R.color.CP))
            b.identifyIV.colorFilter = Fun.pdcf(R.color.CP)
        }

        // Identification
        Work(Work.C_VIEW_ONE, listOf(m.crush.value!!), handler).start()
        b.identify.setOnClickListener {
            val bi = IdentifyBinding.inflate(layoutInflater, null, false)

            // Default Values
            val cal = Calendar.getInstance()
            var yea = -1
            var mon = -1
            var day = -1
            if (crush != null) {
                bi.fName.setText(crush!!.fName)
                bi.lName.setText(crush!!.lName)
                bi.masc.isChecked = crush!!.masc
                bi.real.isChecked = crush!!.real
                if (crush!!.height != -1f)
                    bi.height.setText(crush!!.height.toString())
                yea = crush!!.bYear.toInt()
                mon = crush!!.bMonth.toInt()
                day = crush!!.bDay.toInt()
                if (yea != -1) cal[Calendar.YEAR] = yea
                if (mon != -1) cal[Calendar.MONTH] = mon
                if (day != -1) cal[Calendar.DAY_OF_MONTH] = day
                bi.location.setText(crush!!.locat)
                bi.instagram.setText(crush!!.insta)
                // Implement Contact
                if (crush!!.gallery != null) {
                    // gallery = Uri.parse(crush!!.gallery)
                    bi.gallery.text = crush!!.gallery
                }
                bi.notifyBirth.isChecked = crush!!.notifyBirth
            }

            // Birth
            bi.birth.setOnClickListener {
                DatePickerDialog.newInstance(
                    { _, year, monthOfYear, dayOfMonth ->
                        yea = year
                        mon = monthOfYear
                        day = dayOfMonth
                        bi.birth.text = "$yea.$mon.$day"
                    }, cal[Calendar.YEAR], cal[Calendar.MONTH], cal[Calendar.DAY_OF_MONTH]
                ).apply {
                    isThemeDark = Fun.night
                    version = DatePickerDialog.Version.VERSION_2
                    accentColor = Fun.color(R.color.CP)
                    setOkColor(Fun.color(R.color.mrvPopupButtons))
                    setCancelColor(Fun.color(R.color.mrvPopupButtons))
                    show(supportFragmentManager, "birth")
                }
            }

            // Contact
            // bi.contact.setOnClickListener { }

            // Gallery
            bi.gallery.setOnClickListener {
                galleryLauncher.launch(Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
                    if (gallery != null) putExtra(DocumentsContract.EXTRA_INITIAL_URI, gallery)
                })
            }

            AlertDialog.Builder(this).apply {
                setTitle("${resources.getString(R.string.identify)}: ${m.crush.value}")
                setView(bi.root)
                setPositiveButton(R.string.save) { _, _ ->
                    val inserted = Crush(
                        m.crush.value!!,
                        if (bi.fName.text.toString().isEmpty()) null else bi.fName.text.toString(),
                        if (bi.lName.text.toString().isEmpty()) null else bi.lName.text.toString(),
                        bi.masc.isChecked,
                        bi.real.isChecked,
                        if (bi.height.text.toString() != "")
                            bi.height.text.toString().toFloat() else -1f,
                        yea.toShort(), mon.toByte(), day.toByte(),
                        if (bi.location.text.toString().isEmpty()) null else
                            bi.location.text.toString(),
                        if (bi.instagram.text.toString().isEmpty()) null else
                            bi.instagram.text.toString(),
                        null,
                        gallery?.path,
                        bi.notifyBirth.isChecked
                    )
                    Work(
                        if (crush == null) Work.C_INSERT_ONE else Work.C_UPDATE_ONE,
                        listOf(inserted), handler
                    ).start()
                }
                setNegativeButton(R.string.discard, null)
                setCancelable(true)
            }.create().apply {
                show()
                Fun.fixADButton(getButton(AlertDialog.BUTTON_POSITIVE))
                Fun.fixADButton(getButton(AlertDialog.BUTTON_NEGATIVE))
            }
        }
    }

    private var gallery: Uri? = null
    private var galleryLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode != Activity.RESULT_OK) return@registerForActivityResult
            gallery = it.data!!.data!!
        }

    companion object {
        lateinit var handler: Handler

        fun sinceTheBeginning(mOnani: ArrayList<Report>): List<String> {
            val now = Calendar.getInstance()
            var oldest = now.timeInMillis
            for (h in mOnani) if (h.time < oldest) oldest = h.time
            val beg = Calendar.getInstance().apply { timeInMillis = oldest }
            val list = arrayListOf<String>()
            if (Fun.calType() != Fun.CalendarType.JALALI) {
                val yDist = now[Calendar.YEAR] - beg[Calendar.YEAR]
                for (y in 0 until (yDist + 1)) {
                    var start = 0
                    var end = 11
                    if (y == 0) start = beg[Calendar.MONTH]
                    if (y == yDist) end = now[Calendar.MONTH]
                    for (m in start..end) list.add(
                        "${c.resources.getStringArray(R.array.months)[m]} ${beg[Calendar.YEAR] + y}"
                    )
                }
            } else {
                val jBeg = Jalali(beg)
                val jNow = Jalali(now)
                val yDist = jNow.Y - jBeg.Y
                for (y in 0 until (yDist + 1)) {
                    var start = 0
                    var end = 11
                    if (y == 0) start = jBeg.M
                    if (y == yDist) end = jNow.M
                    for (m in start..end) list.add(
                        "${c.resources.getStringArray(R.array.jMonths)[m]} ${jBeg.Y + y}"
                    )
                }
            }
            return list.toList()
        }

        fun calcHistory(
            list: ArrayList<Summary.Erection>, month: String, growing: Boolean = false
        ): Float {
            var value = 0f
            val split = month.split(" ")
            val months = c.resources.getStringArray(
                when (Fun.calType()) {
                    Fun.CalendarType.JALALI -> R.array.jMonths
                    else -> R.array.months
                }
            )
            for (i in list) {
                var lm = Calendar.getInstance().apply { timeInMillis = i.time }
                var yea: Int
                var mon: Int
                if (Fun.calType() == Fun.CalendarType.JALALI) Jalali(lm).apply {
                    yea = Y
                    mon = M
                } else {
                    yea = lm[Calendar.YEAR]
                    mon = lm[Calendar.MONTH]
                }
                if (months.indexOf(split[0]) == mon && split[1].toInt() == yea) value += i.value
                if (growing && (split[1].toInt() > yea ||
                            (split[1].toInt() == yea && months.indexOf(split[0]) > mon))
                ) value += i.value
            }
            return value
        }
    }
}
