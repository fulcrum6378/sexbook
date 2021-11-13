package ir.mahdiparastesh.sexbook.stat

import android.annotation.SuppressLint
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.get
import ir.mahdiparastesh.sexbook.Fun
import ir.mahdiparastesh.sexbook.R
import ir.mahdiparastesh.sexbook.more.Jalali
import java.util.*
import kotlin.collections.ArrayList

class Recency(sum: Sum) {
    var res: ArrayList<Item> = ArrayList()

    init {
        sum.scores.forEach { (name, erections) ->
            if (Sum.isUnknown(name)) return@forEach
            var mostRecent = 0L
            for (e in erections) if (e.time > mostRecent) mostRecent = e.time
            res.add(Item(name, mostRecent))
        }
        res.sortBy { it.time }
        res.reverse()
    }

    @SuppressLint("InflateParams", "SetTextI18n")
    fun draw(layoutInflater: LayoutInflater) =
        (layoutInflater.inflate(R.layout.sum, null) as ScrollView).apply {
            val ll = this[0] as LinearLayout
            (ll[0] as EditText).apply {
                addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, r: Int, c: Int, a: Int) {}
                    override fun onTextChanged(s: CharSequence?, r: Int, b: Int, c: Int) {}
                    override fun afterTextChanged(s: Editable?) {
                        val ss = s.toString()
                        for (i in 1 until ll.childCount) (ll[i] as ConstraintLayout).apply {
                            val tv = this[0] as TextView
                            val look = tv.text.toString()
                                .substring(tv.text.toString().indexOf(".") + 2)
                            val col = Fun.color(
                                if (ss != "" && look.contains(ss, true))
                                    R.color.recencySearch else R.color.recency
                            )
                            tv.setTextColor(col)
                            (this[1] as TextView).setTextColor(col)
                        }
                    }
                })
            }
            for (r in 0 until res.size) ll.addView(
                (layoutInflater.inflate(R.layout.recency, null) as ConstraintLayout).apply {
                    (this[0] as TextView).text = "${r + 1}. ${res[r].name}"
                    val lm = Calendar.getInstance().apply { timeInMillis = res[r].time }
                    if (Fun.calType() == Fun.CalendarType.JALALI) {
                        val jal = Jalali(lm)
                        (this[1] as TextView).text =
                            "${Fun.z(jal.Y)}.${Fun.z(jal.M + 1)}.${Fun.z(jal.D)} - " +
                                    "${Fun.z(lm[Calendar.HOUR_OF_DAY])}:${Fun.z(lm[Calendar.MINUTE])}"
                    } else (this[1] as TextView).text =
                        "${Fun.z(lm[Calendar.YEAR])}.${Fun.z(lm[Calendar.MONTH] + 1)}.${Fun.z(lm[Calendar.DAY_OF_MONTH])} - " +
                                "${Fun.z(lm[Calendar.HOUR_OF_DAY])}:${Fun.z(lm[Calendar.MINUTE])}"
                    if (r == res.size - 1) this.removeViewAt(2)
                }
            )
        }

    data class Item(val name: String, val time: Long)
}
