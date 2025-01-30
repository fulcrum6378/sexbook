package ir.mahdiparastesh.sexbook.list

import android.annotation.SuppressLint
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ir.mahdiparastesh.sexbook.Fun.show
import ir.mahdiparastesh.sexbook.People
import ir.mahdiparastesh.sexbook.ctrl.Identify
import ir.mahdiparastesh.sexbook.data.Crush
import ir.mahdiparastesh.sexbook.databinding.ItemPersonBinding
import ir.mahdiparastesh.sexbook.stat.Singular
import ir.mahdiparastesh.sexbook.view.AnyViewHolder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.experimental.or
import kotlin.experimental.xor

class PersonAdap(val c: People) : RecyclerView.Adapter<AnyViewHolder<ItemPersonBinding>>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
            AnyViewHolder<ItemPersonBinding> =
        AnyViewHolder(ItemPersonBinding.inflate(c.layoutInflater, parent, false))

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(h: AnyViewHolder<ItemPersonBinding>, i: Int) {
        val p = c.mm.visPeople.getOrNull(i)?.let { c.m.people[it] } ?: return

        // Active
        h.b.active.setOnCheckedChangeListener(null)
        h.b.active.isChecked = p.active()
        h.b.active.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked == p.active()) return@setOnCheckedChangeListener
            CoroutineScope(Dispatchers.IO).launch {
                p.apply {
                    status = if (isChecked) (status xor Crush.STAT_INACTIVE)
                    else (status or Crush.STAT_INACTIVE)
                }
                c.m.dao.cUpdate(p)
                withContext(Dispatchers.Main) { c.m.onCrushChanged(c, p.key, 1) }
            }
        }

        // Name
        h.b.name.text = "${i + 1}. ${p.visName()}"
        h.b.sum.text = p.getSum(c.m).let { if (it != 0f) "{${it.show()}}" else "" }

        // Clicks
        h.b.root.setOnClickListener {
            c.mm.visPeople.getOrNull(h.layoutPosition)?.also { Identify.create<People>(c, it) }
        }
        h.b.root.setOnLongClickListener {
            c.mm.visPeople.getOrNull(h.layoutPosition)?.also {
                c.goTo(Singular::class) { putExtra(Singular.EXTRA_CRUSH_KEY, it) }
            }; true
        }
    }

    override fun getItemCount() = c.mm.visPeople.size
}