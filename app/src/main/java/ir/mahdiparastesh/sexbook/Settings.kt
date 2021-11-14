package ir.mahdiparastesh.sexbook

import android.content.Intent
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import ir.mahdiparastesh.sexbook.Fun.Companion.c
import ir.mahdiparastesh.sexbook.Fun.Companion.sp
import ir.mahdiparastesh.sexbook.databinding.SettingsBinding

class Settings : AppCompatActivity() {
    private lateinit var b: SettingsBinding
    private lateinit var m: Model
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
        Fun.init(this)


        // Toolbar
        setSupportActionBar(b.toolbar)
        for (g in 0 until b.toolbar.childCount) {
            var getTitle = b.toolbar.getChildAt(g)
            if (getTitle is TextView &&
                getTitle.text.toString() == resources.getString(R.string.app_name)
            ) tbTitle = getTitle
        }
        if (::tbTitle.isInitialized) tbTitle.setTypeface(Main.dateFont, Typeface.BOLD)

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
    }

    override fun onBackPressed() {
        if (changed) {
            finish()
            startActivity(Intent(this, Main::class.java))
        } else super.onBackPressed()
    }
}
