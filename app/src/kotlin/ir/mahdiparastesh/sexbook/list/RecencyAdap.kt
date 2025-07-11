package ir.mahdiparastesh.sexbook.list

import android.annotation.SuppressLint
import android.graphics.drawable.ColorDrawable
import android.icu.util.Calendar
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import ir.mahdiparastesh.sexbook.R
import ir.mahdiparastesh.sexbook.databinding.RecencyBinding
import ir.mahdiparastesh.sexbook.page.Settings
import ir.mahdiparastesh.sexbook.stat.RecencyDialog
import ir.mahdiparastesh.sexbook.stat.Singular
import ir.mahdiparastesh.sexbook.util.NumberUtils.calendar
import ir.mahdiparastesh.sexbook.util.NumberUtils.fullDate
import ir.mahdiparastesh.sexbook.util.NumberUtils.show
import ir.mahdiparastesh.sexbook.util.NumberUtils.z
import ir.mahdiparastesh.sexbook.view.AnyViewHolder

/** Used in [RecencyDialog] */
class RecencyAdap(private val r: RecencyDialog) :
    RecyclerView.Adapter<AnyViewHolder<RecencyBinding>>() {

    private val statOnlyCrushes = r.c.c.sp.getBoolean(Settings.spStatOnlyCrushes, false)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
            AnyViewHolder<RecencyBinding> =
        AnyViewHolder(RecencyBinding.inflate(r.c.layoutInflater, parent, false))

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(h: AnyViewHolder<RecencyBinding>, i: Int) {
        val crushKey = r.items[i].name
        h.b.name.text = "${i + 1}. $crushKey" +
                (if (!statOnlyCrushes && crushKey in r.c.c.liefde) "*" else "") +
                (r.c.c.summary!!.scores[crushKey]?.sum?.show()?.let { " {$it}" } ?: "")
        val lm = r.items[i].time.calendar(r.c.c)
        h.b.date.text =
            "${lm.fullDate()} - ${z(lm[Calendar.HOUR_OF_DAY])}:${z(lm[Calendar.MINUTE])}"
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
