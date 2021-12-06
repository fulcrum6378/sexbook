package ir.mahdiparastesh.sexbook

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.get
import androidx.lifecycle.ViewModelProvider
import ir.mahdiparastesh.sexbook.Fun.Companion.c
import ir.mahdiparastesh.sexbook.Fun.Companion.sp
import ir.mahdiparastesh.sexbook.data.DbFile
import ir.mahdiparastesh.sexbook.databinding.SettingsBinding

class Settings : AppCompatActivity() {
    private lateinit var b: SettingsBinding
    private lateinit var m: Model
    private lateinit var tbTitle: TextView
    private lateinit var calendarTypes: Array<String>
    private var changed = false
    private var dateFont: Typeface? = null

    companion object {
        const val spCalType = "calendarType"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = SettingsBinding.inflate(layoutInflater)
        m = ViewModelProvider(this, Model.Factory()).get("Model", Model::class.java)
        setContentView(b.root)
        Fun.init(this)


        // Fonts
        dateFont = Fun.font()
        for (l in 0 until b.ll.childCount) {
            val first = (b.ll[l] as ConstraintLayout)[0]
            if (first is TextView) first.typeface = dateFont
        }

        // Toolbar
        setSupportActionBar(b.toolbar)
        for (g in 0 until b.toolbar.childCount) {
            var getTitle = b.toolbar.getChildAt(g)
            if (getTitle is TextView &&
                getTitle.text.toString() == resources.getString(R.string.stTitle)
            ) tbTitle = getTitle
        }
        if (::tbTitle.isInitialized) tbTitle.setTypeface(dateFont, Typeface.BOLD)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }
        b.toolbar.navigationIcon?.apply { colorFilter = Fun.pdcf() }

        // Calendar Type
        calendarTypes = resources.getStringArray(R.array.calendarTypes)
        b.stCalendarType.adapter = ArrayAdapter(c, R.layout.spinner_1, calendarTypes)
            .apply { setDropDownViewResource(R.layout.spinner_1_dd) }
        b.stCalendarType.setSelection(sp.getInt(spCalType, 0))
        b.stCalendarType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(adapterView: AdapterView<*>?) {}
            override fun onItemSelected(av: AdapterView<*>?, v: View?, i: Int, l: Long) {
                changed = true
                sp.edit().apply {
                    putInt(spCalType, i)
                    apply()
                }
            }
        }

        // Truncate
        b.stTruncate.setOnClickListener {
            AlertDialog.Builder(this).apply {
                setTitle(resources.getString(R.string.stTruncate))
                setMessage(resources.getString(R.string.stTruncateSure))
                setPositiveButton(R.string.yes) { _, _ ->
                    DbFile(DbFile.Triple.MAIN).delete()
                    DbFile(DbFile.Triple.SHARED_MEMORY).delete()
                    DbFile(DbFile.Triple.WRITE_AHEAD_LOG).delete()
                }
                setNegativeButton(R.string.no, null)
                setCancelable(true)
            }.create().apply {
                show()
                Fun.fixADButton(getButton(AlertDialog.BUTTON_POSITIVE))
                Fun.fixADButton(getButton(AlertDialog.BUTTON_NEGATIVE))
            }
        }
    }

    override fun onBackPressed() {
        if (changed) {
            finish()
            startActivity(Intent(this, Main::class.java))
        } else super.onBackPressed()
    }
}
