package ir.mahdiparastesh.sexbook.list

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ir.mahdiparastesh.sexbook.Fun.show
import ir.mahdiparastesh.sexbook.Settings
import ir.mahdiparastesh.sexbook.data.Crush
import ir.mahdiparastesh.sexbook.data.Identify
import ir.mahdiparastesh.sexbook.databinding.ItemPersonBinding
import ir.mahdiparastesh.sexbook.more.AnyViewHolder
import ir.mahdiparastesh.sexbook.stat.Singular
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.experimental.or
import kotlin.experimental.xor

class BNtfCrushAdap(val c: Settings) : RecyclerView.Adapter<AnyViewHolder<ItemPersonBinding>>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
            AnyViewHolder<ItemPersonBinding> =
        AnyViewHolder(ItemPersonBinding.inflate(c.layoutInflater, parent, false))

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(h: AnyViewHolder<ItemPersonBinding>, i: Int) {
        val p = c.mm.bNtfCrushes.getOrNull(i) ?: return

        // Active
        h.b.active.setOnCheckedChangeListener(null)
        h.b.active.isChecked = p.notifyBirth()
        h.b.active.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked == p.notifyBirth()) return@setOnCheckedChangeListener
            CoroutineScope(Dispatchers.IO).launch {
                p.apply {
                    status = if (isChecked) (status or Crush.STAT_NOTIFY_BIRTH)
                    else (status xor Crush.STAT_NOTIFY_BIRTH)
                }
                c.m.dao.cUpdate(p)
                withContext(Dispatchers.Main) { c.m.onCrushChanged(c, p, 1) }
            }
        }

        // Name
        h.b.name.text = "${i + 1}. ${p.visName()}"
        h.b.sum.text = c.mm.bNtfCrushes[i].getSum(c.m)
            .let { if (it != 0f) "{${it.show()}}" else "" }

        // Clicks
        h.b.root.setOnClickListener {
            c.mm.bNtfCrushes.getOrNull(h.layoutPosition)?.also { identify(it) }
        }
        h.b.root.setOnLongClickListener {
            c.mm.bNtfCrushes.getOrNull(h.layoutPosition)?.also { person ->
                c.goTo(Singular::class) { putExtra(Singular.EXTRA_CRUSH_KEY, person.key) }
            }; true
        }
    }

    override fun getItemCount() = c.mm.bNtfCrushes.size

    private fun identify(crush: Crush) {
        Identify(c, crush).apply {
            arguments = Bundle().apply { putString(Identify.BUNDLE_CRUSH_KEY, crush.key) }
            show(c.supportFragmentManager, Identify.TAG)
        }
    }
}