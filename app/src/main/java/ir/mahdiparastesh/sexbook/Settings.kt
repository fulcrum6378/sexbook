package ir.mahdiparastesh.sexbook

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.get
import androidx.lifecycle.ViewModelProvider
import ir.mahdiparastesh.sexbook.Fun.Companion.sp
import ir.mahdiparastesh.sexbook.Main.Action.RELOAD
import ir.mahdiparastesh.sexbook.data.DbFile
import ir.mahdiparastesh.sexbook.databinding.SettingsBinding
import ir.mahdiparastesh.sexbook.more.SpinnerAdap

class Settings : AppCompatActivity() {
    lateinit var m: Model
    private lateinit var b: SettingsBinding
    private lateinit var tbTitle: TextView
    private lateinit var calendarTypes: Array<String>
    private var changed = false

    companion object {
        const val spCalType = "calendarType"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = SettingsBinding.inflate(layoutInflater)
        m = ViewModelProvider(this, Model.Factory()).get("Model", Model::class.java)
        setContentView(b.root)
        Fun.init(this, b.root)


        // Toolbar
        setSupportActionBar(b.toolbar)
        for (g in 0 until b.toolbar.childCount) {
            var getTitle = b.toolbar.getChildAt(g)
            if (getTitle is TextView &&
                getTitle.text.toString() == resources.getString(R.string.stTitle)
            ) tbTitle = getTitle
        }
        if (::tbTitle.isInitialized) {
            tbTitle.typeface = Fun.font1Bold
            tbTitle.textSize = resources.getDimension(R.dimen.tbTitle)
        }
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }
        b.toolbar.navigationIcon?.apply { colorFilter = Fun.pdcf() }

        // Fonts
        for (l in 0 until b.ll.childCount) {
            val first = (b.ll[l] as ConstraintLayout)[0]
            if (first is TextView) first.typeface = Fun.font1
        }

        // Calendar Type
        calendarTypes = resources.getStringArray(R.array.calendarTypes)
        b.stCalendarType.adapter = SpinnerAdap(calendarTypes.toList())
        b.stCalendarType.setSelection(sp.getInt(spCalType, 0))
        b.stCalendarType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(adapterView: AdapterView<*>?) {}
            override fun onItemSelected(av: AdapterView<*>?, v: View?, i: Int, l: Long) {
                if (sp.getInt(spCalType, 0) == i) return
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
                    changed = true
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
            startActivity(Intent(this, Main::class.java).setAction(RELOAD.s))
        } else super.onBackPressed()
    }
}
