package ir.mahdiparastesh.sexbook.list

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ir.mahdiparastesh.sexbook.People
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

class PersonAdap(val c: People) : RecyclerView.Adapter<AnyViewHolder<ItemPersonBinding>>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
            AnyViewHolder<ItemPersonBinding> =
        AnyViewHolder(ItemPersonBinding.inflate(c.layoutInflater, parent, false))

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(h: AnyViewHolder<ItemPersonBinding>, i: Int) {
        val p = c.m.people?.getOrNull(i) ?: return

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
                withContext(Dispatchers.Main) { c.m.onCrushChanged(c, p, 1) }
            }
        }

        // Name
        h.b.name.text = "${i + 1}. ${p.visName()}"

        // Clicks
        h.b.root.setOnClickListener {
            c.m.people?.getOrNull(h.layoutPosition)?.also { identify(it) }
        }
        h.b.root.setOnLongClickListener {
            c.m.people?.getOrNull(h.layoutPosition)?.also { person ->
                c.goTo(Singular::class) { putExtra(Singular.EXTRA_CRUSH_KEY, person.key) }
            }; true
        }
    }

    override fun getItemCount() = c.m.people?.size ?: 0

    private fun identify(crush: Crush) {
        Identify(crush).apply {
            arguments = Bundle().apply { putString(Identify.BUNDLE_CRUSH_KEY, crush.key) }
            show(c.supportFragmentManager, Identify.TAG)
        }
    }
}