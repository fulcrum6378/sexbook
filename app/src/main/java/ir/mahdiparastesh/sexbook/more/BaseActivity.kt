package ir.mahdiparastesh.sexbook.more

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.Typeface
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import ir.mahdiparastesh.sexbook.Main
import ir.mahdiparastesh.sexbook.Model
import ir.mahdiparastesh.sexbook.R

abstract class BaseActivity : AppCompatActivity() {
    lateinit var m: Model
    lateinit var c: Context
    lateinit var font1: Typeface
    lateinit var font1Bold: Typeface
    var tbTitle: TextView? = null

    companion object {
        lateinit var sp: SharedPreferences
        lateinit var jdtpFont: Typeface
        lateinit var jdtpFontBold: Typeface
        var dm = DisplayMetrics()
        var dirRtl = false

        fun dp(px: Int) = (dm.density * px.toFloat()).toInt()

        fun Context.night(): Boolean = resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        m = ViewModelProvider(this, Model.Factory()).get("Model", Model::class.java)
        c = applicationContext

        dm = resources.displayMetrics
        dirRtl = c.resources.getBoolean(R.bool.dirRtl)
        sp = getSharedPreferences(
            resources.getString(R.string.stSP), Context.MODE_PRIVATE
        )
        if (!::font1.isInitialized) {
            font1 = font()
            jdtpFont = font1
        }
        if (!::font1Bold.isInitialized) {
            font1Bold = font(true)
            jdtpFontBold = font1Bold
        }
    }

    override fun setContentView(root: View?) {
        super.setContentView(root)
        root?.layoutDirection =
            if (!dirRtl) ViewGroup.LAYOUT_DIRECTION_LTR else ViewGroup.LAYOUT_DIRECTION_RTL
    }

    fun toolbar(tb: Toolbar, title: Int) {
        setSupportActionBar(tb)
        for (g in 0 until tb.childCount) {
            val getTitle = tb.getChildAt(g)
            if (getTitle is TextView && getTitle.text.toString() == getString(title))
                tbTitle = getTitle
        }
        tbTitle?.typeface = font1Bold
        if (this !is Main) supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }
        tb.navigationIcon?.colorFilter = pdcf()
    }

    fun font(bold: Boolean = false): Typeface = Typeface.createFromAsset(
        c.assets, if (!bold) c.resources.getString(R.string.font1)
        else c.resources.getString(R.string.font1Bold)
    )

    fun color(res: Int) = ContextCompat.getColor(c, res)

    fun pdcf(res: Int = R.color.CPDD) =
        PorterDuffColorFilter(ContextCompat.getColor(c, res), PorterDuff.Mode.SRC_IN)

    fun fixADButton(button: Button) = button.apply {
        setTextColor(color(R.color.mrvPopupButtons))
        //setBackgroundColor(color(R.color.CP))
        typeface = font1
    }
}
