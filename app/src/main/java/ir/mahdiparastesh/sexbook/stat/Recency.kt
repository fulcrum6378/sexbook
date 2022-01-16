package ir.mahdiparastesh.sexbook.stat

import android.annotation.SuppressLint
import android.content.Intent
import android.icu.util.Calendar
import android.text.Editable
import android.text.TextWatcher
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.get
import ir.mahdiparastesh.sexbook.Fun
import ir.mahdiparastesh.sexbook.Fun.Companion.fullDate
import ir.mahdiparastesh.sexbook.Main
import ir.mahdiparastesh.sexbook.R
import ir.mahdiparastesh.sexbook.databinding.RecencyBinding
import ir.mahdiparastesh.sexbook.databinding.SumChipsBinding

class Recency(sum: Summary) {
    var res: ArrayList<Item> = ArrayList()

    init {
        sum.scores.forEach { (name, erections) ->
            if (Summary.isUnknown(name)) return@forEach
            var mostRecent = 0L
            for (e in erections) if (e.time > mostRecent) mostRecent = e.time
            res.add(Item(name, mostRecent))
        }
        res.sortBy { it.time }
        res.reverse()
    }

    @SuppressLint("InflateParams", "SetTextI18n")
    fun draw(c: Main) = SumChipsBinding.inflate(c.layoutInflater, null, false).apply {
        find.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, r: Int, c: Int, a: Int) {}
            override fun onTextChanged(s: CharSequence?, r: Int, b: Int, c: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val ss = s.toString()
                for (i in 1 until ll.childCount) (ll[i] as ConstraintLayout).apply {
                    val tv = this[0] as TextView
                    val look = tv.text.toString()
                        .substring(tv.text.toString().indexOf(".") + 2)
                    val col = c.color(
                        if (ss != "" && look.contains(ss, true))
                            R.color.recencySearch else R.color.recency
                    )
                    tv.setTextColor(col)
                    (this[1] as TextView).setTextColor(col)
                }
            }
        })
        find.typeface = c.font1

        for (r in 0 until res.size) ll.addView(
            RecencyBinding.inflate(c.layoutInflater).apply {
                name.apply {
                    text = "${r + 1}. ${res[r].name}"
                    typeface = c.font1
                }
                val lm = Calendar.getInstance().apply { timeInMillis = res[r].time }
                date.text = "${fullDate(lm)} - " +
                        "${Fun.z(lm[Calendar.HOUR_OF_DAY])}:${Fun.z(lm[Calendar.MINUTE])}"
                date.typeface = c.font1
                if (r == res.size - 1) root.removeViewAt(2)
                root.setOnClickListener {
                    if (!Main.summarize(c.m)) return@setOnClickListener
                    c.m.crush = res[r].name
                    c.startActivity(Intent(c, Singular::class.java))
                }
            }.root
        )
    }.root

    data class Item(val name: String, val time: Long)
}
