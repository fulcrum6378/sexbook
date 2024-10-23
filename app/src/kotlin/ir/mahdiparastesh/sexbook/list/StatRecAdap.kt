package ir.mahdiparastesh.sexbook.list

import android.annotation.SuppressLint
import android.graphics.drawable.ColorDrawable
import android.icu.util.Calendar
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import ir.mahdiparastesh.sexbook.Fun
import ir.mahdiparastesh.sexbook.Fun.calendar
import ir.mahdiparastesh.sexbook.Fun.fullDate
import ir.mahdiparastesh.sexbook.Fun.show
import ir.mahdiparastesh.sexbook.Fun.sumOf
import ir.mahdiparastesh.sexbook.R
import ir.mahdiparastesh.sexbook.Settings
import ir.mahdiparastesh.sexbook.databinding.RecencyBinding
import ir.mahdiparastesh.sexbook.misc.AnyViewHolder
import ir.mahdiparastesh.sexbook.stat.Recency
import ir.mahdiparastesh.sexbook.stat.Singular

class StatRecAdap(private val r: Recency) : RecyclerView.Adapter<AnyViewHolder<RecencyBinding>>() {
    private val statOnlyCrushes = r.c.sp.getBoolean(Settings.spStatOnlyCrushes, false)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
            AnyViewHolder<RecencyBinding> =
        AnyViewHolder(RecencyBinding.inflate(r.c.layoutInflater, parent, false))

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(h: AnyViewHolder<RecencyBinding>, i: Int) {
        val crushKey = r.items[i].name
        h.b.name.text = "${i + 1}. $crushKey" +
                (if (!statOnlyCrushes && crushKey in r.c.m.liefde) "*" else "") +
                (r.c.m.summary!!.scores[crushKey]?.sumOf { it.value }
                    ?.show()?.let { " {$it}" } ?: "")
        val lm = r.items[i].time.calendar(r.c)
        h.b.date.text = "${lm.fullDate()} - " +
                "${Fun.z(lm[Calendar.HOUR_OF_DAY])}:${Fun.z(lm[Calendar.MINUTE])}"
        h.b.sep.isVisible = i != r.items.size - 1
        h.b.root.setOnClickListener {
            if (!r.c.summarize(true)) return@setOnClickListener
            r.c.goTo(Singular::class) {
                putExtra(Singular.EXTRA_CRUSH_KEY, r.items[h.layoutPosition].name)
            }
        }
        h.b.root.foreground = if (r.lookForIt(crushKey))
            ColorDrawable(r.c.color(R.color.recencyHighlight)) else null
    }

    override fun getItemCount() = r.items.size
}
