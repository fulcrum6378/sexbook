package ir.mahdiparastesh.sexbook.list

import android.graphics.drawable.ColorDrawable
import android.icu.util.Calendar
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ir.mahdiparastesh.sexbook.Fun
import ir.mahdiparastesh.sexbook.Fun.calendar
import ir.mahdiparastesh.sexbook.Fun.fullDate
import ir.mahdiparastesh.sexbook.Fun.vis
import ir.mahdiparastesh.sexbook.Main
import ir.mahdiparastesh.sexbook.R
import ir.mahdiparastesh.sexbook.databinding.RecencyBinding
import ir.mahdiparastesh.sexbook.more.AnyViewHolder
import ir.mahdiparastesh.sexbook.stat.Recency
import ir.mahdiparastesh.sexbook.stat.Singular

class StatRecAdap(private val c: Main, private val rec: Recency) :
    RecyclerView.Adapter<AnyViewHolder<RecencyBinding>>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
            AnyViewHolder<RecencyBinding> =
        AnyViewHolder(RecencyBinding.inflate(c.layoutInflater, parent, false))

    override fun onBindViewHolder(h: AnyViewHolder<RecencyBinding>, i: Int) {
        h.b.name.text = "${i + 1}. ${rec.arr[i].name}"
        val lm = rec.arr[i].time.calendar(c)
        h.b.date.text = "${lm.fullDate()} - " +
                "${Fun.z(lm[Calendar.HOUR_OF_DAY])}:${Fun.z(lm[Calendar.MINUTE])}"
        h.b.sep.vis(i != rec.arr.size - 1)
        h.b.root.setOnClickListener {
            if (!c.summarize(true)) return@setOnClickListener
            c.m.crush = rec.arr[i].name
            c.goTo(Singular::class)
        }
        h.b.root.foreground = if (c.m.lookForIt(rec.arr[i].name))
            ColorDrawable(c.color(R.color.recencyHighlight)) else null
    }

    override fun getItemCount() = rec.arr.size
}
