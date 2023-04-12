package ir.mahdiparastesh.sexbook.stat

import android.annotation.SuppressLint
import android.text.Editable
import android.text.TextWatcher
import ir.mahdiparastesh.sexbook.Main
import ir.mahdiparastesh.sexbook.databinding.SearchableStatBinding
import ir.mahdiparastesh.sexbook.list.StatRecAdap

class Recency(sum: Summary) {
    var arr: ArrayList<Item> = ArrayList()

    init {
        sum.scores.forEach { (name, erections) ->
            if (Summary.isUnknown(name)) return@forEach
            var mostRecent = 0L
            for (e in erections) if (e.time > mostRecent) mostRecent = e.time
            arr.add(Item(name, mostRecent))
        }
        arr.sortByDescending { it.time }
    }

    @SuppressLint("InflateParams", "NotifyDataSetChanged")
    fun draw(c: Main) = SearchableStatBinding.inflate(c.layoutInflater).apply {
        find.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, r: Int, c: Int, a: Int) {}
            override fun onTextChanged(s: CharSequence?, r: Int, b: Int, c: Int) {}
            override fun afterTextChanged(s: Editable?) {
                c.m.lookingFor = s.toString()
                list.adapter?.notifyDataSetChanged()
            }
        })
        c.m.lookingFor?.also { find.setText(it) }
        list.adapter = StatRecAdap(c, this@Recency)
        list.clipToPadding = false
    }.root

    data class Item(val name: String, val time: Long)
}
