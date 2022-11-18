package ir.mahdiparastesh.sexbook.stat

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.icu.util.Calendar
import android.text.Editable
import android.text.TextWatcher
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.get
import ir.mahdiparastesh.sexbook.Fun
import ir.mahdiparastesh.sexbook.Fun.calendar
import ir.mahdiparastesh.sexbook.Fun.fullDate
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
        res.sortByDescending { it.time }
    }

    @SuppressLint("InflateParams")
    fun draw(c: Main) = SumChipsBinding.inflate(c.layoutInflater, null, false).apply {
        find.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, r: Int, c: Int, a: Int) {}
            override fun onTextChanged(s: CharSequence?, r: Int, b: Int, c: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val ss = s.toString()
                for (i in 1 until ll.childCount) (ll[i] as ConstraintLayout)
                    .apply { updateSearch(c, (this[0] as TextView).text.toString(), ss) }
                c.m.lookingFor = ss
            }
        })
        val lookingFor = c.m.lookingFor
        lookingFor?.also { find.setText(it) }

        for (r in 0 until res.size) ll.addView(
            RecencyBinding.inflate(c.layoutInflater).apply {
                name.text = "${r + 1}. ${res[r].name}"
                val lm = res[r].time.calendar(c)
                date.text = "${lm.fullDate()} - " +
                        "${Fun.z(lm[Calendar.HOUR_OF_DAY])}:${Fun.z(lm[Calendar.MINUTE])}"
                if (r == res.size - 1) root.removeViewAt(2)
                root.setOnClickListener {
                    if (!c.summarize()) return@setOnClickListener
                    c.m.crush = res[r].name
                    c.startActivity(Intent(c, Singular::class.java))
                }
                lookingFor?.also { root.updateSearch(c, name.text.toString(), it) }
            }.root
        )
    }.root

    private fun ConstraintLayout.updateSearch(c: Main, tvData: String, ss: String) {
        foreground = if (ss != "" && tvData
                .substring(tvData.indexOf(".") + 2)
                .contains(ss, true)
        ) ColorDrawable(c.color(R.color.recencyHighlight)) else null
    }

    data class Item(val name: String, val time: Long)
}
