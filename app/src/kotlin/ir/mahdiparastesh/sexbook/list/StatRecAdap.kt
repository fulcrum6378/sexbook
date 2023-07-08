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
import ir.mahdiparastesh.sexbook.stat.Singular

class StatRecAdap(private val c: Main) :
    RecyclerView.Adapter<AnyViewHolder<RecencyBinding>>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
            AnyViewHolder<RecencyBinding> =
        AnyViewHolder(RecencyBinding.inflate(c.layoutInflater, parent, false))

    override fun onBindViewHolder(h: AnyViewHolder<RecencyBinding>, i: Int) {
        h.b.name.text = "${i + 1}. ${c.m.recency[i].name}"
        val lm = c.m.recency[i].time.calendar(c)
        h.b.date.text = "${lm.fullDate()} - " +
                "${Fun.z(lm[Calendar.HOUR_OF_DAY])}:${Fun.z(lm[Calendar.MINUTE])}"
        h.b.sep.vis(i != c.m.recency.size - 1)
        h.b.root.setOnClickListener {
            if (!c.summarize(true)) return@setOnClickListener
            c.goTo(Singular::class) {
                putExtra(Singular.EXTRA_CRUSH_KEY, c.m.recency[h.layoutPosition].name)
            }
        }
        h.b.root.foreground = if (c.m.lookForIt(c.m.recency[i].name))
            ColorDrawable(c.color(R.color.recencyHighlight)) else null
    }

    override fun getItemCount() = c.m.recency.size
}
