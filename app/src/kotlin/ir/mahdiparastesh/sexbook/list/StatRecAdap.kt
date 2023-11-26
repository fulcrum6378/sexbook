package ir.mahdiparastesh.sexbook.list

import android.annotation.SuppressLint
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
import ir.mahdiparastesh.sexbook.Settings
import ir.mahdiparastesh.sexbook.databinding.RecencyBinding
import ir.mahdiparastesh.sexbook.more.AnyViewHolder
import ir.mahdiparastesh.sexbook.more.BaseDialog
import ir.mahdiparastesh.sexbook.stat.Singular

class StatRecAdap(private val c: Main, private val searchable: BaseDialog.SearchableStat) :
    RecyclerView.Adapter<AnyViewHolder<RecencyBinding>>() {
    private val curCrushes: List<String>? = c.m.liefde?.map { it.key }
    private val statOnlyCrushes = c.sp.getBoolean(Settings.spStatOnlyCrushes, false)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
            AnyViewHolder<RecencyBinding> =
        AnyViewHolder(RecencyBinding.inflate(c.layoutInflater, parent, false))

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(h: AnyViewHolder<RecencyBinding>, i: Int) {
        val crushKey = c.m.recency[i].name
        h.b.name.text = "${i + 1}. $crushKey" +
                (if (!statOnlyCrushes && curCrushes?.contains(crushKey) == true) "*" else "")
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
        h.b.root.foreground = if (searchable.lookForIt(crushKey))
            ColorDrawable(c.color(R.color.recencyHighlight)) else null
    }

    override fun getItemCount() = c.m.recency.size
}
